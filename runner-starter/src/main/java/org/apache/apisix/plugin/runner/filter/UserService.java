package org.apache.apisix.plugin.runner.filter;

import cn.sichuancredit.apigateway.encryption.*;
import com.alibaba.fastjson.*;
import com.google.common.base.*;
import org.apache.apisix.plugin.runner.db.*;
import org.apache.apisix.plugin.runner.db.model.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserDao userDao;

    public User tryFindUser(String userIdValue) {
        logger.info("输入wolf userid:{}", userIdValue);
        User user = null;
        if (userIdValue == null || userIdValue.trim().isEmpty()) {
            logger.warn("header中未找到用");
        } else {
            try {
                user = userDao.selectByWolfUserId(Integer.valueOf(userIdValue));
            } catch (Exception e) {
                logger.warn("无法获取用户：{}", userIdValue, e);
            }
            if (user == null) {
                logger.warn("未找到用户：{}", userIdValue);
            }
        }

        return user;
    }

    public String decryptBody(String body, User user) {
        Preconditions.checkNotNull(user);
        if (StringUtils.isEmpty(body)) {
            return body;
        }
        logger.info("decryptBody:{}, wolfuser:{}", body, user.getUserid());
        EncryptedData data = JSONObject.parseObject(body, EncryptedData.class);
        Preconditions.checkNotNull(data.getData(), "加密数据为NULL");
        Preconditions.checkNotNull(data.getEncryptKey(), "加密数据key为NULL");
        String sm4Key = MySmUtil.sm2Decrypt(data.getEncryptKey(), user.getPrivatekey());
        return MySmUtil.sm4Decrypt(data.getData(), sm4Key);
    }

    public String encryptBody(String body, User user) {
        Preconditions.checkNotNull(user);
        if (StringUtils.isEmpty(body)) {
            return body;
        }
        // 随机生成Sm4密钥
        String sm4Key = MySmUtil.generateSm4Key();
        // 国密Sm2公钥加密Sm4秘钥
        String encryptKey = MySmUtil.sm2Encrypt(sm4Key, user.getPublickey());
        // Sm4加密传输数据
        String data = MySmUtil.sm4Encrypt(body, sm4Key);
        EncryptedData encryptedData = new EncryptedData();
        encryptedData.setData(data);
        encryptedData.setEncryptKey(encryptKey);

        logger.info("encryptBody:{}, wolfuser:{}", data, user.getUserid());
        return JSONObject.toJSONString(encryptedData);
    }
}
