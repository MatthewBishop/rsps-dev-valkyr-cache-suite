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
		return store.index(3).getLastArchive().getId() + 1;
	}

	public static final int getInterfaceDefinitionsComponentsSize(CacheLibrary store, int interfaceId) {
		return store.index(3).getArchive(interfaceId).last().getId() + 1;
	}

	public static final int getRenderAnimationDefintionsSize(CacheLibrary store) {
		return store.index(2).getArchive(32).last().getId() + 1;
	}

	public static final int getAnimationDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(20).getLastArchive().getId();
		return lastArchiveId * 128 + store.index(20).getArchive(lastArchiveId).last().getId();
	}

	public static final int getItemDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(19).getLastArchive().getId();
		return lastArchiveId * 256 + store.index(19).getArchive(lastArchiveId).last().getId();
	}

	public static int getNPCDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(18).getLastArchive().getId();
		return lastArchiveId * 256 + store.index(18).getArchive(lastArchiveId).last().getId();
	}

	public static final int getObjectDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(16).getLastArchive().getId();
		return lastArchiveId * 256 + store.index(16).getArchive(lastArchiveId).last().getId();
	}

	public static final int getGraphicDefinitionsSize(CacheLibrary store) {
		int lastArchiveId = store.index(21).getLastArchive().getId();
		return lastArchiveId * 256 + store.index(21).getArchive(lastArchiveId).last().getId();
	}

	public static int getTextureDiffuseSize(CacheLibrary store) {
		return store.index(9).getLastArchive().getId();
	}

	public static int getSpriteDefinitionSize(CacheLibrary store) {
		return store.index(8).getLastArchive().getId();
	}

	public static int getParticleConfigSize(CacheLibrary store) {
		return store.index(27).getArchive(0).last().getId() + 1;
	}

	public static int getMagnetConfigSize(CacheLibrary store) {
		return store.index(27).getArchive(1).last().getId() + 1;
	}

	public static int getConfigArchive(int id, int bits) {
		return (id) >> bits;
	}

	public static int getConfigFile(int id, int bits) {
		return (id) & (1 << bits) - 1;
	}

}
