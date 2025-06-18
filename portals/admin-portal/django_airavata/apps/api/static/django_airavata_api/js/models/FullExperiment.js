import ApplicationModule from "./ApplicationModule";
import BaseModel from "./BaseModel";
import ComputeResourceDescription from "./ComputeResourceDescription";
import DataProduct from "./DataProduct";
import Experiment from "./Experiment";
import Job from "./Job";
import Project from "./Project";

const FIELDS = [
  "experimentId",
  {
    name: "experiment",
    type: Experiment,
  },
  {
    name: "project",
    type: Project,
  },
  {
    name: "applicationModule",
    type: ApplicationModule,
  },
  {
    name: "computeResource",
    type: ComputeResourceDescription,
  },
  {
    name: "outputDataProducts",
    type: DataProduct,
    list: true,
  },
  {
    name: "inputDataProducts",
    type: DataProduct,
    list: true,
  },
  {
    name: "jobDetails",
    type: Job,
    list: true,
  },
  {
    name: "outputViews",
    type: Object,
  },
];

export default class FullExperiment extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  get projectName() {
    return this.project ? this.project.name : null;
  }

  get applicationName() {
    return this.applicationModule ? this.applicationModule.appModuleName : null;
  }

  get computeHostName() {
    return this.computeResource ? this.computeResource.hostName : null;
  }

  get resourceHostId() {
    return this.experiment.resourceHostId;
  }

  get experimentStatus() {
    return this.experiment.latestStatus;
  }

  get experimentStatusName() {
    return this.experimentStatus ? this.experimentStatus.state.name : null;
  }
}
