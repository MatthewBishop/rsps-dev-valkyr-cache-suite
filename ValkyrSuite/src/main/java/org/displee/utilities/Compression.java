package org.displee.utilities;

import com.displee.compress.CompressionType;
import com.displee.cache.index.archive.ArchiveSector;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;

/**
 * A class used to (de)compress the data of an {@link ArchiveSector}.
 * 
 * @author Displee
 * @author Apache Ah64
 */
public class Compression {

	/**
	 * Compress archive data with the argued compression type.
	 * 
	 * @param uncompressed    The data to compressed.
	 * @param compressionType The compression type.
	 * @param xteas           The xteas.
	 * @param revision        The revision.
	 * @return The compressed data.
	 */
	public static byte[] compress(byte[] uncompressed, CompressionType compressionType, int[] xteas, int revision) {
		final OutputBuffer outputBuffer = new OutputBuffer(16);
		final byte[] compressed;
		switch (compressionType) {
		case BZIP2:
			compressed = BZIP2Compressor.compress(uncompressed);
			break;
		case GZIP:
			compressed = GZIPCompressor.deflate(uncompressed);
			break;
		case LZMA:
			compressed = LZMACompressor.compress(uncompressed);
			break;
		default:
			compressed = uncompressed;
			break;
		}
		outputBuffer.writeByte(compressionType.ordinal());
		outputBuffer.writeInt(compressed.length);
		if (!compressionType.equals(CompressionType.NONE)) {
			outputBuffer.writeInt(uncompressed.length);
		}
		outputBuffer.writeBytes(compressed);
		if (xteas != null && (xteas[0] != 0 || xteas[1] != 0 || xteas[2] != 0 || 0 != xteas[3])) {
			outputBuffer.encryptXTEA(xteas, 5, outputBuffer.getOffset());
		}
		if (revision != -1) {
			outputBuffer.writeShort(revision);
		}
		return outputBuffer.array();
	}

	/**
	 * Decompress an archive its data.
	 * 
	 * @param archiveSector The archive to decompress.
	 * @param keys          The tea keys.
	 * @return The decompressed data.
	 */
	public static byte[] decompress(ArchiveSector archiveSector, int[] keys) {
		byte[] packedData = archiveSector.getData();
		InputBuffer inputBuffer = new InputBuffer(packedData);
		if (keys != null && (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || 0 != keys[3])) {
			inputBuffer.decryptXTEA(keys, 5, packedData.length);
		}
		int type = inputBuffer.readUnsignedByte();
		archiveSector.setCompressionType(CompressionType.values()[type]);
		if (type > CompressionType.values().length - 1) {
			throw new RuntimeException("Unknown compression type - type=" + type);
		}
		int compressedSize = inputBuffer.readInt() & 0xFFFFFF;
		if (type != 0) {
			int decompressedSize = inputBuffer.readInt() & 0xFFFFFF;
			byte[] decompressed = new byte[decompressedSize];
			if (type == CompressionType.BZIP2.ordinal()) {
				BZIP2Compressor.decompress(decompressed, decompressed.length, archiveSector.getData(), compressedSize,
						9);
			} else if (type == CompressionType.GZIP.ordinal()) {
				if (!GZIPCompressor.inflate(inputBuffer, decompressed)) {
					return null;
				}
			} else if (type == CompressionType.LZMA.ordinal()) {
				return LZMACompressor.decompress(inputBuffer, decompressedSize);
			}
			return decompressed;
		}
		return inputBuffer.array(0, compressedSize);
	}

}