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

import io.github.api7.A6.Err.Code;
import io.netty.channel.ChannelHandlerContext;
import org.apache.apisix.plugin.runner.PostResponse;
import org.apache.apisix.plugin.runner.exception.ExceptionCaught;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalException implements ExceptionCaught {

    private final Logger logger = LoggerFactory.getLogger(GlobalException.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // expand exception
        logger.warn("plugin expend");
        PostResponse errResponse = new PostResponse(Code.SERVICE_UNAVAILABLE);
        errResponse.setStatusCode(10001);
        errResponse.setBody("test exception");
        ctx.writeAndFlush(errResponse);
    }
}
