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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Component
public class HotReloadProcess implements ApplicationContextAware {
    private final Logger logger = LoggerFactory.getLogger(HotReloadProcess.class);
    private ApplicationContext ctx;

    private final ScheduledAnnotationBeanPostProcessor postProcessor;

    public HotReloadProcess(ScheduledAnnotationBeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Value("${apisix.runner.dynamic-filter.load-path:/runner-plugin/src/main/java/org/apache/apisix/plugin/runner/filter/}")
    private String loadPath;

    @Value("${apisix.runner.dynamic-filter.package-name:org.apache.apisix.plugin.runner.filter}")
    private String packageName;

    @Value("${apisix.runner.dynamic-filter.enable:false}")
    private Boolean enableHotReload;

    private BeanDefinitionBuilder compile(String userDir, String filterName, String filePath) throws ClassNotFoundException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        String classDir = userDir + "/target/classes";
        File file = new File(userDir);
        if (!file.exists() && !file.isDirectory()) {
            boolean flag = file.mkdirs();
            if (!flag) {
                logger.error("mkdirs:{} error", file.getAbsolutePath());
            }
        }

        String[] args = {"-d", classDir, filePath};
        compiler.run(null, null, null, args);

        ClassLoader parentClassLoader = DynamicClassLoader.class.getClassLoader();
        DynamicClassLoader classLoader = new DynamicClassLoader(parentClassLoader);
        classLoader.setClassDir(classDir);
        classLoader.setName(filterName);
        classLoader.setPackageName(packageName);
        Class<?> myObjectClass = classLoader.loadClass(filterName);
        return BeanDefinitionBuilder.genericBeanDefinition(myObjectClass).setLazyInit(true);
    }

    @Scheduled(fixedRate = 1000, initialDelay = 1000)
    private void hotReloadFilter() {
        if (!enableHotReload) {
            cancelHotReload("hotReloadFilter");
            return;
        }

        final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) ctx.getAutowireCapableBeanFactory();
        String userDir = System.getProperty("user.dir");
        String workDir = userDir + loadPath;

        Path path = Paths.get(workDir);
        boolean exists = Files.exists(path);
        if (!exists) {
            logger.warn("The filter workdir fot hot reload {} not exists", workDir);
            cancelHotReload("hotReloadFilter");
            return;
        }

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    watchService.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }));

            while (true) {
                final WatchKey key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    final WatchEvent.Kind<?> kind = watchEvent.kind();
                    final String filterFile = watchEvent.context().toString();

                    // ignore the file that is not java file
                    if (!filterFile.endsWith(".java")) {
                        continue;
                    }

                    String filterName = filterFile.substring(0, filterFile.length() - 5);
                    String filterBean = Character.toLowerCase(filterFile.charAt(0)) + filterName.substring(1);
                    final String filePath = workDir + filterFile;

                    if (kind == ENTRY_CREATE) {
                        logger.info("file create: {}", filePath);
                        BeanDefinitionBuilder builder = compile(userDir, filterName, filePath);
                        registry.registerBeanDefinition(filterBean, builder.getBeanDefinition());
                    } else if (kind == ENTRY_MODIFY) {
                        logger.info("file modify: {}", filePath);
                        registry.removeBeanDefinition(filterBean);
                        BeanDefinitionBuilder builder = compile(userDir, filterName, filePath);
                        registry.registerBeanDefinition(filterBean, builder.getBeanDefinition());
                    } else if (kind == ENTRY_DELETE) {
                        if (registry.containsBeanDefinition(filterBean)) {
                            logger.info("file delete: {}, and remove filter: {} ", filePath, filterBean);
                            registry.removeBeanDefinition(filterBean);
                            /*TODO: we need to remove the filter from the filter chain
                             * by remove the conf token in cache or other way
                             * */
                        }
                    } else {
                        logger.warn("unknown event: {}", kind);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    logger.warn("key is invalid");
                }
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            logger.error("watch error", e);
            throw new RuntimeException(e);
        }
    }

    public void cancelHotReload(String taskName) {
        Set<ScheduledTask> tasks = postProcessor.getScheduledTasks();
        tasks.forEach(task -> {
            Task t = task.getTask();
            ScheduledMethodRunnable runnable = (ScheduledMethodRunnable) t.getRunnable();
            if (Objects.equals(runnable.getMethod().getName(), taskName)) {
                postProcessor.postProcessBeforeDestruction(runnable.getTarget(), taskName);
                logger.warn("Cancel hot reload schedule task");
            }
        });
    }
}
