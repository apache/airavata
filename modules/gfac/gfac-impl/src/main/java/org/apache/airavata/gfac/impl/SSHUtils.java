/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.gfac.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.SSHApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to do all ssh and scp related things.
 */
public class SSHUtils {
	private static final Logger log = LoggerFactory.getLogger(SSHUtils.class);


	/**
	 * This will copy a local file to a remote location
	 *
	 * @param remoteFile remote location you want to transfer the file, this cannot be a directory, if user pass
	 *                   a dirctory we do copy it to that directory but we simply return the directory name
	 *                   todo handle the directory name as input and return the proper final output file name
	 * @param localFile  Local file to transfer, this can be a directory
	 * @return returns the final remote file path, so that users can use the new file location
	 */
	public static String scpTo(String localFile, String remoteFile, Session session) throws IOException,
			JSchException, SSHApiException {
		FileInputStream fis = null;
		String prefix = null;
		if (new File(localFile).isDirectory()) {
			prefix = localFile + File.separator;
		}
		boolean ptimestamp = true;

		// exec 'scp -t rfile' remotely
		String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remoteFile;
		Channel channel = session.openChannel("exec");

		StandardOutReader stdOutReader = new StandardOutReader();
		((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
		((ChannelExec) channel).setCommand(command);

		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();

		if (checkAck(in) != 0) {
			String error = "Error Reading input Stream";
			log.error(error);
			throw new SSHApiException(error);
		}

		File _lfile = new File(localFile);

		if (ptimestamp) {
			command = "T" + (_lfile.lastModified() / 1000) + " 0";
			// The access time should be sent here,
			// but it is not accessible with JavaAPI ;-<
			command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				String error = "Error Reading input Stream";
				log.error(error);
				throw new SSHApiException(error);
			}
		}

		// send "C0644 filesize filename", where filename should not include '/'
		long filesize = _lfile.length();
		command = "C0644 " + filesize + " ";
		if (localFile.lastIndexOf('/') > 0) {
			command += localFile.substring(localFile.lastIndexOf('/') + 1);
		} else {
			command += localFile;
		}
		command += "\n";
		out.write(command.getBytes());
		out.flush();
		if (checkAck(in) != 0) {
			String error = "Error Reading input Stream";
			log.error(error);
			throw new SSHApiException(error);
		}

		// send a content of localFile
		fis = new FileInputStream(localFile);
		byte[] buf = new byte[1024];
		while (true) {
			int len = fis.read(buf, 0, buf.length);
			if (len <= 0) break;
			out.write(buf, 0, len); //out.flush();
		}
		fis.close();
		fis = null;
		// send '\0'
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();
		if (checkAck(in) != 0) {
			String error = "Error Reading input Stream";
			log.error(error);
			throw new SSHApiException(error);
		}
		out.close();
		stdOutReader.onOutput(channel);


		channel.disconnect();
		if (stdOutReader.getStdErrorString().contains("scp:")) {
			throw new SSHApiException(stdOutReader.getStdErrorString());
		}
		//since remote file is always a file  we just return the file
		return remoteFile;
	}

	/**
	 * This method will copy a remote file to a local directory
	 *
	 * @param remoteFile remote file path, this has to be a full qualified path
	 * @param localFile  This is the local file to copy, this can be a directory too
	 * @return returns the final local file path of the new file came from the remote resource
	 */
	public static void scpFrom(String remoteFile, String localFile, Session session) throws IOException,
			JSchException, SSHApiException {
		FileOutputStream fos = null;
		try {
			String prefix = null;
			if (new File(localFile).isDirectory()) {
				prefix = localFile + File.separator;
			}

			// exec 'scp -f remotefile' remotely
			String command = "scp -f " + remoteFile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			StandardOutReader stdOutReader = new StandardOutReader();
			((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

            if (!channel.isClosed()){
                channel.connect();
            }

			byte[] buf = new byte[1024];

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			while (true) {
				int c = checkAck(in);
				if (c != 'C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize = 0L;
				while (true) {
					if (in.read(buf, 0, 1) < 0) {
						// error
						break;
					}
					if (buf[0] == ' ') break;
					filesize = filesize * 10L + (long) (buf[0] - '0');
				}

				String file = null;
				for (int i = 0; ; i++) {
					in.read(buf, i, 1);
					if (buf[i] == (byte) 0x0a) {
						file = new String(buf, 0, i);
						break;
					}
				}

				//System.out.println("filesize="+filesize+", file="+file);

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();

				// read a content of lfile
				fos = new FileOutputStream(prefix == null ? localFile : prefix + file);
				int foo;
				while (true) {
					if (buf.length < filesize) foo = buf.length;
					else foo = (int) filesize;
					foo = in.read(buf, 0, foo);
					if (foo < 0) {
						// error
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					if (filesize == 0L) break;
				}
				fos.close();
				fos = null;

				if (checkAck(in) != 0) {
					String error = "Error transfering the file content";
					log.error(error);
					throw new SSHApiException(error);
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
			}
			stdOutReader.onOutput(channel);
			if (stdOutReader.getStdErrorString().contains("scp:")) {
				throw new SSHApiException(stdOutReader.getStdErrorString());
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (fos != null) fos.close();
			} catch (Exception ee) {
			}
		}
	}

    /**
     * This method will copy a remote file to a local directory
     *
     * @param sourceFile remote file path, this has to be a full qualified path
     * @param sourceSession JSch session for source
     * @param destinationFile This is the local file to copy, this can be a directory too
     * @param destinationSession JSch Session for target
     * @return returns the final local file path of the new file came from the remote resource
     */
    public static void scpThirdParty(String sourceFile, Session sourceSession, String destinationFile, Session destinationSession, boolean ignoreEmptyFile) throws
            IOException, JSchException {
        OutputStream sout = null;
        InputStream sin = null;
        OutputStream dout = null;
        InputStream din = null;
        try {
            String prefix = null;

            // exec 'scp -f sourceFile'
            String sourceCommand = "scp -f " + sourceFile;
            Channel sourceChannel = sourceSession.openChannel("exec");
            ((ChannelExec) sourceChannel).setCommand(sourceCommand);
            StandardOutReader sourceStdOutReader = new StandardOutReader();
            ((ChannelExec) sourceChannel).setErrStream(sourceStdOutReader.getStandardError());
            // get I/O streams for remote scp
            sout = sourceChannel.getOutputStream();
            sin = sourceChannel.getInputStream();
            sourceChannel.connect();

            boolean ptimestamp = true;
            // exec 'scp -t destinationFile'
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + destinationFile;
            Channel targetChannel = destinationSession.openChannel("exec");
            StandardOutReader targetStdOutReader = new StandardOutReader();
            ((ChannelExec) targetChannel).setErrStream(targetStdOutReader.getStandardError());
            ((ChannelExec) targetChannel).setCommand(command);
            // get I/O streams for remote scp
            dout = targetChannel.getOutputStream();
            din = targetChannel.getInputStream();
            targetChannel.connect();

            if (checkAck(din) != 0) {
                String error = "Error Reading input Stream";
                log.error(error);
                throw new Exception(error);
            }


            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            sout.write(buf, 0, 1);
            sout.flush();

			log.info("Initiating transfer from:" + sourceFile + " To: " + destinationFile + ", Ignore Empty file : " + ignoreEmptyFile);

            while (true) {
                int c = checkAck(sin);
                if (c != 'C') {
                	log.info("checkAck for Input Stream file: " + sourceFile + " is not valid. Hence skipping reading the file.");
                    break;
                }

                // read '0644 '
                sin.read(buf, 0, 5);

                long fileSize = 0L;
                while (true) {
                    if (sin.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') break;
                    fileSize = fileSize * 10L + (long) (buf[0] - '0');
                }

                String fileName = null;
                for (int i = 0; ; i++) {
                    sin.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        fileName = new String(buf, 0, i);
                        break;
                    }
                }

				//FIXME: Remove me after fixing file transfer issue
				if(fileSize == 0L){
					log.warn("*****Zero byte file*****. Transferring from:" + sourceFile + " To: " + destinationFile + ", File Size : " + fileSize + ", Ignore Empty file : " + ignoreEmptyFile);
				}else{
					log.info("Transferring from:" + sourceFile + " To: " + destinationFile + ", File Size : " + fileSize + ", Ignore Empty file : " + ignoreEmptyFile);
				}

                if (fileSize == 0L && !ignoreEmptyFile){
                    String error = "Input file is empty...";
                    log.error(error);
                    throw new JSchException(error);
                }
                String initData = "C0644 " + fileSize + " " + fileName + "\n";
                assert dout != null;
                dout.write(initData.getBytes());
                dout.flush();

                // send '\0' to source
                buf[0] = 0;
                sout.write(buf, 0, 1);
                sout.flush();

                int rLength;
                while (true) {
                    if (buf.length < fileSize) rLength = buf.length;
                    else rLength = (int) fileSize;
                    rLength = sin.read(buf, 0, rLength); // read content of the source File
                    if (rLength < 0) {
                        // error
                        break;
                    }
                    dout.write(buf, 0, rLength); // write to destination file
                    fileSize -= rLength;
                    if (fileSize == 0L) break;
                }

                // send '\0' to target
                buf[0] = 0;
                dout.write(buf, 0, 1);
                dout.flush();
                if (checkAck(din) != 0) {
                    String error = "Error Reading input Stream";
                    log.error(error);
                    throw new Exception(error);
                }
                dout.close();
                dout = null;

                if (checkAck(sin) != 0) {
                    String error = "Error transfering the file content";
                    log.error(error);
                    throw new Exception(error);
                }

                // send '\0'
                buf[0] = 0;
                sout.write(buf, 0, 1);
                sout.flush();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JSchException(e.getMessage());
        } finally {
            try {
                if (dout != null) dout.close();
            } catch (Exception ee) {
                log.error("", ee);
            }
            try {
                if (din != null) din.close();
            } catch (Exception ee) {
                log.error("", ee);
            }
            try {
                if (sout != null) sout.close();
            } catch (Exception ee) {
                log.error("", ee);
            }
            try {
                if (din != null) din.close();
            } catch (Exception ee) {
                log.error("", ee);
            }
        }
    }

	public static void makeDirectory(String path, Session session) throws IOException, JSchException, GFacException {

		// exec 'scp -t rfile' remotely
		String command = "mkdir -p " + path;
		Channel channel = session.openChannel("exec");
		StandardOutReader stdOutReader = new StandardOutReader();

		((ChannelExec) channel).setCommand(command);


		((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
		try {
			channel.connect();
		} catch (JSchException e) {

			channel.disconnect();
//            session.disconnect();
			log.error("Unable to retrieve command output. Command - " + command +
					" on server - " + session.getHost() + ":" + session.getPort() +
					" connecting user name - "
					+ session.getUserName());
			throw e;
		}
		stdOutReader.onOutput(channel);
		if (stdOutReader.getStdErrorString().contains("mkdir:")) {
			throw new GFacException(stdOutReader.getStdErrorString());
		}

		channel.disconnect();
	}

	public static List<String> listDirectory(String path, Session session) throws IOException, JSchException,
			GFacException {

		// exec 'scp -t rfile' remotely
		String command = "ls " + path;
		Channel channel = session.openChannel("exec");
		StandardOutReader stdOutReader = new StandardOutReader();

		((ChannelExec) channel).setCommand(command);


		((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
		try {
			channel.connect();
		} catch (JSchException e) {

			channel.disconnect();
//            session.disconnect();

			throw new GFacException("Unable to retrieve command output. Command - " + command +
					" on server - " + session.getHost() + ":" + session.getPort() +
					" connecting user name - "
					+ session.getUserName(), e);
		}
		stdOutReader.onOutput(channel);
		stdOutReader.getStdOutputString();
		if (stdOutReader.getStdErrorString().contains("ls:")) {
			throw new GFacException(stdOutReader.getStdErrorString());
		}
		channel.disconnect();
		return Arrays.asList(stdOutReader.getStdOutputString().split("\n"));
	}


	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		if (b == 0) return b;
		if (b == -1) return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			}
			while (c != '\n');
			//FIXME: Redundant
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
			log.warn(sb.toString());
		}
		return b;
	}

}
