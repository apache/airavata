import BaseModel from "./BaseModel";
import ExperimentSummary from "./ExperimentSummary";

const FIELDS = [
  "allExperimentCount",
  "completedExperimentCount",
  "cancelledExperimentCount",
  "failedExperimentCount",
  "createdExperimentCount",
  "runningExperimentCount",
  {
    name: "allExperiments",
    type: ExperimentSummary,
    list: true,
  },
  {
    name: "completedExperiments",
    type: ExperimentSummary,
    list: true,
  },
  {
    name: "failedExperiments",
    type: ExperimentSummary,
    list: true,
  },
  {
    name: "cancelledExperiments",
    type: ExperimentSummary,
    list: true,
  },
  {
    name: "createdExperiments",
    type: ExperimentSummary,
    list: true,
  },
  { name: "runningExperiments", type: ExperimentSummary, list: true },
];

export default class ExperimentStatistics extends BaseModel {
  constructor(data = {}) {
    super(FIELDS, data);
  }
}
