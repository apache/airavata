package org.apache.airavata.core.gfac.provider.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class InputStreamToFileWriter extends Thread {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private BufferedReader in;
    private BufferedWriter out;

    public InputStreamToFileWriter(InputStream in, String out) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = new BufferedWriter(new FileWriter(out));
    }

    public void run() {
        try {
            String line = null;
            while ((line = in.readLine()) != null) {
                if (log.isDebugEnabled()) {
                    log.debug(line);
                }
                out.write(line);
                out.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
