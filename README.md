apisix-java-plugin-runner
=================

Runs [Apache APISIX](http://apisix.apache.org/) plugins written in Java.
Implemented as a sidecar that accompanies APISIX.

![apisix-java-plugin-runner-overview](./docs/assets/images/apisix-java-plugin-runner-overview.png)

Status
------

This project is currently considered experimental.

Why apisix-java-plugin-runner
---------------------

APISIX offers many full-featured plugins covering areas such as authentication,
security, traffic control, serverless, analytics & monitoring, transformations, logging.

It also provides highly extensible API, allowing common phases to be mounted,
and users can use these api to develop their own plugins.

this project is APISIX Java side implementation that supports writing plugins in java.

Version Matrix
-------------

| apisix-java-plugin-runner | APISIX                                                                      |
|---------------------------|-----------------------------------------------------------------------------|
| 0.1.0                     | >= [2.7.0](https://github.com/apache/apisix/blob/master/CHANGELOG.md#270)   |
| 0.2.0                     | >= [2.12.0](https://github.com/apache/apisix/blob/master/CHANGELOG.md#2102) |

How it Works
-------------

See [How it Works](./docs/en/latest/how-it-works.md) to learn how apisix-java-plugin-runner collaborate
with APISIX to run plugins written in java.

The Internal of apisix-java-plugin-runner
---------------------------------

If you're interested in the internal of apisix-java-plugin-runner, we recommend you
to read the [the-internal-of-apisix-java-plugin-runner](./docs/en/latest/the-internal-of-apisix-java-plugin-runner.md),
it explains the details of communication and protocol conversion with APISIX.

Get Involved in Development
---------------------------

Welcome to make contributions, but before you start, please check out
[development.md](./docs/en/latest/development.md) to learn how to run and debug apisix-java-plugin-runner
in your own environment.

<<<<<<< HEAD
Installing Plugin from Maven Central Repository
----------------------------------------

Follow link to https://search.maven.org/artifact/io.github.ericluoliu/apisix-runner-bin-dist/0.4.0/jar. Download tar.gz. Follow the path that contains the apisix-runner-bin-dist-0.4.0.tar.gz file, and tar the file.

```
cd path/to/apisix-runner-bin-dist-0.4.0.tar.gz
tar -zxvf apisix-runner-bin-dist-0.4.0.tar.gz
```
A new directory called `apisix-runner-bin` will be created.

```
cd apisix-runner-bin
```
You will find a JAR file called apisix-java-plugin-runner.jar. To use this JAR in an APISIX process, add the following configuration in the `config.yaml` file of APISIX

```yaml
ext-plugin:
  cmd: ['java', '-jar', '-Xmx4g', '-Xms4g', '/path/to/apisix-runner-bin/apisix-java-plugin-runner.jar']
```



=======
>>>>>>> parent of 144c1bb (Update README.md)
License
-------

[Apache 2.0 LICENSE](./LICENSE)