package misc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;

import lombok.extern.slf4j.Slf4j;
import com.displee.cache.CacheLibrary;
import store.ValkyrCacheLibrary;
import store.cache.index.OSRSIndices;
import com.displee.cache.index.archive.Archive;
import store.codec.util.Utils;
import store.progress.AbstractProgressListener;
import utility.XTEASManager;

/**
 * @author ReverendDread Jan 2, 2018
 */
@Slf4j
@SuppressWarnings("unused")
public class PureOSRSPacker {

	private static final DecimalFormat format = new DecimalFormat("#.##");

	public static final String CACHE_DIR = "G:\\pvmcache\\";
	private static final String OSRS_CACHE = "C:\\Users\\Andrew\\Desktop\\187\\";
	private static final String OSRS_MAPS = "C:\\Users\\Andrew\\Desktop\\177\\";
	private static final String BASES_DIR = "C:\\Users\\Andrew\\Desktop\\dumps\\bases\\";
	private static final String FRAMES_DIR = "C:\\Users\\Andrew\\Desktop\\dumps\\frames\\";
	private static final String SEQS_DIR = "C:\\Users\\Andrew\\Desktop\\seqs\\";
	private static final String NORMAL_CACHE = "F:\\Jankscape Server V1\\data\\cache\\";

	private static final int OVERLAY_OFFSET = 1000;
	private static final int UNDERLAY_OFFSET = 1000;
	private static final int MODEL_OFFSET = 100_000;
	private static final int OBJECT_OFFSET = 100_000;
	private static final int FRAMES_OFFSET = 8000;
	private static final int BASE_OFFSET = 8000;
	private static final int SPOTS_OFFSET = 5000;
	private static final int SEQS_OFFSET = 20000;

	private final static AbstractProgressListener progress = new AbstractProgressListener() {

		@Override
		public void finish(String title, String message) {
			System.out.println("Done!");
		}

		@Override
		public void change(double progress, String message) {
			System.out.println("[" + progress + "%]: " + message);
		}

	};

	public static void main(String[] args) throws IOException {
		
		CacheLibrary cache = new CacheLibrary(CACHE_DIR, true, progress);
		ValkyrCacheLibrary.singleton = cache;
		CacheLibrary osrs_cache = new CacheLibrary(OSRS_CACHE, true, progress);
		ValkyrCacheLibrary.singleton = osrs_cache;
		pack_osrs_dat(cache, osrs_cache, false);
		
	}

	public static void pack_osrs_dat(CacheLibrary cache, CacheLibrary osrs_cache, boolean resetIndices)
			throws IOException {
		dump_maps(osrs_cache);
		import_models(osrs_cache, cache, resetIndices);
		import_items(osrs_cache, cache, resetIndices);
		import_objects(osrs_cache, cache, resetIndices);
//		import_idk(osrs_cache, cache, resetIndices);
		import_npcs(osrs_cache, cache, resetIndices);
		import_maps(cache, osrs_cache, resetIndices);
//		import_frames(osrs_cache, cache, resetIndices);
//		import_skins(osrs_cache, cache, resetIndices);
//		import_sequences(osrs_cache, cache, resetIndices);
		import_flus(osrs_cache, cache);
		import_flos(osrs_cache, cache);
		import_gfx(osrs_cache, cache);
//		create_textures_index(osrs_cache, cache);
	}

	public static void create_textures_index(CacheLibrary osrs_cache, CacheLibrary cache)
			throws NumberFormatException, IOException {
		File[] textures = new File("C:\\Users\\Andrew\\Desktop\\osrs textures\\").listFiles();
		for (File file : textures) {
			cache.index(37).add(Integer.parseInt(stripExtension(file.getName()))).add(0, Files.readAllBytes(file.toPath()));
		}
		cache.index(37).update(progress);
	}

