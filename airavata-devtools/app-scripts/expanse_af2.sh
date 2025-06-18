#!/bin/bash -x

# #####################################################################
# AlphaFold2 Driver + Airavata Agent for Expanse
# #####################################################################
#
# ----------------------------------------------------------------------
# CONTRIBUTORS
# ----------------------------------------------------------------------
# * Sudhakar Pamidigantham
# * Lahiru Jayathilake
# * Dimuthu Wannipurage
# * Yasith Jayawardana
#
# ######################################################################

########################################################################
# Part 1 - Housekeeping
########################################################################

#-----------------------------------------------------------------------
# Step 1.1 - Check command line
#-----------------------------------------------------------------------

while getopts t:p:m: option; do
  case $option in
  t) MaxDate=$OPTARG ;;
  p) MODEL_PRESET=$OPTARG ;;
  m) Num_Multi=$OPTARG ;;
  \?) cat <<ENDCAT ;;
>! Usage: $0  [-t Maximum Template Date ]    !<
>!            [-p  Model Preset ]      !<
>!            [-m  Number of Multimers per Model ]      !<
ENDCAT
    #   exit 1 ;;
  esac
done

if [ $Num_Multi = "" ]; then
  export Num_Multi=1
fi
#set the environment PATH
export PYTHONNOUSERSITE=True
module reset
module load singularitypro
ALPHAFOLD_DATA_PATH=/expanse/projects/qstore/data/alphafold-v2.3.2
ALPHAFOLD_MODELS=/expanse/projects/qstore/data/alphafold-v2.3.2/params

#ALPHAFOLD_DATA_PATH=/expanse/projects/qstore/data/alphafold
#ALPHAFOLD_MODELS=/expanse/projects/qstore/data/alphafold/params
pdb70=""
uniprot=""
pdbseqres=""
nummulti=""

# check_flags
if [ "monomer" = "${MODEL_PRESET%_*}" ]; then
  export pdb70="--pdb70_database_path=/data/pdb70/pdb70"
else
  export uniprot="--uniprot_database_path=/data/uniprot/uniprot.fasta"
  export pdbseqres="--pdb_seqres_database_path=/data/pdb_seqres/pdb_seqres.txt"
  export nummulti="--num_multimer_predictions_per_model=$Num_Multi"
fi

## Copy input to node local scratch
cp input.fasta /scratch/$USER/job_$SLURM_JOBID
#cp -r /expanse/projects/qstore/data/alphafold/uniclust30/uniclust30_2018_08 /scratch/$USER/job_$SLURM_JOBID/
cd /scratch/$USER/job_$SLURM_JOBID
ln -s /expanse/projects/qstore/data/alphafold/uniclust30/uniclust30_2018_08
mkdir bfd
cp /expanse/projects/qstore/data/alphafold/bfd/*index bfd/
#cp /expanse/projects/qstore/data/alphafold/bfd/bfd_metaclust_clu_complete_id30_c90_final_seq.sorted_opt_hhm.ffdata bfd/
#cp /expanse/projects/qstore/data/alphafold/bfd/bfd_metaclust_clu_complete_id30_c90_final_seq.sorted_opt_cs219.ffdata bfd/
cd bfd
ln -s /expanse/projects/qstore/data/alphafold/bfd/bfd_metaclust_clu_complete_id30_c90_final_seq.sorted_opt_hhm.ffdata
ln -s /expanse/projects/qstore/data/alphafold/bfd/bfd_metaclust_clu_complete_id30_c90_final_seq.sorted_opt_cs219.ffdata
ln -s /expanse/projects/qstore/data/alphafold/bfd/bfd_metaclust_clu_complete_id30_c90_final_seq.sorted_opt_a3m.ffdata
cd ../
mkdir alphafold_output
# Create soft links ro rundir form submitdir

ln -s /scratch/$USER/job_$SLURM_JOBID $SLURM_SUBMIT_DIR/rundir

#Run the command
singularity run --nv \
  -B /expanse/lustre \
  -B /expanse/projects \
  -B /scratch \
  -B $ALPHAFOLD_DATA_PATH:/data \
  -B $ALPHAFOLD_MODELS \
  /cm/shared/apps/containers/singularity/alphafold/alphafold_aria2_v2.3.2.simg \
  --fasta_paths=/scratch/$USER/job_$SLURM_JOBID/input.fasta \
  --uniref90_database_path=/data/uniref90/uniref90.fasta \
  --data_dir=/data \
  --mgnify_database_path=/data/mgnify/mgy_clusters_2022_05.fa \
  --bfd_database_path=/scratch/$USER/job_$SLURM_JOBID/bfd/bfd_metaclust_clu_complete_id30_c90_final_seq.sorted_opt \
  --uniref30_database_path=/data/uniref30/UniRef30_2021_03 \
  $pdbseqres \
  $pdb70 \
  $uniprot \
  --template_mmcif_dir=/data/pdb_mmcif/mmcif_files \
  --obsolete_pdbs_path=/data/pdb_mmcif/obsolete.dat \
  --output_dir=/scratch/$USER/job_$SLURM_JOBID/alphafold_output \
  --max_template_date=$MaxDate \
  --model_preset=$MODEL_PRESET \
  --use_gpu_relax=true \
  --models_to_relax=best \
  $nummulti

#-B .:/etc \
#/cm/shared/apps/containers/singularity/alphafold/alphafold.sif  \
#--fasta_paths=input.fasta  \
#--uniref90_database_path=/data/uniref90/uniref90.fasta  \
#--data_dir=/data \
#--mgnify_database_path=/data/mgnify/mgy_clusters.fa   \
#--bfd_database_path=/scratch/$USER/job_$SLURM_JOBID/bfd/bfd_metaclust_clu_complete_id30_c90_final_seq.sorted_opt \
#--uniclust30_database_path=/scratch/$USER/job_$SLURM_JOBID/uniclust30_2018_08/uniclust30_2018_08 \
#--pdb70_database_path=/data/pdb70/pdb70  \
#--template_mmcif_dir=/data/pdb_mmcif/mmcif_files  \
#--obsolete_pdbs_path=/data/pdb_mmcif/obsolete.dat \
#--output_dir=alphafold_output  \
#--max_template_date=$MaxDate \
#--preset=$MODEL_PRESET

#make a user choice --preset=casp14
#make this a user choice  --max_template_date=2020-05-14   \
# --model_names='model_1' \
# Remove model data
unlink $SLURM_SUBMIT_DIR/rundir

### Copy back results

tar -cvf $SLURM_SUBMIT_DIR/alphafold_output.tar alphafold_output
