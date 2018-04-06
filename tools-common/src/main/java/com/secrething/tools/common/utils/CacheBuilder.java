package com.secrething.tools.common.utils;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

/**
 * Created by liuzz on 2018/4/6.
 */
public final class CacheBuilder {
    private CacheBuilder() {
        throw new UnsupportedOperationException("can not be created");
    }

    public static <K, V> LoadingCache<K, V> build(long maxSize, long expireAfterWrite, TimeUnit afterWriteUnit, long expireAfterAccess, TimeUnit afterAccessUnit, CacheLoader<K, V> loader) {

        com.google.common.cache.CacheBuilder builder = com.google.common.cache.CacheBuilder.newBuilder();
        if (maxSize < 0)
            maxSize = 1000;
        builder.maximumSize(maxSize);
        if (expireAfterWrite >= 0 && null != afterWriteUnit)
            builder.expireAfterWrite(expireAfterWrite, afterWriteUnit);
        if (expireAfterAccess >= 0 && null != afterAccessUnit)
            builder.expireAfterAccess(expireAfterAccess, afterAccessUnit);
        if (null == loader)
            loader = new CacheLoader<K, V>() {
                @Override
                public V load(K key) throws Exception {
                    return null;
                }
            };
        return builder.build(loader);
    }

    public static <K, V> LoadingCache<K, V> build(long maxSize, long expireAfterWrite, TimeUnit afterWriteUnit, long expireAfterAccess, TimeUnit afterAccessUnit) {
        return build(maxSize, expireAfterWrite, afterWriteUnit, expireAfterAccess, afterAccessUnit, null);
    }

    public static <K, V> LoadingCache<K, V> build(long maxSize, long expireAfterWrite, TimeUnit afterWriteUnit, CacheLoader<K, V> loader) {
        Assert.isTrue(expireAfterWrite >= 0);
        Assert.notNull(afterWriteUnit);
        return build(maxSize, expireAfterWrite, afterWriteUnit, -1, null, loader);
    }

    public static <K, V> LoadingCache<K, V> build(long maxSize, long expireAfterWrite, TimeUnit afterWriteUnit) {
        Assert.isTrue(expireAfterWrite >= 0);
        Assert.notNull(afterWriteUnit);
        return build(maxSize, expireAfterWrite, afterWriteUnit, null);
    }


}
