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

package org.apache.apisix.plugin.runner;

import io.github.api7.A6.PrepareConf.Req;
import io.github.api7.A6.TextEntry;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;

public class A6Conf {

    public Req getReq() {
        return req;
    }

    private final Req req;

    public PluginFilterChain getChain() {
        return chain;
    }

    private final PluginFilterChain chain;

    public A6Conf(Req req, PluginFilterChain chain) {
        this.req = req;
        this.chain = chain;
    }

    public String get(String key) {
        for (int i = 0; i < this.req.confLength(); i++) {
            TextEntry conf = this.req.conf(i);
            if (conf.name().equals(key)) {
                return conf.value();
            }
        }
        return null;
    }
}
