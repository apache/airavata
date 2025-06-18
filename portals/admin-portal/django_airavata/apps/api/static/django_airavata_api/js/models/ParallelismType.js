import BaseEnum from "./BaseEnum";

export default class ParallelismType extends BaseEnum {}
ParallelismType.init([
  "SERIAL",
  "MPI",
  "OPENMP",
  "OPENMP_MPI",
  "CCM",
  "CRAY_MPI",
]);
