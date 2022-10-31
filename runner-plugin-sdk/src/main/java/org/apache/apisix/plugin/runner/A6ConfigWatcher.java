package org.apache.apisix.plugin.runner;

public interface A6ConfigWatcher {
    /**
     * @return the name of config watcher
     */
    String name();

    /**
     * watch the change of the config
     *
     * @param confToken  the config token
     * @param a6Conf the config
     */
    default void watch(long confToken, A6Conf a6Conf) {
    }
}
