/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package orchestration

import (
	"bytes"
	"context"
	"crypto/tls"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"net"
	"strings"
	"time"

	"github.com/emersion/go-imap/v2"
	"github.com/emersion/go-imap/v2/imapclient"
	// Registers the decoders for the charsets a header or a body part may declare, so a
	// message that is not UTF-8 is read rather than rejected.
	_ "github.com/emersion/go-message/charset"
	"github.com/emersion/go-message/mail"
)

const (
	// gmailIMAPAddr is Gmail's IMAP endpoint, which speaks TLS from the first byte
	// rather than negotiating it with STARTTLS.
	gmailIMAPAddr = "imap.gmail.com:993"

	// defaultMailbox is Gmail's inbox. Note that it holds only mail that has not been
	// archived — "[Gmail]/All Mail" is the everything folder — which is what a monitor
	// draining notifications wants.
	defaultMailbox = "INBOX"

	// defaultMonitorInterval is used when the monitor was built without one.
	defaultMonitorInterval = 60 * time.Second

	// imapDialTimeout bounds the TCP connect and the TLS handshake behind it, so a
	// mail server that is merely unreachable fails the pass quickly instead of holding
	// the monitor until the OS gives up on the socket.
	imapDialTimeout = 30 * time.Second

	// emailPassTimeout bounds a whole pass. Without it a connection that stalls after
	// the handshake — a half-open socket surviving a network move, say — would wedge
	// the monitor permanently rather than costing it one interval.
	emailPassTimeout = 5 * time.Minute

	// maxEmailsPerPass caps how much a single pass pulls into memory. A mailbox left
	// unattended can accumulate far more unread mail than one fetch should hold. The
	// remainder is not lost: it is still unread, so the next pass takes the next batch.
	maxEmailsPerPass = 100
)

// EmailMonitor reads batch job notification mail out of a Gmail mailbox.
//
// It authenticates with a Google app password rather than OAuth, which is what lets it
// run unattended: there is no consent screen to click through and no token to refresh.
// The account must have 2-step verification enabled for such a password to exist, and
// IMAP must be on in Gmail's settings.
//
// Only unread mail is read, and a message is marked read only once it has been handled,
// so nothing is consumed twice and nothing is silently dropped by a failure part way
// through. A handler must therefore be idempotent: a message whose handling succeeded
// but whose flag never reached the server is handed over again on the next pass.
type EmailMonitor struct {
	emailAddress           string
	monitorIntervalSeconds int

	// appPassword is the 16-character Google app password for emailAddress.
	appPassword string

	// imapAddr and mailbox are overridable for tests and for a non-Gmail server; empty
	// means Gmail's endpoint and its inbox.
	imapAddr string
	mailbox  string

	// handle is called once per unread message. Returning an error leaves that message
	// unread for a later pass to retry; a nil handler only logs what arrived.
	handle func(context.Context, Email) error
}

// Email is one message read out of the monitored mailbox.
type Email struct {
	// UID identifies the message within the mailbox. It is stable, unlike a sequence
	// number, which shifts as other messages are expunged.
	UID imap.UID

	From    string
	Subject string
	Date    time.Time
	Body    string
}

// NewEmailMonitor returns a monitor over emailAddress.
//
// appPassword is a Google app password. Google presents it as four space-separated
// groups and accepts it either way, so the spaces are stripped here rather than left to
// produce an authentication failure that looks like a wrong password.
func NewEmailMonitor(emailAddress, appPassword string, monitorIntervalSeconds int, handle func(context.Context, Email) error) *EmailMonitor {
	return &EmailMonitor{
		emailAddress:           strings.TrimSpace(emailAddress),
		appPassword:            strings.Join(strings.Fields(appPassword), ""),
		monitorIntervalSeconds: monitorIntervalSeconds,
		handle:                 handle,
	}
}

