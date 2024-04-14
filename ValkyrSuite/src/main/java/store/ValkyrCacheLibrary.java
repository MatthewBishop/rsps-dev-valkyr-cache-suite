package store;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;
import store.cache.index.OSRSIndices;

public class ValkyrCacheLibrary {
    /**
     * The singleton of this class.
     */
    public static CacheLibrary singleton;

    /**
     * Gets a singleton of this class.
     *
     * @return
     */
    public static CacheLibrary get() {
        return singleton;
    }

    public static Index getIndex(OSRSIndices index) {
        return singleton.index(index.ordinal());
    }

    public static Index getIndex(CacheLibrary cacheLibrary, OSRSIndices index) {
        return cacheLibrary.index(index.ordinal());
    }
}
