# graalvm-trace-metadata-smoketest

- This warehouse is my personal temporary warehouse so that I can record the dynamics of a series of issues in time. All
  unit test designs should be submitted to https://github.com/oracle/graalvm-reachability-metadata as much as possible.

- Refer to https://github.com/linghengqian/graalvm-trace-metadata-smoketest/issues.

# Start nativeTest

- In Windows 11, Jetbrains IntelliJ IDEA Ultimate installed by Jetbrains Toolbox AppImage under WSLg.
- Note, when executing unit tests implemented by GraalVM Truffle, be sure to additionally use `sdk use java 17.0.7-ms`
- Maybe you need to execute `su root` to switch to the root user to install grape, because it needs to write files
  to `/usr/local/bin`.

```shell
cd /tmp
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.8-graalce
sdk install java 17.0.8-ms
sdk use java 17.0.8-graalce
gu install js espresso
sudo apt-get install build-essential libz-dev zlib1g-dev -y
sdk install gradle

curl -sSfL https://raw.githubusercontent.com/anchore/grype/main/install.sh | sh -s -- -b /usr/local/bin
```

- select one.

```shell
cd ./io.jsonwebtoken/jjwt-jackson/
cd ./io.jsonwebtoken/jjwt-gson/
cd ./io.jsonwebtoken/jjwt-orgjson/
cd ./com.github/zstd-jni/
cd ./io.vertx/vertx-core/
cd ./org.apache/groovy/
cd ./com.hazelcast/hazelcast/
cd ./org.ehcache/ehcache/
cd ./org.apache/shardingsphere-jdbc-core/
cd ./org.openjdk/nashorn-core/
cd ./org.graalvm/js/
cd ./com.baomidou/dynamic-datasource-spring-boot-starter/
cd ./org.apache/curator-client/
cd ./org.apache/curator-framework/
cd ./org.apache/curator-recipes/
cd ./io.etcd/jetcd-core/
cd ./com.google/protobuf-java-util/
cd ./org.opengauss/opengauss-jdbc/
cd ./javax.cache/cache-api/
cd ./org.apache/elasticjob-lite-core/
cd ./com.github/caffeine/
cd ./com.alibaba/transmittable-thread-local/
cd ./org.graalvm/polyglot/
cd ./org.apache/commons-dbcp2/
cd ./org.apache/shardingsphere-db-protocol-core/
cd ./org.apache/shardingsphere-mysql-protocol/
cd ./org.apache/shardingsphere-postgresql-protocol/
cd ./org.apache/shardingsphere-opengauss-protocol/
cd ./org.apache/zookeeper/
```

- exec.

```shell
gradle wrapper
./gradlew -Pagent clean test
./gradlew metadataCopy --task test
./gradlew clean nativeTest
```

### Extra tips

- All submitted GraalVM reachability metadata, if the MBean-related part is unnecessary, should actively remove the
  GraalVM reachability metadata related to the MBean. These metadata can cause calls to
  `ManagementFactory.getPlatformMBeanServer()` to fail with `javax.management.openmbean.OpenDataException`. In most
  cases, it would appear that the metadata is sufficient for the bootstrapping of the MBean server to take a different code
  path to normal and this path fails. There are reference operations in some PRs, such
  as https://github.com/oracle/graalvm-reachability-metadata/pull/113, https://github.com/oracle/graalvm-reachability-metadata/pull/161,
  https://github.com/oracle/graalvm-reachability-metadata/pull/162. This includes the following five packages.

```
java.lang.management.**
jdk.management.**
com.sun.management.**
sun.management.**
javax.management.**
```

- Except `java.util.properties`, most JSON entries for `java.**`, `sun.misc.**` and `sun.security.**` can be deleted.

- In `extra-filter.json` it is shown as follows.
```json
{
  "rules": [
    {"includeClasses": "**"},
    {"excludeClasses": "java.lang.management.**"},
    {"excludeClasses": "jdk.management.**"},
    {"excludeClasses": "com.sun.management.**"},
    {"excludeClasses": "sun.management.**"},
    {"excludeClasses": "javax.management.**"},
    {"excludeClasses": "java.**"},
    {"includeClasses": "java.util.Properties"}
  ],
  "regexRules": []
}
```

- The main three commands submitted upstream in https://github.com/oracle/graalvm-reachability-metadata.

```shell
./gradlew scaffold --coordinates org.apache.commons:commons-dbcp2:2.9.0
# `./gradlew check` too slow? use `./gradlew :spotlessApply`.
./gradlew check
./gradlew clean test -Pcoordinates=org.apache.commons:commons-dbcp2:2.9.0
```