	private static void pack_map(int region, CacheLibrary cache, File map, File landscape) throws IOException {
		int regionX = region >> 8;
		int regionY = region & 0xff;
		byte[] data = Files.readAllBytes(map.toPath());
		String name = "m" + regionX + "_" + regionY;
		int hash = Utils.getNameHash(name);
		int archive = ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).archiveId(name);
		int lastArchive = ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).last().getId() + 1;
		if (data != null) { // map pack
			ValkyrCacheLibrary.add2(ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS), archive == -1 ? lastArchive : archive, hash, true).add(0,
					data);
		}
		data = Files.readAllBytes(landscape.toPath());
		name = "l" + regionX + "_" + regionY;
		hash = Utils.getNameHash(name);
		archive = ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).archiveId(name);
		lastArchive = ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).last().getId() + 1;
		if (data != null) {
			ValkyrCacheLibrary.add2(ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS), archive == -1 ? lastArchive : archive, hash, true).add(0,
					data);
		}
		if (ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).flagged()) {
			ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).update(progress);
		}
	}

	private static void import_sequences(CacheLibrary osrs_cache, CacheLibrary cache, boolean resetIndices) {
		if (resetIndices) {
			int clear = cache.index(20).last().getId() + 1;
			for (int archive = 0; archive < clear; archive++) {
				cache.index(20).remove(archive);
			}
			if (cache.index(20).flagged()) {
				cache.index(20).update(progress);
			}
		}
		int size = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.CONFIG).archive(12).last().getId() + 1;
		for (int sequence = 0; sequence < size; sequence++) {
			try {
				Archive archive = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.CONFIG).archive(12);
				byte[] data = archive.file(sequence).getData();
				System.out.println("Packing sequence " + sequence + "/" + size + "...");
				cache.index(20).add(Utils.getConfigArchive(sequence, 7))
						.add(Utils.getConfigFile(sequence, 7), data);
			} catch (Exception ex) {
				System.err.println("Missing data for sequence " + sequence);
			}
		}
		if (cache.index(20).flagged()) {
			cache.index(20).update(progress);
		}
	}

	private static void import_skins(CacheLibrary osrs_cache, CacheLibrary cache, boolean resetIndices) {
		if (resetIndices) {
			int clear = cache.index(1).last().getId() + 1;
			for (int archive = 0; archive < clear; archive++) {
				cache.index(1).remove(archive);
			}
			if (cache.index(1).flagged()) {
				cache.index(1).update(progress);
			}
		}
		int size = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.SKINS).last().getId() + 1;
		for (int skin = 0; skin < size; skin++) {
			try {
				Archive archive = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.SKINS).archive(skin);
				System.out.println("Packing skin " + skin + "/" + size + "...");
				ValkyrCacheLibrary.add2(cache.index(1), archive, true, true, skin);
			} catch (Exception ex) {
				System.err.println("Missing data for skin " + skin);
			}
		}
		if (cache.index(1).flagged()) {
			cache.index(1).update(progress);
		}
	}

	public static void import_frames(CacheLibrary osrs_cache, CacheLibrary cache, boolean resetIndices) {
		if (resetIndices) {
			int clear = cache.index(0).last().getId() + 1;
			for (int archive = 0; archive < clear; archive++) {
				cache.index(0).remove(archive);
			}
			if (cache.index(0).flagged()) {
				cache.index(0).update(progress);
			}
		}
		int size = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.SKELETONS).last().getId() + 1;
		for (int frameset = 0; frameset < size; frameset++) {
			try {
				Archive archive = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.SKELETONS).archive(frameset);
				System.out.println("Packing Frameset " + frameset + "/" + size + "...");
				ValkyrCacheLibrary.add2(cache.index(0), archive, true, true, frameset);
			} catch (Exception ex) {
				System.err.println("Missing data for frameset " + frameset);
			}
		}
		if (cache.index(0).flagged()) {
			cache.index(0).update(progress);
		}
	}

	public static void import_npcs(CacheLibrary osrs_cache, CacheLibrary cache, boolean resetIndices) {
		if (resetIndices) {
			int clear = cache.index(18).last().getId() + 1;
			for (int file = 0; file < clear; file++) {
				cache.index(18).remove(file);
			}
			if (cache.index(18).flagged()) {
				cache.index(18).update(progress);
			}
		}
		int size = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.CONFIG).archive(9).last().getId();
		for (int npc = 0; npc < size; npc++) {
			try {
				byte[] data = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.CONFIG).archive(9).file(npc).getData();
				System.out.println("Packing NPC " + npc + "/" + size + "...");
				cache.index(18).add(getConfigArchive(npc, 7)).add(getConfigFile(npc, 7), data);
			} catch (Exception ex) {
				System.err.println("Missing data for NPC " + npc);
			}
		}
		if (cache.index(18).flagged()) {
			cache.index(18).update(progress);
		}
	}

	public static void import_idk(CacheLibrary osrs_cache, CacheLibrary cache, boolean resetIndices) {
		if (resetIndices) {
			int clear = cache.index(2).archive(3).last().getId();
			for (int file = 0; file < clear; file++) {
				cache.index(2).remove(3);
			}
			if (cache.index(2).flagged()) {
				cache.index(2).update(progress);
			}
		}
		int kits = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.CONFIG).archive(3).last().getId();
		for (int kit = 0; kit < kits; kit++) {
			try {
				byte[] data = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.CONFIG).archive(3).file(kit).getData();
				System.out.println("Packing IDK " + kit + "/" + kits + "...");
				if (data != null) {
					cache.index(2).add(3).add(kit, data);
				}
			} catch (Exception ex) {
				System.err.println("Missing data for IDK " + kit);
			}
		}
		if (cache.index(2).flagged()) {
			cache.index(2).update(progress);
		}
	}

	public static void import_maps(CacheLibrary cache, CacheLibrary osrs_cache, boolean resetIndices)
			throws IOException {
		int hash;
		int archive;
		byte[] data;
		int sample = 32768;

//		int i = cache.getIndex(OSRSIndices.MAPS).getLastArchive().getId() + 1;
//		for (int region = 0; region < sample; region++) {
//			cache.getIndex(OSRSIndices.MAPS).removeArchive(region);
//		}
//		cache.getIndex(5).update(progress);

		for (int region = 0; region < sample; region++) {
			int regionX = region >> 8;
			int regionY = region & 0xff;
			System.out.println("Packing region " + region + "/" + sample + "...");
			File file = new File("C:\\Users\\Andrew\\Desktop\\maps raw\\m" + regionX + "_" + regionY);
			if (file.exists()) {
				// String name = stripDat(file.getName());
				data = Files.readAllBytes(file.toPath());
				hash = Utils.getNameHash(file.getName());
				archive = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.MAPS).archiveId(file.getName());
				ValkyrCacheLibrary.add2(cache.index(5), archive, hash, true).add(0, data);
			}
			file = new File("C:\\Users\\Andrew\\Desktop\\maps raw\\l" + regionX + "_" + regionY);
			if (file.exists()) {
				// String name = stripDat(file.getName());
				data = Files.readAllBytes(file.toPath());
				hash = Utils.getNameHash(file.getName());
				archive = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.MAPS).archiveId(file.getName());
				ValkyrCacheLibrary.add2(cache.index(5), archive, hash, true).add(0, data);
			}
		}
		if (cache.index(5).flagged()) {
			cache.index(5).update(progress);
		}
	}

	private static void dump_maps(CacheLibrary cache) throws IOException {
		for (int region = 0; region < 32768; region++) {

			int x = (region >> 8);
			int y = (region & 0xff);

			byte[] data;
			File file;
			DataOutputStream dos;
			int[] xteas = XTEASManager.lookup(region);

			// Map
			int map = ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).archiveId("m" + x + "_" + y);
			if (map != -1) {
				data = ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).archive(map, xteas).file(0).getData();
				if (data == null)
					continue;
				file = new File("./maps/", "m" + x + "_" + y + ".dat");
				dos = new DataOutputStream(new FileOutputStream(file));
				dos.write(data);
				dos.close();
				System.out.println("Dumped map " + file.getName());
			}

			// Locations
			int location = ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).archiveId("l" + x + "_" + y);
			if (location != -1) {
				data = ValkyrCacheLibrary.getIndex(cache, OSRSIndices.MAPS).archive(location, xteas).file(0).getData();
				if (data == null)
					continue;
				file = new File("./maps/", "l" + x + "_" + y + ".dat");
				dos = new DataOutputStream(new FileOutputStream(file));
				dos.write(data);
				dos.close();
				System.out.println("Dumped landscape " + file.getName());
			}

		}
	}

	public static void import_items(CacheLibrary osrs, CacheLibrary cache, boolean resetIndices) {

		if (resetIndices) {
			int clear = cache.index(19).last().getId();
			for (int index = 0; index < clear; index++) {
				cache.index(19).remove(index);
				System.out.println("Removing archive " + index + ".");
			}
			cache.index(19).update(progress);
		}

		int size = ValkyrCacheLibrary.getIndex(osrs, OSRSIndices.CONFIG).archive(10).last().getId();
		int packed = 0;
		for (int index = 0; index < size; index++) {
			Archive archive = ValkyrCacheLibrary.getIndex(osrs, OSRSIndices.CONFIG).archive(10);
			if (archive == null)
				continue;
			byte[] data = archive.file(index).getData();
			if (data == null)
				continue;
			cache.index(19).add(getConfigArchive(index, 8)).add(getConfigFile(index, 8), data);
			packed++;
		}
		cache.index(19).update(progress);
	}

	public static void import_objects(CacheLibrary osrs_cache, CacheLibrary cache, boolean resetIndices) {

		if (resetIndices) {
			int clear = cache.index(16).last().getId();
			for (int index = 0; index < clear; index++) {
				cache.index(16).remove(index);
				System.out.println("Removing archive " + index + ".");
			}
			cache.index(16).update(progress);
		}

		double percentage;
		int valid_objects = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.CONFIG).archive(6).last().getId();
		for (int id = 0; id < valid_objects; id++) {
			try {
				byte[] data = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.CONFIG).archive(6).file(id).getData();
				if (data == null)
					continue;
				percentage = (double) id / (double) valid_objects * 100D;
				System.out.println("[" + percentage + "%]: Packing object " + id + "...");
				cache.index(16).add(getConfigArchive(id, 8)).add(getConfigFile(id, 8), data);
			} catch (Exception ex) {
				System.err.println("No data exists for object " + id + ".");
			}
		}
		cache.index(16).update(progress);
	}

	public static void import_models(CacheLibrary osrs_cache, CacheLibrary cache, boolean reset) throws IOException {
		double percentage;
		int valid_models = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.MODELS).last().getId();
		for (int id = 0; id < valid_models; id++) {
			Archive archive = ValkyrCacheLibrary.getIndex(osrs_cache, OSRSIndices.MODELS).archive(id);
			if (archive == null)
				continue;
			byte[] data = archive.file(0).getData();
			if (data == null)
				continue;
			percentage = (double) id / (double) valid_models * 100D;
			System.out.println("[" + format.format(percentage) + "%]: Packing model " + id + "...");
			cache.index(7).add(id).add(0, data);
		}
		cache.index(7).update(progress);
	}

	public static void import_gfx(CacheLibrary osrs_cache, CacheLibrary cache) {
		int length = osrs_cache.index(2).archive(13).last().getId() + 1;
		for (int index = 0; index < length; index++) {
			try {
				byte[] data = osrs_cache.index(2).archive(13).file(index).getData();
				System.out.println("Packing gfx " + index + "/" + length + "...");
				cache.index(21).add(Utils.getConfigArchive(index, 8)).add(Utils.getConfigFile(index, 8),
						data);
			} catch (Exception ex) {
				System.err.println("Missing data for gfx " + index + ".");
			}
		}
		cache.index(21).update(progress);
	}

	private static final void import_flos(CacheLibrary osrs_cache, CacheLibrary cache) throws IOException {
		int size = osrs_cache.index(2).archive(4).last().getId();
		Archive archive = osrs_cache.index(2).archive(4);
		for (int id = 0; id < size; id++) {
			com.displee.cache.index.archive.file.File file = archive.file(id);
			if (file == null)
				continue;
			byte[] data = file.getData();
			if (data == null)
				continue;
			cache.index(2).add(4).add(id, data);
			System.out.println("Packed overlay " + file);
		}
		cache.index(2).update(progress);
	}

	private static final void import_flus(CacheLibrary osrs_cache, CacheLibrary cache) throws IOException {
		int size = osrs_cache.index(2).archive(1).last().getId();
		Archive archive = osrs_cache.index(2).archive(1);
		for (int id = 0; id < size; id++) {
			com.displee.cache.index.archive.file.File file = archive.file(id);
			if (file == null)
				continue;
			byte[] data = file.getData();
			if (data == null)
				continue;
			cache.index(2).add(1).add(id, data);
			System.out.println("Packed underlay " + id);
		}
		cache.index(2).update(progress);
	}
