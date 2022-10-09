package org.apache.apisix.plugin.runner.filter;

import com.google.gson.Gson;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.PostRequest;
import org.apache.apisix.plugin.runner.PostResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PostReqWithVarsFilter implements PluginFilter {
    @Override
    public String name() {
        return "PostReqWithVarsFilter";
    }

    @Override
    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
        String configStr = request.getConfig(this);
        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<>();
        conf = gson.fromJson(configStr, conf.getClass());
        request.setPath((String) conf.get("rewrite_path"));
        chain.filter(request, response);
    }

    @Override
    public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
        String configStr = request.getConfig(this);
        Gson gson = new Gson();
        Map<String, Object> conf = new HashMap<>();
        conf = gson.fromJson(configStr, conf.getClass());
        String bodyStr = request.getBody();
        Map<String, Object> body = new HashMap<>();
        body = gson.fromJson(bodyStr, body.getClass());
        assert body.get("url").toString().endsWith((String) conf.get("rewrite_path"));
        String remoteAddr = request.getVars("remote_addr");
        response.setHeader("remote_addr", remoteAddr);
        chain.postFilter(request, response);
    }

    @Override
    public List<String> requiredVars() {
        List<String> vars = new ArrayList<>();
        vars.add("remote_addr");
        return vars;
    }

    @Override
    public Boolean requiredRespBody() {
        return true;
    }
}
