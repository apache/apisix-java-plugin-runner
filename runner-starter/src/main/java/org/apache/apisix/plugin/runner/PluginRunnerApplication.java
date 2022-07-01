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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.tools.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

@SpringBootApplication
@EnableScheduling
public class PluginRunnerApplication {

    @Autowired
    private YAMLConfig myConfig;
    @Autowired
    private ApplicationContext ctx;
    private static ClassLoader parentClassLoader;
    private static DynamicClassLoader classLoader;

    public static void main(String[] args) {
        parentClassLoader = DynamicClassLoader.class.getClassLoader();
        classLoader = new DynamicClassLoader(parentClassLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        new SpringApplicationBuilder(PluginRunnerApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 1000)
    public void reload() throws ClassNotFoundException, IOException, InterruptedException {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) ctx.getAutowireCapableBeanFactory();
        WatchService watchService = FileSystems.getDefault().newWatchService();

        String pathToProject = System.getProperty("user.dir");

        //get packagename and path to user's filters from YAML file
        String packageName = myConfig.getPackageName();
        String absolutePath = myConfig.getPath();
        if(packageName.equals("")) {
            packageName = "org.apache.apisix.plugin.runner.filter";
        }
        if (absolutePath.equals("")) {
            absolutePath = pathToProject + "/runner-plugin/src/main/java/org/apache/apisix/plugin/runner/filter/";
        }
        Path path = Paths.get(absolutePath);

        //make /target/classes directory if not already exists, compiled java files are output here
        new File(pathToProject + "/target").mkdirs();
        new File(pathToProject + "/target/classes").mkdirs();

        //detect changes when files in the path are created, modified, or deleted
        path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        boolean poll = true;
        while (poll) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                String[] allFilters = new File(absolutePath).list();
                HashSet<String> set = new HashSet<>();

                for (int i = 0; i < allFilters.length; i++) {
                    //strangely, watchservice creates a file that ends with ".java~", we ignore this file
                    if (!allFilters[i].equals("package-info.java") && allFilters[i].charAt(allFilters[i].length() - 1) != '~') {
                        allFilters[i] = allFilters[i].substring(0, allFilters[i].length() - 5);
                        set.add(allFilters[i]);
                    }
                }

                for (String filterName : allFilters) {
                    if ((!filterName.equals("package-info.java")) && filterName.charAt(filterName.length() - 1) != '~') {
                        //Bean Filter Name necessary because beans always start with lower case letters
                        String beanFilterName = Character.toLowerCase(filterName.charAt(0)) + filterName.substring(1);
                        if (registry.containsBeanDefinition(beanFilterName)) {
                            registry.removeBeanDefinition(beanFilterName);
                        }
                        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                        String[] args = {"-d", pathToProject + "/target/classes", absolutePath + filterName + ".java"};
                        compiler.run(null, null, null, args);

                        classLoader = new DynamicClassLoader(parentClassLoader);
                        classLoader.setDir(pathToProject + "/target/classes");
                        classLoader.setFilters(set);
                        classLoader.setPackageName(packageName);
                        Class myObjectClass = classLoader.loadClass(filterName);
                        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(myObjectClass).setLazyInit(true);
                        registry.registerBeanDefinition(beanFilterName, builder.getBeanDefinition());
                    }
                }

                //removes a filter dynamically
                List<String> allRemovedFilters = findRemovedFilters(pathToProject, packageName, set);
                for(String removedFilter : allRemovedFilters) {
                    String beanRemovedFilter = Character.toLowerCase(removedFilter.charAt(0)) + removedFilter.substring(1);
                    if(registry.containsBeanDefinition(beanRemovedFilter)) {
                        registry.removeBeanDefinition(beanRemovedFilter);
                    }
                }

            }
            poll = key.reset();
        }
    }

    public List<String> findRemovedFilters (String pathToProject, String packageName, HashSet<String> set) {
        List<String> allRemovedFilters = new ArrayList<>();
        String packagePath = packageName.replaceAll("\\.", "/");
        String[] allClasses = new File(pathToProject + "/target/classes/" + packagePath + "/").list();
        for (String className : allClasses) {
            className = className.substring(0, className.length() - 6);
            if (!set.contains(className)) {
                allRemovedFilters.add(className);
            }
        }
        return allRemovedFilters;
    }
}
