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
package org.apache.airavata.gfac.deployment.classloaders;


import org.apache.airavata.gfac.deployment.ResourceFinder;
import org.apache.axis2.classloader.DirectoryResourceLocation;
import org.apache.axis2.classloader.JarResourceLocation;
import org.apache.axis2.classloader.ResourceEnumeration;
import org.apache.axis2.classloader.ResourceLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @version $Rev: 704201 $ $Date: 2008-10-13 14:52:25 -0400 (Mon, 13 Oct 2008) $
 */
public class UrlResourceFinder implements ResourceFinder {
    private final Object lock = new Object();
    private final LinkedHashSet<URL> urls = new LinkedHashSet<URL>();
    private final LinkedHashMap<URL, ResourceLocation> classPath = new LinkedHashMap<URL, ResourceLocation>();
    private final LinkedHashSet<File> watchedFiles = new LinkedHashSet<File>();

    private boolean destroyed = false;

    public UrlResourceFinder() {
    }

    public UrlResourceFinder(URL[] urls) {
        addUrls(urls);
    }

    public void destroy() {
        synchronized (lock) {
            if (destroyed) {
                return;
            }
            destroyed = true;
            urls.clear();
            for (ResourceLocation resourceLocation : classPath.values()) {
                resourceLocation.close();
            }
            classPath.clear();
        }
    }

    public ResourceHandle getResource(String resourceName) {
        synchronized (lock) {
            if (destroyed) {
                return null;
            }
            for (Map.Entry<URL, ResourceLocation> entry : getClassPath().entrySet()) {
                ResourceLocation resourceLocation = entry.getValue();
                ResourceHandle resourceHandle = (ResourceHandle) resourceLocation.getResourceHandle(resourceName);
                if (resourceHandle != null && !resourceHandle.isDirectory()) {
                    return resourceHandle;
                }
            }
        }
        return null;
    }

    public URL findResource(String resourceName) {
        synchronized (lock) {
            if (destroyed) {
                return null;
            }
            for (Map.Entry<URL, ResourceLocation> entry : getClassPath().entrySet()) {
                ResourceLocation resourceLocation = entry.getValue();
                ResourceHandle resourceHandle = (ResourceHandle) resourceLocation.getResourceHandle(resourceName);
                if (resourceHandle != null) {
                    return resourceHandle.getUrl();
                }
            }
        }
        return null;
    }

    public Enumeration findResources(String resourceName) {
        synchronized (lock) {
            return new ResourceEnumeration(new ArrayList<ResourceLocation>(getClassPath().values()), resourceName);
        }
    }

    public void addUrl(URL url) {
        addUrls(Collections.singletonList(url));
    }

    public URL[] getUrls() {
        synchronized (lock) {
            return urls.toArray(new URL[urls.size()]);
        }
    }

    /**
     * Adds an array of urls to the end of this class loader.
     *
     * @param urls the URLs to add
     */
    protected void addUrls(URL[] urls) {
        addUrls(Arrays.asList(urls));
    }

    /**
     * Adds a list of urls to the end of this class loader.
     *
     * @param urls the URLs to add
     */
    protected void addUrls(List<URL> urls) {
        synchronized (lock) {
            if (destroyed) {
                throw new IllegalStateException("UrlResourceFinder has been destroyed");
            }

            boolean shouldRebuild = this.urls.addAll(urls);
            if (shouldRebuild) {
                rebuildClassPath();
            }
        }
    }

    private LinkedHashMap<URL, ResourceLocation> getClassPath() {
        assert Thread.holdsLock(lock) : "This method can only be called while holding the lock";

        for (File file : watchedFiles) {
            if (file.canRead()) {
                rebuildClassPath();
                break;
            }
        }

        return classPath;
    }

