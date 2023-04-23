# graalvm-trace-metadata-smoketest

- This warehouse is my personal temporary warehouse so that I can record the dynamics of a series of issues in time. All
  unit test designs should be submitted to https://github.com/oracle/graalvm-reachability-metadata as much as possible.

- Refer to https://github.com/linghengqian/graalvm-trace-metadata-smoketest/issues/1 

# Start nativeTest

- In Windows 11, Jetbrains IntelliJ IDEA Ultimate installed by Jetbrains Toolbox AppImage under WSLg.
- Note, when executing unit tests implemented by GraalVM Truffle, be sure to additionally use `sdk use java 17.0.6-ms`
- Maybe you need to execute `su root` to switch to the root user to install grape, because it needs to write files
  to `/usr/local/bin`.

```shell
cd /tmp
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 22.3.1.r17-grl
sdk install java 17.0.6-ms
sdk use java 22.3.1.r17-grl
gu install native-image js espresso
sudo apt-get install build-essential libz-dev zlib1g-dev -y
sdk install gradle

curl -sSfL https://raw.githubusercontent.com/anchore/grype/main/install.sh | sh -s -- -b /usr/local/bin
```

- select one.

```shell
cd ./io.jsonwebtoken/jjwt-jackson/
cd ./io.jsonwebtoken/jjwt-gson/
cd ./io.jsonwebtoken/jjwt-orgjson/
cd ./zstd-jni/
cd ./vertx-core/
cd ./org.apache/groovy/
cd ./hazelcast/
cd ./ehcache/
cd ./org.apache/shardingsphere-jdbc-core/
cd ./nashorn-core/
cd ./org.graalvm/js/
cd ./dynamic-datasource-spring-boot-starter/
cd ./org.apache/curator-client/
cd ./org.apache/curator-framework/
cd ./org.apache/curator-recipes/
cd ./jetcd-core/
cd ./protobuf-java-util/
cd ./opengauss-jdbc/
cd ./cache-api/
cd ./org.apache/elasticjob-lite-core/
cd ./caffeine/
cd ./transmittable-thread-local/
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

- The main three commands submitted upstream in https://github.com/oracle/graalvm-reachability-metadata.

```shell
./gradlew scaffold --coordinates io.jsonwebtoken:jjwt-jackson:0.11.5
./gradlew :spotlessApply
./gradlew clean test -Pcoordinates=io.jsonwebtoken:jjwt-jackson:0.11.5
```
