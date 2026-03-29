#!/bin/bash -x
set -euo pipefail

srun pmemd.cuda "$@"
