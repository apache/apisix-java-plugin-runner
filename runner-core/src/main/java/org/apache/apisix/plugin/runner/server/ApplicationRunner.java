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

import com.google.common.cache.Cache;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.handler.PrepareConfHandler;
import org.apache.apisix.plugin.runner.handler.HTTPReqCallHandler;
import org.apache.apisix.plugin.runner.handler.PayloadDecoder;
import org.apache.apisix.plugin.runner.handler.BinaryProtocolDecoder;
import org.apache.apisix.plugin.runner.handler.PayloadEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApplicationRunner implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);

    @Value("${socket.file}")
    private String socketFile;

    private Cache<Long, A6Conf> cache;

    private ObjectProvider<PluginFilter> beanProvider;

    @Autowired
    public ApplicationRunner(Cache<Long, A6Conf> cache, ObjectProvider<PluginFilter> beanProvider) {
        this.cache = cache;
        this.beanProvider = beanProvider;
    }

    public PrepareConfHandler createConfigReqHandler(Cache<Long, A6Conf> cache, ObjectProvider<PluginFilter> beanProvider) {
        List<PluginFilter> pluginFilterList = beanProvider.orderedStream().collect(Collectors.toList());
        Map<String, PluginFilter> filterMap = new HashMap<>();
        for (PluginFilter filter : pluginFilterList) {
            filterMap.put(filter.name(), filter);
        }
        return new PrepareConfHandler(cache, filterMap);
    }

    public HTTPReqCallHandler createA6HttpHandler(Cache<Long, A6Conf> cache) {
        return new HTTPReqCallHandler(cache);
    }

    public void start(String path) throws Exception {
        EventLoopGroup group;
        ServerBootstrap bootstrap = new ServerBootstrap();
        if (KQueue.isAvailable()) {
            group = new KQueueEventLoopGroup();
            bootstrap.group(group).channel(KQueueServerDomainSocketChannel.class);
        } else if (Epoll.isAvailable()) {
            group = new EpollEventLoopGroup();
            bootstrap.group(group).channel(EpollServerDomainSocketChannel.class);
        } else {
            String errMsg = "java runner is only support epoll or kqueue";
            logger.warn(errMsg);
            throw new RuntimeException(errMsg);
        }

        try {
            initServerBootstrap(bootstrap);
            ChannelFuture future = bootstrap.bind(new DomainSocketAddress(path)).sync();
            Runtime.getRuntime().exec("chmod 777 " + socketFile);
            logger.warn("java runner is listening on the socket file: {}", socketFile);

            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    private void initServerBootstrap(ServerBootstrap bootstrap) {
        bootstrap.childHandler(new ChannelInitializer<DomainSocketChannel>() {
            @Override
            protected void initChannel(DomainSocketChannel channel) {
                channel.pipeline().addFirst("logger", new LoggingHandler())
                        .addAfter("logger", "payloadEncoder", new PayloadEncoder())
                        .addAfter("payloadEncoder", "delayedDecoder", new BinaryProtocolDecoder())
                        .addLast("payloadDecoder", new PayloadDecoder())
                        .addAfter("payloadDecoder", "prepareConfHandler", createConfigReqHandler(cache, beanProvider))
                        .addAfter("prepareConfHandler", "hTTPReqCallHandler", createA6HttpHandler(cache));

            }
        });
    }

    @Override
    public void run(String... args) throws Exception {
        if (socketFile.startsWith("unix:")) {
            socketFile = socketFile.substring("unix:".length());
        }
        Path socketPath = Paths.get(socketFile);
        Files.deleteIfExists(socketPath);
        start(socketPath.toString());
    }
}
