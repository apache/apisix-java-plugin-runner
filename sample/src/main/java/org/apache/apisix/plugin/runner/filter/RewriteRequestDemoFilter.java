/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.apisix.plugin.runner.filter;

import com.google.gson.Gson;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RewriteRequestDemoFilter implements PluginFilter {

    @Override
    public String name() {
        /* It is recommended to keep the name of the filter the same as the class name.
         Configure the filter to be executed on apisix's routes in the following format

        {
            "uri": "/hello",
            "plugins": {
                "ext-plugin-pre-req": {
                    "conf": [{
                        "name": "RewriteRequestDemoFilter",
                        "value": "bar"
                    }]
                }
            },
            "upstream": {
                "nodes": {
                    "127.0.0.1:1980": 1
                },
                "type": "roundrobin"
            }
        }

        The value of name in the configuration corresponds to the value of return here.
         */

        return "RewriteRequestDemoFilter";
    }

    @Override
    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
        /*
         * If the conf you configured is of type json, you can convert it to Map or json.
         */

        String configStr = request.getConfig(this);
        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<>();
        conf = gson.fromJson(configStr, conf.getClass());

        /*
         * You can use the parameters in the configuration.
         */

        // note: the path to the rewrite must start with '/'
        request.setPath((String) conf.get("rewrite_path"));
        request.setHeader((String) conf.get("conf_header_name"), (String) conf.get("conf_header_value"));
        /* note: The value of the parameter is currently a string type.
                 If you need the json type, you need the upstream service to parse the string value to json.
                 For example, if the arg is set as below
                 request.setArg("new arg", "{\"key1\":\"value1\",\"key2\":2}");

                 The arg received by the upstream service will be as below
                 "new arg": "{\"key1\":\"value1\",\"key2\":2}"
         */
        request.setArg((String) conf.get("conf_arg_name"), (String) conf.get("conf_arg_value"));

        /*
         * You can fetch the Nginx variables, and the request body
         */
        String remote_addr = request.getVars("remote_addr");
        String server_port = request.getVars("server_port");
        String body = request.getBody();

        chain.filter(request, response);
    }

    /**
     * If you need to fetch some Nginx variables in the current plugin, you will need to declare them in this function.
     * @return a list of Nginx variables that need to be called in this plugin
     */
    @Override
    public List<String> requiredVars() {
        return List.of("remote_addr", "server_port");
    }

    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredBody() {
        return true;
    }
}
