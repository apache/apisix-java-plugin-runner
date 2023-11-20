package org.apache.apisix.plugin.runner.filter;

import org.apache.apisix.plugin.runner.*;
import org.apache.apisix.plugin.runner.db.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.nio.charset.*;
import java.util.*;

@Component
public class EncryptResponseFilter implements PluginFilter {
    private final Logger logger = LoggerFactory.getLogger(EncryptResponseFilter.class);

    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    @Override
    public String name() {
        return "EncryptResponseFilter";
    }

    @Override
    public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
        List<String> userIds = request.getUpstreamHeaders().get(Constants.HEADER_USER_ID);
        String userId = null;
        if (CollectionUtils.isEmpty(userIds)) {
            logger.warn("No user found in request:{}", request.getUpstreamHeaders());
        } else {
            userId = userIds.get(0);
        }

        User user = userService.tryFindUser(userId);
        if (user == null) {
            response.setStatusCode(403);
            response.setBody(Constants.ERROR_NOT_FOUND);
            logger.warn("not found the user, maybe disabled. wolfuserid: {}", userId);
        } else {
            String encryptedBody = userService.encryptBody(request.getBody(Charset.forName("UTF-8")), user);
            response.setBody(encryptedBody);
            response.setHeader(Constants.HEADER_RESPONSEBODY_ENCRYPTED_FLAG, "true");
            response.setStatusCode(request.getUpstreamStatusCode());
            logger.info("EncryptResponseFilter success：user(wolf)：{}，{}", user.getUserid(), encryptedBody);
        }

        chain.postFilter(request, response);
    }


    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredRespBody() {
        return true;
    }

    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredBody() {
        return true;
    }
}
