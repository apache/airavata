package org.apache.airavata.xbaya.jython.runner;

import java.util.List;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.gui.ErrorMessages;

public class JythonRunner {

    private JythonClassLoader loader;

    /**
     * 
     * Constructs a JythonRunner.
     * 
     */
    public JythonRunner() {
        this.loader = new JythonClassLoader(this.getClass().getClassLoader());
    }

    /**
     * @param script
     * @param arguments
     * @throws XBayaException
     */
    public void run(String script, List<String> arguments) throws XBayaException {
        run(script, arguments.toArray(new String[arguments.size()]));
    }

    /**
     * @param script
     * @param arguments
     * @throws XBayaException
     */
    public void run(final String script, final String[] arguments) throws XBayaException {
        try {
            Class<?> runnerClass = this.loader.loadClass(JythonOneTimeRunnerImpl.class.getName(), true);
            JythonOneTimeRunner runner = (JythonOneTimeRunner) runnerClass.newInstance();
            runner.run(script, arguments);

        } catch (ClassNotFoundException e) {
            throw new XBayaRuntimeException(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (InstantiationException e) {
            throw new XBayaRuntimeException(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (IllegalAccessException e) {
            throw new XBayaRuntimeException(ErrorMessages.UNEXPECTED_ERROR, e);
        } finally {
            loader.cleanUp();
        }
    }
}