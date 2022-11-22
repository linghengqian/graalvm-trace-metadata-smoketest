package com.lingh;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

class ThePortableFactory implements PortableFactory {
    public static final int FACTORY_ID = 1;

    ThePortableFactory() {
    }

    @Override
    public Portable create(int classId) {
        return classId == User.CLASS_ID ? new User() : null;
    }
}