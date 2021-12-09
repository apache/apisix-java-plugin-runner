### Quick Start

#### 一、准备工作

* JDK 8

* Clone the [apisix-java-plugin-runner](https://github.com/apache/apisix-java-plugin-runner) project.

#### 二、开发扩展插件过滤器

在`runner-plugin` 模块 `org.apache.apisix.plugin.runner.filter` 包下编写过滤器处理请求，过滤器要实现`PluginFilter` 接口，可以参考 `apisix-runner-sample` 模块下的样例，官方提供了两个样例还是很全面的，一个是请求重写[RewriteRequestDemoFilter](https://github.com/apache/apisix-java-plugin-runner/blob/main/sample/src/main/java/org/apache/apisix/plugin/runner/filter/RewriteRequestDemoFilter.java)，一个是请求拦截[StopRequestDemoFilter](https://github.com/apache/apisix-java-plugin-runner/blob/main/sample/src/main/java/org/apache/apisix/plugin/runner/filter/StopRequestDemoFilter.java)。

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

#### 三、部署

插件写好后怎么部署是关键，apisix-java-plugin-runner 与 APISIX 用 Unix Domain Socket 进行进程内通讯，
所以他们要部署在一个服务实例，并且APISIX启动的过程中会带着apisix-java-plugin-runner一起启动，如果是容器化部署就必须在一个容器里运行。

所以如果是容器部署就需要把apisix-java-plugin-runner 与 APISIX 生成在一个docker image里。

先打包`apisix-java-plugin-runner`

```bash
mvn package
```

打包完成，你会在`dist`目录看见打包文件

```
apache-apisix-java-plugin-runner-0.1.0-bin.tar.gz
```

在`dist`目录添加`dockerfile`文件

```dockerfile
FROM apache/apisix:2.10.0-alpine

RUN sed -i "s/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g" /etc/apk/repositories && apk add --no-cache openjdk8-jre

ADD aapache-apisix-java-plugin-runner-0.1.0-bin.tar.gz /usr/local/

```

然后运行`docker build`构建镜像

```shell
 cd dist
 docker build -t apache/apisix:2.10.0-alpine-with-java-plugin .
```

最后添加扩展插件配置到APISIX的 `config.yaml` 文件

```yaml
ext-plugin:
  cmd: ['java', '-jar', '-Xmx4g', '-Xms4g', '/path/to/apisix-runner-bin/apisix-java-plugin-runner.jar']
```

这样直接用新生成apache/apisix:2.10.0-alpine-with-java-plugin docker image替换原来的apache/apisix:2.10.0-alpine 部署就可以了。

#### 四、使用插件

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
