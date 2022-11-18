package com.lingh.net.stream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class Batch {
    private final char type;
    private final Buffer buffer;

    public Batch(JsonObject jsonObject) {
        this.buffer = jsonObject.toBuffer();
        this.type = 'O';
    }

    public Batch(JsonArray jsonArray) {
        this.buffer = jsonArray.toBuffer();
        this.type = 'A';
    }

    public Batch(Buffer buffer) {
        this.buffer = buffer;
        this.type = 'B';
    }

    public boolean isArray() {
        return type == 'A';
    }

    public boolean isObject() {
        return type == 'O';
    }

    public boolean isBuffer() {
        return type == 'B';
    }

    public char getType() {
        return type;
    }

    public Buffer getBuffer() {
        return isBuffer() ? buffer : null;
    }

    public JsonObject getJsonObject() {
        return isObject() ? buffer.toJsonObject() : null;
    }

    public JsonArray getJsonArray() {
        return isArray() ? buffer.toJsonArray() : null;
    }

    public Buffer getRaw() {
        return buffer;
    }

}
