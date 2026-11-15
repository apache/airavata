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

package objectstore

import (
	"bytes"
	"context"
	"crypto/rand"
	"errors"
	"fmt"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"reflect"
	"strings"
	"testing"

	"github.com/google/uuid"
	"github.com/johannesboyne/gofakes3"
	"github.com/johannesboyne/gofakes3/backend/s3mem"
	"github.com/minio/minio-go/v7"
)

const (
	testBucket = "test-bucket"
	testSecret = "TEST-SECRET-KEY"
)

// target is a store under test and the key prefix that keeps this test's objects apart.
type target struct {
	store  *S3Store
	prefix string
}

// newFakeTarget serves the fake over TLS, as a real deployment would be. Over plain HTTP
// the client signs uploads in a chunked format the fake does not decode for multipart
// parts, so it would store the signatures as if they were data.
func newFakeTarget(t *testing.T) target {
	t.Helper()
	backend := s3mem.New()
	if err := backend.CreateBucket(testBucket); err != nil {
		t.Fatalf("create bucket: %v", err)
	}
	server := httptest.NewTLSServer(gofakes3.New(backend).Server())
	t.Cleanup(server.Close)

	store, err := newStore(Config{
		EndpointURL: server.URL, Region: "us-east-1", Bucket: testBucket, PathStyle: true,
		AccessKeyID: "test-key", SecretAccessKey: testSecret,
	}, server.Client().Transport)
	if err != nil {
		t.Fatalf("new store: %v", err)
	}
	return target{store: store, prefix: "t/"}
}

// newRealTarget uses a real S3-compatible service, when one is configured:
//
//	docker run -p 9000:9000 -e MINIO_ROOT_USER=minio -e MINIO_ROOT_PASSWORD=minio12345 minio/minio server /data
//	AIRAVATA_TEST_S3_ENDPOINT=http://localhost:9000 AIRAVATA_TEST_S3_ACCESS_KEY=minio \
//	AIRAVATA_TEST_S3_SECRET_KEY=minio12345 AIRAVATA_TEST_S3_BUCKET=airavata-test go test ./internal/objectstore/
//
// The bucket must already exist. Each test writes under a prefix of its own and removes
// what it wrote.
func newRealTarget(t *testing.T) (target, bool) {
	t.Helper()
	endpoint := os.Getenv("AIRAVATA_TEST_S3_ENDPOINT")
	if endpoint == "" {
		return target{}, false
	}
	bucket := os.Getenv("AIRAVATA_TEST_S3_BUCKET")
	store, err := New(Config{
		EndpointURL: endpoint, Region: "us-east-1", Bucket: bucket, PathStyle: true,
		AccessKeyID:     os.Getenv("AIRAVATA_TEST_S3_ACCESS_KEY"),
		SecretAccessKey: os.Getenv("AIRAVATA_TEST_S3_SECRET_KEY"),
	})
	if err != nil {
		t.Fatalf("new store: %v", err)
	}
	prefix := "airavata-test/" + uuid.NewString() + "/"
	t.Cleanup(func() {
		ctx := context.Background()
		objects := store.client.ListObjects(ctx, bucket, minio.ListObjectsOptions{Prefix: prefix, Recursive: true})
		for object := range objects {
			if object.Err == nil {
				store.client.RemoveObject(ctx, bucket, object.Key, minio.RemoveObjectOptions{})
			}
		}
	})
	return target{store: store, prefix: prefix}, true
}

// forEachBackend runs one test against the in-process fake and, when configured, a real
// service.
func forEachBackend(t *testing.T, test func(t *testing.T, tg target)) {
	t.Run("fake", func(t *testing.T) { test(t, newFakeTarget(t)) })
	t.Run("real", func(t *testing.T) {
		tg, ok := newRealTarget(t)
		if !ok {
			t.Skip("AIRAVATA_TEST_S3_ENDPOINT not set")
		}
		test(t, tg)
	})
}

func writeFile(t *testing.T, content []byte) string {
	t.Helper()
	path := filepath.Join(t.TempDir(), "source")
	if err := os.WriteFile(path, content, 0o600); err != nil {
		t.Fatalf("write %s: %v", path, err)
	}
	return path
}

