package org.apache.airavata.accountprovisioning;

import com.jcraft.jsch.JSchException;
import org.apache.airavata.model.credential.store.SSHCredential;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSSHUtil extends SSHUtil {
        public static void main(String[] args) throws JSchException {

        // Test the validate method
        String username = System.getProperty("user.name");
        String privateKeyFilepath = System.getProperty("user.home") + "/.ssh/id_rsa";
        String publicKeyFilepath = privateKeyFilepath + ".pub";
        String passphrase = "changeme";
        String hostname = "changeme";

        Path privateKeyPath = Paths.get(privateKeyFilepath);
        Path publicKeyPath = Paths.get(publicKeyFilepath);

        SSHCredential sshCredential = new SSHCredential();
        sshCredential.setPassphrase(passphrase);
        try {
            sshCredential.setPublicKey(new String(Files.readAllBytes(publicKeyPath), "UTF-8"));
            sshCredential.setPrivateKey(new String(Files.readAllBytes(privateKeyPath), "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean result = validate(hostname, 22, username, sshCredential);
        System.out.println(result);
    }
}
