package org.apache.airavata.integration.utils;

import com.jcraft.jsch.*;
import org.apache.airavata.integration.clients.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class SFTPFileHandler extends Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SFTPFileHandler.class);

    public SFTPFileHandler(String configurationFile) throws IOException {
        super(configurationFile);
    }


    private Session setupJsch(String username, String token) throws JSchException {
        JSch jsch = new JSch();
        jsch.setKnownHosts(getProperties().getProperty(Constants.KNOWN_HOSTS_FILE_PATH));
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        Session jschSession = jsch.getSession(username, getProperties().getProperty(Constants.GATEWAY_HOST),
                Integer.parseInt(getProperties().getProperty(Constants.GATEWAY_HOST_SFTP_PORT)));
        jschSession.setPassword(token);
        jschSession.setConfig(config);
        return jschSession;
    }

    public String uploadFile(String username, String token, String localPath) throws SftpException, JSchException {
        Session session = null;
        ChannelSftp channelSftp = null;
        String path = null;
        try {
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("M_dd_yyyy_hh_mm_ss");
            String strDate = formatter.format(date);
            String experimentDataDir = getProperties().getProperty(Constants.APP_NAME) + "_" + strDate;
            path = File.separator +
                    getProperties().getProperty(Constants.PROJECT_NAME) + File.separator + experimentDataDir;
            session = setupJsch(username, token);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");

            channelSftp.connect();
            createFolder(channelSftp, path);
            channelSftp.put(localPath, path);

            return path;
        } catch (Exception ex) {
            LOGGER.error("Error occurred while uploading file " + localPath + "to " + path + " reason :" + ex.getMessage());
            throw ex;
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }


    public boolean downloadFiles(String username, String token, String localDirPath, String remoteDirPath) throws Exception {
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            session = setupJsch(username, token);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            List<ChannelSftp.LsEntry> list = getFiles(channelSftp, localDirPath, remoteDirPath);
            for (ChannelSftp.LsEntry file : list) {
                if (!file.getFilename().equals(".") && !file.getFilename().equals("..")) {
                    channelSftp.get(file.getFilename(), file.getFilename());
                }
            }
            session.disconnect();
            return true;
        } catch (Exception ex) {
            LOGGER.error("Error occurred while downloading file from " + remoteDirPath + "to " + localDirPath + " reason :" + ex.getMessage());
            throw ex;
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }


    private boolean createFolder(ChannelSftp sftp, String path) throws SftpException {
        String[] folders = path.split("/");
        for (String folder : folders) {
            if (folder.length() > 0) {
                try {
                    sftp.cd(folder);
                } catch (SftpException e) {
                    sftp.mkdir(folder);
                    sftp.cd(folder);
                }
            }
        }
        return true;
    }


    private List<ChannelSftp.LsEntry> getFiles(ChannelSftp sftpChannel, String srcDir,
                                               String destDir) throws Exception {

        sftpChannel.lcd(destDir);

        sftpChannel.cd(srcDir);

        List<ChannelSftp.LsEntry> list = sftpChannel.ls(".");
        return list;
    }

}
