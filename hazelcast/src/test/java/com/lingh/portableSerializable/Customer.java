package com.lingh.portableSerializable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;
import java.util.Date;

public class Customer implements Portable {
    public static final int CLASS_ID = 1;
    public String name;
    public int id;
    public Date lastOrder;

    @Override
    public int getFactoryId() {
        return SamplePortableFactory.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt("id", id);
        writer.writeString("name", name);
        writer.writeLong("lastOrder", lastOrder.getTime());
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        id = reader.readInt("id");
        name = reader.readString("name");
        lastOrder = new Date(reader.readLong("lastOrder"));
    }
}