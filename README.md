# graalvm-trace-metadata-smoketest

- This warehouse is my personal temporary warehouse so that I can record the dynamics of a series of issues in time. All
  unit test designs should be submitted to https://github.com/oracle/graalvm-reachability-metadata as much as possible.

- `org.apache.shardingsphere:shardingsphere-proxy-bootstrap:5.2.1`,
  at https://github.com/apache/shardingsphere/issues/21347 .
- `com.github.luben:zstd-jni:1.5.2`, at https://github.com/luben/zstd-jni/issues/115 .
- `io.jsonwebtoken:jjwt-api:0.11.5`, at https://github.com/jwtk/jjwt/issues/637 .
- `io.jsonwebtoken:jjwt-impl:0.11.5`, at https://github.com/jwtk/jjwt/issues/637 .
- `io.jsonwebtoken:jjwt-jackson:0.11.5`, at https://github.com/jwtk/jjwt/issues/637 .
- `io.jsonwebtoken:jjwt-gson:0.11.5`, at https://github.com/jwtk/jjwt/issues/637 .
- `io.jsonwebtoken:jjwt-orgjson:0.11.5`, at https://github.com/jwtk/jjwt/issues/637 .
- `org.apache.skywalking:java-agent:8.13.0`, at https://github.com/apache/skywalking/discussions/7640 .
- `org.ehcache:ehcache:3.10.4`, at https://github.com/ehcache/ehcache3/issues/2992 .
- `io.vertx:vertx-core:4.3.3`( Or `io.vertx:vertx-core:4.3.4` ?) ,at https://github.com/netty/netty/issues/11369 .

# start demo

```shell
sdk use java 22.3.r17-grl
gu install native-image
sudo apt-get install build-essential libz-dev zlib1g-dev -y
```

```shell
cd ./jjwt-jackson/
./gradlew -Pagent clean test
./gradlew metadataCopy --task test --dir src/main/resources/META-INF/native-image/io.jsonwebtoken/jjwt-jackson/0.11.5
```

```shell
cd ./jjwt-gson/
./gradlew -Pagent clean test
./gradlew metadataCopy --task test --dir src/main/resources/META-INF/native-image/io.jsonwebtoken/jjwt-gson/0.11.5
```

```shell
cd ./jjwt-orgjson/
./gradlew -Pagent clean test
./gradlew metadataCopy --task test --dir src/main/resources/META-INF/native-image/io.jsonwebtoken/jjwt-orgjson/0.11.5
```

```shell
cd ./zstd-jni/
sdk install scala 2.13.10
./gradlew -Pagent clean test
./gradlew metadataCopy --task test --dir src/main/resources/META-INF/native-image/com.github.luben/zstd-jni/1.5.2-5
```

- According to https://central.sonatype.dev/artifact/com.github.luben/zstd-jni/1.5.2-5/dependencies
  , `com.github.luben:zstd-jni:1.5.2-5` uses scala 2.13.
- A weird test issue with `ZstdSpec.scala` I added a TODO.