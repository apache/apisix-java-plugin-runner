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

Project Maintenance
=================

## Release steps

### Publish Maven Artifacts
Apache projects release all software packages through the ASF distribution system. 

1. Set up your development environment. For more details, see the [Publishing Maven Releases to Maven Central Repository](https://infra.apache.org/publishing-maven-artifacts.html).

2. Deploy the snapshot artifacts to Apache Nexus

```shell
make deploy
```
