package com.secrething.tools.common.utils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import static com.dyuproject.protostuff.LinkedBuffer.allocate;

/**
 * Created by liuzengzeng on 2017/12/10.
 */
public class SerializeUtil {


    public static <T> byte[] serialize(T t) {
        try {
            Assert.notNull(t);
            Class<T> clazz = (Class<T>) t.getClass();

            RuntimeSchema<T> schema = RuntimeSchema.createFrom(clazz);
            return ProtostuffIOUtil.toByteArray(t, schema, allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T deserialize(byte[] bs, Class<T> clzz) {
        try {
            Assert.notNull(bs);
            Assert.notNull(clzz);
            RuntimeSchema<T> schema = RuntimeSchema.createFrom(clzz);
            T t = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(bs, t, schema);
            return t;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
