/**
 * 
 */
package store.codec.osrs;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;
import store.ValkyrCacheLibrary;
import store.codec.IdentityKit;
import com.displee.io.impl.InputBuffer;

/**
 * @author ReverendDread Dec 3, 2018
 */
public class IdkConverter {

	private static final DecimalFormat format = new DecimalFormat("#.##");

	private static ArrayList<Integer> modelsToReplace = new ArrayList<Integer>();

	public static void main(String[] args) throws IOException {

		CacheLibrary cache = new CacheLibrary("", false, null);
		ValkyrCacheLibrary.singleton = cache;
		CacheLibrary old_cache = new CacheLibrary("C:\\Users\\Andrew\\Desktop\\667 cache\\", false, null);
		ValkyrCacheLibrary.singleton = old_cache;

		int size = old_cache.index(2).archive(3).last().getId();

		System.out.println("Reading " + size + " IdentityKits...");
		for (int index = 0; index < size; index++) {
			IdentityKit kit = new IdentityKit(index);
			byte[] data = old_cache.index(2).archive(3).file(index).getData();
			if (data != null) {
				kit.decode(new InputBuffer(data));
				for (int model = 0; model < kit.bodyModels.length; model++) {
					modelsToReplace.add(kit.bodyModels[model]);
				}
				for (int model = 0; model < kit.headModels.length; model++) {
					modelsToReplace.add(kit.headModels[model]);
				}
			} else {
				System.out.println("data is null for idk " + index);
			}
		}

		System.out.println("Replacing models...");
		modelsToReplace.forEach((model) -> {
			byte[] data = old_cache.index(7).archive(model).file(0).getData();
			if (data != null) {
				cache.index(7).add(model).add(0, data);
			}
		});

		System.out.println("Rewritting model reference table...");
		cache.index(7).update();

		System.out.println("Importing new identity kits...");
		transport_archive(old_cache, 2, cache, 2, 3);

	}

	private static void transport_index(CacheLibrary source_cache, int source_id, CacheLibrary target_cache,
			int target_id) throws IOException {
		System.out.println(
				"Attempting to transport index from source id of " + source_id + " and target id of " + target_id);
		Index source_index = source_cache.index(source_id);
		if (target_cache.indices().length <= target_id) {
			if (target_cache.indices().length != target_id) {
				throw new IllegalStateException(
						"The cache has more than one gap between the source_index and the target_index!");
			}
			ValkyrCacheLibrary.createIndex(target_cache, source_index.isNamed(), source_index.hasWhirlpool());
			System.out.println("\t ^ Index was created!");
		}

		Index target_index = target_cache.index(target_id);
		int num_groups = source_index.last().getId() + 1;
		System.out.println("\t ^ Attempting to pack " + num_groups + " group(s)..");

		double last = 0.0D;
		for (int group_id = 0; group_id < num_groups; group_id++) {
			if (source_index.archive(group_id) != null) {
				ValkyrCacheLibrary.add2(target_index, source_index.archive(group_id), true, false);
				double percentage = (double) group_id / (double) num_groups * 100D;
				if (group_id == num_groups - 1 || percentage - last >= 1.0D) {
					System.out.println("\t ^ Percentage Completed: " + format.format(percentage) + "%");
					last = percentage;
				}
			}
		}
		System.out.println("\t ^ Rewriting table..");
		target_index.update();
		System.out.println("\t ^ Finished!");
		System.out.println();
	}

	private static void transport_archive(CacheLibrary source_cache, int source_id, CacheLibrary target_cache,
			int target_id, int group_id) throws IOException {
		transport_archive(source_cache, source_id, target_cache, target_id, group_id, true);
	}

	private static void transport_archive(CacheLibrary source_cache, int source_id, CacheLibrary target_cache,
			int target_id, int group_id, boolean rewrite) throws IOException {
		Index target_index = target_cache.index(target_id);
		System.out.println("Attempting to transport group of id " + group_id + "..");
		if (source_cache.index(source_id).archive(group_id) != null) {
			ValkyrCacheLibrary.add2(target_index, source_cache.index(source_id).archive(group_id), true, false);
		}
		if (rewrite) {
			System.out.println("\t ^ Rewriting table..");
			target_index.update();
		}
		System.out.println("\t ^ Finished!");
		System.out.println();
	}

}
