---
title: Installation
keywords:
- apisix-java-plugin-runner
- Installation
description: This document explains how to installation and use apisix-java-plugin-runner.
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

## Overview

This document explains how to install apisix-java-plugin-runner.

Prerequisites
-------------

* JDK 21
* APISIX master branch
* Refer to [Debug](how-it-works.md#debug)  to build the debug environment.

Install
-------

1. Create a simple web application with Spring Boot, and choose Maven as the build tool.

2. Add the apisix-java-plugin-runner dependency in your POM, like:

```xml
<dependency>
    <groupId>org.apache.apisix</groupId> 
    <artifactId>apisix-runner-starter</artifactId>
    <version>0.6.0</version>
</dependency>
```

3. Configuring the scan package path

```xml

```java
@SpringBootApplication(scanBasePackages = {"your-filter's-package-name","org.apache.apisix.plugin.runner"})
```

4. Excluding the default logging framework

To prevent multiple slf4j bindings, exclude the `logback-classic` and `log4j-to-slf4j` in `pom.xml`, like:

```xml
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

5. Configuring the address for Unix Domain Socket communication with APISIX

```properties
socket.file = /tmp/runner.sock
```

6. Implementing the `PluginFilter` interface

When you write your custom plugins, you need to implement the `PluginFilter` interface and 
inject filters into Spring Boot's object lifecycle management using `@Component`.

code example:

```java
@Component
public class RewriteRequestDemoFilter implements PluginFilter {
  ......
  implementing functions
}
```

You can refer to [Development](development.md) to learn how to write custom plugins.

Demo
-------

A Demo Project that work with apisix-java-plugin-runner and custom filters 
can be found at: [java-plugin-runner-demo](https://github.com/tzssangglass/java-plugin-runner-demo-1).
