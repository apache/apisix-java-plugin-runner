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

# Development

This document explains how to get started to develop the apisix-java-plugin-runner.

Prerequisites
-------------

* JDK 8
* APISIX 2.6.0
* Clone the [apisix-java-plugin-runner](https://github.com/apache/apisix-java-plugin-runner) project.
* Refer to [Debug](how-it-works.md#debug)  to build the debug environment.

Install
-------

```shell
cd /path/to/apisix-java-plugin-runner
./mvnw install
```

Write Filter
------------

Refer to the code in the [sample](https://github.com/apache/apisix-java-plugin-runner/tree/main/sample)
to learn how to extend `PluginFilter`, define the order, rewrite requests and stop requests.

####  Code Location

You need to put the code in [runner-plugin](https://github.com/apache/apisix-java-plugin-runner/tree/main/runner-plugin/src/main/java/org/apache/apisix/plugin/runner/filter)
so that the `apisix-java-plugin-runner.jar` will contain the filter implementation class you wrote when you package it.

####  The order of filter execution

The order of execution of the filter in the runner is determined by the index of the `conf` array in the `ext-plugin-pre-req` or `ext-plugin-post-req` configuration.

####  The name of filter execution

The requests go through filters that are dynamically configured on APISIX.
For example, if the following configuration is done on APISIX

```shell
curl http://127.0.0.1:9080/apisix/admin/routes/1 -H 'X-API-KEY: edd1c9f034335f136f87ad84b625c8f1' -X PUT -d '
{
    "uri":"/hello",
    "plugins":{
        "ext-plugin-pre-req":{
            "conf":[
                {
                    "name":"FooFilter",
                    "value":"bar"
                }
            ]
        }
    },
    "upstream":{
        "nodes":{
            "127.0.0.1:1980":1
        },
        "type":"roundrobin"
    }
}'
```

apisix-java-plugin-runner will look for implementation classes named `FooFilter`,
and the name of each filter's implementation class is the return value of its overridden function `public String name()`.


####  Rewrite Request

If you perform the following function call in the filter chain of the implementation class

*  request.setPath()
*  request.setHeader()
*  request.setArg()

this means to rewrit the current request, the upstream server will receive
the relevant parameters rewritten here.

####  Stop Request

If you perform the following function call in the filter chain of the implementation class

*  response.setStatusCode()
*  response.setHeader()
*  response.setBody()

this means to stop the current request, the client will receive
the relevant parameters generated here.

Test
----

### Run Unit Test Suites

```shell
cd /path/to/apisix-java-plugin-runner
 ./mvnw test
```


### Mimic practical environment

If you want to mimic the practical environment, you need to configure the route on APISIX
by having the request go through the filter you want to test, for example

```json
"plugins":{
    "ext-plugin-pre-req":{
        "conf":[
            {
                "name":"FooFilter",
                "value":"bar"
            }
        ]
    }
}
```

and then make a request to APISIX to trigger the route.
