package store.codec.util;

import java.math.BigInteger;

import com.displee.cache.CacheLibrary;
import com.displee.io.impl.OutputBuffer;

public final class Utils {

	public static byte[] cryptRSA(byte[] data, BigInteger exponent, BigInteger modulus) {
		return (new BigInteger(data)).modPow(exponent, modulus).toByteArray();
	}

	public static byte[] getArchivePacketData(int indexId, int archiveId, byte[] archive) {
		OutputBuffer stream = new OutputBuffer(archive.length + 4);
		stream.writeByte(indexId);
		stream.writeShort(archiveId);
		stream.writeByte(0);
		stream.writeInt(archive.length);
		int offset = 8;

		for (int var6 = 0; var6 < archive.length; ++var6) {
			if (offset == 512) {
				stream.writeByte(-1);
				offset = 1;
			}

			stream.writeByte(archive[var6]);
			++offset;
		}
		return stream.array();
	}

	public static int getNameHash(String name) {
		return name.toLowerCase().hashCode();
	}

	public static final int getInterfaceDefinitionsSize(CacheLibrary store) {
		return store.index(3).last().getId() + 1;
	}

	public static final int getInterfaceDefinitionsComponentsSize(CacheLibrary store, int interfaceId) {
		return store.index(3).archive(interfaceId).last().getId() + 1;
	}

	public static final int getRenderAnimationDefintionsSize(CacheLibrary store) {
		return store.index(2).archive(32).last().getId() + 1;
	}

	public static final int getAnimationDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(20).last().getId();
		return lastArchiveId * 128 + store.index(20).archive(lastArchiveId).last().getId();
	}

	public static final int getItemDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(19).last().getId();
		return lastArchiveId * 256 + store.index(19).archive(lastArchiveId).last().getId();
	}

	public static int getNPCDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(18).last().getId();
		return lastArchiveId * 256 + store.index(18).archive(lastArchiveId).last().getId();
	}

	public static final int getObjectDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(16).last().getId();
		return lastArchiveId * 256 + store.index(16).archive(lastArchiveId).last().getId();
	}

	public static final int getGraphicDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(21).last().getId();
		return lastArchiveId * 256 + store.index(21).archive(lastArchiveId).last().getId();
	}

	public static int getTextureDiffuseSize(CacheLibrary store) {
		return store.index(9).last().getId();
	}

	public static int getSpriteDefinitionSize(CacheLibrary store) {
		return store.index(8).last().getId();
	}

	public static int getParticleConfigSize(CacheLibrary store) {
		return store.index(27).archive(0).last().getId() + 1;
	}

	public static int getMagnetConfigSize(CacheLibrary store) {
		return store.index(27).archive(1).last().getId() + 1;
	}

	public static int getConfigArchive(int id, int bits) {
		return (id) >> bits;
	}

	public static int getConfigFile(int id, int bits) {
		return (id) & (1 << bits) - 1;
	}

}
