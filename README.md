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

```shell
cd ./jjwt-jackson/
./gradlew -Pagent clean test
./gradlew metadataCopy --task test
```

```shell
cd ./jjwt-gson/
./gradlew -Pagent clean test
./gradlew metadataCopy --task test
```

```shell
cd ./jjwt-orgjson/
./gradlew -Pagent clean test
./gradlew metadataCopy --task test
```

```shell
cd ./zstd-jni/
sdk install scala 2.13.10
./gradlew -Pagent clean test
./gradlew metadataCopy --task test
```

- According to https://central.sonatype.dev/artifact/com.github.luben/zstd-jni/1.5.2-5/dependencies
  , `com.github.luben:zstd-jni:1.5.2-5` uses scala 2.13.
- A weird test issue with `ZstdSpec.scala` I added a TODO.

```shell
cd ./vertx-core/
./gradlew -Pagent clean test
./gradlew metadataCopy --task test
```