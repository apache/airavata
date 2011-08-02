/*
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
 *
 */

package org.apache.airavata.xbaya.graph.dynamic.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

public class XmlBeansFiler /* implements Filer */{

    private String path;

    private List<File> javaFilePaths = new LinkedList<File>();

    /**
     * Constructs a XmlBeansFiler.
     * 
     * @param file
     */
    public XmlBeansFiler(String path) {
        this.path = path + "/generated";
    }

    /**
     * @see org.apache.xmlbeans.Filer#createBinaryFile(java.lang.String)
     */
    public OutputStream createBinaryFile(String name) throws IOException {
        name = name.replace('.', File.separatorChar);
        String binPath = this.path + File.separatorChar + name;
        File srcFile = new File(binPath);
        srcFile.getParentFile().mkdirs();
        return new FileOutputStream(new File(binPath));
    }

    /**
     * @see org.apache.xmlbeans.Filer#createSourceFile(java.lang.String)
     */
    public Writer createSourceFile(String name) throws IOException {
        name = name.replace('.', File.separatorChar);
        String srcPath = this.path + File.separatorChar + name + ".java";
        File srcFile = new File(srcPath);
        srcFile.getParentFile().mkdirs();
        javaFilePaths.add(new File(srcFile.getCanonicalPath()));
        return new FileWriter(srcFile);
    }

    // public void compileJavaFiles() {
    //
    // // int status = 0;
    // // Main main = new Main();
    // // System.out.println(System.getProperty("JAVA_HOME"));
    // // for ( int i=0; i<javaFilePaths.size() ; i++ ){
    // // String[] args = new String[]
    // // {"-classpath",System.getProperty("java.class.path",".") , "-d",
    // // this.path, (String) javaFilePaths.elementAt(i)};
    // // status = main.compile(args);
    // // }
    // // if (status != 0) {
    // // throw new XBayaRuntimeException(
    // // "Failed in compiling the xmlBeans generated source files");
    // // }
    // // Project p = new Project();
    // // Path srcPath = new Path(p, this.path);
    // // Javac javac = new Javac();
    // // javac.setTarget("compile");
    // // javac.setSrcdir(srcPath);
    // // javac.setDestdir(new File(this.path));
    // // javac.execute();
    // //
    // // p.executeTarget("compile");
    //
    // // CodeGenUtil.externalCompile((File[])this.javaFilePaths.toArray(), new
    // // File(this.path), false);
    //
    // }
    //
    // public boolean makeJarFile(String jarFile) throws IOException {
    // JarHelper jarHelperObj = new JarHelper();
    // File fileObj = new File(jarFile);
    // fileObj.createNewFile();
    //
    // jarHelperObj.jarDir(new File(this.path), fileObj);
    //
    // return true;
    //
    // }
    //
    // public static void main(String[] args) {
    // String path = "/nfs/mneme/home/users/cherath/generated/";
    // Project p = new Project();
    // Path srcPath = new Path(p, path);
    // Javac javac = new Javac();
    // javac.setSrcdir(srcPath);
    // javac.setDestdir(new File(path));
    // // javac.execute();
    // Target target = new Target();
    // target.setName("compile");
    // target.addTask(javac);
    // p.addTarget(target);
    // // p.executeTarget("compile");
    // Ant ant = new Ant();
    // ant.setProject(p);
    // p.executeTarget("compile");
    // }

}