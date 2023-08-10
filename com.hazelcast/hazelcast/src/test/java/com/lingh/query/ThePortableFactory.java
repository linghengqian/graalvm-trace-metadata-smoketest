package com.lingh.query;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class ThePortableFactory implements PortableFactory {
    public static final int FACTORY_ID = 1;

    @Override
    public Portable create(int classId) {
        return classId == User.CLASS_ID ? new User() : null;
    }
}