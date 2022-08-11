---
title: Deployment Guide
---

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

This document explains how to support multiple ways to deploy custom plugins.

:::note

This feature is WIP.

:::

### Ship plugin

In your plugin's `pom.xml`, add the following configuration:
```
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <classifier>exec</classifier>
            </configuration>
        </plugin>
    </plugins>
</build>
```

The standard Spring Boot executable JAR places all of your application classes inside *BOOT-INF/classes*, 
making it impossible to inject into another project. This config builds an additional non-executable JAR 
that can be used for dependency injection.

Deploy the JARs to Maven Central.

### Using a deployed plugin

To use someone else's plugin, add their plugin's non-executable JAR as a dependency in your project. 
Add the package name of their filters (usually the same as the Group ID) in *scanBasePackages* in your main 
SpringBootApplication class to allow Spring to find the plugin *@Component*.
