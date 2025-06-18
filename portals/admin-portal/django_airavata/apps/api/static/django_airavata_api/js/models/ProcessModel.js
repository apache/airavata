import BaseModel from "./BaseModel";
import ProcessStatus from "./ProcessStatus";
import InputDataObjectType from "./InputDataObjectType";
import OutputDataObjectType from "./OutputDataObjectType";
import ComputationalResourceSchedulingModel from "./ComputationalResourceSchedulingModel";
import Task from "./Task";
import ErrorModel from "./ErrorModel";
import ProcessWorkflow from "./ProcessWorkflow";

const FIELDS = [
  "processId",
  "experimentId",
  {
    name: "creationTime",
    type: Date,
  },
  {
    name: "lastUpdateTime",
    type: Date,
  },
  {
    name: "processStatuses",
    type: ProcessStatus,
    list: true,
  },
  "processDetail",
  "applicationInterfaceId",
  "applicationDeploymentId",
  "computeResourceId",
  {
    name: "processInputs",
    type: InputDataObjectType,
    list: true,
  },
  {
    name: "processOutputs",
    type: OutputDataObjectType,
    list: true,
  },
  {
    name: "processResourceSchedule",
    type: ComputationalResourceSchedulingModel,
  },
  {
    name: "tasks",
    type: Task,
    list: true,
  },
  "taskDag",
  {
    name: "processErrors",
    type: ErrorModel,
    list: true,
  },
  "gatewayExecutionId",
  "enableEmailNotification",
  "emailAddresses",
  "storageResourceId",
  "userDn",
  "generateCert",
  "experimentDataDir",
  "userName",
  "useUserCRPref",
  "groupResourceProfileId",
  {
    name: "processWorkflows",
    type: ProcessWorkflow,
    list: true,
  },
];

export default class ProcessModel extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  /**
   * Return tasks sorted by task DAG order.
   */
  get sortedTasks() {
    const tasksArrCopy = this.tasks.slice();
    tasksArrCopy.sort((a, b) => {
      const aIndex = this.taskDagArray.findIndex((t) => t === a.taskId);
      const bIndex = this.taskDagArray.findIndex((t) => t === b.taskId);
      return aIndex - bIndex;
    });
    return tasksArrCopy;
  }

  get taskDagArray() {
    return this.taskDag ? this.taskDag.split(",") : [];
  }
}
