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

package main

import (
	"bufio"
	"context"
	"errors"
	"flag"
	"fmt"
	"io"
	"os"
	"strings"
	"unicode"

	"golang.org/x/term"

	"github.com/apache/airavata/internal/mcp/client"
)

// prompter reads answers from a terminal; secret answers are read without echo.
type prompter interface {
	Line(prompt string) (string, error)
	Secret(prompt string) (string, error)
}

type terminal struct {
	in  *bufio.Reader
	fd  int
	out io.Writer
}

func (t *terminal) Line(prompt string) (string, error) {
	fmt.Fprint(t.out, prompt)
	s, err := t.in.ReadString('\n')
	if err != nil && (s == "" || !errors.Is(err, io.EOF)) {
		return "", err
	}
	return strings.TrimRight(s, "\r\n"), nil
}

func (t *terminal) Secret(prompt string) (string, error) {
	fmt.Fprint(t.out, prompt)
	b, err := term.ReadPassword(t.fd)
	fmt.Fprintln(t.out)
	return string(b), err
}

func sshAuth(ctx context.Context, args []string) error {
	fs := flag.NewFlagSet("ssh-auth", flag.ContinueOnError)
	var a authFlags
	a.register(fs)
	if err := fs.Parse(args); err != nil {
		return err
	}
	fd := int(os.Stdin.Fd())
	if !term.IsTerminal(fd) {
		return errors.New("ssh-auth must run in an interactive terminal, so answers are never piped or logged")
	}
	sess := &lazySession{cfg: a.config()}
	api, err := client.New(a.apiURL, sess.Token, nil)
	if err != nil {
		return err
	}
	return answerAuths(ctx, api, &terminal{in: bufio.NewReader(os.Stdin), fd: fd, out: os.Stderr}, os.Stderr)
}

func answerAuths(ctx context.Context, api *client.ClientWithResponses, p prompter, out io.Writer) error {
	auths, err := client.JSON[[]client.InteractiveAuthResponse](api.ListInteractiveAuthsWithResponse(ctx))
	if err != nil {
		return err
	}
	if len(*auths) == 0 {
		fmt.Fprintln(out, "No SSH logins are waiting for an answer.")
		return nil
	}
	for _, a := range *auths {
		fmt.Fprintf(out, "\nSSH login to %s:%d as %s (waiting since %s)\n", a.HostName, a.Port, a.LoginUser, a.CreatedAt.Local().Format("15:04:05"))
		fmt.Fprintln(out, "The text below comes from the remote host.")
		for _, text := range []string{a.Banner, a.Name, a.Instruction} {
			if t := printable(text); t != "" {
				fmt.Fprintln(out, t)
			}
		}
		choice, err := p.Line("Answer this login? [Y/n/c to cancel it] ")
		if err != nil {
			return err
		}
		switch strings.ToLower(strings.TrimSpace(choice)) {
		case "n", "no":
			continue
		case "c", "cancel":
			cancel := true
			if err := client.NoContent(api.AnswerInteractiveAuthRestWithResponse(ctx, a.AuthId, client.InteractiveAuthAnswer{Cancel: &cancel})); err != nil {
				return err
			}
			fmt.Fprintln(out, "Login cancelled.")
			continue
		}
		var responses []string
		for _, prompt := range a.Prompts {
			label := printable(prompt.Text)
			var answer string
			if prompt.Echo {
				answer, err = p.Line(label + " ")
			} else {
				answer, err = p.Secret(label + " ")
			}
			if err != nil {
				return err
			}
			responses = append(responses, answer)
		}
		err = client.NoContent(api.AnswerInteractiveAuthRestWithResponse(ctx, a.AuthId, client.InteractiveAuthAnswer{Responses: &responses}))
		if err != nil {
			if client.IsStatus(err, 404) || client.IsStatus(err, 409) {
				fmt.Fprintln(out, "That login is no longer waiting (it was answered elsewhere or gave up).")
				continue
			}
			return err
		}
		fmt.Fprintln(out, "Answer sent to the SSH login; the host decides whether it is accepted.")
	}
	return nil
}

// printable strips control characters so remote text cannot drive the terminal.
func printable(s string) string {
	return strings.TrimSpace(strings.Map(func(r rune) rune {
		if r == '\n' || r == '\t' {
			return r
		}
		if unicode.IsControl(r) {
			return -1
		}
		return r
	}, s))
}
