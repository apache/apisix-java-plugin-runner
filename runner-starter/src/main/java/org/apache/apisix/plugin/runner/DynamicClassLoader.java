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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

public class DynamicClassLoader extends ClassLoader {
    private HashSet<String> allFilters;
    private String dir;
    private String packageName;

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
        allFilters = new HashSet<>();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        if (!allFilters.contains(name)) {
            return super.loadClass(name);
        }
        try {
            String packagePath = packageName.replaceAll("\\.", "/");
            String url = "file:" + dir + "/" + packagePath + "/" + name + ".class";
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            String fullyQualifiedName = packageName + "." + name;
            return defineClass(fullyQualifiedName,
                    classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setDir(String path) {
        dir = path;
    }

    public void setFilters(HashSet<String> filters) {
        allFilters = filters;
    }

    public void setPackageName(String name) {
        packageName = name;
    }
}