func put(t *testing.T, tg target, key string, content []byte) {
	t.Helper()
	if err := tg.store.Upload(context.Background(), writeFile(t, content), key); err != nil {
		t.Fatalf("upload %s: %v", key, err)
	}
}

func get(t *testing.T, tg target, key string) []byte {
	t.Helper()
	path := filepath.Join(t.TempDir(), "downloaded")
	if err := tg.store.Download(context.Background(), key, path); err != nil {
		t.Fatalf("download %s: %v", key, err)
	}
	got, err := os.ReadFile(path)
	if err != nil {
		t.Fatalf("read %s: %v", path, err)
	}
	return got
}

func TestUploadDownloadStat(t *testing.T) {
	forEachBackend(t, func(t *testing.T, tg target) {
		for name, content := range map[string][]byte{
			"small": []byte("hello object store"),
			"empty": {},
		} {
			key := tg.prefix + name + ".dat"
			put(t, tg, key, content)

			if got := get(t, tg, key); !bytes.Equal(got, content) {
				t.Errorf("%s: downloaded %q, want %q", name, got, content)
			}
			info, err := tg.store.Stat(context.Background(), key)
			if err != nil {
				t.Fatalf("%s: stat: %v", name, err)
			}
			if info.Key != key || info.Size != int64(len(content)) || info.ETag == "" {
				t.Errorf("%s: stat = %+v, want key %s and size %d", name, info, key, len(content))
			}
		}
	})
}

// Above the client's part size the upload goes multipart, so a large file exercises a
// different path through the store.
func TestLargeFileRoundTrip(t *testing.T) {
	forEachBackend(t, func(t *testing.T, tg target) {
		content := make([]byte, 17<<20)
		if _, err := rand.Read(content); err != nil {
			t.Fatalf("random content: %v", err)
		}
		key := tg.prefix + "large.bin"
		put(t, tg, key, content)
		if got := get(t, tg, key); !bytes.Equal(got, content) {
			t.Errorf("downloaded %d bytes differ from the %d uploaded", len(got), len(content))
		}
	})
}

func TestDownloadReplacesAnExistingFile(t *testing.T) {
	forEachBackend(t, func(t *testing.T, tg target) {
		key := tg.prefix + "replace.dat"
		put(t, tg, key, []byte("new"))

		path := writeFile(t, []byte("a much longer previous file"))
		if err := tg.store.Download(context.Background(), key, path); err != nil {
			t.Fatalf("download: %v", err)
		}
		got, err := os.ReadFile(path)
		if err != nil {
			t.Fatalf("read: %v", err)
		}
		if string(got) != "new" {
			t.Errorf("file = %q, want it replaced", got)
		}
	})
}

func TestMissingObjectIsErrNotFound(t *testing.T) {
	forEachBackend(t, func(t *testing.T, tg target) {
		key := tg.prefix + "missing.dat"
		if _, err := tg.store.Stat(context.Background(), key); !errors.Is(err, ErrNotFound) {
			t.Errorf("stat: err = %v, want ErrNotFound", err)
		}

		path := filepath.Join(t.TempDir(), "never")
		if err := tg.store.Download(context.Background(), key, path); !errors.Is(err, ErrNotFound) {
			t.Errorf("download: err = %v, want ErrNotFound", err)
		}
		if _, err := os.Stat(path); !os.IsNotExist(err) {
			t.Errorf("a failed download left a file behind: %v", err)
		}
	})
}

