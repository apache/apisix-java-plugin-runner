package org.apache.apisix.plugin.runner.server;

import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import org.apache.apisix.plugin.runner.codec.PluginRunnerConfiguration;
import org.apache.apisix.plugin.runner.handler.A6HandlerConfiguration;
import org.apache.apisix.plugin.runner.handler.IOHandlerConfiguration;
import org.apache.apisix.plugin.runner.server.config.TcpServerConfiguration;
import org.apache.apisix.plugin.runner.service.CacheConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unix Domain Socket Listen Test")
@ExtendWith(SpringExtension.class)
class ApplicationRunnerTest {

    protected ConfigurableApplicationContext context;

    @BeforeEach
    @DisplayName("start java plugin runner")
    void setup() {
        context = new SpringApplicationBuilder(ApplicationRunner.class,
                IOHandlerConfiguration.class,
                TcpServerConfiguration.class,
                CacheConfiguration.class,
                A6HandlerConfiguration.class,
                PluginRunnerConfiguration.class)
                .web(WebApplicationType.NONE)
                .run();
    }

    @Test
    @DisplayName("test listen on socket file success")
    @Disabled
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
            TcpClient.create()
                    .remoteAddress(() -> new DomainSocketAddress("/tmp/error.sock"))
                    .connectNow();
        });
        assertThat(exception.getMessage()).isEqualTo("java.io.FileNotFoundException");
    }

    @AfterEach
    @DisplayName("shutdown java plugin runner")
    void down() {
        context.close();
    }
}
