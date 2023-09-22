package org.apache.apisix.plugin.runner;

import io.github.api7.A6.HTTPRespCall.*;
import org.apache.apisix.plugin.runner.filter.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

import java.lang.reflect.*;
import java.util.*;

@Component
public class EncryptResponseFilter implements PluginFilter {
    private final Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);

    @Override
    public String name() {
        return "EncryptResponseFilter";
    }

    @Override
    public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
        String configStr = request.getConfig(this);
        logger.info("doing post filter...{}", configStr);


        Map<String, String> headers = request.getUpstreamHeaders();
        String contentType = headers.get("Content-Type");
        Integer upstreamStatusCode = request.getUpstreamStatusCode();
        logger.info("req headers: {}", headers);
        logger.info("req headers map:{}", request.getUpstreamHeadersMap());
        // response.setStatusCode(Double.valueOf(conf.get("response_code").toString()).intValue());
        response.setBody("This is new body");
        response.setHeader("X-header-y", "X-header-y");
        chain.postFilter(request, response);
        logger.info("完成替换。");
    }

    private void accessHeaders(PostRequest req) {
        try {
            Field reqField = PostRequest.class.getDeclaredField("req");
            reqField.setAccessible(true);
            Req r = (Req) reqField.get(req);
            for (int i = 0; i < r.headersLength(); i++) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
