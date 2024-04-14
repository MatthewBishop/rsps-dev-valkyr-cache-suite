package com.displee.cache.index;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.displee.cache.Container;
import com.displee.cache.index.archive.file.File;
import com.displee.cache.CacheLibrary;
import com.displee.cache.CacheLibraryMode;
import com.displee.cache.index.archive.Archive;
import com.displee.cache.index.archive.ArchiveSector;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;
import org.displee.utilities.Compression;
import org.displee.utilities.Constants;
import org.displee.utilities.Miscellaneous;

/**
 * A class that represents contains usefull data of this index.
 * 
 * @author Displee
 */
public class ReferenceTable implements Container {

	/**
	 * The id of the index.
	 */
	protected int id;

	/**
	 * The version.
	 */
	protected int version;

	/**
	 * The revision.
	 */
	protected int revision;

	/**
	 * If this index contains archive names.
	 */
	protected boolean named;

	/**
	 * If this index is using whirlpool.
	 */
	protected boolean whirlpool;

	/**
	 * An unknown flag.
	 */
	protected boolean flag4;

	/**
	 * An unknown flag.
	 */
	protected boolean flag8;

	/**
	 * An array of archive ids.
	 */
	protected int[] archiveIds = new int[0];

	/**
	 * An array of real archives inside this index.
	 */
	protected Archive[] archives = new Archive[0];

	/**
	 * If this index needs to be updated.
	 */
	protected boolean needUpdate;

	/**
	 * The cache library.
	 */
	protected CacheLibrary origin;

	/**
	 * A list of archive names.
	 */
	protected List<Integer> archiveNames = new ArrayList<>();

	/**
	 * Constructs a new {@code ReferenceTable} {@code Object}.
	 * 
	 * @param id The id of the index.
	 */
	public ReferenceTable(CacheLibrary origin, int id) throws IllegalAccessError {
		if (origin.isClosed()) {
			throw new IllegalAccessError("Cache library has been closed.");
		}
		this.origin = origin;
		this.id = id;
	}

