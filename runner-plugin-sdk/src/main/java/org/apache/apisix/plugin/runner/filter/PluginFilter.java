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

import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.PostRequest;
import org.apache.apisix.plugin.runner.PostResponse;

import java.util.List;

public interface PluginFilter {

    /**
     * @return the name of plugin filter
     */
    String name();

    /**
     * do the plugin filter chain
     *
     * @param request  the request form APISIX
     * @param response the response for APISIX
     * @param chain    the chain of filters
     */
    default void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
    }

    /**
     * filtering after the upstream response is complete
     *
     * @param request  context of the upstream return
     * @param response modify the context of the upstream response
     */
    default void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
    }

    /**
     * declare in advance the nginx variables that you want to use in the plugin
     *
     * @return the nginx variables as list
     */
    default List<String> requiredVars() {
        return null;
    }

    /**
     * need request body in plugins or not
     *
     * @return true if need request body
     */
    default Boolean requiredBody() {
        return false;
    }

    /**
     * need response body of upstream server in plugins or not
     *
     * @return true if need response body
     */
    default Boolean requiredRespBody() {
        return false;
    }
}

