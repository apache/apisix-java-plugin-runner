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
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.apisix.plugin.runner.A6ErrResponse;
import org.apache.apisix.plugin.runner.exception.ExceptionCaught;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExceptionCaughtHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(ExceptionCaughtHandler.class);

    List<ExceptionCaught> exceptionCaughtList = new ArrayList<ExceptionCaught>();

    public ExceptionCaughtHandler() {

    }

    public ExceptionCaughtHandler(List<ExceptionCaught> exceptionCaughtList) {
        this.exceptionCaughtList = exceptionCaughtList;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("handle request error: ", cause);
        if (!exceptionCaughtList.isEmpty()) {
            exceptionCaughtList.get(0).exceptionCaught(ctx, cause);
            return;
        }
        A6ErrResponse errResponse = new A6ErrResponse(Code.SERVICE_UNAVAILABLE);
        ctx.writeAndFlush(errResponse);
    }
}
