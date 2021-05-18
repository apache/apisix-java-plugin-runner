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
 
package org.apache.apisix.plugin.runner.handler;

import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.A6Response;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.filter.FilterChain;

public class A6HttpCallHandler implements Handler {
    
    private final FilterChain chain;
    
    public A6HttpCallHandler(FilterChain chain) {
        this.chain = chain;
    }
    
    @Override
    public void handle(A6Request request, A6Response response) {
        HttpRequest req = null;
        HttpResponse rsp = null;
        chain.doFilter(req, rsp);
    }
}
