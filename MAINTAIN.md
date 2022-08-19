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
2. Create a release PR, the release PR (e.g.: https://github.com/apache/apisix-java-plugin-runner/pull/183) should do the following:
   - contains the changelog
   - contains version change(remove `SNAPSHOT` suffix)
3. After the release PR merged, create a branch named `release/<version>` form `main` branch.
4. Execute `make release-src` to build vote artifact, package the vote artifact to Apache's dev-apisix repo
5. When the vote is passed, Send the [vote email](https://lists.apache.org/thread/721kfy9yqp4cm5cokg4yydczxgr08nbq) to dev@apisix.apache.org
6. When the vote is passed, send the [vote result email](https://lists.apache.org/thread/ky55hf5swklb880x3tf3rdwfj5wyt1hs) to dev@apisix.apache.org
7. Move the vote artifact to Apache's apisix repo
8. Register the release info in https://reporter.apache.org/addrelease.html?apisix
9. Checkout the release branch, execute `make deploy` to deploy the release artifact to Apache's apisix repo
10. Create a [GitHub release](https://github.com/apache/apisix-java-plugin-runner/releases/tag/0.3.0) from the release branch
11. Update [APISIX's website](https://github.com/apache/apisix-website/pull/1295)
12. Send the [ANNOUNCE email](https://lists.apache.org/thread/4s4msqwl1tq13p9dnv3hx7skbgpkozw1) to dev@apisix.apache.org & announce@apache.org
