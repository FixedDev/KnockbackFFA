package net.minebukket.knockbackpvp.handlers.mongo;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import net.minebukket.knockbackpvp.KnockbackPvpPlugin;
import net.minebukket.knockbackpvp.handlers.abstractmanager.DataHandler;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;

import java.util.concurrent.Callable;

/*******************************************************************************
 *  Copyright (C) SparkNetwork - All Rights Reserved
 *   * Unauthorized copying of this file, via any medium is strictly prohibited
 *   * Proprietary and confidential
 *   * Written by Gilberto Garcia <gilbertodamian14@gmail.com>, May 2018
 *
 ******************************************************************************/
public abstract class MongoDataHandler<K, V> extends DataHandler<K, V> {

    protected Datastore datastore;

    private Class<V> entityClazz;

    public MongoDataHandler(KnockbackPvpPlugin plugin, ListeningExecutorService executorService, Datastore datastore, Class<V> entityClass) {
        super(plugin, executorService);
        this.datastore = datastore;
        this.initType(entityClass);
    }

    @Override
    public ListenableFuture<V> loadData(K key) {
        return executorService.submit(() -> {
            V value = datastore
                    .find(entityClazz)
                    .field("_id")
                    .equal(key)
                    .get();

            if(value == null){
                return createNewObjectInstance(key);
            }

            return value;
        });
    }

    @Override
    public void saveData(V value) {
        executorService.submit(() -> datastore.save(value));
    }

    protected void initType(final Class<V> type) {
        entityClazz = type;
    }

}