func TestListWithPrefixAndDelimiter(t *testing.T) {
	forEachBackend(t, func(t *testing.T, tg target) {
		for _, key := range []string{"a.txt", "b.txt", "dir/c.txt", "dir/sub/d.txt", "other/e.txt"} {
			put(t, tg, tg.prefix+key, []byte(key))
		}
		ctx := context.Background()

		top, err := tg.store.List(ctx, ListOptions{Prefix: tg.prefix, Delimiter: "/"})
		if err != nil {
			t.Fatalf("list top level: %v", err)
		}
		if got, want := keysOf(top), []string{tg.prefix + "a.txt", tg.prefix + "b.txt"}; !reflect.DeepEqual(got, want) {
			t.Errorf("top-level objects = %v, want %v", got, want)
		}
		if want := []string{tg.prefix + "dir/", tg.prefix + "other/"}; !reflect.DeepEqual(top.CommonPrefixes, want) {
			t.Errorf("top-level prefixes = %v, want %v", top.CommonPrefixes, want)
		}

		dir, err := tg.store.List(ctx, ListOptions{Prefix: tg.prefix + "dir/", Delimiter: "/"})
		if err != nil {
			t.Fatalf("list dir: %v", err)
		}
		if got, want := keysOf(dir), []string{tg.prefix + "dir/c.txt"}; !reflect.DeepEqual(got, want) {
			t.Errorf("dir objects = %v, want %v", got, want)
		}
		if want := []string{tg.prefix + "dir/sub/"}; !reflect.DeepEqual(dir.CommonPrefixes, want) {
			t.Errorf("dir prefixes = %v, want %v", dir.CommonPrefixes, want)
		}

		all, err := tg.store.List(ctx, ListOptions{Prefix: tg.prefix})
		if err != nil {
			t.Fatalf("list recursive: %v", err)
		}
		if len(all.Objects) != 5 || len(all.CommonPrefixes) != 0 {
			t.Errorf("recursive listing = %d objects and %d prefixes, want 5 and 0", len(all.Objects), len(all.CommonPrefixes))
		}
		if all.Objects[0].Size != int64(len("a.txt")) || all.Objects[0].ETag == "" {
			t.Errorf("listed object = %+v, want its size and etag", all.Objects[0])
		}

		empty, err := tg.store.List(ctx, ListOptions{Prefix: tg.prefix + "nothing/"})
		if err != nil {
			t.Fatalf("list empty: %v", err)
		}
		if len(empty.Objects) != 0 || empty.NextContinuationToken != "" {
			t.Errorf("empty listing = %+v", empty)
		}
	})
}

func TestListPagination(t *testing.T) {
	forEachBackend(t, func(t *testing.T, tg target) {
		var want []string
		for i := range 5 {
			key := fmt.Sprintf("%s%d.txt", tg.prefix, i)
			put(t, tg, key, []byte("x"))
			want = append(want, key)
		}

		var got []string
		opts := ListOptions{Prefix: tg.prefix, MaxKeys: 2}
		for pages := 0; ; pages++ {
			if pages > 5 {
				t.Fatalf("listing did not end; keys so far: %v", got)
			}
			page, err := tg.store.List(context.Background(), opts)
			if err != nil {
				t.Fatalf("list: %v", err)
			}
			if len(page.Objects) > 2 {
				t.Errorf("page has %d objects, want at most 2", len(page.Objects))
			}
			got = append(got, keysOf(page)...)
			if page.NextContinuationToken == "" {
				break
			}
			opts.ContinuationToken = page.NextContinuationToken
		}
		if !reflect.DeepEqual(got, want) {
			t.Errorf("paged keys = %v, want %v", got, want)
		}
	})
}

func keysOf(l *Listing) []string {
	keys := make([]string, 0, len(l.Objects))
	for _, object := range l.Objects {
		keys = append(keys, object.Key)
	}
	return keys
}

func TestCanceledContextStopsTheCall(t *testing.T) {
	tg := newFakeTarget(t)
	ctx, cancel := context.WithCancel(context.Background())
	cancel()

	if _, err := tg.store.List(ctx, ListOptions{}); !errors.Is(err, context.Canceled) {
		t.Errorf("list: err = %v, want context.Canceled", err)
	}
	if _, err := tg.store.Stat(ctx, "k"); !errors.Is(err, context.Canceled) {
		t.Errorf("stat: err = %v, want context.Canceled", err)
	}
	path := filepath.Join(t.TempDir(), "never")
	if err := tg.store.Download(ctx, "k", path); err == nil {
		t.Error("download with a canceled context succeeded")
	}
	if _, err := os.Stat(path); !os.IsNotExist(err) {
		t.Errorf("a canceled download left a file behind: %v", err)
	}
}

