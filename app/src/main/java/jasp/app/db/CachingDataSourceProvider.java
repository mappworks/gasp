package jasp.app.db;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jasp.app.App;
import jasp.core.model.User;
import jasp.core.util.Passwords;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Wrapper that caches data sources to avoid repeated
 */
public class CachingDataSourceProvider implements DataSourceProvider {

    DataSourceProvider delegate;
    Cache<String,Value> cache;

    public CachingDataSourceProvider(DataSourceProvider delegate) {
        this.delegate = delegate;
        cache = CacheBuilder.newBuilder().build();
    }

    @Override
    public DataSource get(User user, App app) throws Exception {
        Value cached = cache.getIfPresent(user.handle());
        if (cached != null) {
            // check the password
            if (Passwords.digester().matches(user.password(), cached.passwdDigest)) {
                // match, good to go
                return cached.dataSource;
            }
            else {
                cache.invalidate(user.handle());
            }
        }

        DataSource dataSource = delegate.get(user, app);
        if (dataSource != null) {
            // check a connection so that we don't cache a bad one
            try {
                try (Connection cx = dataSource.getConnection()){
                    // cache
                    cache.put(user.handle(), new Value(Passwords.digester().digest(user.password()), dataSource));
                }
            }
            catch(Exception ignore) {}
        }

        return dataSource;
    }

    @Override
    public void release(DataSource dataSource) {
        // do nothing, will release later
    }

    static class Value {
        final byte[] passwdDigest;
        final DataSource dataSource;

        Value(byte[] passwdDigest, DataSource dataSource) {
            this.passwdDigest = passwdDigest;
            this.dataSource = dataSource;
        }
    }
}
