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

package org.apache.airavata.wsmg.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Responsible for loading the configuration parameters specified in a configuration file.
 */

@Deprecated
public class ConfigurationParamsReader implements Configuration {

    static final Logger log = Logger.getLogger(ConfigurationParamsReader.class);
    public final String FILE_SEPARATOR = "/";

    public final String DELIMITER = ",";

    private final int INITIAL_LIST_SIZE = 3;

    private String basePath = null;

    private ArrayList keysList = new ArrayList();

    protected Configuration defaults = null;

    protected Hashtable store = new Hashtable();

    public ConfigurationParamsReader() {
    }

    public ConfigurationParamsReader(String file) {
        try {
            load(file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            log.info(ioe.getMessage());
        }
    }

    public ConfigurationParamsReader(Configuration defaults) {
        this.defaults = defaults;
    }

    /**
     * Load the properties from the given file
     * 
     * @param file
     *            The properties file to load
     * @throws IOException
     */
    public void load(String file) throws IOException {
        load(this.getClass().getClassLoader().getResourceAsStream(file));
    }

    /**
     * Load the properties from the given input stream.
     * 
     * @param input
     *            An InputStream on the properties file
     * @throws IOException
     */
    public void load(InputStream input) throws IOException {
        load(input, null);
    }

    /**
     * Load the properties from the given input stream and using the specified encoding.
     * 
     * @param input
     *            An InputStream.
     * @param enc
     *            An encoding.
     * @exception IOException
     */
    public synchronized void load(InputStream input, String enc) throws IOException {

        LineNumberReader reader = null;

        if (enc != null) {
            try {
                reader = new LineNumberReader(new InputStreamReader(input, enc));
            } catch (UnsupportedEncodingException e) {
                // Get one with the default encoding...
            }
        }
        if (reader == null) {
            reader = new LineNumberReader(new InputStreamReader(input));
        }

        StringBuffer sb = new StringBuffer();
        while (true) {

            String line = reader.readLine();
            if (line == null) {
                break; // EOF
            }

            line = line.trim();
            if (line.startsWith("#")) {
                continue;

            } else if (line.equals("")) {
                continue;

            } else if (line.endsWith("\\")) {
                sb.append(line.substring(0, line.length() - 1));
                continue;

            } else {
                line = sb.append(line).toString();
                sb = new StringBuffer();

                int equalIndex = line.indexOf('=');
                /**
                 * fix commons.configuration bug
                 * 
                 * @see BasePropertiesConfiguration#load we not only make sure there is an equal sign, but also there is
                 *      something after the equal sign
                 */
                if (equalIndex > 0 && (equalIndex + 1) < line.length()) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();

                    addProperty(key, value);

                }
            }
        }
    }

    /**
     * @param The
     *            file name with which to get InputStream
     * @return An InputStream
     * @throws IOException
     */
    public InputStream getPropertyStream(String resourceName) throws IOException {

        InputStream resource = null;
        File file = null;

        if (resourceName.startsWith(FILE_SEPARATOR)) {
            // an absolute path
            file = new File(resourceName);
            log.info("configuration: load " + resourceName);
        } else if (basePath == null || basePath.equals("")) {

            file = new File(resourceName);
            String canonName = file.getCanonicalPath();
            file = new File(canonName);
            log.info("configuration: load " + canonName);
        } else {
            StringBuffer fileName = new StringBuffer();

            if (!basePath.endsWith(FILE_SEPARATOR)) {
                basePath = basePath + FILE_SEPARATOR;
            }
            fileName.append(basePath).append(resourceName);
            file = new File(fileName.toString());
            String canonName = file.getCanonicalPath();
            file = new File(canonName);
            log.info("configuration: load " + canonName);
        }

        if (file == null || !file.exists()) {
            throw new FileNotFoundException("File not exists " + resourceName);

        } else if (!file.canRead()) {
            throw new IOException("File " + resourceName + " exists but could not be read.");

        } else {
            resource = new FileInputStream(file);
        }

        return resource;
    }

