package org.apache.airavata.accountprovisioning.provisioner;

import org.apache.airavata.accountprovisioning.ConfigParam;
import org.apache.airavata.accountprovisioning.InvalidUsernameException;

import java.util.HashMap;
import java.util.Map;

public class TestIULdapSSHAccountProvisioner extends IULdapSSHAccountProvisioner {

        public static void main(String[] args) throws InvalidUsernameException {
        String ldapPassword = args[0];
        IULdapSSHAccountProvisioner sshAccountProvisioner = new IULdapSSHAccountProvisioner();
        Map<ConfigParam, String> config = new HashMap<>();
        // Create SSH tunnel to server that has firewall access to bazooka:
        //   ssh airavata@apidev.scigap.org -L 9000:bazooka.hps.iu.edu:636 -N &
        // Put entry in /etc/hosts with the following
        //   127.0.0.1	bazooka.hps.iu.edu
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_HOST, "bazooka.hps.iu.edu");
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_PORT, "9000"); // ssh tunnel port
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_USERNAME, "cn=sgrcusr,dc=rt,dc=iu,dc=edu");
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_PASSWORD, ldapPassword);
        config.put(IULdapSSHAccountProvisionerProvider.LDAP_BASE_DN, "ou=bigred2-sgrc,dc=rt,dc=iu,dc=edu");
        config.put(
                IULdapSSHAccountProvisionerProvider.CANONICAL_SCRATCH_LOCATION,
                "/N/dc2/scratch/${username}/iu-gateway");
        config.put(
                IULdapSSHAccountProvisionerProvider.CYBERGATEWAY_GROUP_DN,
                "cn=cybergateway,ou=Group,dc=rt,dc=iu,dc=edu");
        sshAccountProvisioner.init(config);
        String userId = "machrist@iu.edu";
        System.out.println("hasAccount=" + sshAccountProvisioner.hasAccount(userId));
        System.out.println("scratchLocation=" + sshAccountProvisioner.getScratchLocation(userId));
        String sshPublicKey = "foobar12345";
        boolean sshAccountProvisioningComplete =
                sshAccountProvisioner.isSSHAccountProvisioningComplete(userId, sshPublicKey);
        System.out.println("isSSHAccountProvisioningComplete=" + sshAccountProvisioningComplete);
        if (!sshAccountProvisioningComplete) {
            sshAccountProvisioner.installSSHKey(userId, sshPublicKey);
            sshAccountProvisioningComplete =
                    sshAccountProvisioner.isSSHAccountProvisioningComplete(userId, sshPublicKey);
            System.out.println("isSSHAccountProvisioningComplete=" + sshAccountProvisioningComplete);
        }
    }
}
