package org.apache.apisix.plugin.runner.handler;

import java.util.Map;

import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.A6ConfigWatcher;

class TestWatcher implements A6ConfigWatcher {
    private Map<String, String> config;
    private long token;

    public Map<String, String> getConfig() {
        return config;
    }

    public long getToken() {
        return token;
    }

    @Override
    public String name() {
        return "test";
    }

    @Override
    public void watch(long confToken, A6Conf a6Conf) {
        config = a6Conf.getConfig();
        token = confToken;
    }
}