    /**
     * @see Configuration
     */
    public void addProperty(String key, Object token) {

        List tokenAdd = null;

        if (token instanceof String) {
            tokenAdd = processString((String) token);

        } else if (token instanceof Collection) {

            for (Iterator it = ((Collection) token).iterator(); it.hasNext();) {
                addProperty(key, it.next());
            }
            return;

        } else {
            tokenAdd = new Vector(1);
            tokenAdd.add(token);
        }

        Object o = store.get(key);
        if (o instanceof Vector) {
            for (Iterator it = tokenAdd.iterator(); it.hasNext();) {
                ((Vector) o).add(it.next());
            }

        } else {
            Vector v = new Vector();
            if (o != null) {
                v.add(o);
            }
            for (Iterator it = tokenAdd.iterator(); it.hasNext();) {
                v.add(it.next());
            }

            if (o == null && v.size() == 1) {
                addPropertyDirect(key, v.get(0));

            } else {
                addPropertyDirect(key, v);
            }
        }
    }

    /**
     * @param token
     *            A String token
     * @return A List of Strings
     */
    protected List processString(String token) {
        List l = new ArrayList(INITIAL_LIST_SIZE);

        if (token.indexOf(DELIMITER) > 0) {
            StringTokenizer tokenizer = new StringTokenizer(token, DELIMITER);

            while (tokenizer.hasMoreTokens()) {
                String value = tokenizer.nextToken();
                /**
                 * fix commons.configuration bug
                 * 
                 * @see PropertiesConfiguration#processString() more trim() call allows both comma "," AND whitespace
                 *      between values
                 */
                l.add(value.trim());
            }
        } else {
            l.add(token);
        }

        return l;
    }

    /**
     * @param key
     *            key to use for mapping
     * @param obj
     *            object to store
     */
    protected void addPropertyDirect(String key, Object obj) {

        if (!store.containsKey(key)) {
            keysList.add(key);
        }

        store.put(key, obj);
    }

    /**
     * Map <code> true </code>, <code> on </code>, <code> yes </code> to true; <code> false </code>, <code> off </code>,
     * <code> no </code> to false.
     * 
     * @param value
     *            The value to test for boolean state.
     * @return String of true, false or null
     */
    public String testBoolean(String value) {

        String val = value.toLowerCase();

        if (val.equals("true") || val.equals("on") || val.equals("yes")) {
            return "true";

        } else if (val.equals("false") || val.equals("off") || val.equals("no")) {
            return "false";

        } else {
            return null;
        }

    }

    /**
     * 
     * @see Configuration
     */
    public Configuration subset(String prefix) {

        Configuration pc = new ConfigurationParamsReader();
        Iterator keys = this.getKeys();
        boolean valid = false;

        while (keys.hasNext()) {
            Object key = keys.next();

            if (key instanceof String && ((String) key).startsWith(prefix)) {
                if (!valid) {
                    valid = true;
                }

                String newKey = null;
                if (((String) key).length() == prefix.length()) {
                    newKey = prefix;

                } else {
                    newKey = ((String) key).substring(prefix.length() + 1);
                }

                Object value = store.get(key);
                ((ConfigurationParamsReader) pc).addPropertyDirect(newKey, value);

            }
        }

        if (valid) {
            return pc;
        } else {
            return null;
        }

    }

    /**
     * @see Configuration#isEmpty
     */
    public boolean isEmpty() {
        return store.isEmpty();
    }

    /**
     * @see Configuration
     */
    public boolean containsKey(String key) {
        return store.containsKey(key);
    }

    /**
     * @see Configuration
     */
    public void setProperty(String key, Object value) {
        clearProperty(key);
        addProperty(key, value);
    }

    /**
     * @see Configuration
     */
    public void clearProperty(String key) {

        if (containsKey(key)) {
            for (int i = 0; i < keysList.size(); i++) {
                if (((String) keysList.get(i)).equals(key)) {
                    keysList.remove(i);
                    break;
                }
            }
            store.remove(key);
        }
    }

    /**
     * @see Configuration
     */
    public Iterator getKeys() {
        return keysList.iterator();
    }

