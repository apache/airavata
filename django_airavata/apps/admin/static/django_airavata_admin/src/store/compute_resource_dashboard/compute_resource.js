import Vue from 'vue'
import cloudJobSubmission from './cloud_job_submission'
import globusJobSubmission from './globus_job_submission'
import localJobSubmission from './local_job_submission'
import sshJobSubmission from './ssh_job_submission'
import unicoreJobSubmission from './unicore_job_submission'
import dataMovement from './data_movement'
import DjangoAiravataAPI from 'django-airavata-api'
import Utils from '../../utils'

let batchQueues = function () {
  return {
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
  }
}
let jobSubmission = function (id, protocol) {
  return {
    jobSubmissionInterfaceId: id,
    jobSubmissionProtocol: protocol,
    priorityOrder: null,
  }
}

let dataMovementDefault = function (id, protocol) {
  return {
    dataMovementInterfaceId: id,
    dataMovementProtocol: protocol,
    priorityOrder: null
  }
}

let defaultData = function () {
  return {
    hostName: '',
    hostAliases: [''],
    ipAddresses: [''],
    resourceDescription: '',
    maxMemoryPerNode: 0,
    batchQueues: [batchQueues()],
    fileSystems: {
      0: '',
      1: '',
      2: '',
      3: '',
      4: ''
    },
    jobSubmissionInterfaces: [],
    dataMovementInterfaces: [],
    gatewayUsageReporting: false,
    gatewayUsageModuleLoadCommand: null,
    gatewayUsageExecutable: null
  }
}

export default {
  namespaced: true,
  modules: {
    cloudJobSubmission,
    globusJobSubmission,
    localJobSubmission,
    sshJobSubmission,
    unicoreJobSubmission,
    dataMovement
  },
  state: {
    data: defaultData(),
    fetch: true,
    editable: false,
    counter: 0,
    id: null
  },
  mutations: {
    updateStore: function (state, data) {
      state.data = data
    },
    resetStore: function (state, {resetFields = []}) {
      if (resetFields.length == 0) {
        state.data = defaultData()
      } else {
        let defData = defaultData();
        for (let resetField of resetFields) {
          Vue.set(state.data, resetField, defData[resetField])
        }
      }
    },
    addJobSubmission: function (state, {id = null, protocol = null}) {
      state.data.jobSubmissionInterfaces.push(jobSubmission(id, protocol))
    },
    addDataMovement: function (state, {id = null, protocol = null}) {
      state.data.dataMovementInterfaces.push(dataMovementDefault(id, protocol))
    },
    setComputeResourceId: function (state, id) {
      state.id = id
    }
  },
  getters: {
    data: (state) => {
      if (state.fetch) {
        state.fetch = false
        console.log("before")
        DjangoAiravataAPI.services.ComputeResourceService.retrieve(state.id).then((value) => {
          Vue.set(state, 'data', value);
        })
        console.log("after")

      }
      return state.data
    },
    counter: (state) => () => {
      state.counter++
      return state.counter
    },
    editable: (state) => state.editable,
    createBatchQueue: (state) => batchQueues(),
    createJobSubmission: (state) => jobSubmission()
  },
  actions: {
    save: function ({commit, state, rootState}) {
    },
    fetch: function ({commit, state, rootState}, value) {
      console.log(state, rootState)
      state.fetch = value
      rootState.computeResource.cloudJobSubmission.fetch = value
      rootState.computeResource.dataMovement.fetch = value
      rootState.computeResource.globusJobSubmission.fetch = value
      rootState.computeResource.localJobSubmission.fetch = value
      rootState.computeResource.sshJobSubmission.fetch = value
      rootState.computeResource.unicoreJobSubmission.fetch = value
    }
  }
}
