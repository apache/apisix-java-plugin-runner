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
import io.netty.channel.unix.DomainSocketChannel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unix Domain Socket Listen Test")
@ExtendWith(SpringExtension.class)
class ApplicationRunnerTest {

    @Test
    @DisplayName("test listen on socket file success")
    void testUDSConnectSuccess() {
        Connection client =
                TcpClient.create()
                        .remoteAddress(() -> new DomainSocketAddress("/tmp/runner.sock"))
                        .connectNow();
        assertThat(client.channel().isOpen()).isTrue();
        assertThat(client.channel().isActive()).isTrue();
        assertThat(client.channel().isRegistered()).isTrue();
        assertThat(client.channel() instanceof DomainSocketChannel).isTrue();
        assertThat(client.channel().remoteAddress().toString()).isEqualTo("/tmp/runner.sock");
        client.disposeNow();
    }

    @Test
    @DisplayName("test listen on error socket file")
    @Disabled
    void testUDSConnectFail() {
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            Connection client = TcpClient.create()
                    .remoteAddress(() -> new DomainSocketAddress("/tmp/error.sock"))
                    .connectNow();
//            client.disposeNow();
        });
        assertThat(exception.getMessage()).isEqualTo("java.io.FileNotFoundException");

    }
}
