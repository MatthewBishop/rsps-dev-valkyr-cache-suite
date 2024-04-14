/**
 * 
 */
package store.loader;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.Getter;
import store.ValkyrCacheLibrary;
import store.cache.index.OSRSIndices;
import com.displee.cache.index.archive.Archive;
import com.displee.cache.index.archive.file.File;
import store.codec.model.Mesh;

/**
 * @author ReverendDread
 * Oct 1, 2019
 */
public class MeshLoader implements DefinitionLoader {

	@Getter private static Map<Integer, Mesh> meshes = Maps.newHashMap();
	
	public MeshLoader() {
		long start = System.currentTimeMillis();
		if (!initialize())
			System.err.println("Failed to load meshes.");
		else
			System.out.println("[MeshLoader] Took " + (System.currentTimeMillis() - start) + " millis to load "
					+ meshes.size() + " meshes.");
	}
	
	@Override
	public boolean initialize() {
		try {
			int size = ValkyrCacheLibrary.getIndex(OSRSIndices.MODELS).getLastArchive().getId();
			for (int id = 0; id < size; id++) {
				Mesh mesh = new Mesh(id);
				Archive archive = ValkyrCacheLibrary.getIndex(OSRSIndices.MODELS).getArchive(id);
				if (archive == null)
					continue;
				File file = archive.file(0);
				if (file == null || file.getData() == null)
					continue;
				meshes.put(id, mesh);
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public void reload() {
		meshes.clear();
		initialize();
	}
	
}
