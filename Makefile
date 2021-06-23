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

SHELL := /bin/bash -o pipefail

VERSION ?= latest
RELEASE_SRC = apache-apisix-java-plugin-runner-${VERSION}-src

.PHONY: release-src
release-src: compress-tar
	gpg --batch --yes --armor --detach-sig ./dist/$(RELEASE_SRC).tgz
	shasum -a 512 ./dist/$(RELEASE_SRC).tgz > ./dist/$(RELEASE_SRC).tgz.sha512

	mkdir -p release
	mv ./dist/$(RELEASE_SRC).tgz release/$(RELEASE_SRC).tgz
	mv ./dist/$(RELEASE_SRC).tgz.asc release/$(RELEASE_SRC).tgz.asc
	mv ./dist/$(RELEASE_SRC).tgz.sha512 release/$(RELEASE_SRC).tgz.sha512

.PHONY: compress-tar
compress-tar:
	./mvnw package

