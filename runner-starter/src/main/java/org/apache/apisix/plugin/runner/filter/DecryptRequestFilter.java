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

import org.apache.apisix.plugin.runner.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import static org.apache.apisix.plugin.runner.filter.Constants.*;

/**
 * stage: ext-plugin-post-req
 */
@Component
public class DecryptRequestFilter implements PluginFilter {
    private final Logger logger = LoggerFactory.getLogger(DecryptRequestFilter.class);

    @Autowired
    UserService userService;

    @Override
    public String name() {
        /* It is recommended to keep the name of the filter the same as the class name.
         Configure the filter to be executed on apisix's routes in the following format

        {
            "uri": "/hello",
            "plugins": {
                "ext-plugin-post-req": {
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

        return "DecryptRequestFilter";
    }

    @Override
    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
        logger.info("input headers:{}, raw input:{}", request.getHeaders(), request.getBody());
        User user = userService.tryFindUser(request.getHeader(Constants.HEADER_USER_ID));
        if (user == null) {
            response.setStatusCode(403);
            response.setBody(ERROR_NOT_FOUND);
            logger.warn("未找到用户：{}", request.getHeaders());
        } else {
            try {
                String decryptedBody = userService.decryptBody(request.getBody(), user);
                request.changeBody(decryptedBody);
                request.setHeader(HEADER_REQUESTBODY_ENCRYPTED_FLAG, "true");
                logger.info("DecryptRequestFilter：request:{}, user：{}，{}", request.getRequestId(), user.getUserid(), decryptedBody);
            } catch (Exception e) {
                logger.error("decrypt request failure", e);
                response.setStatusCode(400);
                response.setBody(ERROR_DECRYPT_REQUEST_FAILURE);
            }
        }
        chain.filter(request, response);
    }


    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredBody() {
        return true;
    }
}
