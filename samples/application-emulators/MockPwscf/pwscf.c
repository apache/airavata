#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "mpi.h"

/* header files for getting hostname and process id */
#include <unistd.h>
#include <sys/types.h>

main(int argc, char* argv[]) {
int my_rank; /* rank of process */
int num; /* number of processes */

/* Variables to store the hostname and process id */
char hostname[128];
size_t len = 126;
pid_t pid;

//Read inputs
FILE *fp_Pwscf_Input;
FILE *fp_pseudo_1;
FILE *fp_pseudo_2; 

int i;


//Output data
FILE *fp_output;
FILE *fp_output_2;

char string_input[2048];
char string_pseudo_1[2048];
char string_pseudo_2[2048];

char string_output[2048];
char string_output_2[2048];

 if((fp_output=fopen("Pwscf_Output", "w")) == NULL) {
    printf("Cannot Pwscf_Output open file.\n");
  }

 if((fp_output_2=fopen("tmp/output_2_binary", "w")) == NULL) {
    printf("Cannot Output binary open file.\n");
  }


//Writing output
   strcpy(string_output," For Norm-Conserving or Ultrasoft (Vanderbilt) Pseudopotentials or PAW");
   strcat(string_output, "\nCurrent dimensions of program pwscf are:");
   strcat(string_output,"\nMax number of different atomic species (ntypx) = 10");
   strcat(string_output,"\nMax number of k-points (npk) =  40000");
   strcat(string_output,"\nMax angular momentum in pseudopotentials (lmaxx) =  3\n");
   strcat(string_output,"\n pwscf_ctrl_msg 0002 : dynamics converged !\n");
   strcat(string_output, "\nWriting output data file pwscf.save\n");
   strcat(string_output, "\nPWSCF        :     4.65s CPU time,    9.53s wall time\n");

//Writing data output 2
   strcpy(string_output_2," This part consist of binary data.");


/* Start up MPI */
MPI_Init(&argc, &argv);

/* Find out process rank */
MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);

/* Find out number of processes */
MPI_Comm_size(MPI_COMM_WORLD, &num);

/* Get the hostname for this process */
gethostname(hostname, len);
pid = getpid();

/* Print out the hostname and process id for this process */
printf("Hello from process %d(%s:%d) out of %d!\n" , my_rank , hostname, pid, num);
fflush(stdout);

/* Sleep until I login to the host, run the debugger and attach to the relevant process */
sleep(5);

for (i=0;i<num;i++) {
  if (my_rank == i) {
    gethostname(hostname, len);
    printf("Dummy computation by process %d out of %d!\n",my_rank,num);

     if((fp_Pwscf_Input=fopen("Pwscf_Input", "r")) == NULL) {
        printf("Cannot open Pwscf_Input file.\n");
      }
      fscanf(fp_Pwscf_Input, "%s", string_input) ; 	
      fprintf(stdout, "%s", string_input); /* print on screen */	

     if((fp_pseudo_1=fopen("pseudo_1", "r")) == NULL) {
        printf("Cannot open Pseudo_File1 file.\n");
      }
     fscanf(fp_pseudo_1, "%s", string_pseudo_1) ; 
      fprintf(stdout, "%s", string_pseudo_1); /* print on screen */
 
      if((fp_pseudo_2=fopen("pseudo_2", "r")) == NULL) {
        printf("Cannot open Pseudo_File2 file.\n");
      }	
     fscanf(fp_pseudo_2, "%s", string_pseudo_2) ;
      fprintf(stdout, "%s", string_pseudo_2); /* print on screen */	

    fflush(stdout);

   fprintf(fp_output, "%s", string_output); /* write to file */
   fprintf(fp_output_2, "%s", string_output_2); /* write to file - 2 */

    fclose(fp_output); 
    //fclose(fp_Pwscf_Input);
    //fclose(fp_pseudo_1);
    //fclose(fp_pseudo_2);
    fclose(fp_output_2);
  }
}

/* Shut down MPI */
MPI_Finalize();
} /* main */
