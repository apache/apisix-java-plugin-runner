package org.apache.apisix.plugin.runner.filter;

import com.google.gson.Gson;
import org.apache.apisix.plugin.runner.PostRequest;
import org.apache.apisix.plugin.runner.PostResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseFilter implements PluginFilter {
    @Override
    public String name() {
        return "ResponseFilter";
    }

    @Override
    public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
        String configStr = request.getConfig(this);
        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<>();
        conf = gson.fromJson(configStr, conf.getClass());

        Map<String, String> headers = request.getUpstreamHeaders();
        String contentType = headers.get("Content-Type");
        Integer upstreamStatusCode = request.getUpstreamStatusCode();

        response.setStatusCode(Double.valueOf(conf.get("response_code").toString()).intValue());
        response.setBody((String) conf.get("response_body"));
        response.setHeader((String) conf.get("response_header_name"), (String) conf.get("response_header_value"));
        chain.postFilter(request, response);
    }
}
