package store;

import org.displee.CacheLibrary;
import org.displee.cache.index.Index;
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
        return singleton.getIndex(index.ordinal());
    }

    public static Index getIndex(CacheLibrary cacheLibrary, OSRSIndices index) {
        return cacheLibrary.getIndex(index.ordinal());
    }
}
