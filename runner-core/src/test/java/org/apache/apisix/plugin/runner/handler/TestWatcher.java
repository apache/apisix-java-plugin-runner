package org.apache.apisix.plugin.runner.handler;

import java.util.Map;

import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.A6ConfigWatcher;

class TestWatcher implements A6ConfigWatcher {
    public Map<String, String> config;

    public Map<String, String> getConfig() {
        return config;
    }

    @Override
    public String name() {
        return "test";
    }

    @Override
    public void watch(long confToken, A6Conf a6Conf) {
        config = a6Conf.getConfig();
    }
}