	@Override
	public boolean read(InputBuffer inputBuffer) {
		version = inputBuffer.readByte() & 0xFF;
		if (version < 5 || version > 7) {
			throw new RuntimeException("Unknown version=" + version);
		}
		revision = version >= 6 ? inputBuffer.readInt() : 0;
		final int flag = inputBuffer.readByte();
		named = (flag & 0x1) != 0;
		whirlpool = (flag & 0x2) != 0;
		flag4 = (flag & 0x4) != 0;
		flag8 = (flag & 0x8) != 0;
		archiveIds = new int[version >= 7 ? inputBuffer.readBigSmart() : inputBuffer.readUnsignedShort()];
		int lastArchiveId = 0;
		for (int i = 0; i < archiveIds.length; i++) {
			archiveIds[i] = (version >= 7 ? inputBuffer.readBigSmart() : inputBuffer.readUnsignedShort())
					+ (i == 0 ? 0 : archiveIds[i - 1]);
			if (archiveIds[i] > lastArchiveId) {
				lastArchiveId = archiveIds[i];
			}
		}
		archives = new Archive[archiveIds.length];
		for (int i = 0; i < archives.length; i++) {
			archives[i] = new Archive(archiveIds[i]);
		}
		if (named) {
			for (int i = 0; i < archives.length; i++) {
				archives[i].setHashName(inputBuffer.readInt());
				archiveNames.add(archives[i].getHashName());
			}
		}
		if (origin.isRS3()) {
			for (int i = 0; i < archives.length; i++) {
				archives[i].setCrc(inputBuffer.readInt());
			}
			if (flag8) {
				for (int i = 0; i < archives.length; i++) {
					archives[i].setFlag8Value(inputBuffer.readInt());
				}
			}
			if (whirlpool) {
				for (int i = 0; i < archives.length; i++) {
					inputBuffer.readBytes(archives[i].getWhirlpool(), 0, Constants.WHIRLPOOL_SIZE);
				}
			}
			if (flag4) {// flag 4
				for (int i = 0; i < archives.length; i++) {
					archives[i].setFlag4Value1(inputBuffer.readInt());
					archives[i].setFlag4Value2(inputBuffer.readInt());
				}
			}
		} else {
			if (whirlpool) {
				for (int i = 0; i < archives.length; i++) {
					inputBuffer.readBytes(archives[i].getWhirlpool(), 0, Constants.WHIRLPOOL_SIZE);
				}
			}
			for (int i = 0; i < archives.length; i++) {
				archives[i].setCrc(inputBuffer.readInt());
			}
		}
		for (int i = 0; i < archives.length; i++) {
			archives[i].setRevision(inputBuffer.readInt());
		}
		for (int i = 0; i < archives.length; i++) {
			archives[i].setFileIds(version >= 7 ? inputBuffer.readBigSmart() : inputBuffer.readUnsignedShort());
		}
		for (int i = 0; i < archives.length; i++) {
			int lastFileId = -1;
			final Archive archive = archives[i];
			for (int fileIndex = 0; fileIndex < archive.fileIds().length; fileIndex++) {
				archive.fileIds()[fileIndex] = (version >= 7 ? inputBuffer.readBigSmart()
						: inputBuffer.readUnsignedShort()) + (fileIndex == 0 ? 0 : archive.fileIds()[fileIndex - 1]);
				if (archive.fileIds()[fileIndex] > lastFileId) {
					lastFileId = archive.fileIds()[fileIndex];
				}
			}
			archive.setFiles(archive.fileIds().length);
			for (int fileIndex = 0; fileIndex < archive.fileIds().length; fileIndex++) {
				archive.files()[fileIndex] = new File(archive.fileIds()[fileIndex]);
			}
		}
		if (named) {
			for (int i = 0; i < archives.length; i++) {
				final Archive archive = archives[i];
				for (int fileIndex = 0; fileIndex < archive.fileIds().length; fileIndex++) {
					archive.file(archive.fileIds()[fileIndex]).setName(inputBuffer.readInt());
				}
			}
		}
		return true;
	}

	@Override
	public byte[] write(OutputBuffer outputBuffer) {
		outputBuffer.writeByte(version);
		if (version >= 6) {
			outputBuffer.writeInt(revision);
		}
		int flag = 0x0;
		if (named) {
			flag |= 0x1;
		}
		if (whirlpool) {
			flag |= 0x2;
		}
		if (flag4) {
			flag |= 0x4;
		}
		if (flag8) {
			flag |= 0x8;
		}
		outputBuffer.writeByte(flag);
		if (version >= 7) {
			outputBuffer.writeBigSmart(archives.length);
		} else {
			outputBuffer.writeShort(archives.length);
		}
		for (int i = 0; i < archives.length; i++) {
			if (version >= 7) {
				outputBuffer.writeBigSmart(archiveIds[i] - (i == 0 ? 0 : archiveIds[i - 1]));
			} else {
				outputBuffer.writeShort(archiveIds[i] - (i == 0 ? 0 : archiveIds[i - 1]));
			}
		}
		if (named) {
			for (int i = 0; i < archives.length; i++) {
				outputBuffer.writeInt(archives[i].getHashName());
			}
		}
		if (origin.isRS3()) {
			for (int i = 0; i < archives.length; i++) {
				outputBuffer.writeInt(archives[i].getCrc());
			}
			if (flag8) {
				for (int i = 0; i < archives.length; i++) {
					outputBuffer.writeInt(archives[i].getFlag8Value());
				}
			}
			if (whirlpool) {
				for (int i = 0; i < archives.length; i++) {
					outputBuffer.writeBytes(archives[i].getWhirlpool());
				}
			}
			if (flag4) {
				for (int i = 0; i < archives.length; i++) {
					outputBuffer.writeInt(archives[i].getFlag4Value1());
					outputBuffer.writeInt(archives[i].getFlag4Value2());
				}
			}
		} else {
			if (whirlpool) {
				for (int i = 0; i < archives.length; i++) {
					outputBuffer.writeBytes(archives[i].getWhirlpool());
				}
			}
			for (int i = 0; i < archives.length; i++) {
				outputBuffer.writeInt(archives[i].getCrc());
			}
		}
		for (int i = 0; i < archives.length; i++) {
			outputBuffer.writeInt(archives[i].getRevision());
		}
		for (int i = 0; i < archives.length; i++) {
			if (version >= 7) {
				outputBuffer.writeBigSmart(archives[i].fileIds().length);
			} else {
				outputBuffer.writeShort(archives[i].fileIds().length);
			}
		}
		for (int i = 0; i < archives.length; i++) {
			final Archive archive = archives[i];
			for (int fileIndex = 0; fileIndex < archive.fileIds().length; fileIndex++) {
				if (version >= 7) {
					outputBuffer.writeBigSmart(archive.fileIds()[fileIndex]
							- (fileIndex == 0 ? 0 : archive.fileIds()[fileIndex - 1]));
				} else {
					outputBuffer.writeShort(archive.fileIds()[fileIndex]
							- (fileIndex == 0 ? 0 : archive.fileIds()[fileIndex - 1]));
				}
			}
		}
		if (named) {
			for (int i = 0; i < archives.length; i++) {
				final Archive archive = archives[i];
				for (int fileIndex = 0; fileIndex < archive.fileIds().length; fileIndex++) {
					outputBuffer.writeInt(archive.file(archive.fileIds()[fileIndex]).getName());
				}
			}
		}
		return outputBuffer.array();
	}

