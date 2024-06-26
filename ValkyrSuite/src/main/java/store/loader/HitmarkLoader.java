/**
 * 
 */
package store.loader;

import lombok.Getter;
import com.displee.cache.CacheLibrary;
import com.displee.cache.index.Index;
import store.codec.HitmarkDefinition;
import com.displee.io.impl.InputBuffer;

/**
 * @author ReverendDread Jan 14, 2019
 */
public class HitmarkLoader implements DefinitionLoader {

	private CacheLibrary cache;

	@Getter
	private static HitmarkDefinition[] definitions;

	public HitmarkLoader(CacheLibrary cache) {
		this.cache = cache;
		long start = System.currentTimeMillis();
		if (!initialize())
			System.err.println("Failed to load Hit Definitions.");
		else
			System.out.println("[HitLoader] Took " + (System.currentTimeMillis() - start) + " millis to load "
					+ definitions.length + " definitions.");
	}

	/* (non-Javadoc)
	 * @see com.alex.loaders.DefinitionLoader#initialize()
	 */
	@Override
	public boolean initialize() {
		try {
			Index index = cache.index(2);
			int size = index.archive(46).last().getId();
			definitions = new HitmarkDefinition[size];
			for (int id = 0; id < size; id++) {
				HitmarkDefinition definition = new HitmarkDefinition(id);
				try {
					byte[] data = index.archive(46).file(id).getData();
					if (data != null) {
						definition.decode(new InputBuffer(data));
					}
				} catch (Exception e) {
					if (suite.Constants.settings.debug)
						System.err.println("No data for hitmark " + id + ".");
				}
				definitions[id] = definition;
			}
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.alex.loaders.DefinitionLoader#reload()
	 */
	@Override
	public void reload() {
		definitions = null;
		initialize();
	}

}
