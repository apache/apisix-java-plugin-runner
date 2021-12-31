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

import java.util.List;

public class PluginFilterChain {
    private final int index;

    private final List<PluginFilter> filters;

    public int getIndex() {
        return index;
    }

    public PluginFilterChain(List<PluginFilter> filters) {
        this.filters = filters;
        this.index = 0;
    }

    public PluginFilterChain(PluginFilterChain parent, int index) {
        this.filters = parent.getFilters();
        this.index = index;
    }

    public List<PluginFilter> getFilters() {
        return filters;
    }

    public void filter(HttpRequest request, HttpResponse response) {
        if (this.index < filters.size()) {
            PluginFilter filter = filters.get(this.index);
            PluginFilterChain next = new PluginFilterChain(this,
                    this.index + 1);
            filter.filter(request, response, next);
        }
    }
}
