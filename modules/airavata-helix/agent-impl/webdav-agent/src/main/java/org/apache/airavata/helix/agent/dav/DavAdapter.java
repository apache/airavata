package org.apache.airavata.helix.agent.dav;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.AgentUtils;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.helix.agent.dav.Exception.NextcloudApiException;
import org.apache.airavata.helix.agent.ssh.StandardOutReader;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.thrift.TException;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.List;

public class DavAdapter implements StorageResourceAdaptor {
    DavConfig configInstance;
    private Sardine sardine;
    String basepath = "remote.php/dav/files";

    public void init(String storageResourceId, String gatewayId, String userName, String token) throws AgentException {

        try {
            //token = "nextcloudadmin";
            StorageResourceDescription storageResource = AgentUtils.getRegistryServiceClient().getStorageResource(storageResourceId);
            String host = storageResource.getHostName();

            DataMovementInterface inty = storageResource.getDataMovementInterfaces().get(0);
            inty.getDataMovementProtocol();
            int port = 80;
            configInstance = new DavConfig(host, false, port, "admin", token, basepath);
        } catch (TException e) {
            e.printStackTrace();
        }

        sardine = SardineFactory.begin();
        sardine.setCredentials(userName, token);
        sardine.enablePreemptiveAuthentication(configInstance.getServerName());
    }

    public void createFolder(String remotepath) {
        String rootremotepath = (configInstance.isUseHTTPS() ? "https" : "http") + "://" + configInstance.getServerName() + "/" + configInstance.getBasepath() + "/" + configInstance.getUserName();;
        String path = rootremotepath+remotepath;
        try {
                sardine.createDirectory(path);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
    }

    public boolean Exists(String rootpath){
        String path = (configInstance.isUseHTTPS() ? "https" : "http") + "://" + configInstance.getServerName() + "/" + configInstance.getBasepath() + "/" + configInstance.getUserName() + rootpath;
        try {
            if(sardine.exists(path)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void uploadFile(String sourceFile, String destFile) {
        String rootRemotepath = (configInstance.isUseHTTPS() ? "https" : "http") + "://" + configInstance.getServerName() + "/" + configInstance.getBasepath() + "/"+ configInstance.getUserName();
        String path = rootRemotepath + destFile;
        try {
            InputStream inputStream = new FileInputStream(sourceFile);
            String[] segments = destFile.split("/");
            String appendpath="";
            for(int i = 1; i < segments.length-1 ; i++)
            {
                appendpath = appendpath + "/" + segments[i];
                if(!Exists(appendpath)) {
                    createFolder(appendpath);
                }
            }
            sardine.put(path, inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
    }

    public void downloadFile(String sourceFile, String destFile) {
        String rootremotepath = (configInstance.isUseHTTPS() ? "https" : "http") + "://" + configInstance.getServerName() + "/" + configInstance.getBasepath() + "/" + configInstance.getUserName();
        String path = rootremotepath + destFile;
        File downloadFilepath = new File(sourceFile);
        if(!downloadFilepath.getParentFile().exists()) {
            downloadFilepath.getParentFile().mkdirs();
        }
        try {
            InputStream in = sardine.get(path);
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            File targetFile = new File(sourceFile);
            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);
            outStream.close();
            in.close();
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
    }

    public void downloadFolder(String remotepath, String rootDownloadirpath) throws IOException {
        int depth=1;
        String rootremotepath = (configInstance.isUseHTTPS() ? "https" : "http") + "://" + configInstance.getServerName() + "/" + configInstance.getBasepath() + "/" + configInstance.getUserName();
        String newDownloadir = rootDownloadirpath;

        File localDir = new File(newDownloadir);

        if(!localDir.exists()){
            localDir.mkdirs();
        }

        String rootpathnew= rootremotepath + remotepath ;
        int count = 0;
        String filepath;
        List<DavResource> resources;
        try {
            resources = sardine.list(rootpathnew, depth);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }

        for (DavResource res : resources)
        {
            if(count != 0) {
                if(res.getName().equals(".") || res.getName().equals("..")){
                    continue;
                }
                else if(res.isDirectory()) {
                    String subFoldername = res.getName();
                    String downloadDirtosend = newDownloadir + "/" + subFoldername;
                    String pathToSend = remotepath + "/" + subFoldername;
                    downloadFolder(pathToSend,downloadDirtosend);
                }
                else {
                    String filename = res.getName();
                    filepath = rootpathnew + "/" + filename;
                    if (sardine.exists(filepath)) {
                        InputStream in = sardine.get(filepath);
                        byte[] buffer = new byte[in.available()];
                        in.read(buffer);
                        File targetFile = new File(newDownloadir + "/" + filename);
                        OutputStream outStream = new FileOutputStream(targetFile);
                        outStream.write(buffer);
                        in.close();
                        outStream.close();
                    }
                }
            }
            count ++;
        }
    }

    public void uploadFolder(String localpath, String remotepath) {
        String path;
        String remoterootpath = (configInstance.isUseHTTPS() ? "https" : "http") + "://" + configInstance.getServerName() + "/" + configInstance.getBasepath() + "/" + configInstance.getUserName();
        path = remoterootpath + remotepath;
        File localfile = new File(localpath);
        try {
            if (localfile.exists() && localfile.isDirectory()) {
                //Extract the Foldername from the path
                String[] segments = localpath.split("/");
                String foldername = segments[segments.length - 1];
                String folderemotepath = remotepath + "/" + foldername;

                if (!Exists(folderemotepath)) {
                    createFolder(folderemotepath);
                }

                File[] listfil = localfile.listFiles();
                if (listfil != null) {
                    for (File child : listfil) {
                        if(child.isDirectory()) {
                            String childfoldername = child.getName();
                            String newremotepath = folderemotepath;
                            String newlocalpath = localpath + "/" + childfoldername;
                            uploadFolder(newlocalpath, newremotepath);
                        } else {
                            String filename = child.getName();
                            String newpath = path + "/" + foldername + "/" + filename;
                            InputStream input = new FileInputStream(child.getAbsolutePath());
                            sardine.put(newpath, input);
                            input.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
    }

    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        downloadFile("/tmp/"+workingDirectory+"/archive.tar", workingDirectory + "/" + "archive.tar");
        File outDir = new File("/tmp/"+workingDirectory + "/" + "archive" + "/" + "archive.tar");
        if(!outDir.exists()) {
         outDir.getParentFile().mkdirs();
        }
        File out = new File("/tmp/" + workingDirectory + "/" + "archive");
        try {
            decompress("/tmp/"+workingDirectory+"/archive.tar", out);
        } catch (FileNotFoundException e) {
            throw new AgentException(e.getMessage());
        } catch (IOException e) {
            throw new AgentException(e.getMessage());
        }
        StandardOutReader standardOutReader = new StandardOutReader();
        standardOutReader.setExitCode(0);
        return standardOutReader;
    }

    private void decompress(String in, File out) throws IOException {
        TarArchiveInputStream fin = new TarArchiveInputStream(new FileInputStream(in));
        try {
            TarArchiveEntry entry;
            while ((entry = fin.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File curfile = new File(out, entry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                IOUtils.copy(fin, new FileOutputStream(curfile));
            }
            fin.close();
        } catch (IOException ex) {
            throw ex;
        }
    }

    public void shutdown() throws IOException {
        sardine.shutdown();
    }
}
