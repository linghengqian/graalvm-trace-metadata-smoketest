package com.lingh.customSerializer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomSerializer implements StreamSerializer<CustomSerializable> {
    @Override
    public int getTypeId() {
        return 10;
    }

    @Override
    public void destroy() {
    }
    @Override
    public void write(ObjectDataOutput out, CustomSerializable object) throws IOException {
        byte[] bytes = object.value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    @Override
    public CustomSerializable read(ObjectDataInput in) throws IOException {
        int len = in.readInt();
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new CustomSerializable(new String(bytes, StandardCharsets.UTF_8));
    }
}