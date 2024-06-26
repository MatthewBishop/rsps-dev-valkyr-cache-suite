package com.displee.cache.index.archive;

import com.displee.cache.index.archive.file.File;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;
import org.displee.utilities.BZIP2Compressor;
import com.displee.compress.CompressionType;
import org.displee.utilities.GZIPCompressor;

public class Archive317 extends Archive {

	private boolean extracted;

	private CompressionType compressionType;

	private int priority;

	public Archive317(int id) {
		super(id);
	}

	public Archive317(int id, int name) {
		super(id, name);
	}

	@Override
	public boolean read(InputBuffer s) {
		if (compressionType == CompressionType.GZIP) {
			setFileIds(1);
			setFiles(1);
			files()[0] = new File(0, GZIPCompressor.deflate317(s.raw()));
			return read = true;
		}
		byte[] decompressed;
		int decompressedLength = s.read24BitInt();
		int compressedLength = s.read24BitInt();
		if (decompressedLength != compressedLength) {
			decompressed = BZIP2Compressor.decompress317(decompressedLength, compressedLength, s);
			extracted = true;
		} else {
			decompressed = s.readBytes(s.remaining());
		}

		InputBuffer stream = new InputBuffer(decompressed);
		int filesLength = stream.readUnsignedShort();
		setFileIds(filesLength);
		setFiles(filesLength);
		InputBuffer filesInputBuffer = new InputBuffer(stream.raw());
		filesInputBuffer.setOffset(stream.getOffset() + filesLength * 10);
		for (int i = 0; i < filesLength; i++) {
			int fileName = stream.readInt();
			decompressedLength = stream.read24BitInt();
			compressedLength = stream.read24BitInt();
			byte[] data;
			if (extracted) {
				data = filesInputBuffer.readBytes(decompressedLength);
			} else {
				data = BZIP2Compressor.decompress317(decompressedLength, compressedLength, filesInputBuffer);
			}
			fileIds()[i] = i;
			files()[i] = new File(i, data, fileName);
		}
		return read = true;
	}

	@Override
	public byte[] write(OutputBuffer outputBuffer) {
		if (compressionType == CompressionType.GZIP) {
			return GZIPCompressor.inflate317(files()[0] == null ? new byte[0] : files()[0].getData());
		}
		int length = files().length;
		outputBuffer.writeShort(length);
		final OutputBuffer out = new OutputBuffer(2048);
		for (int i = 0; i < length; i++) {
			File file = files()[i];
			outputBuffer.writeInt(file.getName());
			outputBuffer.write24BitInt(file.getData().length);
			byte[] toWrite = extracted ? file.getData() : BZIP2Compressor.compress(file.getData());
			outputBuffer.write24BitInt(toWrite.length);

			out.writeBytes(toWrite);
		}
		outputBuffer.writeBytes(out.array());

		byte[] data = outputBuffer.array();

		byte[] compressed = extracted ? BZIP2Compressor.compress(data) : data;

		final OutputBuffer finalStream = new OutputBuffer(compressed.length + 6);
		finalStream.write24BitInt(data.length);
		finalStream.write24BitInt(compressed.length);

		finalStream.writeBytes(compressed);
		return finalStream.array();
	}

	public CompressionType getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(CompressionType compressionType) {
		this.compressionType = compressionType;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
