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

package prompts

import (
	"context"
	"fmt"
	"strings"

	"github.com/modelcontextprotocol/go-sdk/mcp"

	"github.com/apache/airavata/internal/mcp/policy"
)

// Register adds the guided-flow prompts.
func Register(s *mcp.Server, p *policy.Policy) {
	writes := p.AllowWrite
	s.AddPrompt(&mcp.Prompt{Name: "onboard", Title: "Get started with Airavata",
		Description: "Walk a new user from sign-in to a first successful job."},
		func(context.Context, *mcp.GetPromptRequest) (*mcp.GetPromptResult, error) {
			steps := []string{
				"Help me run my first job on Airavata. Go step by step and check with me between steps.",
				"1. Call list_clusters and show me the clusters I can use.",
				"2. Call list_ssh_credentials. If I have none, tell me an SSH credential has to be registered first (key material is never handled in this conversation). If the one I pick is fully interactive, explain that I will answer its prompts with `airavata-mcp ssh-auth` in a terminal.",
				"3. Call list_cluster_configs for the cluster I pick.",
			}
			if writes {
				steps = append(steps,
					"   If I have no login config there, offer register_cluster_config with my login user, credential and work root.",
					"4. Call list_applications and find a small test application (for example hello-world). Use plan_run, show me the plan, then launch_process.",
					"5. Follow the run with get_process_status until it ends. If it fails, use diagnose_process.")
			} else {
				steps = append(steps,
					"   This server is read-only, so if I have no login config there, tell me how to enable writes (--allow-write) rather than creating one.",
					"4. Call list_applications and use plan_run to show me what a first test run would look like.")
			}
			return userPrompt("Get started with Airavata", strings.Join(steps, "\n")), nil
		})

	s.AddPrompt(&mcp.Prompt{Name: "bring_my_slurm_script", Title: "Turn an sbatch script into an Airavata application",
		Description: "Convert an existing sbatch script, review the diff, and register it (admins) or prepare it for an admin.",
		Arguments:   []*mcp.PromptArgument{{Name: "script", Title: "sbatch script", Description: "The full text of the script", Required: true}}},
		func(_ context.Context, req *mcp.GetPromptRequest) (*mcp.GetPromptResult, error) {
			script := req.Params.Arguments["script"]
			if strings.TrimSpace(script) == "" {
				return nil, fmt.Errorf("the script argument is required")
			}
			text := "Convert this sbatch script into an Airavata application.\n" +
				"1. Call convert_sbatch_script. Suggest which hard-coded values (input files, parameters) should become inputs, with names and types; ask me before applying them.\n" +
				"2. Show me the round-trip diff, the unsupported options and any problems. Do not drop an option silently.\n" +
				"3. Ask which cluster to deploy on (list_clusters), then call convert_sbatch_script again with cluster_id.\n" +
				"4. If register_application is available, offer to register it. Otherwise, give me the proposal to hand to an Airavata admin.\n\n" +
				"Script:\n```bash\n" + script + "\n```"
			return userPrompt("Bring my Slurm script", text), nil
		})

	s.AddPrompt(&mcp.Prompt{Name: "run_experiment", Title: "Run an experiment",
		Description: "Find the right application, plan a run, confirm and launch it, and monitor it.",
		Arguments:   []*mcp.PromptArgument{{Name: "goal", Title: "Goal", Description: "What you want to run, in plain language", Required: true}}},
		func(_ context.Context, req *mcp.GetPromptRequest) (*mcp.GetPromptResult, error) {
			text := "I want to: " + req.Params.Arguments["goal"] + "\n" +
				"1. Find the application with list_applications and get_application. Take ids from the tools; never invent them.\n" +
				"2. Find my input files with list_data_products.\n" +
				"3. Call plan_run, and show me the plan: cluster, partition, resources, walltime and requested core-hours and GPU-hours. Ask me about anything ambiguous.\n" +
				"4. Launch with launch_process; I will confirm in my client. If I decline, ask me what to change.\n" +
				"5. Follow it with get_process_status, and use diagnose_process if it fails."
			return userPrompt("Run an experiment", text), nil
		})

	s.AddPrompt(&mcp.Prompt{Name: "why_did_my_job_fail", Title: "Why did my job fail?",
		Description: "Explain a failed run from its recorded job states and offer a corrected run.",
		Arguments:   []*mcp.PromptArgument{{Name: "processId", Title: "Process id", Description: "The run's process id", Required: true}}},
		func(_ context.Context, req *mcp.GetPromptRequest) (*mcp.GetPromptResult, error) {
			text := "Explain why run " + req.Params.Arguments["processId"] + " failed.\n" +
				"1. Call get_process_status and show me the timeline.\n" +
				"2. Call diagnose_process and explain the cause in plain language, including what is not known yet.\n" +
				"3. If it returns a corrected proposal, describe the change and offer to launch it with launch_process."
			return userPrompt("Why did my job fail?", text), nil
		})
}

func userPrompt(description, text string) *mcp.GetPromptResult {
	return &mcp.GetPromptResult{Description: description,
		Messages: []*mcp.PromptMessage{{Role: "user", Content: &mcp.TextContent{Text: text}}}}
}
