package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.registry.core.entities.appcatalog.*;
import org.apache.airavata.registry.core.utils.AppCatalogUtils;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.ComputeResource;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeResourceRepository extends AppCatAbstractRepository<ComputeResourceDescription, ComputeResourceEntity, String> implements ComputeResource {

    private final static Logger logger = LoggerFactory.getLogger(ComputeResourceRepository.class);


    public ComputeResourceRepository() {
        super(ComputeResourceDescription.class, ComputeResourceEntity.class);
    }

    @Override
    public String addComputeResource(ComputeResourceDescription description) throws AppCatalogException {
        if (description.getComputeResourceId().equals("") || description.getComputeResourceId().equals(compute_resource_modelConstants.DEFAULT_ID)){
            description.setComputeResourceId(AppCatalogUtils.getID(description.getHostName()));
        }
        return saveComputeResourceDescriptorData(description);
    }

    protected String saveComputeResourceDescriptorData(
            ComputeResourceDescription description) throws AppCatalogException {
        //TODO remove existing one
        ComputeResourceEntity computeResourceEntity = saveComputeResource(description);
        saveHostAliases(description, computeResourceEntity);
        saveIpAddresses(description, computeResourceEntity);
        saveBatchQueues(description, computeResourceEntity);
        saveFileSystems(description, computeResourceEntity);
        saveJobSubmissionInterfaces(description, computeResourceEntity);
        saveDataMovementInterfaces(description, computeResourceEntity);
        return computeResourceEntity.getComputeResourceId();
    }

    protected ComputeResourceEntity saveComputeResource(
            ComputeResourceDescription description) throws AppCatalogException {
        ComputeResourceDescription computeResourceDescription = (new ComputeResourceRepository()).create(description);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(computeResourceDescription, ComputeResourceEntity.class);
    }

    protected void saveDataMovementInterfaces(
            ComputeResourceDescription description,
            ComputeResourceEntity computeHostResource)
            throws AppCatalogException {

        List<DataMovementInterface> dataMovemenetInterfaces = description.getDataMovementInterfaces();
        if (dataMovemenetInterfaces != null && !dataMovemenetInterfaces.isEmpty()) {
            for (DataMovementInterface dataMovementInterface : dataMovemenetInterfaces) {
                DataMovementInterfacePK dataMovementInterfacePK = new DataMovementInterfacePK();
                dataMovementInterfacePK.setComputeResourceId(computeHostResource.getComputeResourceId());
                dataMovementInterfacePK.setDataMovementInterfaceId(dataMovementInterface.getDataMovementInterfaceId());
                Mapper mapper = ObjectMapperSingleton.getInstance();
                DataMovementInterfaceEntity dataMovementInterfaceEntity = mapper.map(dataMovementInterface, DataMovementInterfaceEntity.class);
                dataMovementInterfaceEntity.setComputeResource(computeHostResource);
                dataMovementInterfaceEntity.setId(dataMovementInterfacePK);
                execute(entityManager -> entityManager.merge(dataMovementInterfaceEntity));
            }
        }

    }

    protected void saveJobSubmissionInterfaces(
            ComputeResourceDescription description,
            ComputeResourceEntity computeHostResource)
            throws AppCatalogException {

        List<JobSubmissionInterface> jobSubmissionInterfaces = description.getJobSubmissionInterfaces();
        if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
            for (JobSubmissionInterface jobSubmissionInterface : jobSubmissionInterfaces) {
                JobSubmissionInterfacePK jobSubmissionInterfacePK = new JobSubmissionInterfacePK();
                jobSubmissionInterfacePK.setComputeResourceId(computeHostResource.getComputeResourceId());
                jobSubmissionInterfacePK.setJobSubmissionInterfaceId(jobSubmissionInterface.getJobSubmissionInterfaceId());
                Mapper mapper = ObjectMapperSingleton.getInstance();
                JobSubmissionInterfaceEntity jobSubmissionInterfaceEntity= mapper.map(jobSubmissionInterface, JobSubmissionInterfaceEntity.class);
                jobSubmissionInterfaceEntity.setComputeResource(computeHostResource);
                jobSubmissionInterfaceEntity.setId(jobSubmissionInterfacePK);
                execute(entityManager -> entityManager.merge(jobSubmissionInterfaceEntity));
            }
        }
    }

    protected void saveFileSystems(ComputeResourceDescription description,
                                   ComputeResourceEntity computeHostResource)
            throws AppCatalogException {
        Map<FileSystems, String> fileSystems = description.getFileSystems();
        if (fileSystems != null && !fileSystems.isEmpty()) {
            for (FileSystems key : fileSystems.keySet()) {
                ComputeResourceFileSystemPK computeResourceFileSystemPK = new ComputeResourceFileSystemPK();
                computeResourceFileSystemPK.setComputeResourceId(computeHostResource.getComputeResourceId());
                computeResourceFileSystemPK.setFileSystem(key.toString());
                ComputeResourceFileSystemEntity computeResourceFileSystemEntity = new ComputeResourceFileSystemEntity();
                computeResourceFileSystemEntity.setId(computeResourceFileSystemPK);
                computeResourceFileSystemEntity.setComputeResource(computeHostResource);
                execute(entityManager -> entityManager.merge(computeResourceFileSystemEntity));
            }
        }
    }

    protected void saveBatchQueues(ComputeResourceDescription description,
                                   ComputeResourceEntity computeHostResource)
            throws AppCatalogException {
        List<BatchQueue> batchQueueList = description.getBatchQueues();
        if (batchQueueList != null && !batchQueueList.isEmpty()) {
            for (BatchQueue batchQueue : batchQueueList) {
                BatchQueuePK batchQueuePK =  new BatchQueuePK();
                batchQueuePK.setComputeResourceId(computeHostResource.getComputeResourceId());
                batchQueuePK.setQueueName(batchQueue.getQueueName());
                Mapper mapper = ObjectMapperSingleton.getInstance();
                BatchQueueEntity batchQueueEntity = mapper.map(batchQueue, BatchQueueEntity.class);
                batchQueueEntity.setComputeResource(computeHostResource);
                batchQueueEntity.setId(batchQueuePK);
                execute(entityManager -> entityManager.merge(batchQueueEntity));
            }
        }
    }

    protected void saveHostAliases(ComputeResourceDescription description,
                                   ComputeResourceEntity computeHostResource)
            throws AppCatalogException {
        List<String> hostAliases = description.getHostAliases();

        if (hostAliases != null && !hostAliases.isEmpty()) {
            for (String alias : hostAliases) {
                HostAliasPK hostAliasPKCheck = new HostAliasPK();
                hostAliasPKCheck.setAlias(alias);
                hostAliasPKCheck.setResourceId(description.getComputeResourceId());
                // delete previous host aliases
                execute(entityManager -> {
                    HostAliasEntity entity = entityManager.find(HostAliasEntity.class, hostAliasPKCheck);
                    entityManager.remove(entity);
                    return entity;
                });
            }
        }

        if (hostAliases != null && !hostAliases.isEmpty()) {
            for (String alias : hostAliases) {
                HostAliasPK hostAliasPK= new HostAliasPK();
                hostAliasPK.setResourceId(computeHostResource.getComputeResourceId());
                hostAliasPK.setAlias(alias);
                HostAliasEntity aliasEntity = new HostAliasEntity();
                aliasEntity.setComputeResource(computeHostResource);
                aliasEntity.setId(hostAliasPK);
                execute(entityManager -> entityManager.merge(aliasEntity));
            }
        }
    }

    protected void saveIpAddresses(ComputeResourceDescription description,
                                   ComputeResourceEntity computeHostResource)
            throws AppCatalogException {
        List<String> ipAddresses = description.getIpAddresses();
        if (ipAddresses != null && !ipAddresses.isEmpty()) {
            for (String ipAddress : ipAddresses) {
                HostIpaddressPK hostIpaddressPKCheck =  new HostIpaddressPK();
                hostIpaddressPKCheck.setResourceId(description.getComputeResourceId());
                hostIpaddressPKCheck.setIpAddress(ipAddress);
                execute(entityManager -> {
                    HostIpaddressEntity entity = entityManager.find(HostIpaddressEntity.class, hostIpaddressPKCheck);
                    entityManager.remove(entity);
                    return entity;
                });
            }
        }

        if (ipAddresses != null && !ipAddresses.isEmpty()) {
            for (String ipAddress : ipAddresses) {
                HostIpaddressPK hostIpaddressPK = new HostIpaddressPK();
                hostIpaddressPK.setIpAddress(ipAddress);
                hostIpaddressPK.setResourceId(computeHostResource.getComputeResourceId());
                HostIpaddressEntity hostIpaddressEntity = new HostIpaddressEntity();
                hostIpaddressEntity.setComputeResource(computeHostResource);
                hostIpaddressEntity.setId(hostIpaddressPK);
                execute(entityManager -> entityManager.merge(hostIpaddressEntity));
            }
        }
    }

    @Override
    public void updateComputeResource(String computeResourceId, ComputeResourceDescription updatedComputeResource) throws AppCatalogException {
            saveComputeResourceDescriptorData(updatedComputeResource);
    }

    @Override
    public ComputeResourceDescription getComputeResource(String resourceId) throws AppCatalogException {
        return get(resourceId);
    }

    @Override
    public List<ComputeResourceDescription> getComputeResourceList(Map<String, String> filters) throws AppCatalogException {
        if (filters.containsKey(DBConstants.ComputeResource.HOST_NAME)) {
            Map<String,Object> queryParameters = new HashMap<>();
            queryParameters.put(DBConstants.ComputeResource.HOST_NAME, filters.get(DBConstants.ComputeResource.HOST_NAME));
            return select(QueryConstants.FIND_COMPUTE_RESOURCE, -1, 0, queryParameters);
        }
        else {
            logger.error("Unsupported field name for compute resource.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported field name for compute resource.");
        }
    }

    @Override
    public List<ComputeResourceDescription> getAllComputeResourceList() throws AppCatalogException {
        return select(QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
    }

    @Override
    public Map<String, String> getAllComputeResourceIdList() throws AppCatalogException {
        Map<String, String> computeResourceMap = new HashMap<String, String>();
        List<ComputeResourceDescription> computeResourceDescriptionList = select(QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
        if (computeResourceDescriptionList != null && !computeResourceDescriptionList.isEmpty()) {
            for (ComputeResourceDescription computeResourceDescription: computeResourceDescriptionList) {
                computeResourceMap.put(computeResourceDescription.getComputeResourceId(), computeResourceDescription.getHostName());
            }
        }
        return computeResourceMap;
    }

    @Override
    public Map<String, String> getAvailableComputeResourceIdList() throws AppCatalogException {
        Map<String, String> computeResourceMap = new HashMap<String, String>();
        List<ComputeResourceDescription> computeResourceDescriptionList = select(QueryConstants.FIND_ALL_COMPUTE_RESOURCES, 0);
        if (computeResourceDescriptionList != null && !computeResourceDescriptionList.isEmpty()) {
            for (ComputeResourceDescription computeResourceDescription : computeResourceDescriptionList) {
                if (computeResourceDescription.isEnabled()){
                    computeResourceMap.put(computeResourceDescription.getComputeResourceId(), computeResourceDescription.getHostName());
                }
            }
        }
        return computeResourceMap;
    }

    @Override
    public boolean isComputeResourceExists(String resourceId) throws AppCatalogException {
        return isExists(resourceId);
    }

    @Override
    public void removeComputeResource(String resourceId) throws AppCatalogException {
        delete(resourceId);
    }

    @Override
    public String addSSHJobSubmission(SSHJobSubmission sshJobSubmission) throws AppCatalogException {
        String submissionId = AppCatalogUtils.getID("SSH");
        sshJobSubmission.setJobSubmissionInterfaceId(submissionId);
        String resourceJobManagerId = addResourceJobManager(sshJobSubmission.getResourceJobManager());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        SshJobSubmissionEntity sshJobSubmissionEntity = mapper.map(sshJobSubmission, SshJobSubmissionEntity.class);
        sshJobSubmissionEntity.setResourceJobManagerId(resourceJobManagerId);
        sshJobSubmissionEntity.getResourceJobManager().setResourceJobManagerId(resourceJobManagerId);
        if (sshJobSubmission.getMonitorMode() != null){
            sshJobSubmissionEntity.setMonitorMode(sshJobSubmission.getMonitorMode().toString());
        }
        execute(entityManager -> entityManager.merge(sshJobSubmissionEntity));
        return submissionId;
    }

    @Override
    public String addCloudJobSubmission(CloudJobSubmission cloudJobSubmission) throws AppCatalogException {
        cloudJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("Cloud"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        CloudJobSubmissionEntity cloudJobSubmissionEntity = mapper.map(cloudJobSubmission, CloudJobSubmissionEntity.class);
        execute(entityManager -> entityManager.merge(cloudJobSubmissionEntity));
        return cloudJobSubmissionEntity.getJobSubmissionInterfaceId();
    }

    @Override
    public String addResourceJobManager(ResourceJobManager resourceJobManager) throws AppCatalogException {
        ResourceJobManagerRepository resourceJobManagerRepository = new ResourceJobManagerRepository();
        resourceJobManager.setResourceJobManagerId(AppCatalogUtils.getID("RJM"));
        resourceJobManagerRepository.create(resourceJobManager);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ResourceJobManagerEntity resourceJobManagerEntity = mapper.map(resourceJobManager, ResourceJobManagerEntity.class);
        Map<JobManagerCommand, String> jobManagerCommands = resourceJobManager.getJobManagerCommands();
        if (jobManagerCommands!=null && jobManagerCommands.size() != 0) {
            resourceJobManagerRepository.createJobManagerCommand(jobManagerCommands, resourceJobManagerEntity);
        }

        Map<ApplicationParallelismType, String> parallelismPrefix = resourceJobManager.getParallelismPrefix();
        if (parallelismPrefix!=null && parallelismPrefix.size() != 0) {
            resourceJobManagerRepository.createParallesimPrefix(parallelismPrefix, resourceJobManagerEntity);
        }
        return resourceJobManager.getResourceJobManagerId();
    }

    @Override
    public void updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager) throws AppCatalogException {
        ResourceJobManagerRepository resourceJobManagerRepository = new ResourceJobManagerRepository();
        updatedResourceJobManager.setResourceJobManagerId(resourceJobManagerId);
        ResourceJobManager resourceJobManager = resourceJobManagerRepository.create(updatedResourceJobManager);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ResourceJobManagerEntity resourceJobManagerEntity = mapper.map(resourceJobManager, ResourceJobManagerEntity.class);
        Map<JobManagerCommand, String> jobManagerCommands = updatedResourceJobManager.getJobManagerCommands();
        if (jobManagerCommands!=null && jobManagerCommands.size() != 0) {
            resourceJobManagerRepository.createJobManagerCommand(jobManagerCommands, resourceJobManagerEntity);
        }

        Map<ApplicationParallelismType, String> parallelismPrefix = updatedResourceJobManager.getParallelismPrefix();
        if (parallelismPrefix!=null && parallelismPrefix.size() != 0) {
            resourceJobManagerRepository.createParallesimPrefix(parallelismPrefix, resourceJobManagerEntity);
        }
    }

    @Override
    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        return (new ResourceJobManagerRepository()).get(resourceJobManagerId);
    }

    @Override
    public void deleteResourceJobManager(String resourceJobManagerId) throws AppCatalogException {
        (new ResourceJobManagerRepository()).delete(resourceJobManagerId);
    }

    @Override
    public String addJobSubmissionProtocol(String computeResourceId, JobSubmissionInterface jobSubmissionInterface) throws AppCatalogException {
        JobSubmissionInterfacePK jobSubmissionInterfacePK = new JobSubmissionInterfacePK();
        jobSubmissionInterfacePK.setJobSubmissionInterfaceId(jobSubmissionInterface.getJobSubmissionInterfaceId());
        jobSubmissionInterfacePK.setComputeResourceId(computeResourceId);
        Mapper mapper = ObjectMapperSingleton.getInstance();
        JobSubmissionInterfaceEntity jobSubmissionInterfaceEntity = mapper.map(jobSubmissionInterface, JobSubmissionInterfaceEntity.class);
        ComputeResourceDescription computeResourceDescription = get(computeResourceId);
        ComputeResourceEntity computeResourceEntity = mapper.map(computeResourceDescription, ComputeResourceEntity.class);
        jobSubmissionInterfaceEntity.setComputeResource(computeResourceEntity);
        jobSubmissionInterfaceEntity.setId(jobSubmissionInterfacePK);
        execute(entityManager -> entityManager.merge(jobSubmissionInterfaceEntity));

        return jobSubmissionInterfacePK.getJobSubmissionInterfaceId();
    }

    @Override
    public String addLocalJobSubmission(LOCALSubmission localSubmission) throws AppCatalogException {
        localSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("LOCAL"));
        String resourceJobManagerId = addResourceJobManager(localSubmission.getResourceJobManager());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        LocalSubmissionEntity localSubmissionEntity = mapper.map(localSubmission, LocalSubmissionEntity.class);
        localSubmissionEntity.setResourceJobManagerId(resourceJobManagerId);
        localSubmissionEntity.getResourceJobManager().setResourceJobManagerId(resourceJobManagerId);
        localSubmissionEntity.setSecurityProtocol(localSubmission.getSecurityProtocol().toString());
        execute(entityManager -> entityManager.merge(localSubmissionEntity));
        return localSubmissionEntity.getJobSubmissionInterfaceId();
    }

    @Override
    public String addGlobusJobSubmission(GlobusJobSubmission globusJobSubmission) throws AppCatalogException {
        return null;
    }

    @Override
    public String addUNICOREJobSubmission(UnicoreJobSubmission unicoreJobSubmission) throws AppCatalogException {
        unicoreJobSubmission.setJobSubmissionInterfaceId(AppCatalogUtils.getID("UNICORE"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UnicoreSubmissionEntity unicoreSubmissionEntity = mapper.map(unicoreJobSubmission, UnicoreSubmissionEntity.class);
        if (unicoreJobSubmission.getSecurityProtocol() !=  null) {
            unicoreSubmissionEntity.setSecurityProtocol(unicoreJobSubmission.getSecurityProtocol().toString());
        }
        execute(entityManager -> entityManager.merge(unicoreSubmissionEntity));
        return unicoreJobSubmission.getJobSubmissionInterfaceId();
    }

    @Override
    public String addLocalDataMovement(LOCALDataMovement localDataMovement) throws AppCatalogException {
        localDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("LOCAL"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        LocalDataMovementEntity localDataMovementEntity = mapper.map(localDataMovement, LocalDataMovementEntity.class);
        execute(entityManager -> entityManager.merge(localDataMovementEntity));
        return localDataMovementEntity.getDataMovementInterfaceId();
    }

    @Override
    public String addScpDataMovement(SCPDataMovement scpDataMovement) throws AppCatalogException {
        scpDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("SCP"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ScpDataMovementEntity scpDataMovementEntity = mapper.map(scpDataMovement, ScpDataMovementEntity.class);
        execute(entityManager -> entityManager.merge(scpDataMovementEntity));
        return scpDataMovementEntity.getDataMovementInterfaceId();
    }

    @Override
    public String addUnicoreDataMovement(UnicoreDataMovement unicoreDataMovement) throws AppCatalogException {
        unicoreDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("UNICORE"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        UnicoreDatamovementEntity unicoreDatamovementEntity = mapper.map(unicoreDataMovement, UnicoreDatamovementEntity.class);
        execute(entityManager -> entityManager.merge(unicoreDatamovementEntity));
        return unicoreDatamovementEntity.getDataMovementInterfaceId();
    }

    @Override
    public String addDataMovementProtocol(String resourceId, DMType dmType, DataMovementInterface dataMovementInterface) throws AppCatalogException {
        if (dmType.equals(DMType.COMPUTE_RESOURCE)){
            DataMovementInterfacePK dataMovementInterfacePK = new DataMovementInterfacePK();
            dataMovementInterfacePK.setComputeResourceId(resourceId);
            dataMovementInterfacePK.setDataMovementInterfaceId(dataMovementInterface.getDataMovementInterfaceId());
            Mapper mapper = ObjectMapperSingleton.getInstance();
            DataMovementInterfaceEntity dataMovementInterfaceEntity = mapper.map(dataMovementInterface, DataMovementInterfaceEntity.class);
            ComputeResourceEntity computeResourceEntity = mapper.map(get(resourceId), ComputeResourceEntity.class);
            dataMovementInterfaceEntity.setComputeResource(computeResourceEntity);
            dataMovementInterfaceEntity.setId(dataMovementInterfacePK);
            return dataMovementInterfacePK.getDataMovementInterfaceId();
        }
        else if (dmType.equals(DMType.STORAGE_RESOURCE)){
            //TODO - COMPLETE this after StorageResourceRepo implementation
            return null;
        }
        return null;
    }

    @Override
    public String addGridFTPDataMovement(GridFTPDataMovement gridFTPDataMovement) throws AppCatalogException {
        gridFTPDataMovement.setDataMovementInterfaceId(AppCatalogUtils.getID("GRIDFTP"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        GridftpDataMovementEntity gridftpDataMovementEntity = mapper.map(gridFTPDataMovement, GridftpDataMovementEntity.class);
        execute(entityManager -> entityManager.merge(gridftpDataMovementEntity));
        List<String> gridFTPEndPoint = gridFTPDataMovement.getGridFTPEndPoints();
        if (gridFTPEndPoint != null && !gridFTPEndPoint.isEmpty()) {
            for (String endpoint : gridFTPEndPoint) {
                GridftpEndpointPK gridftpEndpointPK = new GridftpEndpointPK();
                gridftpEndpointPK.setDataMovementInterfaceId(gridFTPDataMovement.getDataMovementInterfaceId());
                gridftpEndpointPK.setEndpoint(endpoint);
                GridftpEndpointEntity gridftpEndpointEntity = new GridftpEndpointEntity();
                gridftpEndpointEntity.setGridftpDataMovement(gridftpDataMovementEntity);
                gridftpEndpointEntity.setId(gridftpEndpointPK);
                execute(entityManager -> entityManager.merge(gridftpEndpointEntity));
            }
        }
        return gridftpDataMovementEntity.getDataMovementInterfaceId();
    }

    @Override
    public SSHJobSubmission getSSHJobSubmission(String submissionId) throws AppCatalogException {
        SshJobSubmissionEntity entity = execute(entityManager -> entityManager
                .find(SshJobSubmissionEntity.class, submissionId));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, SSHJobSubmission.class);
    }

    @Override
    public UnicoreJobSubmission getUNICOREJobSubmission(String submissionId) throws AppCatalogException {
        UnicoreSubmissionEntity entity = execute(entityManager -> entityManager
                .find(UnicoreSubmissionEntity.class, submissionId));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, UnicoreJobSubmission.class);
    }

    @Override
    public UnicoreDataMovement getUNICOREDataMovement(String dataMovementId) throws AppCatalogException {
        UnicoreDatamovementEntity entity = execute(entityManager -> entityManager
                .find(UnicoreDatamovementEntity.class, dataMovementId));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, UnicoreDataMovement.class);
    }

    @Override
    public CloudJobSubmission getCloudJobSubmission(String submissionId) throws AppCatalogException {
        CloudJobSubmissionEntity entity = execute(entityManager -> entityManager
                .find(CloudJobSubmissionEntity.class, submissionId));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, CloudJobSubmission.class);
    }

    @Override
    public SCPDataMovement getSCPDataMovement(String dataMoveId) throws AppCatalogException {
        ScpDataMovementEntity entity = execute(entityManager -> entityManager
                .find(ScpDataMovementEntity.class, dataMoveId));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, SCPDataMovement.class);
    }

    @Override
    public GridFTPDataMovement getGridFTPDataMovement(String dataMoveId) throws AppCatalogException {
        GridftpDataMovementEntity entity = execute(entityManager -> entityManager
                .find(GridftpDataMovementEntity.class, dataMoveId));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, GridFTPDataMovement.class);
    }

    @Override
    public void removeJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId) throws AppCatalogException {
        JobSubmissionInterfacePK jobSubmissionInterfacePK = new JobSubmissionInterfacePK();
        jobSubmissionInterfacePK.setComputeResourceId(computeResourceId);
        jobSubmissionInterfacePK.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
        execute(entityManager -> {
            JobSubmissionInterfaceEntity entity = entityManager.find(JobSubmissionInterfaceEntity.class, jobSubmissionInterfacePK);
            entityManager.remove(entity);
            return entity;
        });
    }

    @Override
    public void removeDataMovementInterface(String computeResourceId, String dataMovementInterfaceId) throws AppCatalogException {
        DataMovementInterfacePK dataMovementInterfacePK = new DataMovementInterfacePK();
        dataMovementInterfacePK.setDataMovementInterfaceId(dataMovementInterfaceId);
        dataMovementInterfacePK.setComputeResourceId(computeResourceId);
        execute(entityManager -> {
            DataMovementInterfaceEntity entity = entityManager.find(DataMovementInterfaceEntity.class, dataMovementInterfacePK);
            entityManager.remove(entity);
            return entity;
        });
    }

    @Override
    public void removeBatchQueue(String computeResourceId, String queueName) throws AppCatalogException {
        BatchQueuePK batchQueuePK = new BatchQueuePK();
        batchQueuePK.setQueueName(queueName);
        batchQueuePK.setComputeResourceId(computeResourceId);
        execute(entityManager -> {
            BatchQueueEntity entity = entityManager.find(BatchQueueEntity.class, batchQueuePK);
            entityManager.remove(entity);
            return entity;
        });
    }

    @Override
    public LOCALSubmission getLocalJobSubmission(String submissionId) throws AppCatalogException {
        LocalSubmissionEntity entity = execute(entityManager -> entityManager
                .find(LocalSubmissionEntity.class, submissionId));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, LOCALSubmission.class);
    }

    @Override
    public LOCALDataMovement getLocalDataMovement(String datamovementId) throws AppCatalogException {
        LocalDataMovementEntity entity = execute(entityManager -> entityManager
                .find(LocalDataMovementEntity.class, datamovementId));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, LOCALDataMovement.class);
    }
}
