/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.apisix.plugin.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DynamicClassLoader extends ClassLoader {
    private final Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);

    private String name;
    private String classDir;
    private String packageName;

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (this.name == null) {
            return super.findClass(name);
        }

        // can we do replacements for windows only?
        String packagePath = packageName.replaceAll("\\.", "/");
        String classPath = "file:" + classDir + "/" + packagePath + "/" + this.name + ".class";

        URL url;
        URLConnection connection;
        try {
            url = new URL(classPath);
            connection = url.openConnection();
        } catch (IOException e) {
            logger.error("failed to open class file: {}", classPath, e);
            throw new RuntimeException(e);
        }
        try (InputStream input = connection.getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int data = input.read();
            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }
            input.close();
            byte[] classData = buffer.toByteArray();
            String fullyQualifiedName = packageName + "." + name;
            return defineClass(fullyQualifiedName, classData, 0, classData.length);
        } catch (IOException e) {
            logger.error("failed to read class file: {}", classPath, e);
            throw new RuntimeException(e);
        }
    }

    public void setClassDir(String classDir) {
        this.classDir = classDir;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPackageName(String name) {
        packageName = name;
    }
}
