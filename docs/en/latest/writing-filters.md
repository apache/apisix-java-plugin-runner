<!--
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
-->

# Overview

This document explains how to develop Java plugins using apisix-java-plugin-runner's official Maven release.

A Demo Project can be found at: https://github.com/tzssangglass/java-plugin-runner-demo-1

___

Create a new Maven Spring Boot Project.

Add the GAV of `apisix-java-plugin-runner` in `pom.xml`.
```
<dependency>
    <groupId>org.apache.apisix</groupId> 
    <artifactId>apisix-runner-starter</artifactId>
    <version>0.3.0</version>
</dependency>
```
Be sure to add the Maven JAR into the class path. Use `org.apache.apisix:apisix-runner-starter:0.3.0` when asked for Maven coordinates. For Intellij IDEA users unsure on how to add files to the class path, follow https://stackoverflow.com/questions/16742085/adding-jar-files-to-intellijidea-classpath.

To prevent multiple slf4j (a facade for various logging frameworks) bindings, exclude the *logback-classic* and *log4j-to-slf4j* transitive dependencies from being built within *spring-boot-starter*

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <exclusions>
           <exclusion>
                <groupId>ch.qos.logback</groupId>
                <artifactId>logback-classic</artifactId>
           </exclusion>
           <exclusion>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-to-slf4j</artifactId>
           </exclusion>
    </exclusions>
</dependency>
```
The final pom.xml file should look similar to
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.1</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>demo</description>
    <properties>
        <java.version>11</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>ch.qos.logback</groupId>
                    <artifactId>logback-classic</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.apache.logging.log4j</groupId>
                    <artifactId>log4j-to-slf4j</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
            <groupId>org.apache.apisix</groupId> 
            <artifactId>apisix-runner-starter</artifactId>
            <version>0.3.0</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

In the Java main class, include the line
```
@SpringBootApplication(scanBasePackages = {"your-filter's-package-name","org.apache.apisix.plugin.runner"})
```
*scanBasePackages* allows Spring Boot to read the *@Component* classes that exist inside of the Maven JAR along with the implemented Java filter.

An example main class looks like
```
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.demo","org.apache.apisix.plugin.runner"})
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```

In *~/src/main/resources/application.properties*, add
```
socket.file = /tmp/runner.sock
```
This allows our java-plugin-runner to communicate with the main APISIX process.

Finally, build your Java plugin! Be sure to label each filter class as a Spring *@Component* while following the guide at:
https://github.com/apache/apisix-java-plugin-runner/blob/main/docs/en/latest/development.md





