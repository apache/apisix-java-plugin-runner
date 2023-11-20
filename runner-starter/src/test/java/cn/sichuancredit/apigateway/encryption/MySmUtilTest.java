package cn.sichuancredit.apigateway.encryption;

import cn.hutool.core.io.*;
import com.alibaba.fastjson.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.*;

public class MySmUtilTest {

    @Test
    public void test() {

        // Sm2KeyPair k = MySmUtil.generateSm2KeyPair();

        String privKey = FileUtil.readString(new File("D:\\temp\\priv.txt"), Charset.forName("UTF-8")).trim();
        String pubKey = FileUtil.readString(new File("D:\\temp\\pub.txt"), Charset.forName("UTF-8")).trim();

        String rawData = "{\"hello\": \"world\"}";

        // 随机生成Sm4密钥
        String sm4Key = MySmUtil.generateSm4Key();
        // 国密Sm2公钥加密Sm4秘钥
        String encryptKey = MySmUtil.sm2Encrypt(sm4Key, pubKey);
        // Sm4加密传输数据
        String data = MySmUtil.sm4Encrypt(rawData, sm4Key);
        // 请求参数map
        JSONObject encryptParam = new JSONObject();
        encryptParam.put("encryptKey", encryptKey);
        encryptParam.put("data", data);

        String responseData = encryptParam.toString();
        System.out.println("加密后："  + responseData);

        // 解密：
        String encryptKey2 = encryptParam.getString("encryptKey");
        String data2 = encryptParam.getString("data");
        String sm4Key2 = MySmUtil.sm2Decrypt(encryptKey2, privKey);
        // 国密Sm4解密返回的数据
        String dataStr = MySmUtil.sm4Decrypt(data2, sm4Key2);
        System.out.println("Sm解密数据为: " + dataStr);
    }
}