    /**
     * @see Configuration
     */
    public Iterator getKeys(String prefix) {
        Iterator keys = getKeys();
        ArrayList matchingKeys = new ArrayList();

        while (keys.hasNext()) {
            Object key = keys.next();

            if (key instanceof String && ((String) key).startsWith(prefix)) {
                matchingKeys.add(key);
            }
        }
        return matchingKeys.iterator();
    }

    /**
     * Returns all the properties that have been read from the specified configuration file
     */
    public Properties getProperties(Properties _defaults) {
        Properties props = new Properties(_defaults);

        Enumeration e = store.keys();
        while (e.hasMoreElements()) {
            String _key = (String) e.nextElement();
            String _value = store.get(_key).toString();
            props.setProperty(_key, _value);
        }
        return props;

    }

    /**
     * @see Configuration
     * @see #getProperties(String, Properties)
     */
    public Properties getProperties(String key) {
        return getProperties(key, null);
    }

    /**
     * @see Configuration
     */
    public Properties getProperties(String key, Properties defaults) {

        String[] tokens = getStringArray(key);

        /*
         * Each token is of the form 'key=value'.
         */
        Properties props = (defaults == null ? new Properties() : new Properties(defaults));

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            int equalIndex = token.indexOf('=');
            if (equalIndex > 0) {
                String pkey = token.substring(0, equalIndex).trim();
                String pvalue = token.substring(equalIndex + 1).trim();
                props.put(pkey, pvalue);
            } else if (tokens.length == 1 && token.equals("")) {
                // Semantically equivalent to an empty Properties
                // object.
                break;
            } else {
                throw new IllegalArgumentException('\'' + token + "' does not contain an equals sign");
            }
        }
        return props;
    }

    /**
     * @see Configuration
     */
    public String getProperty(String key) {

        Object o = store.get(key);

        if (o == null) {
            if (defaults != null) {
                o = defaults.getProperty(key);
            }
        }
        return o.toString();
    }

    /**
     * @see Configuration
     */
    public boolean getBoolean(String key) {
        Boolean b = getBoolean(key, (Boolean) null);
        if (b != null) {
            return b.booleanValue();

        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * @see Configuration
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key, new Boolean(defaultValue)).booleanValue();
    }

    /**
     * @see Configuration
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        Object value = resolveVectorStore(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String s = testBoolean((String) value);
            Boolean b = new Boolean(s);
            return b;
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getBoolean(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Boolean object");
        }
    }

    /**
     * @see Configuration
     */
    public byte getByte(String key) {
        Byte b = getByte(key, null);
        if (b != null) {
            return b.byteValue();
        } else {
            throw new NoSuchElementException('\'' + key + " doesn't map to an existing object");
        }
    }

    /**
     * @see Configuration
     */
    public byte getByte(String key, byte defaultValue) {
        return getByte(key, new Byte(defaultValue)).byteValue();
    }

    /**
     * @see Configuration
     */
    public Byte getByte(String key, Byte defaultValue) {
        Object value = resolveVectorStore(key);

        if (value instanceof Byte) {
            return (Byte) value;
        } else if (value instanceof String) {
            Byte b = new Byte((String) value);
            return b;
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getByte(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Byte object");
        }
    }

    /**
     * @see Configuration
     */
    public double getDouble(String key) {
        Double d = getDouble(key, null);
        if (d != null) {
            return d.doubleValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * @see Configuration
     */
    public double getDouble(String key, double defaultValue) {
        return getDouble(key, new Double(defaultValue)).doubleValue();
    }

    /**
     * @see Configuration
     */
    public Double getDouble(String key, Double defaultValue) {
        Object value = resolveVectorStore(key);

        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            Double d = new Double((String) value);
            return d;
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getDouble(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Double object");
        }
    }

    /**
     * @see Configuration
     */
    public float getFloat(String key) {
        Float f = getFloat(key, null);
        if (f != null) {
            return f.floatValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * @see Configuration
     */
    public float getFloat(String key, float defaultValue) {
        return getFloat(key, new Float(defaultValue)).floatValue();
    }

    /**
     * @see Configuration
     */
    public Float getFloat(String key, Float defaultValue) {
        Object value = resolveVectorStore(key);

        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof String) {
            Float f = new Float((String) value);
            return f;
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getFloat(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Float object");
        }
    }

    /**
     * @see Configuration
     */
    public int getInt(String key) {
        Integer i = getInteger(key, null);
        if (i != null) {
            return i.intValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * @see Configuration
     */
    public int getInt(String key, int defaultValue) {
        Integer i = getInteger(key, null);

        if (i == null) {
            return defaultValue;
        }

        return i.intValue();
    }

    /**
     * @see Configuration
     */
    public Integer getInteger(String key, Integer defaultValue) {
        Object value = resolveVectorStore(key);

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            Integer i = new Integer((String) value);
            return i;
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getInteger(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Integer object");
        }
    }

    /**
     * @see Configuration
     */
    public long getLong(String key) {
        Long l = getLong(key, null);
        if (l != null) {
            return l.longValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * @see Configuration
     */
    public long getLong(String key, long defaultValue) {
        return getLong(key, new Long(defaultValue)).longValue();
    }

    /**
     * @see Configuration
     */
    public Long getLong(String key, Long defaultValue) {
        Object value = resolveVectorStore(key);

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            Long l = new Long((String) value);
            return l;
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getLong(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Long object");
        }
    }

    /**
     * @see Configuration
     */
    public short getShort(String key) {
        Short s = getShort(key, null);
        if (s != null) {
            return s.shortValue();
        } else {
            throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
        }
    }

    /**
     * @see Configuration
     */
    public short getShort(String key, short defaultValue) {
        return getShort(key, new Short(defaultValue)).shortValue();
    }

    /**
     * @see Configuration
     */
    public Short getShort(String key, Short defaultValue) {
        Object value = resolveVectorStore(key);

        if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof String) {
            Short s = new Short((String) value);
            return s;
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getShort(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Short object");
        }
    }

    /**
     * @see Configuration
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * @see Configuration
     */
    public String getString(String key, String defaultValue) {
        Object value = resolveVectorStore(key);

        if (value instanceof String) {
            return (String) value;

        } else if (value == null) {
            if (defaults != null) {
                return defaults.getString(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a String object");
        }
    }

    /**
     * @see Configuration
     */
    public String[] getStringArray(String key) {
        Object value = store.get(key);

        String[] tokens;

        if (value instanceof String) {
            tokens = new String[1];
            tokens[0] = (String) value;

        } else if (value instanceof Vector) {
            tokens = new String[((Vector) value).size()];

            for (int i = 0; i < tokens.length; i++) {

                tokens[i] = (String) ((Vector) value).get(i);
            }

        } else if (value == null) {
            if (defaults != null) {
                tokens = defaults.getStringArray(key);
            } else {
                tokens = new String[0];
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a String/Vector object");
        }
        return tokens;
    }

    /**
     * @see Configuration
     */
    public Vector getVector(String key) {
        return getVector(key, null);
    }

    /**
     * @see Configuration
     */
    public Vector getVector(String key, Vector defaultValue) {
        Object value = store.get(key);
        Vector v = null;

        if (value instanceof String) {
            v = new Vector(1);
            v.addElement((String) value);

        } else if (value == null) {
            if (defaults != null) {
                v = defaults.getVector(key, defaultValue);
            } else {
                v = ((defaultValue == null) ? new Vector() : defaultValue);
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Vector object: " + value + ", a "
                    + value.getClass().getName());
        }
        return v;
    }

    /**
	 * 
	 */
    public String getBasePath() {
        return this.basePath;
    }

    /**
	 * 
	 */
    public void setBasePath(String path) {
        this.basePath = path;
    }

    /**
     * Returns an object from the store described by the key. If the value is a Vector object, replace it with the first
     * object in the container
     * 
     * @param key
     *            The property key.
     * 
     * @return value Value, transparently resolving a possible Vector dependency.
     */
    private Object resolveVectorStore(String key) {
        Object value = store.get(key);
        if (value != null && value instanceof Vector) {
            value = ((Vector) value).get(0);
        }
        return value;
    }

}
