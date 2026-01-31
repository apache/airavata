/**
 * JPA entities for the Airavata registry.
 *
 * <h2>Package layout</h2>
 *
 * <ul>
 *   <li><b>Root (this package)</b> – Cross-cutting entities: {@link org.apache.airavata.registry.entities.GatewayEntity},
 *       {@link org.apache.airavata.registry.entities.UserEntity}, {@link org.apache.airavata.registry.entities.StatusEntity},
 *       {@link org.apache.airavata.registry.entities.ErrorEntity}, {@link org.apache.airavata.registry.entities.MetadataEntity},
 *       {@link org.apache.airavata.registry.entities.InputDataEntity}, {@link org.apache.airavata.registry.entities.OutputDataEntity},
 *       {@link org.apache.airavata.registry.entities.ResourceAccessEntity}, {@link org.apache.airavata.registry.entities.ResourceAccessGrantEntity},
 *       {@link org.apache.airavata.registry.entities.UserGroupSelectionEntity}.</li>
 *   <li><b>appcatalog</b> – Application catalog: interfaces, deployments, compute/storage resources,
 *       job submission and data movement, resource profiles and preferences, batch queues, parsers.</li>
 *   <li><b>expcatalog</b> – Experiment catalog: projects, experiments, processes, tasks, jobs,
 *       notifications, user configuration.</li>
 *   <li><b>airavataworkflowcatalog</b> – Workflow definitions: Airavata workflows, applications,
 *       connections, handlers, data blocks.</li>
 *   <li><b>replicacatalog</b> – Replica catalog: data products and replica locations.</li>
 *   <li><b>catalog</b> – Research catalog resources (datasets, repositories).</li>
 * </ul>
 *
 * <h2>Conventions</h2>
 *
 * <ul>
 *   <li><b>gatewayId</b> is denormalized on many entities for query performance and gateway scoping; keep in sync on write.</li>
 *   <li>Composite keys use dedicated {@code *PK} classes and {@code @IdClass}.</li>
 *   <li>Credential references use token IDs with {@link org.apache.airavata.credential.entities.CredentialEntity} as source of truth.</li>
 *   <li>Partition and allocation project are always (Slurm cluster, credential)-specific; discover via
 *       {@link org.apache.airavata.service.cluster.ClusterInfoService} / {@link org.apache.airavata.registry.entities.appcatalog.CredentialClusterInfoEntity}.</li>
 *   <li>Groups (group resource profiles) model shared allocations; use {@link org.apache.airavata.registry.entities.appcatalog.AllocationPoolEntity}
 *       to merge groups that represent the same project across runtimes into one pool.</li>
 * </ul>
 */
package org.apache.airavata.registry.entities;