	/**
	 * Reset this index.
	 */
	public void reset() {
		archiveIds = new int[0];
		archives = new Archive[0];
	}

	/**
	 * Copy this index.
	 * 
	 * @return The new index.
	 */
	public ReferenceTable copy() {
		final ReferenceTable referenceTable = new ReferenceTable(origin, id);
		referenceTable.version = version;
		referenceTable.revision = revision;
		referenceTable.named = named;
		referenceTable.archiveIds = archiveIds;
		referenceTable.archives = archives;
		referenceTable.whirlpool = whirlpool;
		return referenceTable;
	}

	/**
	 * Get the id of this index descriptor.
	 * 
	 * @return {@code id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set a new id for this index.
	 * 
	 * @param id The new index to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get the version.
	 * 
	 * @return {@code version}
	 */
	public int getVersion() {
		return version;
	}


	/**
	 * Set a new value for the version.
	 *
	 * @param version The new value.
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Get the revision.
	 * 
	 * @return {@code revision}
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * Set a new value for the revision.
	 * 
	 * @param revision The new value.
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}

	/**
	 * Get the archive ids.
	 * 
	 * @return {@code archiveIds}
	 */
	public int[] archiveIds() {
		return archiveIds;
	}

	/**
	 * Get the archives.
	 * 
	 * @return {@code archives}
	 */
	public Archive[] getArchives() {
		return archives;
	}

	/**
	 * Get the archives.
	 * 
	 * @return {@code archives}
	 */
	public Archive[] copyArchives() {
		final Archive[] archives = new Archive[archiveIds.length];
		for (int i = 0; i < archives.length; i++) {
			final Archive original = archive(archiveIds[i]);
			archives[i] = original.copy();
			if (origin.getMode() == CacheLibraryMode.UN_CACHED) {
				original.restore();
			}
		}
		return archives;
	}

	/**
	 * Add multiple archives to this index.
	 * 
	 * @param archives   An array of archives.
	 * @param resetFiles If we need to reset all the files in the archives.
	 */
	public void addArchives(Archive[] archives, boolean resetFiles) {
		for (Archive archive : archives) {
			add(archive, resetFiles);
		}
	}

