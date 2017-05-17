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
package org.apache.airavata.xbaya.jython.runner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AllPermission;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.SecureClassLoader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.xbaya.XBayaVersion;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class loader loads jython related classes without counting on parent class loader. This is because jython
 * related classes use a lot of static fields and cannot be used to invoke Jython scripts multiple times.
 * 
 */
public class JythonClassLoader extends SecureClassLoader {

    private final static Logger logger = LoggerFactory.getLogger(JythonClassLoader.class);

    private ClassLoader parent;

    private Map<String, Class> classes = new HashMap<String, Class>();

    private URL jythonURL;

    private URL xbayaURL;

    private JarFile jythonJarFile;

    private JarFile xbayaJarFile;

    private File tmpJarDirectory;

    /**
     * Constructs a JythonClassLoader.
     * 
     * @param parent
     *            the parent class loader.
     * 
     *            This has to be explicitly passed because WebStart applications use user-level class loader. The
     *            default system loader cannot load classes in the downloaded jar files.
     */
    public JythonClassLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent;

        this.jythonURL = getBaseURL(PythonInterpreter.class);
        this.xbayaURL = getBaseURL(XBayaVersion.class);
    }

    /**
     * @return XBaya jar file.
     */
    public JarFile getXBayaJarFile() {
        if (this.xbayaJarFile == null)
            this.xbayaJarFile = maybeGetJarFile(this.xbayaURL);
        return this.xbayaJarFile;
    }

    /**
     * Cleans up temporary files.
     */
    public void cleanUp() {
        this.jythonJarFile = null;
        this.xbayaJarFile = null;
        if (this.tmpJarDirectory != null) {
            try {
                IOUtil.deleteDirectory(this.tmpJarDirectory);
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, false));
    }

    /**
     * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     */
    @Override
    public synchronized Class<?> loadClass(String name, boolean resolveIt) throws ClassNotFoundException {

        Class klass = null;
        try {
            klass = findClass(name);
        } catch (ClassNotFoundException e) {
            try {
                klass = super.loadClass(name, false);
            } catch (ClassNotFoundException e2) {
                klass = this.parent.loadClass(name);
                logger.debug("found from parent, klass: " + klass);
            }
        }

        if (resolveIt) {
            resolveClass(klass);
        }

        // logger.exiting(klass);
        return klass;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {

        // find jar location first
        if (this.jythonJarFile == null)
            this.jythonJarFile = maybeGetJarFile(this.jythonURL);
        if (this.jythonJarFile == null)
            this.xbayaJarFile = maybeGetJarFile(this.xbayaURL);

        Class klass;

        // Check if the class has been loaded by this class loader.
        klass = this.classes.get(name);
        if (klass != null) {
            return klass;
        }

        // If the class is python, load separatly. NotificationSender also uses
        // PyObject, so it has to be read from this class loader.
        // JythonOneTimeRunner also needs to be loaded separatly.
        if (name.startsWith("org.python.")) {
            klass = findClassFromURL(name, this.jythonURL, this.jythonJarFile);
        }
//        else if (name.startsWith(NotificationSender.class.getPackage().getName())
//                || name.startsWith(JythonOneTimeRunnerImpl.class.getName())) {
//            klass = findClassFromURL(name, this.xbayaURL, this.xbayaJarFile);
//        }

        if (klass != null) {
            this.classes.put(name, klass);
            return klass;
        } else {
            throw new ClassNotFoundException();
        }
    }

    /**
     * @see java.security.SecureClassLoader#getPermissions(java.security.CodeSource)
     */
    @Override
    protected PermissionCollection getPermissions(CodeSource codesource) {
        // Grant all perission. This could be avoided if code signers were
        // extracted correctly.
        Permissions permissions = new Permissions();
        AllPermission permission = new AllPermission();
        permissions.add(permission);
        return permissions;
    }

    private URL getBaseURL(Class klass) {
        String path = klass.getName().replace('.', '/').concat(".class"); // /d/e/f.class
        URL classURL = this.parent.getResource(path);

        String jarURLString;
        if ("jar".equals(classURL.getProtocol())) {
            // classURL = jar:file/a/b/c.jar!/d/e/f.class
            // or jar:http://example.org/a/b/c.jar!/d/e/f.class
            String file = classURL.getFile();
            // file = file:/a/b/c.jar!d/e/f.class
            // or http://example.org/a/b/c.jar!d/e/f.class
            logger.debug("file: " + file);
            jarURLString = file.substring(0, file.lastIndexOf('!'));
            // jarURLString = file:/a/b/c.jar
            // or http://example.org/a/b/c.jar
        } else {
            // file:/a/b/c/d/e/f.class
            String file = classURL.getFile(); // /a/b/c/d/e/f.class
            int index = file.lastIndexOf(path);
            jarURLString = "file:" + file.substring(0, index); // /a/b/c/
        }
        try {
            URL jarURL = new URL(jarURLString);
            return jarURL;
        } catch (MalformedURLException e) {
            throw new WorkflowRuntimeException(e);
        }
    }

    private JarFile maybeGetJarFile(URL url) {
        String path;
        try {
            path = URLDecoder.decode(url.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new WorkflowRuntimeException(e);
        }
        logger.debug("path: " + path);
        if (path.endsWith("/")) {
            // url = file:/a/b/c/
            // It's a local directory
            return null;
        } else if ("file".equals(url.getProtocol())) {
            // url = file:/a/b/c.jar
            // Jar file
            try {
                JarFile jarFile = new JarFile(path);
                return jarFile;
            } catch (IOException e) {
                throw new WorkflowRuntimeException(e);
            }
        } else {
            // url = http://example.com/a/b/c.jar
            // A Jar file
            try {
                if (this.tmpJarDirectory == null) {
                    Date date = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss-S");
                    String time = format.format(date);
                    String fileName = ".xbaya-jars-" + time;
                    String tmpdir = System.getProperty("java.io.tmpdir");
                    this.tmpJarDirectory = new File(tmpdir, fileName);
                    this.tmpJarDirectory.mkdir();
                }

                int i = path.lastIndexOf('/');
                File file = new File(this.tmpJarDirectory, path.substring(i + 1));
                logger.debug("file: " + file);
                InputStream stream = url.openStream();
                IOUtil.writeToFile(stream, file);
                JarFile jarFile = new JarFile(file);
                return jarFile;
            } catch (IOException e) {
                throw new WorkflowRuntimeException(e);
            }
        }
    }

    private Class findClassFromURL(String name, URL url, JarFile jarFile) throws ClassNotFoundException {
        // logger.entering(new Object[] { name, url, jarFile });

        String classPath = name.replace('.', '/').concat(".class");
        // logger.info("classPath: " + classPath);

        try {
            byte[] classBytes;
            CodeSource codeSource = null;
            if (jarFile == null) {
                // It's a local directory
                String dirPath = URLDecoder.decode(url.getPath(), "UTF-8");
                File classFile = new File(dirPath, classPath);
                classBytes = IOUtil.readToByteArray(classFile);

            } else {
                // A Jar file
                JarEntry jarEntry = jarFile.getJarEntry(classPath);
                CodeSigner[] codeSigners = jarEntry.getCodeSigners();
                // logger.info("codeSigners: " + codeSigners);
                if (codeSigners != null) {
                    // Somehow it's null.
                    for (CodeSigner signer : codeSigners) {
                        logger.debug("signer: " + signer);
                    }
                }
                codeSource = new CodeSource(this.xbayaURL, codeSigners);
                InputStream classInputStream = jarFile.getInputStream(jarEntry);
                classBytes = IOUtil.readToByteArray(classInputStream);
            }

            Class<?> klass = defineClass(name, classBytes, 0, classBytes.length, codeSource);
            this.classes.put(name, klass);
            return klass;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ClassNotFoundException();
        }
    }

    /**
     * @see java.lang.ClassLoader#clearAssertionStatus()
     */
    @Override
    public synchronized void clearAssertionStatus() {
        super.clearAssertionStatus();
    }

    /**
     * @see java.lang.ClassLoader#definePackage(java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, java.net.URL)
     */
    @Override
    protected Package definePackage(String name, String specTitle, String specVersion, String specVendor,
            String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
        return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor,
                sealBase);
    }

    /**
     * @see java.lang.ClassLoader#findLibrary(java.lang.String)
     */
    @Override
    protected String findLibrary(String libname) {
        return super.findLibrary(libname);
    }

    /**
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    @Override
    protected URL findResource(String name) {
        return super.findResource(name);
    }

    /**
     * @see java.lang.ClassLoader#findResources(java.lang.String)
     */
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    /**
     * @see java.lang.ClassLoader#getPackage(java.lang.String)
     */
    @Override
    protected Package getPackage(String name) {
        return super.getPackage(name);
    }

    /**
     * @see java.lang.ClassLoader#getPackages()
     */
    @Override
    protected Package[] getPackages() {
        return super.getPackages();
    }

    /**
     * @see java.lang.ClassLoader#getResource(java.lang.String)
     */
    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    /**
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }

    /**
     * @see java.lang.ClassLoader#getResources(java.lang.String)
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    /**
     * @see java.lang.ClassLoader#setClassAssertionStatus(java.lang.String, boolean)
     */
    @Override
    public synchronized void setClassAssertionStatus(String className, boolean enabled) {
        super.setClassAssertionStatus(className, enabled);
    }

    /**
     * @see java.lang.ClassLoader#setDefaultAssertionStatus(boolean)
     */
    @Override
    public synchronized void setDefaultAssertionStatus(boolean enabled) {
        super.setDefaultAssertionStatus(enabled);
    }

    /**
     * @see java.lang.ClassLoader#setPackageAssertionStatus(java.lang.String, boolean)
     */
    @Override
    public synchronized void setPackageAssertionStatus(String packageName, boolean enabled) {
        super.setPackageAssertionStatus(packageName, enabled);
    }

}