package com.lingh.identifiedDataSerializable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;

public class Employee implements IdentifiedDataSerializable {
    private static final int CLASS_ID = 100;
    public int id;
    public String name;

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readInt();
        name = in.readString();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(id);
        out.writeString(name);
    }

    @Override
    public int getFactoryId() {
        return SampleDataSerializableFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }
}