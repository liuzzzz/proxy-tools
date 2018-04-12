package com.secrething.tools.common.utils;


import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by liuzengzeng on 2017/12/6.
 */
public class DataContainer<K, D> {
    private final Map<K, DataContainer<K, D>> children;
    private final boolean concurrent;
    private D data;
    private Sync sync;

    private DataContainer(boolean concurrent) {
        this.concurrent = concurrent;
        this.children = new HashMap<>();
        if (concurrent)
            this.sync = lock();
        else
            this.sync = unlock();

    }

    private DataContainer(boolean concurrent, D data) {
        this(concurrent);
        this.data = data;
    }

    public final static <K, D> DataContainer<K, D> createInstance() {
        return new DataContainer<K, D>(false);
    }

    public final static <K, D> DataContainer<K, D> createConcurrentInstance() {
        return new DataContainer<K, D>(true);
    }

    static Sync lock() {
        return new RealLock();
    }

    static Sync unlock() {
        return new Sync() {
            @Override
            public void locakRead() {

            }

            @Override
            public void unlockRead() {

            }

            @Override
            public void lockWrite() {

            }

            @Override
            public void unlockWrite() {

            }
        };
    }

    public static void main(String[] args) {
        DataContainer dataContainer = DataContainer.createInstance();
        dataContainer.data = 1;
        System.out.println(dataContainer.getData());
    }

    private final <K, D> DataContainer<K, D> createChild(D data) {
        DataContainer<K, D> node = new DataContainer<>(this.concurrent, data);
        return node;
    }

    private final <K, D> DataContainer<K, D> createParent() {
        return new DataContainer<K, D>(this.concurrent);
    }

    public final D getData() {
        return lockRead(new Caller<D>() {
            @Override
            public D call() {
                return data;
            }
        });

    }

    <T> T lockRead(Caller<T> handler) {
        sync.locakRead();
        try {
            return handler.call();
        } finally {
            sync.unlockRead();
        }
    }

    <T> T lockWrite(Caller<T> caller) {
        sync.lockWrite();
        try {
            return caller.call();
        } finally {
            sync.unlockWrite();
        }
    }

    public final Map<K, DataContainer<K, D>> getNodes() {
        return lockRead(new Caller<Map<K, DataContainer<K, D>>>() {
            @Override
            public Map<K, DataContainer<K, D>> call() {
                return children;
            }
        });
    }

    public final DataContainer<K, D> getNode(final K... keys) {
        if (keysCantGet(keys))
            return null;
        if (keys.length == 1)
            return lockRead(new Caller<DataContainer<K, D>>() {
                @Override
                public DataContainer<K, D> call() {
                    return children.get(keys[0]);
                }
            });
        K key = keys[keys.length - 1];
        K[] parentKeys = Arrays.copyOf(keys, keys.length - 1);
        return getNodeSplitKey(key, parentKeys);

    }

    public final D getNodeData(K... keys) {
        if (keysCantGet(keys))
            return null;
        if (keys.length == 1)
            return getNodeDataSplitKey(keys[0]);
        int newlen = keys.length - 1;
        K key = selectKey(newlen, keys);
        K[] parentKeys = selectParentKeys(newlen, keys);
        return getNodeDataSplitKey(key, parentKeys);

    }

    public final DataContainer<K, D> put(D data, K... keys) {
        if (keysCantPut(keys))
            return this;
        if (keys.length == 1) {
            return putSplitKey(keys[0], data);
        }
        int newlen = keys.length - 1;
        K key = selectKey(newlen, keys);
        K[] parentKeys = selectParentKeys(newlen, keys);
        return putSplitKey(key, data, parentKeys);

    }

    private K selectKey(int index, K... keys) {
        return keys[index];
    }

    private K[] selectParentKeys(int len, K... keys) {
        K[] parentKeys = (K[]) Array.newInstance(keys[0].getClass(), len);
        System.arraycopy(keys, 0, parentKeys, 0, len);
        return parentKeys;
    }

