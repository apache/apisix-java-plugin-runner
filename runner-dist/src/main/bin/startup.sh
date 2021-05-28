#!/usr/bin/env sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

RUNNER_HOME=$(dirname "$0")/..
RUNNER_DIR=${RUNNER_HOME%/bin*}
RUNNER_HEAP=${JAVA_HEAP:-4g}

JAVA_OPS="${JAVA_OPS} -Xmx${RUNNER_HEAP} -Xms${RUNNER_HEAP}"

nohup java -jar ${JAVA_OPS} ${RUNNER_DIR}/apisix-java-plugin-*.jar \
 1>${PWD}/logs/runner.out \
 2>${PWD}/logs/runner.err &

echo $! > ${PWD}/logs/runner.pid
