package org.apache.apisix.plugin.runner;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

@Component
public class YAMLConfig {
    @Value("${plugin-directory-absolute-path}")
    private String path;

    @Value("${filter-package-name}")
    private String packageName;

    public String getPath() {
        return path;
    }

    public String getPackageName() {
        return packageName;
    }
}
