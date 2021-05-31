# How It Works

This article explains how apisix-java-plugin-runner collaborate with [Apache APISIX](https://apisix.apache.org) to run plugins written in java.

## Run Mode

apisix-java-plugin-runner can be run alone or bundled with Apache APISIX.
It depends on whether you need to debug it or run it.

### Debug

If you are developing a new plugin and need to debug the code, then you can run the main class
[PluginRunnerApplication](https://github.com/apache/apisix-java-plugin-runner/blob/main/runner-starter/src/main/java/org/apache/apisix/plugin/runner/PluginRunnerApplication.java),
and before start, you need to set the following two environment variables:

- APISIX_LISTEN_ADDRESS: apisix-java-plugin-runner and APISIX for inter-process communication (Unix Domain Socket) socket type file address.
  And do not need to actively create this file, apisix-java-plugin-runner will automatically create this file when it starts.
- APISIX_CONF_EXPIRE_TIME: the time that APISIX's configuration is cached in the apisix-java-plugin-runner process.

For example, if you start apisix-java-plugin-runner as a jar package, pass the environment variables as follows

```shell
java -jar -DAPISIX_LISTEN_ADDRESS=unix:/tmp/runner.sock -DAPISIX_CONF_EXPIRE_TIME=3600 /path/to/apisix-java-plugin-runner.jar
```

Note: Refer to [apisix-java-plugin-runner.jar](#run) to get it.

and add the following configure in the `config.yaml` file of APISIX

```yaml
ext-plugin:
  path_for_test: /tmp/runner.sock
```

The `/tmp/runner.sock` is the address of the file where apisix-java-plugin-runner
and APISIX communicate between processes and must be consistent.

Note: If you see some error logs like

```
phase_func(): failed to connect to the unix socket unix:/tmp/runner.sock: permission denied
```

in the `error.log` of APISIX, you can change the permissions of this file for debug, execute commands like

```shell
chmod 777 /tmp/runner.sock
```

### Run

No environment variables need to be set in Run mode, execute

```shell
cd /path/to/apisix-java-plugin-runner
 ./mvnw package
```

to built apisix-java-plugin-runner as a jar package, then you will see the `dist` directory, execute

```
cd dist
tar -zxvf apache-apisix-runner-bin.tar.gz
```

file's layout in the `dist` directory are as follows

```
dist
├── apache-apisix-runner-bin.tar.gz
└── apisix-runner-bin
    ├── apisix-java-plugin-runner.jar
    ├── bin
    │   ├── shutdown.sh
    │   └── startup.sh
    ├── LICENSE
    ├── NOTICE
    └── README.md

```

then add the following configure in the `config.yaml` file of APISIX

```yaml
ext-plugin:
  cmd: ['java', '-jar', '-Xmx4g', '-Xms4g', '/path/to/apisix-runner-bin/apisix-java-plugin-runner.jar']
```
