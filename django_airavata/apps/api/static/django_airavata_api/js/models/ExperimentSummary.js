import BaseModel from "./BaseModel";
import Experiment from "./Experiment";
import ExperimentStatus from "./ExperimentStatus";
import ExperimentState from "./ExperimentState";

const FIELDS = [
  "experimentId",
  "projectId",
  "gatewayId",
  {
    name: "creationTime",
    type: "date",
  },
  "userName",
  "name",
  "description",
  "executionId",
  "resourceHostId",
  {
    name: "experimentStatus",
    type: ExperimentState,
  },
  {
    name: "statusUpdateTime",
    type: "date",
  },
  {
    name: "userHasWriteAccess",
    type: "boolean",
    default: false,
  },
];

export default class ExperimentSummary extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }

  get isEditable() {
    return this.convertToExperiment().isEditable;
  }

  convertToExperiment() {
    // Purpose of this is to be able to access computed properties on
    // Experiment.js
    return new Experiment(
      Object.assign({}, this, {
        // Most properties are named the same as on Experiment, but the
        // following require some conversion
        experimentName: this.name,
        experimentStatus: [
          new ExperimentStatus({
            state: this.experimentStatus,
            timeOfStateChange: this.statusUpdateTime,
          }),
        ],
      })
    );
  }
}
