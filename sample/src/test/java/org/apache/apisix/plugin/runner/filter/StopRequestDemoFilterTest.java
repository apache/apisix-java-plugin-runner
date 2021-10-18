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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StopRequestDemoFilterTest {
    
    @Test
    @DisplayName("test stop response code of config string")
    void testConfigStringResponseCodeConverter() {

        String configStr;

        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<String, Object>();

        configStr = "{\"stop_response_code\": 200, \"stop_response_header_name\": \"header_java_runner\", \"stop_response_header_value\": \"via-java-runner\",  \"stop_response_body\": \"hellox\"}";
        conf = gson.fromJson(configStr, conf.getClass());
        Assertions.assertTrue(conf.get("stop_response_code") instanceof Double);
        Assertions.assertTrue(Double.valueOf(conf.get("stop_response_code").toString()).intValue() == 200);

        configStr = "{\"stop_response_code\": \"200\", \"stop_response_header_name\": \"header_java_runner\", \"stop_response_header_value\": \"via-java-runner\",  \"stop_response_body\": \"hellox\"}";
        conf = gson.fromJson(configStr, conf.getClass());
        Assertions.assertTrue(conf.get("stop_response_code") instanceof String);
        Assertions.assertTrue(Double.valueOf(conf.get("stop_response_code").toString()).intValue() == 200);

    }

    @Test
    @DisplayName("test filter")
    void testFilter() {

        StopRequestDemoFilter filter = new StopRequestDemoFilter();

        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = new HttpResponse(1);
        PluginFilterChain chain = mock(PluginFilterChain.class);

        String configStr = "{\"stop_response_code\": 200, \"stop_response_header_name\": \"header_java_runner\", \"stop_response_header_value\": \"via-java-runner\",  \"stop_response_body\": \"hellox\"}";
        when(request.getConfig(filter)).thenReturn(configStr);
        Assertions.assertNull(filter.filter(request, response, chain));

        configStr = "{\"stop_response_code\": 200.0, \"stop_response_header_name\": \"header_java_runner\", \"stop_response_header_value\": \"via-java-runner\",  \"stop_response_body\": \"hellox\"}";
        when(request.getConfig(filter)).thenReturn(configStr);
        Assertions.assertNull(filter.filter(request, response, chain));

        configStr = "{\"stop_response_code\": \"200\", \"stop_response_header_name\": \"header_java_runner\", \"stop_response_header_value\": \"via-java-runner\",  \"stop_response_body\": \"hellox\"}";
        when(request.getConfig(filter)).thenReturn(configStr);
        Assertions.assertNull(filter.filter(request, response, chain));
    }

    @Test
    @DisplayName("test name")
    void testName() {      
        StopRequestDemoFilter filter = new StopRequestDemoFilter();

        Assertions.assertEquals("StopRequestDemoFilter", filter.name());
    }
}
