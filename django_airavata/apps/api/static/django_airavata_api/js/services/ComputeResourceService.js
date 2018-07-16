import FetchUtils from '../utils/FetchUtils'

let defaultValue = {
    hostName: 'Hi',
    hostAliases: [''],
    ipAddresses: [''],
    resourceDescription: '',
    maxMemoryPerNode: 0,
    batchQueues: [{
        queueDescription: '',
        maxRunTime: 0,
        maxNodes: 0,
        maxProcessors: 0,
        maxJobsInQueue: 0,
        maxMemory: 0,
        cpuPerNode: 0,
        defaultNodeCount: 0,
        defaultCPUCount: 0,
        defaultWalltime: 0,
        queueSpecificMacros: '',
        isDefaultQueue: false
    }],
    fileSystems: {
        0: '',
        1: '',
        2: '',
        3: '',
        4: ''
    },
    jobSubmissionInterfaces: [],
    dataMovementInterfaces: [{
        dataMovementInterfaceId: 0,
        dataMovementProtocol: 3,
        priorityOrder: null
    }],
    gatewayUsageReporting: false,
    gatewayUsageModuleLoadCommand: null,
    gatewayUsageExecutable: null
}

class ComputeResourceService {
    constructor() {
        this.data=null
    }

    list() {
        return FetchUtils.get('/api/compute-resources/all_names_list')
    }

     retrieve(id) {
        this.data=null
        return  FetchUtils.get('/api/compute-resource/' + encodeURIComponent(id) + '/')
    }
}

export default new ComputeResourceService()