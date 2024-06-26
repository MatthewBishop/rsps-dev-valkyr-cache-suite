package com.displee.cache.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.archive.Archive;
import com.displee.cache.index.archive.Archive317;
import com.displee.cache.index.archive.ArchiveSector;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;
import com.displee.cache.ProgressListener;
import com.displee.compress.CompressionType;
import org.displee.utilities.Constants;
import org.displee.utilities.HashGenerator;
import org.displee.utilities.Whirlpool;

/**
 * A class that represents a single index inside the cache.
 * 
 * @author Displee
 */
public class Index317 extends Index {

	private static final String[] VERSION_NAMES = { "model_version", "anim_version", "midi_version", "map_version" };

	private static final String[] CRC_NAMES = { "model_crc", "anim_crc", "midi_crc", "map_crc" };

	private static final String[] INDEX_NAMES = { "model_index", "anim_index", "midi_index", "map_index" };

	/**
	 * Constructs a new {@code Index} {@code Object}.
	 * 
	 * @param id               The id of this index.
	 * @param randomAccessFile The {@link RandomAccessFile} of this index.
	 */
	public Index317(CacheLibrary origin, int id, RandomAccessFile randomAccessFile) {
		super(origin, id, randomAccessFile);
	}

	@Override
	public boolean update(ProgressListener listener, Map<Integer, int[]> map) {
		boolean updateChecksumTable = false;
		int updateCount = 0;
		for (Archive archive : archives) {
			if (archive == null || !archive.flagged()) {
				continue;
			}
			updateCount++;
		}
		double i = 0;
		for (Archive archive : archives) {
			if (archive == null || !archive.flagged()) {
				continue;
			}
			i++;
			archive.unFlag();
			if (!updateChecksumTable) {
				updateChecksumTable = true;
			}
			if (listener != null) {
				listener.notify((i / updateCount) * 80.0, "Repacking archive " + archive.getId() + "...");
			}
			final byte[] compressed = archive.write(new OutputBuffer(1024));
			archive.setCrc(HashGenerator.getCRCHash(compressed));
			archive.setWhirlpool(Whirlpool.getHash(compressed));
			final ArchiveSector backup = readArchiveSector(archive.getId());
			if (!writeArchiveSector(archive.getId(), compressed)) {
				System.err.println("Could not write the archive sector for index[id=" + super.id + ", archive="
						+ archive.getId() + "]");
				System.err.println("Reverting changes...");
				if (backup != null) {
					if (writeArchiveSector(archive.getId(), backup.getData())) {
						System.out.println("Changes have been reverted.");
					} else {
						System.err.println("Your cache is corrupt.");
					}
				}
				return false;
			}
			if (origin.getClearDataAfterUpdate()) {
				archive.restore();
			}
		}
		if (id != 0 && id < VERSION_NAMES.length && updateChecksumTable) {
			writeArchiveProperties(Arrays.stream(archives).mapToInt(Archive::getRevision).toArray(),
					VERSION_NAMES[id - 1], 1);
			writeArchiveProperties(Arrays.stream(archives).mapToInt(Archive::getCrc).toArray(), CRC_NAMES[id - 1], 2);
			writeArchiveProperties(Arrays.stream(archives).mapToInt(e -> ((Archive317) e).getPriority()).toArray(),
					INDEX_NAMES[id - 1], id == 2 ? 1 : 0);
		}
		if (listener != null) {
			listener.notify(100, "Successfully updated index " + id + ".");
		}
		return true;
	}

	@Override
	protected boolean isIndexValid(int index) {
		return this.id == index - 1;
	}

	@Override
	protected int indexToWrite(int index) {
		return index + 1;
	}

	/**
	 * Read this index.
	 */
	@Override
	protected void read() {
		int archiveLength;
		try {
			archiveLength = (int) (getRandomAccessFile().length() / (long) Constants.INDEX_SIZE);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		archiveIds = new int[archiveLength];
		archives = new Archive[archiveLength];
		int[] versions = null;
		int[] crcs = null;
		int[] priorities = null;
		if (id != 0 && id < VERSION_NAMES.length) {
			versions = readArchiveProperties(VERSION_NAMES[id - 1], 1);
			crcs = readArchiveProperties(CRC_NAMES[id - 1], 2);
			priorities = readArchiveProperties(INDEX_NAMES[id - 1], id == 2 ? 1 : 0);
		}
		for (int i = 0; i < archives.length; i++) {
			archiveIds[i] = i;
			Archive317 archive = (Archive317) (archives[i] = new Archive317(i));
			if (versions == null || crcs == null || i >= versions.length) {
				continue;
			}
			archive.setRevision(versions[i]);
			archive.setCrc(crcs[i]);
			if (priorities != null) {
				archive.setPriority(i < priorities.length ? priorities[i] : 0);
			}
		}
	}

	@Override
	public boolean read(InputBuffer inputBuffer) {
		return true;
	}

	@Override
	public byte[] write(OutputBuffer outputBuffer) {
		return null;
	}

	@Override
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
				((Archive317) archive).setCompressionType(this.id == 0 ? CompressionType.BZIP2 : CompressionType.GZIP);
				archive.read(new InputBuffer(archiveSector.getData()));
				return archive;
			}
		}
		return null;
	}

	@Override
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
		final Archive317 archive = new Archive317(id, name);
		if (this.id != 0) {
			archive.setCompressionType(CompressionType.GZIP);
		}
		archive.reset();
		archive.setNew(true);
		archive.flag();
		archives[archives.length - 1] = archive;
		flag();
		return archive;
	}

	private int[] readArchiveProperties(String fileId, int type) {
		if (id == 0 || id == 4 || id > VERSION_NAMES.length) {
			return null;
		}
		byte[] data = origin.index(0).archive(5).file(fileId).getData();
		InputBuffer buffer = new InputBuffer(data);
		int[] properties = new int[data.length / (type == 0 ? 1 : type == 1 ? 2 : 4)];
		switch (type) {
		case 0:
			for (int i = 0; i < properties.length; i++) {
				properties[i] = buffer.readUnsignedByte();
			}
			break;
		case 1:
			for (int i = 0; i < properties.length; i++) {
				properties[i] = buffer.readUnsignedShort();
			}
			break;
		case 2:
			for (int i = 0; i < properties.length; i++) {
				properties[i] = buffer.readInt();
			}
			break;
		}
		return properties;
	}

	private boolean writeArchiveProperties(int[] properties, String fileId, int type) {
		if (id == 0 || id == 4 || id > VERSION_NAMES.length) {
			return false;
		}
		OutputBuffer buffer = new OutputBuffer(properties.length);
		if (type == 0) {
			for (int i : properties) {
				buffer.writeByte(i);
			}
		} else if (type == 1) {
			for (int i : properties) {
				buffer.writeShort(i);
			}
		} else if (type == 2) {
			for (int i : properties) {
				buffer.writeInt(i);
			}
		}
		origin.index(0).archive(5).add(fileId, buffer.array());
		return origin.index(0).update();
	}

}
