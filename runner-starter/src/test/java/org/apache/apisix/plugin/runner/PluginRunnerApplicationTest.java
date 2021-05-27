package org.apache.apisix.plugin.runner;

import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unix Domain Socket Listen Test")
class PluginRunnerApplicationTest {

    @Test
    void testMain() {
        PluginRunnerApplication.main(new String[]{"args"});
    }

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
    void testUDSConnectFail() {
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            Connection client = TcpClient.create()
                    .remoteAddress(() -> new DomainSocketAddress("/tmp/error.sock"))
                    .connectNow();
            client.disposeNow();
        });
        assertThat(exception.getMessage()).isEqualTo("java.io.FileNotFoundException");
    }
}
