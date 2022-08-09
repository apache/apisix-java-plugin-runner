---
title: Hot-Reload
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

Hot Reloading
=================

Overview
-----
This document explains the new hot reload feature provided by apisix-java-plugin-runner.

APISIX offers many fully-featured plugins and also allows users to develop their own plugins in Lua, Java, and other languages. Currently, APISIX offers hot reloading of plugins developed internally (Lua), however, hot reloading of external Java plugins is currently not supported.

This feature provides hot reloading of Java plugins through the implementation of a custom classloader that can reload both Java classes and SpringBoot beans during runtime. When apisix-java-plugin-runner is launched, a scheduled side process watches for file modifications (create, delete, or change) in the folder that holds the user-implemented filters. When a user changes their filter while the plugin runner is running, the file watcher immediately picks up on it, and the file is recompiled and reloaded. The changes can immediately be seen by sending a request to APISIX to trigger the filter’s route.

This saves time during development and enables users to change parts of their code without restarting the entire plugin-runner.

How to use
-----
Using and understanding the hot reload feature is simple.

To begin, make sure your filters are located in the *runner/filter* module (along with *package-info.java*). If you are implementing filters from a different location, be sure to specify the load-path and the package name in the *application.yaml* file located in *runner-starter/src/main/resources/*.

Path names should be enclosed in forward slashes *~/runner-plugin/src/main/java/org/apache/apisix/plugin/runner/filter/*

Package name should look like *org.apache.apisix.plugin.runner.filter*

Run APISIX and apisix-java-plugin-runner.

Create a route for your filter (follow https://apisix.apache.org/blog/2021/06/21/use-java-to-write-apache-apisix-plugins/ for more information).

Send a request to your route to ensure everything is functioning.

Modify your files in *runner/filter* (or other location).

Send another request to your route, changes should be noticeable immediately.

Keep in mind, for the file watcher to detect a change, it may be necessary to save the file.  

How it works
-----
Hot reload in apisix-java-plugin-runner is implemented using a custom classloader that dynamically recompiles and reloads filters. The Java WatchService is used to track file updates. Each file update is registered either as a
1. File Modification
2. File Creation
3. File Deletion

where each type of file update is handled slightly differently.

After File Modification, the following process occurs:
1. The Spring Bean (associated with the modified filter) is removed from the Spring Boot Application Context
2. The Filter source code is recompiled
3. The Filter class is reloaded
4. The new Spring Bean is generated from the modified Filter class
5. The new Spring Bean is loaded into Application Context

File Creation and Deletion follow a similar process.
