package cn.sichuancredit.apigateway.encryption;

import lombok.*;

import java.io.*;

/**
 * 国密 Sm2 非对称加密密钥对(String 格式)
 *
 */
@Data
public class Sm2KeyPair implements Serializable {
    private static final long serialVersionUID = 1L;
    private String publicKey;

    private String privateKey;
}
