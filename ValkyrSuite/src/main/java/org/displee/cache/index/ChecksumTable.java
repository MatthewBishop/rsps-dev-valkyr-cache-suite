package org.displee.cache.index;

import java.io.RandomAccessFile;

import org.displee.CacheLibrary;
import org.displee.cache.Container;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;

/**
 * A class representing the checksum table.
 * 
 * @author Displee
 */
public class ChecksumTable extends Index implements Container {

	/**
	 * The data of this checksum table.
	 */
	private byte[] data;

	/**
	 * Constructs a new {@code ChecksumTable} {@code Object}.
	 * 
	 * @param id               The index.
	 * @param randomAccessFile The random access file of this index.
	 */
	public ChecksumTable(CacheLibrary origin, int id, RandomAccessFile randomAccessFile) {
		super(origin, id, randomAccessFile);
	}

	@Override
	public boolean read(InputBuffer inputBuffer) {
		for (int i = 0; i < super.origin.getIndices().length; i++) {
			int crc = inputBuffer.readInt();
			if (crc > 0) {
				super.origin.getIndex(i).setCRC(crc);
			}
			int revision = inputBuffer.readInt();
			if (revision > 0) {
				super.origin.getIndex(i).setRevision(revision);
			}
		}
		return true;
	}

	@Override
	public byte[] write(OutputBuffer outputBuffer) {
		for (final Index index : super.origin.getIndices()) {
			outputBuffer.writeInt(index == null ? 0 : index.getCRC());
			outputBuffer.writeInt(index == null ? 0 : index.getRevision());
		}
		return data = outputBuffer.array();
	}

	/**
	 * Get the checksum table data.
	 * 
	 * @return {@code data}
	 */
	public byte[] getData() {
		return data;
	}

}