# set maven opts
it requires at least jdk11.
```cmd
setx JAVA_HOME "D:\Program Files\Amazon Corretto\jdk11.0.17_8"
set MAVEN_OPTS="-Dmaven.compiler.fork=true -Dmaven.compiler.executable=D:\Program Files\Amazon Corretto\jdk11.0.17_8\bin\javac"
```

# skip test


# build without test
```cmd
.\mvnw.cmd package -DskipTests -Dcheckstyle.skip
```