    public final DataContainer<K, D> remove(final K... keys) {
        if (null == keys || keys.length < 1 || null == children)
            return null;
        else if (keys.length == 1) {
            return lockWrite(new Caller<DataContainer<K, D>>() {
                @Override
                public DataContainer<K, D> call() {
                    return children.remove(keys[0]);
                }
            });
        }
        K[] parent = selectParentKeys(keys.length - 1, keys);
        final DataContainer<K, D> node = getNode(parent);
        return lockWrite(new Caller<DataContainer<K, D>>() {
            @Override
            public DataContainer<K, D> call() {
                return node == null ? null : node.children.remove(keys[keys.length - 1]);
            }
        });

    }

    private DataContainer<K, D> putBase(K key, DataContainer<K, D> node, K... parentKeys) {
        if (null == key || null == node)
            throw new NullPointerException("key or node can't be null");

        DataContainer<K, D> next = this;
        if (null != parentKeys && parentKeys.length > 0) {
            for (int i = 0; i < parentKeys.length; i++) {
                K parentKey = parentKeys[i];
                DataContainer curr = next.children.get(parentKey);
                if (null == curr) {
                    curr = next.createParent();
                    next.children.put(parentKey, curr);
                }
                next = curr;
            }
        }
        return next.children.put(key, node);
    }

    private boolean hasChildren() {
        return lockRead(new Caller<Boolean>() {
            @Override
            public Boolean call() {
                return null != children && !children.isEmpty();
            }
        });
    }

    /**
     * 查找最后一个节点,返回最后一个叶子节点
     *
     * @param index
     * @param parentKeys
     * @return Node
     */

    private DataContainer<K, D> findNode(K... parentKeys) {
        if (!hasChildren())
            return this;
        DataContainer<K, D> next = this;
        for (int i = 0; i < parentKeys.length; i++) {
            if (next.hasChildren()) {
                next = next.children.get(parentKeys[i]);
                if (null == next)
                    return null;
            }
            if (i == (parentKeys.length - 1) || !next.hasChildren()) {
                return next;
            }
        }
        return null;

    }

    private DataContainer<K, D> putSplitKey(final K key, final D data, final K... parentKeys) {
        final DataContainer<K, D> curr = this;
        return lockWrite(new Caller<DataContainer<K, D>>() {
            @Override
            public DataContainer<K, D> call() {
                DataContainer<K, D> node = curr.createChild(data);
                return putBase(key, node, parentKeys);
            }
        });
    }

    private DataContainer<K, D> getNodeSplitKey(final K key, final K... parentKeys) {
        return lockRead(new Caller<DataContainer<K, D>>() {
            @Override
            public DataContainer<K, D> call() {
                if (null != parentKeys && parentKeys.length > 0) {
                    DataContainer<K, D> node = findNode(parentKeys); //循环方式
                    if (null == node || null == node.children)
                        return null;
                    return node.children.get(key);
                }
                return children.get(key);
            }
        });
    }

    private D getNodeDataSplitKey(final K key, final K... parentKeys) {
        return lockRead(new Caller<D>() {
            @Override
            public D call() {
                DataContainer<K, D> node = getNodeSplitKey(key, parentKeys);
                if (null == node)
                    return null;
                return node.getData();
            }
        });
    }

    private boolean keysCantPut(K... keys) {
        return null == keys || keys.length < 1;
    }

    private boolean keysCantGet(K... keys) {
        return keysCantPut(keys) || null == this.children;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    static interface Caller<T> {
        T call();
    }

    interface Sync {
        void locakRead();

        void unlockRead();

        void lockWrite();

        void unlockWrite();
    }

    static class RealLock extends ReentrantReadWriteLock implements Sync {
        ReadLock readLock = readLock();
        WriteLock writeLock = writeLock();

        @Override
        public void locakRead() {
            readLock.lock();
        }

        @Override
        public void unlockRead() {
            readLock.unlock();
        }

        @Override
        public void lockWrite() {
            writeLock.lock();
        }

        @Override
        public void unlockWrite() {
            writeLock.unlock();
        }
    }
}
