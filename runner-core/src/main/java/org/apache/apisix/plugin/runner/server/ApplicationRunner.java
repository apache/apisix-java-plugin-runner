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

package org.apache.apisix.plugin.runner.server;

import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.handler.IOHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.time.Duration;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ApplicationRunner implements CommandLineRunner {

    private final TcpServer tcpServer;

    @Value("${socket.file:/tmp/runner.sock}")
    private String socketFile;

    private final IOHandler handler;

    private DisposableServer server;

    @Override
    public void run(String... args) throws Exception {
        TcpServer tcpServer = this.tcpServer;

        if (Objects.isNull(tcpServer)) {
            tcpServer = TcpServer.create();
        }
        tcpServer = tcpServer.bindAddress(() -> new DomainSocketAddress(socketFile));
        tcpServer = tcpServer.doOnChannelInit((observer, channel, addr) -> {
//            if (Objects.nonNull(channelHandlers)) {
//                channel.pipeline().addLast(channelHandlers);
//            }
            channel.pipeline().addFirst("logger", new LoggingHandler());
        });

        if (Objects.nonNull(handler)) {
            tcpServer = tcpServer.handle(handler::handle);
        }
        this.server = tcpServer.bindNow();

        // delete socket file when tcp server shutdown
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> this.server.disposeNow(Duration.ofSeconds(45))));

        Thread awaitThread = new Thread(() -> this.server.onDispose().block());
        awaitThread.setDaemon(false);
        awaitThread.setName("uds-server");
        awaitThread.start();
    }
}