// monitorBatchJobEmails reads the mailbox every interval until ctx is cancelled.
//
// A pass that fails is logged and retried at the next tick rather than ending the
// monitor: a mail server refusing connections is ordinarily temporary, and the mail it
// is holding stays unread until a pass succeeds.
func (e *EmailMonitor) MonitorBatchJobEmails(ctx context.Context) {
	if err := e.validate(); err != nil {
		slog.Error("Failed to start email monitor", "address", e.emailAddress, "error", err)
		return
	}

	interval := time.Duration(e.monitorIntervalSeconds) * time.Second
	if interval <= 0 {
		interval = defaultMonitorInterval
	}

	slog.Info("Starting email monitor", "address", e.emailAddress, "mailbox", e.mailboxName(),
		"server", e.serverAddr(), "intervalSeconds", int(interval/time.Second))

	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		// Each pass gets its own deadline so a stalled connection costs one interval
		// rather than the monitor's life. Passes never overlap, so a slow one simply
		// delays the next.
		passCtx, cancel := context.WithTimeout(ctx, emailPassTimeout)
		count, err := e.readUnreadEmails(passCtx)
		cancel()

		switch {
		case ctx.Err() != nil:
			// The parent context, not the pass deadline: the process is shutting down.
			slog.Info("Stopping email monitor", "address", e.emailAddress)
			return
		case err != nil:
			slog.Error("Failed to read unread emails", "address", e.emailAddress, "mailbox", e.mailboxName(), "error", err)
		case count > 0:
			slog.Info("Read unread emails", "address", e.emailAddress, "mailbox", e.mailboxName(), "count", count)
		}

		select {
		case <-ctx.Done():
			slog.Info("Stopping email monitor", "address", e.emailAddress)
			return
		case <-ticker.C:
		}
	}
}

// readUnreadEmails makes one pass over the mailbox: connect, take what is unread, hand
// each message to the handler, and mark read only what the handler accepted.
//
// It returns how many messages were handled and marked read.
func (e *EmailMonitor) readUnreadEmails(ctx context.Context) (int, error) {
	if err := e.validate(); err != nil {
		return 0, err
	}

	c, err := e.dial(ctx)
	if err != nil {
		return 0, err
	}
	defer c.Close()

	// Every command blocks on the socket, so cancellation has to arrive by closing the
	// connection under it. done stops the watcher when the pass finishes first.
	done := make(chan struct{})
	defer close(done)
	go func() {
		select {
		case <-ctx.Done():
			c.Close()
		case <-done:
		}
	}()

	if err := c.Login(e.emailAddress, e.appPassword).Wait(); err != nil {
		return 0, e.fail(ctx, fmt.Errorf("logging in as %s: %w", e.emailAddress, err))
	}

	// Read-write, because the flags set at the end of the pass are the point of it: a
	// read-only SELECT would have the server refuse the STORE.
	if _, err := c.Select(e.mailboxName(), nil).Wait(); err != nil {
		return 0, e.fail(ctx, fmt.Errorf("selecting %s: %w", e.mailboxName(), err))
	}

	// "Unread" is the absence of \Seen, which is the same flag Gmail's own unread
	// marker is built on.
	search, err := c.UIDSearch(&imap.SearchCriteria{NotFlag: []imap.Flag{imap.FlagSeen}}, nil).Wait()
	if err != nil {
		return 0, e.fail(ctx, fmt.Errorf("searching %s for unread mail: %w", e.mailboxName(), err))
	}

	// UIDs rather than sequence numbers throughout: a sequence number is only valid
	// until something is expunged, and a live mailbox can be changed by another client
	// between this search and the fetch and store that follow it.
	uids := search.AllUIDs()
	if len(uids) == 0 {
		return 0, e.logout(ctx, c)
	}
	if len(uids) > maxEmailsPerPass {
		slog.Info("More unread mail than one pass reads", "address", e.emailAddress, "mailbox", e.mailboxName(),
			"unread", len(uids), "reading", maxEmailsPerPass)
		uids = uids[:maxEmailsPerPass]
	}

	// BODY.PEEK[] rather than BODY[]: a plain fetch sets \Seen as a side effect, which
	// would mark mail read before anything had been done with it — and a crash in
	// between would lose it for good. Peeking leaves the flag alone, so the STORE below
	// is what marks a message, after its handler has accepted it.
	section := &imap.FetchItemBodySection{Peek: true}
	messages, err := c.Fetch(imap.UIDSetNum(uids...), &imap.FetchOptions{
		UID:         true,
		Envelope:    true,
		BodySection: []*imap.FetchItemBodySection{section},
	}).Collect()
	if err != nil {
		return 0, e.fail(ctx, fmt.Errorf("fetching %d unread messages from %s: %w", len(uids), e.mailboxName(), err))
	}

	var handled imap.UIDSet
	for _, message := range messages {
		email, err := newEmail(message, section)
		if err != nil {
			// The message is left unread deliberately. A body that cannot be parsed is
			// unlikely to parse on the next pass either, but marking it read would hide
			// it, and an operator looking at the mailbox is how this gets noticed.
			slog.Error("Failed to read email", "address", e.emailAddress, "uid", message.UID, "subject", email.Subject, "error", err)
			continue
		}
		if e.handle == nil {
			slog.Info("Read email", "address", e.emailAddress, "uid", email.UID,
				"from", email.From, "subject", email.Subject, "date", email.Date)
		} else if err := e.handle(ctx, email); err != nil {
			slog.Error("Failed to handle email", "address", e.emailAddress, "uid", email.UID,
				"from", email.From, "subject", email.Subject, "error", err)
			continue
		}
		handled.AddNum(email.UID)
	}

	if len(handled) == 0 {
		return 0, e.logout(ctx, c)
	}

	// One STORE for the batch. Silent, because the updated flags would only be echoed
	// back to be discarded.
	count := countUIDs(handled)
	if err := c.Store(handled, &imap.StoreFlags{
		Op:     imap.StoreFlagsAdd,
		Flags:  []imap.Flag{imap.FlagSeen},
		Silent: true,
	}, nil).Close(); err != nil {
		// The handlers already ran, so this is reported as a failed pass: the messages
		// are still unread and will come round again, which is why a handler has to be
		// idempotent.
		return 0, e.fail(ctx, fmt.Errorf("marking %d messages read in %s: %w", count, e.mailboxName(), err))
	}

	return count, e.logout(ctx, c)
}

