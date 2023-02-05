# graalvm-trace-metadata-smoketest

- This warehouse is my personal temporary warehouse so that I can record the dynamics of a series of issues in time. All
  unit test designs should be submitted to https://github.com/oracle/graalvm-reachability-metadata as much as possible.

- https://github.com/linghengqian/graalvm-trace-metadata-smoketest/issues/1

# Start nativeTest

- In Windows 11, Jetbrains IntelliJ IDEA Ultimate installed by Jetbrains Toolbox AppImage under WSLg.
- Note, when executing unit tests implemented by GraalVM Truffle, be sure to additionally use `sdk use java 17.0.6-ms`

```shell
cd /tmp
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 22.3.1.r17-grl
sdk install java 17.0.6-ms
sdk use java 22.3.1.r17-grl
gu install native-image
sudo apt-get install build-essential libz-dev zlib1g-dev -y
sdk install gradle
gu install js espresso

```

- select one.

```shell
cd ./io.jsonwebtoken/jjwt-jackson/
cd ./io.jsonwebtoken/jjwt-gson/
cd ./io.jsonwebtoken/jjwt-orgjson/
cd ./zstd-jni/
cd ./vertx-core/
cd ./groovy/
cd ./hazelcast/
cd ./ehcache/
cd ./org.apache.shardingsphere/shardingsphere-jdbc-core/
cd ./nashorn-core/
cd ./org.graalvm.js/js/
cd ./dynamic-datasource-spring-boot-starter/
cd ./org.apache.curator/curator-client/
cd ./org.apache.curator/curator-framework/
cd ./jetcd-core/
cd ./protobuf-java-util/
cd ./opengauss-jdbc/
cd ./cache-api/
cd ./elasticjob-lite-core/
cd ./caffeine/
cd ./transmittable-thread-local/
cd ./org.graalvm.espresso/polyglot/
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
  `ManagementFactory.getPlatformMBeanServer()` to fail with `javax.management.openmbean.OpenDataException`. In most cases,
  it would appear that the metadata is sufficient for the bootstrapping of the MBean server to take a different code path
  to normal and this path fails. There are reference operations in some PRs, such
  as https://github.com/oracle/graalvm-reachability-metadata/pull/113, https://github.com/oracle/graalvm-reachability-metadata/pull/161,
  https://github.com/oracle/graalvm-reachability-metadata/pull/162. This includes the following five packages.

```
java.lang.management.**
jdk.management.**
com.sun.management.**
sun.management.**
javax.management.**
```

- The main three commands submitted upstream.

```shell
./gradlew scaffold --coordinates io.jsonwebtoken:jjwt-jackson:0.11.5
./gradlew :spotlessApply
./gradlew clean test -Pcoordinates=io.jsonwebtoken:jjwt-jackson:0.11.5
```