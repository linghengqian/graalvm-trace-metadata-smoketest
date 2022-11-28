# graalvm-trace-metadata-smoketest

- This warehouse is my personal temporary warehouse so that I can record the dynamics of a series of issues in time. All
  unit test designs should be submitted to https://github.com/oracle/graalvm-reachability-metadata as much as possible.

- https://github.com/linghengqian/graalvm-trace-metadata-smoketest/issues/1

# start demo

- In Windows 11, IntelliJ IDEA Snapcraft Version under WSLg.

```shell
sdk use java 22.3.r17-grl
gu install native-image
sudo apt-get install build-essential libz-dev zlib1g-dev -y
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
```

- exec.

```shell
./gradlew -Pagent clean test
./gradlew metadataCopy --task test
./gradlew clean nativeTest
```

- According to https://github.com/oracle/graalvm-reachability-metadata/pull/122#discussion_r1030139343, we need to
  remove `com.sun.management.**`, `sun.management.**`, `java. management.**` related reachability metadata.
