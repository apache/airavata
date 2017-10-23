package org.apache.airavata.k8s.api.server.model.job;

import org.apache.airavata.k8s.api.server.model.task.TaskModel;

import javax.persistence.*;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "JOB_MODEL")
public class JobModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private TaskModel task;

    private String jobDescription; // required
    private long creationTime; // optional

    @OneToMany
    private List<JobStatus> jobStatuses; // optional

    private String computeResourceConsumed; // optional
    private String jobName; // optional
    private String workingDir; // optional
    private String stdOut; // optional
    private String stdErr; // optional
    private int exitCode; // optional
}