	/**
	 * Add multiple archives to this index.
	 * 
	 * @param archives   An array of archives.
	 * @param addFiles   If we need to add the files in the archives to the new
	 *                   archive.
	 * @param resetFiles If we need to reset all the files in the archives.
	 */
	public void addArchives(Archive[] archives, boolean addFiles, boolean resetFiles) {
		for (Archive archive : archives) {
			add(archive, addFiles, resetFiles);
		}
	}

	/**
	 * Add a archive instance to this index.
	 * 
	 * @param archive    The archive to add.
	 * @param resetFiles If we need to reset all files in the new archive.
	 * @return The new archive instance.
	 */
	public Archive add(Archive archive, boolean resetFiles) {
		return add(archive, true, resetFiles, last().getId() + 1);
	}

	/**
	 * Add an archive instance to this index.
	 * 
	 * @param archive    The archive instance.
	 * @param addFiles   If we need to add the files in this archive to the new
	 *                   archive.
	 * @param resetFiles If we need to reset all the files in the new archive.
	 * @return The new archive instance.
	 */
	public Archive add(Archive archive, boolean addFiles, boolean resetFiles) {
		return add(archive, addFiles, resetFiles, archive.getId());
	}

	/**
	 * Add an archive instance to this index. This will create a new archive.
	 * 
	 * @param archive    The archive instance.
	 * @param addFiles   If we need to add the files in this archive to the new
	 *                   archive.
	 * @param resetFiles If we need to reset all the files in the new archive.
	 * @param id         The id to give to the new archive.
	 * @return The new archive instance.
	 */
	public Archive add(Archive archive, boolean addFiles, boolean resetFiles, int id) {
		final File[] files = archive.copy().files();
		final Archive newArchive = add(id, archive.getHashName(), resetFiles);
		if (addFiles) {
			newArchive.addFiles(files);
			newArchive.setRead(true);
		}
		return newArchive;
	}

	/**
	 * Add an archive to this index.
	 * 
	 * @return The new added archive.
	 */
	public Archive add() {
		return add(last().getId() + 1);
	}

	/**
	 * Add a new archive to this index.
	 * 
	 * @param name The name.
	 * @return The archive instance.
	 */
	public Archive add(String name) {
		return add(name, false);
	}

	/**
	 * Add a new archive to this index.
	 * 
	 * @param name       The name of the archive to add.
	 * @param resetFiles If we need to reset all the files in this archive.
	 * @return The archive instance.
	 */
	public Archive add(String name, boolean resetFiles) {
		final int archiveId = archiveId(name);
		int hashedName = name == null ? -1
				: this instanceof Index317 ? Miscellaneous.to317Hash(name) : name.toLowerCase().hashCode();
		return add(archiveId == -1 ? (last().getId() + 1) : archiveId, hashedName, resetFiles);
	}

	/**
	 * Add a new archive to this index.
	 * 
	 * @param id The id of the new archive.
	 * @return The archive instance.
	 */
	public Archive add(int id) {
		return add(id, false);
	}

	/**
	 * Add a new archive to this index.
	 * 
	 * @param id         The id of the new archive.
	 * @param resetFiles If we need to reset all the files in this archive.
	 * @return The archive instance.
	 */
	public Archive add(int id, boolean resetFiles) {
		return add(id, -1, resetFiles);
	}

	/**
	 * Add a new archive to this index.
	 * 
	 * @param id         The id of the new archive.
	 * @param name       The archive name.
	 * @param resetFiles If we need to reset all the files in this archive.
	 * @return The archive instance.
	 */
	public Archive add(int id, int name, boolean resetFiles) {
		Archive current = archive(id, true);
		if (current != null && !current.getRead() && !current.getNew() && !current.flagged()) {
			current = archive(id);
		}
		if (current != null) {
			if (name != -1 && current.getHashName() != name) {
				if (current.getHashName() > 0) {
					archiveNames.set(archiveNames.indexOf(current.getHashName()), name);
				}
				current.setHashName(name);
			}
			if (resetFiles) {
				current.reset();
			}
			flag();
			current.flag();
			return current;
		}
		archiveIds = Arrays.copyOf(archiveIds, archiveIds.length + 1);
		archiveIds[archiveIds.length - 1] = id;
		archives = Arrays.copyOf(archives, archives.length + 1);
		final Archive archive = new Archive(id, name);
		archive.reset();
		archive.setNew(true);
		archive.flag();
		archives[archives.length - 1] = archive;
		flag();
		return archive;
	}

