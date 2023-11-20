package org.apache.apisix.plugin.runner.db.model;

import lombok.*;

import java.io.*;
import java.util.*;

/**
 * user
 * @author 
 */
@Data
public class User implements Serializable {
    private Integer id;

    /**
     * wolf中的用户id
     */
    private Integer userid;

    /**
     * 公钥
     */
    private String publickey;

    /**
     * 私钥
     */
    private String privatekey;

    /**
     * 状态：1.正常 0.删除
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private Date gmtcreate;

    /**
     * 修改时间
     */
    private Date gmtmodified;

    private static final long serialVersionUID = 1L;
}