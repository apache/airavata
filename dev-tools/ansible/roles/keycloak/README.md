## Keycloak Deployment

###Configuration variables are distributed between :
1. `roles/keycloak/default/main.yml` - defaults for keycloak internal variables
2. `inventories/airavata-iam/group_vars/all.yml` - Global variables

###Running instructions:

1. Make sure all the variables are configured correctly
2. Dont use the Database role, rather set up the VM with default version provided by centos, tested with MySql 5.6 & MariaDB 5.5.52), Ansible role for the same is coming soon.
3. Ensure the host file: `inventories/airavata-iam/hosts` has correct IP address
4. For Standalone mode deployment : `ansible-playbook -i inventories/airavata-iam keycloak.yml`
