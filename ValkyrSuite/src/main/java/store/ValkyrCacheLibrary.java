package store;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.ReferenceTable;
import com.displee.cache.index.archive.Archive;
import com.displee.cache.index.archive.file.File;
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

    public static Archive add2(ReferenceTable referenceTable, int id, int name, boolean resetFiles) {
        return referenceTable.add(id, name, resetFiles);
    }

    /**
     * Add an archive instance to this index.
     *
     * @param referenceTable
     * @param archive    The archive instance.
     * @param addFiles   If we need to add the files in this archive to the new
     *                   archive.
     * @param resetFiles If we need to reset all the files in the new archive.
     * @return The new archive instance.
     */
    public static Archive add2(ReferenceTable referenceTable, Archive archive, boolean addFiles, boolean resetFiles) {
        return add2(referenceTable, archive, addFiles, resetFiles, archive.getId());
    }

    /**
     * Add an archive instance to this index. This will create a new archive.
     *
     * @param referenceTable
     * @param archive    The archive instance.
     * @param addFiles   If we need to add the files in this archive to the new
     *                   archive.
     * @param resetFiles If we need to reset all the files in the new archive.
     * @param id         The id to give to the new archive.
     * @return The new archive instance.
     */
    public static Archive add2(ReferenceTable referenceTable, Archive archive, boolean addFiles, boolean resetFiles, int id) {
        final File[] files = archive.copy().files();
        final Archive newArchive = add2(referenceTable, id, archive.getHashName(), resetFiles);
        if (addFiles) {
            newArchive.addFiles(files);
            newArchive.setRead(true);
        }
        return newArchive;
    }
}
