# build
first install the localfix jar:
```shell
cd runner-code
mvn install -DskipTests -Dgpg.skip=true

cd runner-plugin-sdk
mvn install -DskipTests -Dgpg.skip=true

mvn package task in the apisix java runner starter module.

```


# This project requires 3.6.0

https://github.com/apache/apisix/pull/9990




# require db information
```sql
CREATE DATABASE apigateway;
CREATE TABLE apigateway.user
(
    id          int auto_increment primary key                     not null,
    userid      int unique                          not null COMMENT 'wolf中的用户id',
    publickey   varchar(1024)                       not null COMMENT '公钥',
    privatekey  varchar(1024)                       not null COMMENT '私钥',
    status      tinyint(1) default 1 not null comment '状态：1.正常 0.删除',
    gmtcreate   timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    gmtmodified timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间'
)
```

# generate the encryption key pair
[MySmUtilTest.java](./runner-starer/src/test/java/cn/sichuancredit/apigateway/encryption/MySmUtilTest.java)

# how it works
https://github.com/apache/apisix-java-plugin-runner/blob/main/docs/en/latest/how-it-works.md
