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

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

@RequiredArgsConstructor
public class IOHandler {

    private final PayloadHandler payloadHandler;
    
    public Publisher<Void> handle(NettyInbound in, NettyOutbound out) {
        return in.receive().asByteBuffer()
                .map(payloadHandler::decode)
                .map(payloadHandler::dispatch)
                .flatMap(e -> out.sendByteArray(Mono.just(payloadHandler.encode(e).array())).then());
    }
    
}
