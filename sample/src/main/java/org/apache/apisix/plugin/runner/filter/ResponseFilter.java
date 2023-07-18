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
import org.apache.apisix.plugin.runner.PostRequest;
import org.apache.apisix.plugin.runner.PostResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResponseFilter implements PluginFilter {
    @Override
    public String name() {
        return "ResponseFilter";
    }

    @Override
    public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
        String configStr = request.getConfig(this);
        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<>();
        conf = gson.fromJson(configStr, conf.getClass());

        Map<String, List<String>> headers = request.getUpstreamHeaders();
        String contentType = CollectionUtils.isEmpty(headers.get("Content-Type")) ? null : headers.get("Content-Type").get(0);
        Integer upstreamStatusCode = request.getUpstreamStatusCode();

        response.setStatusCode(Double.valueOf(conf.get("response_code").toString()).intValue());
        response.setBody((String) conf.get("response_body"));
        response.setHeader((String) conf.get("response_header_name"), (String) conf.get("response_header_value"));
        chain.postFilter(request, response);
    }
}
