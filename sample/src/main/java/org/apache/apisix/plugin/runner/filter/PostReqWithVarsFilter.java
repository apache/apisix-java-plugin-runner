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
import org.apache.apisix.plugin.runner.PostRequest;
import org.apache.apisix.plugin.runner.PostResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PostReqWithVarsFilter implements PluginFilter {
    @Override
    public String name() {
        return "PostReqWithVarsFilter";
    }

    @Override
    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
        String configStr = request.getConfig(this);
        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<>();
        conf = gson.fromJson(configStr, conf.getClass());
        request.setPath((String) conf.get("rewrite_path"));
        chain.filter(request, response);
    }

    @Override
    public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
        String configStr = request.getConfig(this);
        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<>();
        conf = gson.fromJson(configStr, conf.getClass());
        String bodyStr = request.getBody();
        Map<String, Object> body = new HashMap<>();
        body = gson.fromJson(bodyStr, body.getClass());
        assert body.get("url").toString().endsWith((String) conf.get("rewrite_path"));
        String serverPort = request.getVars("server_port");
        response.setHeader("server_port", serverPort);
        chain.postFilter(request, response);
    }

    @Override
    public List<String> requiredVars() {
        List<String> vars = new ArrayList<>();
        vars.add("server_port");
        return vars;
    }

    @Override
    public Boolean requiredRespBody() {
        return true;
    }
}
