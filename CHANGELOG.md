---
title: Changelog
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

## Table of Contents

- [0.1.0](#010)
- [0.2.0](#020)

## 0.2.0

This release mainly provides the ability to get variables and request body.

### Change

- change the network communication framework from reactor-netty to netty. [100](https://github.com/apache/apisix-java-plugin-runner/pull/100)
- change the return value of filter function in PluginFilter interface. [100](https://github.com/apache/apisix-java-plugin-runner/pull/100)
- the requiredVars and requiredBody functions have been added to the PluginFilter interface. [100](https://github.com/apache/apisix-java-plugin-runner/pull/100)

### Core

- support for getting variables and request body. [100](https://github.com/apache/apisix-java-plugin-runner/pull/100)
- catching exceptions thrown during the writeAndFlush. [107](https://github.com/apache/apisix-java-plugin-runner/pull/107)

### Bugfix

- chinese encoding in the response body. [#53](https://github.com/apache/apisix-java-plugin-runner/pull/53)
- stop request but not setStatusCode will trigger an exception In APISIX. [#56](https://github.com/apache/apisix-java-plugin-runner/pull/56)
- reset vtable_start and vtable_size of PrepareConf/Req. [#66](https://github.com/apache/apisix-java-plugin-runner/pull/66)
- convert the conf req to an object and put it in the cache. [#73](https://github.com/apache/apisix-java-plugin-runner/pull/73)
- modify socket file permissions so that APISIX has permission to read and write. [#96](https://github.com/apache/apisix-java-plugin-runner/pull/96)
- disable null as key of req/resp headers and args. [#105](https://github.com/apache/apisix-java-plugin-runner/pull/105)
- pre-read requests prevent read/write index confusion. [#113](https://github.com/apache/apisix-java-plugin-runner/pull/113)

[Back to TOC](#table-of-contents)

## 0.1.0

This release mainly provides basic features and adds test cases.

### Core

- complete project skeleton and available features.
- complete united test with [APISIX](https://github.com/apache/apisix).
- supported debug mode.

### Bugfix

- set more headers and args. [#30](https://github.com/apache/apisix-java-plugin-runner/pull/30)
- ensure correct encoding and decoding when data length is greater than 256. [#32](https://github.com/apache/apisix-java-plugin-runner/pull/32)
- use netty's decoder to handle TCP half-packet issues that occur in custom protocols. [#26](https://github.com/apache/apisix-java-plugin-runner/pull/26)

[Back to TOC](#table-of-contents)
