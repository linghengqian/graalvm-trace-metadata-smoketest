# graalvm-trace-metadata-smoketest

- This warehouse is my personal temporary warehouse so that I can record the dynamics of a series of issues in time. All
  unit test designs should be submitted to https://github.com/oracle/graalvm-reachability-metadata as much as possible.

- https://github.com/linghengqian/graalvm-trace-metadata-smoketest/issues/1

# Start nativeTest

- In Windows 11, Jetbrains IntelliJ IDEA Ultimate installed by Jetbrains Toolbox AppImage under WSLg. 

```shell
cd /tmp
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 22.3.r17-grl
sdk use java 22.3.r17-grl
gu install native-image
sudo apt-get install build-essential libz-dev zlib1g-dev -y
sdk install gradle
gu install js espresso

```

- select one.

```shell
cd ./jjwt-jackson/
cd ./jjwt-gson/
cd ./jjwt-orgjson/
cd ./zstd-jni/
cd ./vertx-core/
cd ./groovy/
cd ./hazelcast/
cd ./ehcache/
cd ./shardingsphere-jdbc-core/
cd ./nashorn-core/
cd ./js/
cd ./dynamic-datasource-spring-boot-starter/
cd ./curator-client/
cd ./jetcd-core/
cd ./protobuf-java-util/
cd ./opengauss-jdbc/
cd ./cache-api/
cd ./elasticjob-lite-core/
cd ./caffeine/
cd ./transmittable-thread-local/
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
  GraalVM reachability metadata related to the MBean, because this part of the metadata can always make the boot process
  of the MBean server, using a different code path from the normal flow, which often leads to unreasonable nativeTest
  results. This includes the following five packages.
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