    /**
     * Rebuilds the entire class path.  This class is called when new URLs are added or one of the watched files
     * becomes readable.  This method will not open jar files again, but will add any new entries not alredy open
     * to the class path.  If any file based url is does not exist, we will watch for that file to appear.
     */
    private void rebuildClassPath() {
        assert Thread.holdsLock(lock) : "This method can only be called while holding the lock";

        // copy all of the existing locations into a temp map and clear the class path
        Map<URL, ResourceLocation> existingJarFiles = new LinkedHashMap<URL, ResourceLocation>(classPath);
        classPath.clear();

        LinkedList<URL> locationStack = new LinkedList<URL>(urls);
        try {
            while (!locationStack.isEmpty()) {
                URL url = locationStack.removeFirst();

                // Skip any duplicate urls in the classpath
                if (classPath.containsKey(url)) {
                    continue;
                }

                // Check is this URL has already been opened
                ResourceLocation resourceLocation = existingJarFiles.remove(url);

                // If not opened, cache the url and wrap it with a resource location
                if (resourceLocation == null) {
                    try {
                        File file = cacheUrl(url);
                        resourceLocation = createResourceLocation(url, file);
                    } catch (FileNotFoundException e) {
                        // if this is a file URL, the file doesn't exist yet... watch to see if it appears later
                        if ("file".equals(url.getProtocol())) {
                            File file = new File(url.getPath());
                            watchedFiles.add(file);
                            continue;

                        }
                    } catch (IOException ignored) {
                        // can't seem to open the file... this is most likely a bad jar file
                        // so don't keep a watch out for it because that would require lots of checking
                        // Dain: We may want to review this decision later
                        continue;
                    } catch (UnsupportedOperationException ex) {
                        // the protocol for the JAR file's URL is not supported.  This can occur when
                        // the jar file is embedded in an EAR or CAR file.  Proceed but log the message.
                        System.out.println("The protocol for the JAR file's URL is not supported" + ex);
                        continue;
                    }
                }

                // add the jar to our class path
                classPath.put(resourceLocation.getCodeSource(), resourceLocation);

                // push the manifest classpath on the stack (make sure to maintain the order)
                List<URL> manifestClassPath = getManifestClassPath(resourceLocation);
                locationStack.addAll(0, manifestClassPath);
            }
        } catch (Error e) {
            destroy();
            throw e;
        }

        for (ResourceLocation resourceLocation : existingJarFiles.values()) {
            resourceLocation.close();
        }
    }

    protected File cacheUrl(URL url) throws IOException {
        if (!"file".equals(url.getProtocol())) {
            // download the jar
            throw new UnsupportedOperationException("Only local file jars are supported " + url);
        }

        File file = new File(url.getPath());
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("File is not readable: " + file.getAbsolutePath());
        }
        return file;
    }

    protected ResourceLocation createResourceLocation(URL codeSource, File cacheFile) throws IOException {
        if (!cacheFile.exists()) {
            throw new FileNotFoundException(cacheFile.getAbsolutePath());
        }
        if (!cacheFile.canRead()) {
            throw new IOException("File is not readable: " + cacheFile.getAbsolutePath());
        }

        ResourceLocation resourceLocation;
        if (cacheFile.isDirectory()) {
            // DirectoryResourceLocation will only return "file" URLs within this directory
            // do not user the DirectoryResourceLocation for non file based urls
            resourceLocation = new DirectoryResourceLocation(cacheFile);
        } else {
            resourceLocation = new JarResourceLocation(codeSource, cacheFile);
        }
        return resourceLocation;
    }

    private List<URL> getManifestClassPath(ResourceLocation resourceLocation) {
        try {
            // get the manifest, if possible
            Manifest manifest = resourceLocation.getManifest();
            if (manifest == null) {
                // some locations don't have a manifest
                return Collections.EMPTY_LIST;
            }

            // get the class-path attribute, if possible
            String manifestClassPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if (manifestClassPath == null) {
                return Collections.EMPTY_LIST;
            }

            // build the urls...
            // the class-path attribute is space delimited
            URL codeSource = resourceLocation.getCodeSource();
            LinkedList<URL> classPathUrls = new LinkedList<URL>();
            for (StringTokenizer tokenizer = new StringTokenizer(manifestClassPath, " "); tokenizer.hasMoreTokens(); ) {
                String entry = tokenizer.nextToken();
                try {
                    // the class path entry is relative to the resource location code source
                    URL entryUrl = new URL(codeSource, entry);
                    classPathUrls.addLast(entryUrl);
                } catch (MalformedURLException ignored) {
                    // most likely a poorly named entry
                }
            }
            return classPathUrls;
        } catch (IOException ignored) {
            // error opening the manifest
            return Collections.EMPTY_LIST;
        }
    }
}

