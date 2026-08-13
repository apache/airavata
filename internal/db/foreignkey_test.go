package db_test

import (
	"fmt"
	"testing"
)

// TestForeignKeyDirections pins down which table owns each foreign key.
//
// This is not a formality. GORM decides between "belongs to" and "has one" by
// guessing from field names, and when the guess goes the wrong way it silently emits
// the constraint on the *parent* table pointing at the child — a schema that migrates
// cleanly and then rejects every insert into the parent. Naming a foreign key field
// by GORM's own convention (Cluster + ID) while also spelling out `foreignKey:` in
// the tag is enough to trigger it. These assertions catch that inversion the moment
// a tag changes.
func TestForeignKeyDirections(t *testing.T) {
	gdb := newTestDB(t)

	type fk struct {
		table, column, refTable, refColumn string
	}
	want := []fk{
		{"user_roles", "user_id", "users", "user_id"},
		{"ssh_user_credentials", "ssh_key_id", "ssh_keys", "ssh_key_id"},
		{"cluster_partitions", "cluster_id", "clusters", "cluster_id"},
		{"cluster_credentials", "cluster_id", "clusters", "cluster_id"},
		{"cluster_credentials", "ssh_credential_id", "ssh_user_credentials", "ssh_credential_id"},
		{"cluster_credentials", "user_id", "users", "user_id"},
		{"application_template_inputs", "template_id", "application_templates", "template_id"},
		{"application_template_outputs", "template_id", "application_templates", "template_id"},
		{"batch_application_deployments", "cluster_id", "clusters", "cluster_id"},
		{"batch_application_deployments", "template_id", "application_templates", "template_id"},
		{"batch_application_deployments", "batch_job_config_id", "batch_job_configs", "batch_job_config_id"},
		{"batch_application_deployments", "default_submission_credential_id", "cluster_credentials", "cluster_credential_id"},
		{"scp_data", "slurm_cluster_credential_id", "cluster_credentials", "cluster_credential_id"},
		{"scp_data", "user_id", "users", "user_id"},
		{"batch_job_processes", "deployment_id", "batch_application_deployments", "deployment_id"},
		{"batch_job_processes", "user_id", "users", "user_id"},
		{"batch_job_processes", "batch_job_config_id", "batch_job_configs", "batch_job_config_id"},
		{"batch_job_process_statuses", "process_id", "batch_job_processes", "process_id"},
		{"batch_job_processes", "last_status_id", "batch_job_process_statuses", "process_status_id"},
	}

	// Collect what the schema actually declares.
	got := map[fk]bool{}
	tables := map[string]bool{}
	for _, w := range want {
		tables[w.table] = true
		tables[w.refTable] = true
	}
	for table := range tables {
		rows, err := gdb.Raw(fmt.Sprintf("PRAGMA foreign_key_list(%q)", table)).Rows()
		if err != nil {
			t.Fatalf("read foreign keys of %s: %v", table, err)
		}
		for rows.Next() {
			var (
				id, seq                   int
				refTable, from, to        string
				onUpdate, onDelete, match string
			)
			if err := rows.Scan(&id, &seq, &refTable, &from, &to, &onUpdate, &onDelete, &match); err != nil {
				rows.Close()
				t.Fatalf("scan foreign key of %s: %v", table, err)
			}
			got[fk{table, from, refTable, to}] = true
		}
		rows.Close()
	}

	for _, w := range want {
		if !got[w] {
			t.Errorf("missing foreign key %s.%s -> %s.%s", w.table, w.column, w.refTable, w.refColumn)
		}
		delete(got, w)
	}
	for extra := range got {
		t.Errorf("unexpected foreign key %s.%s -> %s.%s (likely an inverted belongs-to)",
			extra.table, extra.column, extra.refTable, extra.refColumn)
	}
}
