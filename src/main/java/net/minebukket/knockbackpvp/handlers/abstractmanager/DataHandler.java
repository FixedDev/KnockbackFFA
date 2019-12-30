package net.minebukket.knockbackpvp.handlers.abstractmanager;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import lombok.AccessLevel;
import lombok.Getter;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public abstract class DataHandler<K, V> {

    @Getter(value = AccessLevel.PUBLIC)
    protected final KnockbackPvpPlugin plugin;
    protected final Map<K, V> wrappedMap;

    @Getter(value = AccessLevel.PUBLIC)
    protected ListeningExecutorService executorService;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    public DataHandler(KnockbackPvpPlugin plugin, ListeningExecutorService executorService) {
        this.plugin = plugin;
        this.wrappedMap = new ConcurrentHashMap<>();
        this.executorService = executorService;

    }

    public abstract ListenableFuture<V> loadData(K key);

    public abstract void saveData(V value);

    protected abstract V createNewObjectInstance(K key);

    public ListenableFuture<V> getData(K key) {
        ListenableFuture<V> data = executorService.submit(() -> {
            lock.readLock().lock();
            try {
                return wrappedMap.get(key);
            } finally {
                lock.readLock().unlock();
            }
        });

        try {
            if (data.get() == null) {
                data = loadData(key);

                Futures.addCallback(data, new FutureCallback<V>() {
                    @Override
                    public void onSuccess(V result) {
                        lock.writeLock().lock();

                        try {
                            wrappedMap.put(key, result);
                        } finally {
                            lock.writeLock().unlock();
                        }

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                });

            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        return data;
    }

    public void saveAndUnloadData(K key) {
        if (!wrappedMap.containsKey(key)) return;

        V data = wrappedMap.get(key);
        this.saveData(data);
        wrappedMap.remove(key);
    }
}
