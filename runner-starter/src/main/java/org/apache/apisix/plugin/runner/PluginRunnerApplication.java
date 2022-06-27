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
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PluginRunnerApplication {

   @Autowired ApplicationContext ctx;

   static ClassLoader parentClassLoader;
   static MyClassLoader classLoader;

   public static void main(String[] args) {
       parentClassLoader = MyClassLoader.class.getClassLoader();
       classLoader = new MyClassLoader(parentClassLoader);
       Thread.currentThread().setContextClassLoader(classLoader);
       new SpringApplicationBuilder(PluginRunnerApplication.class)
               .web(WebApplicationType.NONE)
               .run(args);
   }

   @Scheduled(fixedRate = 5000, initialDelay = 2000)
   public void reload() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
       AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
       BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
       registry.removeBeanDefinition("rewriteRequestDemoFilter");
       classLoader = new MyClassLoader(parentClassLoader);
       Class myObjectClass = classLoader.loadClass("org.apache.apisix.plugin.runner.filter.RewriteRequestDemoFilter");
       BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(myObjectClass).setLazyInit(true);
       registry.registerBeanDefinition("rewriteRequestDemoFilter", builder.getBeanDefinition());

   }

}
