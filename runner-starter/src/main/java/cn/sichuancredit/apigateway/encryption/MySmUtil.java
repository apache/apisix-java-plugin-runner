package cn.sichuancredit.apigateway.encryption;

import cn.hutool.core.codec.*;
import cn.hutool.core.util.*;
import cn.hutool.crypto.*;
import cn.hutool.crypto.asymmetric.*;
import cn.hutool.crypto.symmetric.*;
import org.apache.commons.lang3.*;

import java.security.*;

/**
 * 国密 SM2 的非对称加解密和 SM4 的对称加密工具类
 *
 */
public class MySmUtil {
    /**
     * 生成 SM2 非对称公钥/私钥对
     *
     * @return 加密的公钥和解密的私钥
     */
    public static Sm2KeyPair generateSm2KeyPair() {
        KeyPair keyPair = SecureUtil.generateKeyPair("SM2");
        String privateKey = Base64.encode(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.encode(keyPair.getPublic().getEncoded());

        Sm2KeyPair sm2KeyPair = new Sm2KeyPair();
        sm2KeyPair.setPrivateKey(privateKey);
        sm2KeyPair.setPublicKey(publicKey);

        return sm2KeyPair;
    }

    /**
     * SM2 非对称加密
     *
     * @param content   原文文本
     * @param publicKey 公钥
     * @return 加密密文文本
     */
    public static String sm2Encrypt(String content, String publicKey) {
        if (StringUtils.isAnyEmpty(content, publicKey)) {
            throw new IllegalArgumentException("文本和密钥不可为空！");
        }
        SM2 sm2Encrypt = new SM2(null, publicKey);
        byte[] encryptBytes = sm2Encrypt.encrypt(content, KeyType.PublicKey);
        return Base64.encode(encryptBytes);
    }

    /**
     * SM2 非对称加密
     *
     * @param content   原文文本
     * @param publicKey 公钥
     * @return 加密密文文本
     */
    public static String sm2Encrypt(String content, Sm2KeyPair publicKey) {
        return sm2Encrypt(content, publicKey.getPublicKey());
    }

    /**
     * SM2 非对称解密
     *
     * @param ciphertext 密文文本
     * @param privateKey 私钥
     * @return 解密出的原文
     */
    public static String sm2Decrypt(String ciphertext, String privateKey) {
        if (StringUtils.isAnyEmpty(ciphertext, privateKey)) {
            throw new IllegalArgumentException("密文和密钥不可为空！");
        }
        SM2 sm2Decrypt = new SM2(privateKey, null);
        byte[] decryptBytes = sm2Decrypt.decrypt(ciphertext, KeyType.PrivateKey);
        return StrUtil.utf8Str(decryptBytes);
    }

    /**
     * SM2 非对称解密
     *
     * @param ciphertext 密文文本
     * @param privateKey 私钥
     * @return 解密出的原文
     */
    public static String sm2Decrypt(String ciphertext, Sm2KeyPair privateKey) {
        return sm2Decrypt(ciphertext, privateKey.getPrivateKey());
    }

    /**
     * 生成 SM4 对称加解密密钥
     *
     * @return SM4 对称加解密密钥
     */
    public static String generateSm4Key() {
        SymmetricCrypto sm4 = SmUtil.sm4();
        return Base64.encode(sm4.getSecretKey().getEncoded());
    }

    /**
     * SM4 对称加密(UTF-8)
     *
     * @param content 原文文本内容
     * @param sm4Key  密钥
     * @return 加密密文
     */
    public static String sm4Encrypt(String content, String sm4Key) {
        if (StringUtils.isAnyEmpty(content, sm4Key)) {
            throw new IllegalArgumentException("文本内容和密钥不可为空！");
        }
        SymmetricCrypto sm4 = SmUtil.sm4(Base64.decode(sm4Key));
        return sm4.encryptBase64(content);
    }

    /**
     * SM4 对称解密(UTF-8)
     *
     * @param ciphertext 密文文本
     * @param sm4Key     密钥
     * @return 加密密文
     */
    public static String sm4Decrypt(String ciphertext, String sm4Key) {
        if (StringUtils.isAnyEmpty(ciphertext, sm4Key)) {
            throw new IllegalArgumentException("密文内容和密钥不可为空！");
        }
        SymmetricCrypto sm4 = SmUtil.sm4(Base64.decode(sm4Key));
        return sm4.decryptStr(ciphertext, CharsetUtil.CHARSET_UTF_8);
    }
}