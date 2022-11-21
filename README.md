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
sdk install scala 2.13.10
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
```
- exec.
```shell
./gradlew -Pagent clean test
./gradlew metadataCopy --task test
./gradlew -Pagent nativeTest
```

- According to https://central.sonatype.dev/artifact/com.github.luben/zstd-jni/1.5.2-5/dependencies
  , `com.github.luben:zstd-jni:1.5.2-5` uses scala 2.13.
- A weird test issue with `ZstdSpec.scala` I added a TODO.