	/**
	 * Remove an archive by its name.
	 * 
	 * @param name The name of the archive to remove.
	 */
	public void remove(String name) {
		remove(archiveId(name));
	}

	public boolean archiveExists(String name) {
		return archive(name) != null;
	}

	public boolean archiveExists(int id) {
		return archive(id) != null;
	}

	/**
	 * Remove an archive.
	 * 
	 * @param id The id of the archive to remove.
	 */
	public void remove(int id) {
		try {
			boolean exists = false;
			for (final Archive archive : archives) {
				if (archive.getId() == id) {
					exists = true;
				}
			}
			if (!exists) {
				throw new FileNotFoundException(
						"The argued archive id=" + id + " could not be removed as it is not stored in this index.");
			}
			final int[] archiveIds = new int[this.archiveIds.length - 1];
			int offset = 0;
			for (int i = 0; i < this.archiveIds.length; i++) {
				if (this.archiveIds[i] != id) {
					archiveIds[offset++] = this.archiveIds[i];
				}
			}
			this.archiveIds = archiveIds;
			offset = 0;
			final Archive[] archives = new Archive[this.archives.length - 1];
			for (int i = 0; i < this.archives.length; i++) {
				if (this.archives[i].getId() != id) {
					archives[offset++] = this.archives[i];
				}
			}
			this.archives = archives;
			flag();
		} catch (Exception exception) {
			exception.printStackTrace();
			return;
		}
	}

	/**
	 * Flag this index to be updated.
	 */
	public void flag() {
		sort();
		needUpdate = true;
	}

	/**
	 * Sort the holders of this index.
	 */
	public void sort() {
		Arrays.sort(archiveIds);
		Arrays.sort(archives, Comparator.comparingInt(Archive::getId));
	}

	/**
	 * Get an archive instance by its name.
	 * 
	 * @param name The name of the archive to get.
	 * @return The archive instance.
	 */
	public Archive archive(String name) {
		return archive(name, null);
	}

	/**
	 * Get an archive instance by its name and decoded by the argued xteas.
	 * 
	 * @param name  The name of the archive to get.
	 * @param xteas The xteas
	 * @return The archive instance.
	 */
	public Archive archive(String name, int[] xteas) {
		return archive(archiveId(name), xteas, false);
	}

	/**
	 * Get a single archive by its name.
	 * 
	 * @param name   The name of the archive.
	 * @param direct If we must get the archive instance without reading it.
	 * @return The archive instance.
	 */
	public Archive archive(String name, boolean direct) {
		return archive(archiveId(name), null, direct);
	}

	/**
	 * Get a single archive.
	 * 
	 * @param id The id of the archive to get.
	 * @return The archive instance.
	 */
	public Archive archive(int id) {
		return archive(id, false);
	}

	/**
	 * Get a single archive with decrypting the argued xteas.
	 * 
	 * @param id    The id of the archive to get.
	 * @param xteas The xteas.
	 * @return The archive instance.
	 */
	public Archive archive(int id, int[] xteas) {
		return archive(id, xteas, false);
	}

	/**
	 * Get a single archive.
	 * 
	 * @param id     The id of the archive to get.
	 * @param direct If we want to get the instance without reading it.
	 * @return The archive instance.
	 */
	public Archive archive(int id, boolean direct) {
		return archive(id, null, direct);
	}

