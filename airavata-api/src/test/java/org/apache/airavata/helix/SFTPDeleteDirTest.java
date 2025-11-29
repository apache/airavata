package org.apache.airavata.helix;

import org.apache.airavata.helix.adaptor.SSHJAgentAdaptor;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class SFTPDeleteDirTest {

    private SshServer sshd;
    private int port;
    private Path sftpRootDir;
    private int sftpPort = 52122;

    private String privateKey = "-----BEGIN OPENSSH PRIVATE KEY-----\n" +
            "b3BlbnNzaC1rZXktdjEAAAAACmFlczI1Ni1jdHIAAAAGYmNyeXB0AAAAGAAAABCHHZONdz\n" +
            "yrWLbnw4nyEw3BAAAAGAAAAAEAAAGXAAAAB3NzaC1yc2EAAAADAQABAAABgQCxhKaGaUlU\n" +
            "Znlr6OtFQ7hjVceZsWLBaIWB5NUwp45IoWLm7Hnor+Y8J/SwLBgdUSsjxkMUQMMbJdY/rP\n" +
            "Gwc8aW0K1JMjSNhv03dBxvXHdY+NSd24WjSezD89l6v78lGhVQ5g3rI4eTFsfPy2WSxZw1\n" +
            "Fo0UUDVzzBtLuvC9ZCWd3nsT8Ox4LnZWLHrrRxGX2eCotEEO6CT1+wmk4szIkeDDmX79Tb\n" +
            "KatcN2vv7H6WjsoGH1bhc1QwS2/hmOdBwqGfm+sE0BI3VgMJ1NVDQrnt0IXlMtLH9Feg1y\n" +
            "tdagCzaHulQ9lHn1wBzSARP/NqYzu2vNwpWSSJeafClHpA8yF/9FW9gOi4k+Oo949b+Xd1\n" +
            "NHqjt+7lnlsepm0IsgfJ9Gr/0sweYfUSsTfGZGMstSRMu8V+bD1BaVqXQKZ80XoCm0NMnR\n" +
            "Chm109wXtt5+0atDmIFiy1Byr8QjwjqsIap1j93R/8R3L3mhUmLruSl7IPKPhjShEIL253\n" +
            "GpoHiSENae9e0AAAWgEaos8m239pnUDpWU3N9VtUvg3XVh9WC1YwL9wg1rnl+uW3ygA4Xq\n" +
            "VvGUEc5Xx5AR3buKaYGI7+Tb4RAwQL8HkQS78mDtmSiNKJbxmUWkLIWERBe/OZGO/HYPSl\n" +
            "WS3nkXogcYy5Q/9Fy4U35Trg82yq/kaSjIneJAGLz0ShbQNgWBtnpzK8eHqceoMFYQsvZ+\n" +
            "eaK3JWTwQPgXinj2E37OU5N0y5ncZ8yQ5bKEbOBZ62uYdZFnIgQhz9oNVS8ShIVZtBC0h4\n" +
            "ytl45Tdsd4H8cy2RMzzvvLtsfnvA6EOzj5exSNrtsbjZMFvK7f1oatKkm71IknvieGr0nh\n" +
            "qvmR+qc15wwnmmFus9MFpqxsOKdPzkeSvBjhe9Oj5Qc9g9ecNHuSuS7MTRcx6UFmB9tvo+\n" +
            "iLW0uEzIguQSyaAo1VBNgbr+wz11TaB+rhi2krdUc59skS6/mrah7gJr0kGAJowLR+YGjN\n" +
            "/UTJpaEhMWkktuAznY56qs7AlHqKzcNq+258LpIOQJzN9/gw9IB2rz0PNnA+NqDCHttQLw\n" +
            "0dZe/oPHJQ6vI/5ykakSas5GJZOph5udSz05ndM5kRoMOGHhi8WeYA0vFBed3BH+lkZ59K\n" +
            "z+vjf4sGmOb0ptW95QA9ZeMN899QvuCYOgnuyCPguVL3SsRkQ9AXmOrLT4oPTSUOY3t7vv\n" +
            "GI5WN5ZN9zYtT21bOMqYi+cHlIhnaqz+GjRpEfGaqJFPLcj1tVznHbi+2HHCG0M+NTjw9G\n" +
            "JRjAjqOfkJZ0/7KmfBT7lGWNPPNgXtYPDdYRHHiIeDMLu4s2gBbqn8pmIdG14K4IqLl7uC\n" +
            "payMNJxmQ75oRFpv3Vtf31FlpnsT762iS0e7P0CwBxVZyjdCYet9IVjw6MJC62svnTDznn\n" +
            "0ZxPdz78acoXlBkH67zDH69LyPGZlZ9e7HeKrMbOTU5mnUfSiHc3mk8PYEuphnKXFd8Zzi\n" +
            "bc/SfaxLbf19MsuqlM+gqKR9hVqDn6Ri9JAmHJBgFNc5hdLSKucunNFFamCslCXRkB3TNl\n" +
            "pbPxSLMJ9UDTcrRnzgi5zyQxSe3K8tspqhXQ6ek5Z2sZ+zZuFzcKzgUcd8fpYxC9lZvJ1b\n" +
            "pS8OCuGUI6KHHmGJmNKBTbxvp0B4EjRIy3lDJDBMap/GN9GsgqscrvYPIfqlnVR7GXN+qj\n" +
            "MgOsue1jtVzG1SBAmBxcctEFLzBsr4k/fNNTXPt/mPKeO3w59zt1OSPyNx63NbNmo/uWO1\n" +
            "8P24MBcO5crhlYa5ptb6Fvi1/j6Yrg1NYDPutRopcZNemEFPkR4dqW5AhJwT8L8hqZmmhs\n" +
            "DH97qNiqkqyVmrRIygnVMdYqXsn/uV8yEb5mgRw8C6fJ7OZsvwsSfy052tBKJhj/63Ay/S\n" +
            "wJ+HxQ/8vthvEkXsaJWiQ2RwatZIoVpOhYEpKwSDuBHMKrnMiCow13+pAq9Gf/CbXUd/Ko\n" +
            "xNQ8RZ8lkreUDjJJhTXRRcpufJChL6zQj9bat6E9QBq4l1XjGDhAqgfvQT/1fDataZW3vW\n" +
            "skze0s7diqtYIWNlx2+4vGxL38pSCSqtOWjHS6Rbjf37ERKQMH57z4w3aEiahtBcgKTWBy\n" +
            "n4UD18TfLGd2i7jtENLxOcWBFzRxtIbFnKGiktLcp0XILs/lOhtRF+K2abiif26rDx++jI\n" +
            "4iQCet6ltdeQJLekjmNh4/9Y4hCf5yx9lKuGbzGeZPI66ClbY+R2l29ZXUNUxZmVKM4BDw\n" +
            "2LqMlVLcM1Nzg6ftQ09Dku1ApX/uKeOaf0I0DPaBwVD+iTGCeZWuOg5b1LZUuxxYT4ZB6F\n" +
            "hoZ8/1mt5gTzo4XdZCmJ7jCOqEc75JV2NEfcIwpy6TOZPVMMWFYT88OgkF86Vxx8GR0FQU\n" +
            "CLSDGVZjFU7kv1eKpDJ0oETyGBELC1PPMpm90nxCkzCx7uQw\n" +
            "-----END OPENSSH PRIVATE KEY-----\n";

    private String publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCxhKaGaUlUZnlr6OtFQ7hjVceZsWLBaIWB5NUwp45IoWLm7Hnor+Y8J/SwLBgdUSsjxkMUQMMbJdY/rPGwc8aW0K1JMjSNhv03dBxvXHdY+NSd24WjSezD89l6v78lGhVQ5g3rI4eTFsfPy2WSxZw1Fo0UUDVzzBtLuvC9ZCWd3nsT8Ox4LnZWLHrrRxGX2eCotEEO6CT1+wmk4szIkeDDmX79TbKatcN2vv7H6WjsoGH1bhc1QwS2/hmOdBwqGfm+sE0BI3VgMJ1NVDQrnt0IXlMtLH9Feg1ytdagCzaHulQ9lHn1wBzSARP/NqYzu2vNwpWSSJeafClHpA8yF/9FW9gOi4k+Oo949b+Xd1NHqjt+7lnlsepm0IsgfJ9Gr/0sweYfUSsTfGZGMstSRMu8V+bD1BaVqXQKZ80XoCm0NMnRChm109wXtt5+0atDmIFiy1Byr8QjwjqsIap1j93R/8R3L3mhUmLruSl7IPKPhjShEIL253GpoHiSENae9e0= dwannipu@Dimuthus-MacBook-Pro.local";
    private String passphrase = "airavata";

    @BeforeEach
    void setUp() throws Exception {
        sftpRootDir = Files.createTempDirectory("sftp-root-");
        sftpRootDir.toFile().deleteOnExit();

        Path authorizedKeysFile = Files.createTempFile("authorized_keys-", "");
        Files.write(authorizedKeysFile, publicKey.getBytes(StandardCharsets.UTF_8));
        authorizedKeysFile.toFile().deleteOnExit();

        sshd = SshServer.setUpDefaultServer();
        sshd.setHost("localhost");
        sshd.setPort(sftpPort);

        // Host key (for the server itself, unrelated to client auth)
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        // SFTP subsystem
        sshd.setSubsystemFactories(
                Collections.singletonList(new SftpSubsystemFactory.Builder().build())
        );

        // Virtual root
        sshd.setFileSystemFactory(new VirtualFileSystemFactory(sftpRootDir));

        // *** AUTH CONFIG ***

        // 1) Disable password auth (only key-based logins allowed)
        sshd.setPasswordAuthenticator(null);

        sshd.setPublickeyAuthenticator(new AuthorizedKeysAuthenticator(authorizedKeysFile));

        sshd.start();
        port = sshd.getPort();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (sshd != null && !sshd.isClosed()) {
            sshd.stop(true);
        }
    }

    public void createFilesInDir(Path root) throws IOException {
        Path dir1 = Files.createDirectory(root.resolve("dir1"));
        dir1.toFile().deleteOnExit();
        Path dir2 = Files.createDirectory(root.resolve("dir2"));
        dir2.toFile().deleteOnExit();

        Path file1 = Files.createFile(root.resolve("file1.txt"));
        file1.toFile().deleteOnExit();
        Path file2 = Files.createFile(root.resolve("file2.txt"));
        file2.toFile().deleteOnExit();

        Path file3 = Files.createFile(dir1.resolve("file3.txt"));
        file3.toFile().deleteOnExit();
        Path file4 = Files.createFile(dir1.resolve("file4.txt"));
        file4.toFile().deleteOnExit();

        Files.writeString(file1, "Hello from file1\n");
        Files.writeString(file2, "Hello from file2\n");
        Files.writeString(file3, "Hello from file3\n");
        Files.writeString(file4, "Hello from file4\n");
    }

    @Test
    public void deleteNonEmptyDir() throws Exception {
        System.out.printf("Root dir: %s\n", sftpRootDir);

        Path dir1 = Files.createDirectory(sftpRootDir.resolve("dir1"));
        dir1.toFile().deleteOnExit();
        Path dir2 = Files.createDirectory(sftpRootDir.resolve("dir2"));
        dir2.toFile().deleteOnExit();

        createFilesInDir(dir1);
        createFilesInDir(dir2);

        SSHJAgentAdaptor adaptor = new SSHJAgentAdaptor();
        adaptor.init("testuser", "localhost", sftpPort, publicKey, privateKey, passphrase);

        List<String> itemsBefore = adaptor.listDirectory("/");
        adaptor.deleteDirectory("dir1");
        List<String> itemsAfter = adaptor.listDirectory("/");
        System.out.printf("Before: %s\n", itemsBefore);
        System.out.printf("After: %s\n", itemsAfter);

    }

    @Test
    public void deleteEmptyDir() throws Exception {
        Path dir1 = Files.createDirectory(sftpRootDir.resolve("dir1"));
        dir1.toFile().deleteOnExit();

        SSHJAgentAdaptor adaptor = new SSHJAgentAdaptor();
        adaptor.init("testuser", "localhost", sftpPort, publicKey, privateKey, passphrase);
        List<String> itemsBefore = adaptor.listDirectory("/");
        adaptor.deleteDirectory("dir1");
        List<String> itemsAfter = adaptor.listDirectory("/");

        Assertions.assertTrue(itemsBefore.get(0).equals("dir1"));
        Assertions.assertTrue(itemsAfter.isEmpty());
    }
}
