package store;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.ReferenceTable;
import com.displee.cache.index.archive.Archive;
import com.displee.cache.index.archive.file.File;
import com.displee.compress.CompressionType;
import com.displee.io.impl.OutputBuffer;
import org.displee.utilities.Compression;
import store.cache.index.OSRSIndices;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

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
            newArchive.add(files);
            newArchive.setRead(true);
        }
        return newArchive;
    }

    /**
     * Add an index to this cache library.
     *
     * @param cacheLibrary
     * @param named     If the index contains archive and/or file names.
     * @param whirlpool If the index is using whirlpool.
     */
    public static Index createIndex(CacheLibrary cacheLibrary, boolean named, boolean whirlpool) {
        try {
            if (cacheLibrary.is317()) {
                throw new UnsupportedOperationException("317 not supported to add new indices yet.");
            }
            final OutputBuffer outputBuffer = new OutputBuffer(4);
            outputBuffer.writeByte(5);
            outputBuffer.writeByte((named ? 0x1 : 0x0) | (whirlpool ? 0x2 : 0x0));
            outputBuffer.writeShort(0);
            final int id = cacheLibrary.indices().length;
            if (!cacheLibrary.getChecksumTable().writeArchiveSector(id,
                    Compression.compress(outputBuffer.array(), CompressionType.GZIP, null, -1))) {
                throw new RuntimeException("Failed to write the archive sector for a new index[id=" + id + "]");
            }
            cacheLibrary.setIndices(Arrays.copyOf(cacheLibrary.indices(), cacheLibrary.indices().length + 1));
            Index index = cacheLibrary.indices()[id] = new Index(cacheLibrary, id,
                    new RandomAccessFile(new java.io.File(cacheLibrary.getPath() + "main_file_cache.idx" + id), "rw"));
            index.flag();
            if (!index.update()) {
                throw new IOException("Unable to write CRC for the new index.");
            }
            return index;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Add a new archive to this index.
     *
     * @param referenceTable
     * @param id         The id of the new archive.
     * @param name       The archive name.
     * @param resetFiles If we need to reset all the files in this archive.
     * @return The archive instance.
     */
//    public static Archive add2(ReferenceTable referenceTable, int id, int name, boolean resetFiles) {
//        Archive current = referenceTable.archive(id, true);
//        if (current != null && !current.getRead() && !current.getNew() && !current.flagged()) {
//            current = referenceTable.archive(id);
//        }
//        if (current != null) {
//            if (name != -1 && current.getHashName() != name) {
//                if (current.getHashName() > 0) {
//                    referenceTable.archiveNames.set(referenceTable.archiveNames.indexOf(current.getHashName()), name);
//                }
//                current.setHashName(name);
//            }
//            if (resetFiles) {
//                current.reset();
//            }
//            referenceTable.flag();
//            current.flag();
//            return current;
//        }
//        referenceTable.archiveIds = Arrays.copyOf(referenceTable.archiveIds, referenceTable.archiveIds.length + 1);
//        referenceTable.archiveIds[referenceTable.archiveIds.length - 1] = id;
//        referenceTable.archives = Arrays.copyOf(referenceTable.archives, referenceTable.archives.length + 1);
//        final Archive archive = new Archive(id, name);
//        archive.reset();
//        archive.setNew(true);
//        archive.flag();
//        referenceTable.archives[referenceTable.archives.length - 1] = archive;
//        referenceTable.flag();
//        return archive;
//    }
}