	/**
	 * Get a single archive.
	 * 
	 * @param id     The id of the archive to get.
	 * @param xtea   An array of xteas.
	 * @param direct If we want to get the archive without reading it.
	 * @return The archive instance.
	 */
	public Archive archive(int id, int[] xtea, boolean direct) {
		if (origin.isClosed()) {
			return null;
		}
		for (final Archive archive : archives) {
			if (archive.getId() == id) {
				if (direct || archive.getRead() || archive.getNew()) {
					return archive;
				}
				final ArchiveSector archiveSector = origin.index(this.id).readArchiveSector(id);
				if (archiveSector == null) {
					archive.setRead(true);
					archive.setNew(true);
					archive.reset();
					return archive;
				}
				archive.read(new InputBuffer(Compression.decompress(archiveSector, xtea)));
				if (this.id == 5 && !archive.containsData()) {// reset map data if archive has no data
					archive.setRead(false);
					return archive;
				}
				final InputBuffer inputBuffer = new InputBuffer(archiveSector.getData());
				inputBuffer.setOffset(1);
				final int remaining = inputBuffer.raw().length
						- ((inputBuffer.readInt() & 0xFFFFFF) + inputBuffer.getOffset());
				if (remaining >= 2) {
					inputBuffer.setOffset(inputBuffer.raw().length - 2);
					archive.setRevision(inputBuffer.readUnsignedShort());
				}
				return archive;
			}
		}
		return null;
	}

	/**
	 * Get the id of an archive by its name.
	 * 
	 * @param name The name.
	 * @return The archive id of the argued name.
	 */
	public int archiveId(String name) {
		boolean is317 = origin.is317();
		for (Archive archive : archives) {
			if (name != null && (is317 ? archive.getHashName() == Miscellaneous.to317Hash(name)
					: archive.getHashName() == name.toLowerCase().hashCode())) {
				return archive.getId();
			}
		}
		return -1;
	}

	public File getFile(int archive, int file) {
		return archive(archive).file(file);
	}

	public byte[] getFileData(int archive, int file) {
		return getFile(archive, file).getData();
	}

	public boolean containsName(String name) {
		return archiveNames.indexOf(origin.is317() ? Miscellaneous.to317Hash(name) : name.hashCode()) != -1;
	}

	/**
	 * Get the last archive.
	 * 
	 * @return The last archive instance of this index.
	 */
	public Archive last() {
		Archive archive = archives[archives.length - 1];
		if (!archive.getRead()) {
			archive = archive(archive.getId());
		}
		return archive;
	}

	/**
	 * Check if this index needs to be updated.
	 * 
	 * @return {@code needUpdate}
	 */
	public boolean flagged() {
		return needUpdate;
	}

	/**
	 * Check if this index is named.
	 * 
	 * @return {@code named}
	 */
	public boolean isNamed() {
		return named;
	}

	public void setNamed(boolean named) {
		this.named = named;
	}

	/**
	 * Check if this index is using whirlpool
	 * 
	 * @return {@code whirlpool}
	 */
	public boolean hasWhirlpool() {
		return whirlpool;
	}

	public void setWhirlpool(boolean whirlpool) {
		this.whirlpool = whirlpool;
	}

	/**
	 * Get the origin.
	 * 
	 * @return {@code origin}
	 */
	public CacheLibrary getOrigin() {
		return origin;
	}

	/**
	 * Set a new length for the {@code archives}.
	 * 
	 * @param length The new length to set.
	 */
	public void setArchivesLength(int length) {
		archives = new Archive[length];
	}

	/**
	 * Checks if the index has flag 4
	 *
	 * @return {@code flag4}
	 */
	public boolean hasFlag4() {
		return flag4;
	}

	public void setFlag4(boolean flag4) {
		this.flag4 = flag4;
	}

	/**
	 * Checks if the index has flag 8.
	 *
	 * @return {@code flag8}
	 */
	public boolean hasFlag8() {
		return flag8;
	}

	public void setFlag8(boolean flag8) {
		this.flag8 = flag8;
	}

}