//
//	private static void transport_archive(CacheLibrary source_cache, int source_id, CacheLibrary target_cache, int target_id, int group_id) throws IOException {
//		transport_archive(source_cache, source_id, target_cache, target_id, group_id, true);
//	}

//	private static void transport_archive(CacheLibrary source_cache, int source_id, CacheLibrary target_cache, int target_id, int group_id, boolean rewrite) throws IOException {
//		Index target_index = target_cache.getIndexes()[target_id];
//		System.out.println("Attempting to transport group of id " + group_id + "..");
//		if (source_cache.getIndexes()[source_id].archiveExists(group_id)) {
//			target_index.putArchive(source_id + 1000, group_id, source_cache, false, true);
//		}
//		if (rewrite) {
//			System.out.println("\t ^ Rewriting table..");
//			target_index.rewriteTable();
//		}
//		System.out.println("\t ^ Finished!");
//		System.out.println();
//	}
//	
//	private static void transport_index(CacheLibrary source_cache, int source_id, CacheLibrary target_cache, int target_id) throws IOException {
//		System.out.println("Attempting to transport index from source id of " + source_id + " and target id of " + target_id);
//		Index source_index = source_cache.getIndexes()[source_id];
//		if (target_cache.getIndexes().length <= target_id) {
//			if (target_cache.getIndexes().length != target_id) {
//				throw new IllegalStateException("The cache has more than one gap between the source_index and the target_index!");
//			}
//			target_cache.addIndex(source_index.getTable().isNamed(), source_index.getTable().usesWhirpool(), Constants.GZIP_COMPRESSION);
//			System.out.println("\t ^ Index was created!");
//		}
//
//		Index target_index = target_cache.getIndexes()[target_id];
//		int num_groups = source_index.getLastArchiveId() + 1;
//		System.out.println("\t ^ Attempting to pack " + num_groups + " group(s)..");
//
//		double last = 0.0D;
//		for (int group_id = 0; group_id < num_groups; group_id++) {
//			if (source_index.archiveExists(group_id)) {
//				target_index.putArchive(source_id, group_id, source_cache, false, true);
//				double percentage = (double) group_id / (double) num_groups * 100D;
//				if (group_id == num_groups - 1 || percentage - last >= 1.0D) {
//					System.out.println("\t ^ Percentage Completed: " + format.format(percentage) + "%");
//					last = percentage;
//				}
//			}
//		}
//		System.out.println("\t ^ Rewriting table..");
//		target_index.rewriteTable();
//		System.out.println("\t ^ Finished!");
//		System.out.println();
//	}
//	
//	public static void reset_index(CacheLibrary store, int index) {
//		try {
//			System.out.println("Resetting index " + index + " data...");
//			store.resetIndex(index, store.getIndexes()[index].getTable().isNamed(), store.getIndexes()[index].getTable().usesWhirpool(), store.getIndexes()[index].getTable().getCompression());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private static int getConfigArchive(int id, int bits) {
		return (id) >> bits;
	}

	private static int getConfigFile(int id, int bits) {
		return (id) & (1 << bits) - 1;
	}

	private static String stripDat(String name) {
		return name.substring(0, name.length() - ".dat".length());
	}

	private static String stripExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	private static int stripId(String name) {
		return Integer.parseInt(name.substring(0, name.length() - ".dat".length()));
	}

}