// A store that refuses a request must report it as a failure, not as a missing object,
// and the secret must reach neither the error nor the wire.
func TestRefusedRequestIsReportedWithoutTheSecret(t *testing.T) {
	var seen []*http.Request
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		seen = append(seen, r.Clone(context.Background()))
		w.Header().Set("Content-Type", "application/xml")
		w.WriteHeader(http.StatusForbidden)
		fmt.Fprint(w, `<Error><Code>AccessDenied</Code><Message>Access Denied</Message></Error>`)
	}))
	t.Cleanup(server.Close)

	store, err := New(Config{
		EndpointURL: server.URL, Region: "us-east-1", Bucket: testBucket, PathStyle: true,
		AccessKeyID: "test-key", SecretAccessKey: testSecret, SessionToken: "TEST-SESSION-TOKEN",
	})
	if err != nil {
		t.Fatalf("new store: %v", err)
	}

	_, err = store.Stat(context.Background(), "some/key")
	if err == nil || errors.Is(err, ErrNotFound) {
		t.Fatalf("stat: err = %v, want a refusal rather than ErrNotFound", err)
	}
	if strings.Contains(err.Error(), testSecret) {
		t.Errorf("error leaks the secret: %v", err)
	}

	if len(seen) == 0 {
		t.Fatal("no request reached the server")
	}
	request := seen[0]
	if got, want := request.URL.Path, "/"+testBucket+"/some/key"; got != want {
		t.Errorf("path-style request went to %s, want %s", got, want)
	}
	if got := request.Header.Get("X-Amz-Security-Token"); got != "TEST-SESSION-TOKEN" {
		t.Errorf("session token header = %q", got)
	}
	for name, values := range request.Header {
		for _, value := range values {
			if strings.Contains(value, testSecret) {
				t.Errorf("header %s carries the secret", name)
			}
		}
	}
}

func TestNewRejectsAnIncompleteConfig(t *testing.T) {
	valid := Config{Bucket: "b", AccessKeyID: "k", SecretAccessKey: "s"}
	if _, err := New(valid); err != nil {
		t.Fatalf("valid config: %v", err)
	}
	for name, mutate := range map[string]func(*Config){
		"no bucket":      func(c *Config) { c.Bucket = "" },
		"no access key":  func(c *Config) { c.AccessKeyID = "" },
		"no secret":      func(c *Config) { c.SecretAccessKey = "" },
		"wrong scheme":   func(c *Config) { c.EndpointURL = "ftp://store.example.edu" },
		"no scheme":      func(c *Config) { c.EndpointURL = "store.example.edu" },
		"unparseable":    func(c *Config) { c.EndpointURL = "http://[bad" },
		"scheme no host": func(c *Config) { c.EndpointURL = "https://" },
	} {
		cfg := valid
		mutate(&cfg)
		if _, err := New(cfg); err == nil {
			t.Errorf("%s: New succeeded", name)
		}
	}
}

func TestParseEndpoint(t *testing.T) {
	for _, tc := range []struct {
		in, host string
		secure   bool
	}{
		{"", "s3.amazonaws.com", true},
		{"https://minio.example.edu:9000", "minio.example.edu:9000", true},
		{"http://localhost:9000", "localhost:9000", false},
	} {
		host, secure, err := parseEndpoint(tc.in)
		if err != nil {
			t.Errorf("%q: %v", tc.in, err)
			continue
		}
		if host != tc.host || secure != tc.secure {
			t.Errorf("%q = %s secure=%v, want %s secure=%v", tc.in, host, secure, tc.host, tc.secure)
		}
	}
}

func TestBucketLookup(t *testing.T) {
	if got := bucketLookup(true); got != minio.BucketLookupPath {
		t.Errorf("path style lookup = %v", got)
	}
	if got := bucketLookup(false); got != minio.BucketLookupDNS {
		t.Errorf("virtual-hosted lookup = %v", got)
	}
}