// logout closes the session politely. The mail has already been read and flagged by
// this point, so a logout that fails changes nothing and is logged rather than returned.
func (e *EmailMonitor) logout(ctx context.Context, c *imapclient.Client) error {
	if err := c.Logout().Wait(); err != nil {
		if ctxErr := ctx.Err(); ctxErr != nil {
			return ctxErr
		}
		slog.Warn("Failed to log out of mailbox", "address", e.emailAddress, "error", err)
	}
	return nil
}

// fail names the mailbox in err, letting a cancelled context take precedence: once the
// connection is closed under it every command fails, and the resulting I/O error says
// nothing useful about why.
func (e *EmailMonitor) fail(ctx context.Context, err error) error {
	if ctxErr := ctx.Err(); ctxErr != nil {
		return fmt.Errorf("imap %s@%s: %w", e.emailAddress, e.serverAddr(), ctxErr)
	}
	return fmt.Errorf("imap %s@%s: %w", e.emailAddress, e.serverAddr(), err)
}

// dial opens a TLS connection to the mail server.
//
// The connection is built here rather than with imapclient.DialTLS so that the dial and
// the handshake both honour ctx: a pass has a deadline, and a connect going nowhere has
// to be abandonable.
func (e *EmailMonitor) dial(ctx context.Context) (*imapclient.Client, error) {
	addr := e.serverAddr()
	host, _, err := net.SplitHostPort(addr)
	if err != nil {
		return nil, fmt.Errorf("imap server address %q is not host:port: %w", addr, err)
	}

	dialer := net.Dialer{Timeout: imapDialTimeout}
	conn, err := dialer.DialContext(ctx, "tcp", addr)
	if err != nil {
		return nil, fmt.Errorf("connecting to %s: %w", addr, err)
	}
	tlsConn := tls.Client(conn, &tls.Config{ServerName: host, NextProtos: []string{"imap"}})
	if err := tlsConn.HandshakeContext(ctx); err != nil {
		conn.Close()
		return nil, fmt.Errorf("tls handshake with %s: %w", addr, err)
	}
	return imapclient.New(tlsConn, nil), nil
}

