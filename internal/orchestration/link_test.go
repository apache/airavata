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
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"
)

func TestProvisioningRefusesALinkspanThatIsNotTheRelease(t *testing.T) {
	for _, sum := range []string{linuxX86Sum, linuxARMSum} {
		if !strings.Contains(ProvisionScript, sum) {
			t.Errorf("the script does not pin %s", sum)
		}
	}
	dir := t.TempDir()
	if err := os.WriteFile(filepath.Join(dir, "linkspan"), []byte("#!/bin/sh\necho tampered\n"), 0o755); err != nil {
		t.Fatal(err)
	}
	if out, err := exec.Command("tar", "-czf", filepath.Join(dir, "asset"), "-C", dir, "linkspan").CombinedOutput(); err != nil {
		t.Fatalf("tar: %v\n%s", err, out)
	}
	curl := "#!/bin/sh\nout=\nwhile [ $# -gt 0 ]; do [ \"$1\" = -o ] && out=$2; shift; done\n" +
		"if [ -n \"$out\" ]; then cp '" + dir + "/asset' \"$out\"; else cat '" + dir + "/asset'; fi\n"
	if err := os.WriteFile(filepath.Join(dir, "curl"), []byte(curl), 0o755); err != nil {
		t.Fatal(err)
	}
	run := exec.Command("sh", "-s")
	run.Stdin = strings.NewReader(ProvisionScript)
	run.Env = []string{"HOME=" + dir, "PATH=" + dir + ":" + os.Getenv("PATH")}
	if out, err := run.CombinedOutput(); err == nil {
		t.Errorf("provisioning a tampered download succeeded\n%s", out)
	}
	if _, err := os.Stat(filepath.Join(dir, ".cybershuttle/bin/linkspan")); err == nil {
		t.Error("a tampered Linkspan was installed")
	}
}
