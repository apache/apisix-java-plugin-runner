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

## 0.1.0

This release mainly improve basic features, bugfix and adds test cases.

### Core

- complete project skeleton and available features.
- complete joint debug with [APISIX](https://github.com/apache/apisix).
- mecessary documentation. [#19](https://github.com/apache/apisix-java-plugin-runner/pull/19)
- the test cases cover the codec. [#14](https://github.com/apache/apisix-java-plugin-runner/pull/14)
- the test cases cover the filter. [#15](https://github.com/apache/apisix-java-plugin-runner/pull/15)
- supported debug mode.

## Bugfix

- set more headers and args. [#30](https://github.com/apache/apisix-java-plugin-runner/pull/30)
- ensure correct encoding and decoding when data length is greater than 256. [#32](https://github.com/apache/apisix-java-plugin-runner/pull/32)
- use netty's decoder to handle TCP half-packet issues that occur in custom protocols. [#26](https://github.com/apache/apisix-java-plugin-runner/pull/26)

[Back to TOC](#table-of-contents)