// validate reports what is missing before a connection is attempted, so the monitor
// says which setting is unset rather than letting the server answer with an
// authentication failure that names nothing.
func (e *EmailMonitor) validate() error {
	if e.emailAddress == "" {
		return errors.New("email monitor names no mailbox address")
	}
	if e.appPassword == "" {
		return fmt.Errorf("email monitor for %s carries no app password", e.emailAddress)
	}
	return nil
}

func (e *EmailMonitor) serverAddr() string {
	if strings.TrimSpace(e.imapAddr) == "" {
		return gmailIMAPAddr
	}
	return e.imapAddr
}

func (e *EmailMonitor) mailboxName() string {
	if strings.TrimSpace(e.mailbox) == "" {
		return defaultMailbox
	}
	return e.mailbox
}

// newEmail assembles one message from what the fetch returned.
//
// The envelope supplies the metadata already decoded — a subject arrives as text rather
// than as an RFC 2047 encoded word — so only the body has to be parsed out of the raw
// message.
func newEmail(message *imapclient.FetchMessageBuffer, section *imap.FetchItemBodySection) (Email, error) {
	email := Email{UID: message.UID}

	// Zero is not a UID a server may assign: in a number set it is the "*" wildcard, so
	// letting one through would have the STORE below mark the mailbox's last message
	// read instead of this one.
	if email.UID == 0 {
		return email, errors.New("server returned no uid for the message")
	}

	if message.Envelope != nil {
		email.Subject = message.Envelope.Subject
		email.Date = message.Envelope.Date
		for _, from := range message.Envelope.From {
			// Addr is empty for the markers that open and close an address group.
			if addr := from.Addr(); addr != "" {
				email.From = addr
				break
			}
		}
	}

	raw := message.FindBodySection(section)
	if raw == nil {
		return email, errors.New("server returned no body for the message")
	}
	body, err := messageText(raw)
	if err != nil {
		return email, err
	}
	email.Body = body
	return email, nil
}

// messageText pulls the plain text out of a raw RFC 5322 message, decoding whatever
// transfer encoding and charset it declares.
//
// A scheduler's notification mail is a single plain text part, but the mailbox is a real
// inbox and will also hold multipart mail, so the parts are walked and the first inline
// text/plain wins. An HTML-only message has no such part; the first inline part of any
// type is kept as a fallback so something is returned rather than nothing. Attachments
// are skipped.
func messageText(raw []byte) (string, error) {
	reader, err := mail.CreateReader(bytes.NewReader(raw))
	if err != nil {
		return "", fmt.Errorf("parsing message: %w", err)
	}
	defer reader.Close()

	var fallback string
	for {
		part, err := reader.NextPart()
		if errors.Is(err, io.EOF) {
			break
		}
		if err != nil {
			return "", fmt.Errorf("reading message part: %w", err)
		}

		header, inline := part.Header.(*mail.InlineHeader)
		if !inline {
			continue
		}
		body, err := io.ReadAll(part.Body)
		if err != nil {
			return "", fmt.Errorf("reading message part: %w", err)
		}

		// A part that declares no Content-Type is text/plain by default, which is what
		// ContentType reports, so an unparseable value is the only reason to look at
		// the error — and a part that names a broken type is not the one to return.
		mediaType, _, err := header.ContentType()
		if err == nil && mediaType == "text/plain" {
			return string(body), nil
		}
		if fallback == "" {
			fallback = string(body)
		}
	}
	return fallback, nil
}

// countUIDs reports how many messages a UID set names. The set is built one UID at a
// time here, but AddNum coalesces adjacent UIDs into ranges, so its length is a count of
// ranges rather than of messages.
func countUIDs(set imap.UIDSet) int {
	uids, ok := set.Nums()
	if !ok {
		return 0
	}
	return len(uids)
}
