## 快速开始

### 准备工作

* JDK 8
* APISIX 2.10.0
* Clone the [apisix-java-plugin-runner](https://github.com/apache/apisix-java-plugin-runner) project。

### 开发扩展插件过滤器

在 [runner-plugin](https://github.com/apache/apisix-java-plugin-runner/tree/main/runner-plugin/src/main/java/org/apache/apisix/plugin/runner/filter) 模块的 `org.apache.apisix.plugin.runner.filter` 包下编写过滤器处理请求，过滤器要实现 `PluginFilter` 接口，参考 `apisix-runner-sample` 模块下的样例：
* 请求重写[RewriteRequestDemoFilter](https://github.com/apache/apisix-java-plugin-runner/blob/main/sample/src/main/java/org/apache/apisix/plugin/runner/filter/RewriteRequestDemoFilter.java)
* 请求拦截[StopRequestDemoFilter](https://github.com/apache/apisix-java-plugin-runner/blob/main/sample/src/main/java/org/apache/apisix/plugin/runner/filter/StopRequestDemoFilter.java)


```java
@Component
public class CheckTokenFilter implements PluginFilter {
    @Override
    public String name() {
        return "CheckTokenFilter";
    }

    @Override
    public Mono<Void> filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
        /*
         * todo your business here
         */

        
        return chain.filter(request, response);
    }
}
```

### 部署

apisix-java-plugin-runner 与 APISIX 用 `Unix Domain Socket` 进行进程间通讯，需要部署在同一个宿主环境。apisix-java-plugin-runner 的生命周期由 APISIX 管理，如果是容器化部署，apisix-java-plugin-runner 与 APISIX 必须部署在同一个容器中。

所以如果是容器部署就需要把apisix-java-plugin-runner 与 APISIX 生成在一个docker image里。
下面是如何构建包含 apisix-java-plugin-runner 与 APISIX 的容器镜像的步骤：

先构建 `apisix-java-plugin-runner` 的可执行 jar

```bash
./mvnw package
```

构建完成，你会在 `dist` 目录看见构建产物

```
apache-apisix-java-plugin-runner-0.1.0-bin.tar.gz
```

在`dist`目录添加`Dockerfile`文件

```dockerfile
FROM apache/apisix:2.10.0-alpine

RUN sed -i "s/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g" /etc/apk/repositories && apk add --no-cache openjdk8-jre

ADD apache-apisix-java-plugin-runner-0.1.0-bin.tar.gz /usr/local/

```

然后构建容器镜像

```shell
 cd dist
 docker build -t apache/apisix:2.10.0-alpine-with-java-plugin .
```

最后在 APISIX 的 `config.yaml` 文件中增加配置，如下

```yaml
ext-plugin:
  cmd: ['java', '-jar', '-Xmx4g', '-Xms4g', '/path/to/apisix-runner-bin/apisix-java-plugin-runner.jar']
```

构建完成的 `apache/apisix:2.10.0-alpine-with-java-plugin` 镜像内即包含 APISIX 与 apisix-java-plugun-runner。

### 使用插件

配置路由

```bash
$ curl http://127.0.0.1:9080/apisix/admin/routes/1 -H 'X-API-KEY: edd1c9f034335f136f87ad84b625c8f1' -X PUT -d '
{
  "uri": "/get",
  "name": "java-plugin",
  "plugins": {
    "ext-plugin-pre-req": {
      "conf": [
        {
          "name": "CheckTokenFilter",
          "value": "{\"body\":\"hello\"}"
        }
      ]
    }
  },
  "upstream": {
    "nodes": [
      {
        "host": "httpbin.org",
        "port": 80,
        "weight": 1
      }
    ],
    "type": "roundrobin"
  },
  "status": 1
}
```
