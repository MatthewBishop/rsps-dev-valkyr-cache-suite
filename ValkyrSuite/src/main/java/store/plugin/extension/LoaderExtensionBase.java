/**
 * 
 */
package store.plugin.extension;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.displee.cache.CacheLibrary;
import store.ValkyrCacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.archive.Archive;
import com.displee.io.impl.InputBuffer;

/**
 * @author ReverendDread
 * Oct 6, 2019
 */
@Slf4j
public abstract class LoaderExtensionBase {

	@Getter
	protected Map<Integer, ConfigExtensionBase> definitions = Maps.newHashMap();
	
	public LoaderExtensionBase() {
		load();
	}
	
	public abstract boolean load();
	
	public abstract int getFile();
	
	public abstract int getArchive();
	
	public abstract int getIndex();
	
	public void reload() {
		definitions.clear();
		load();
	}

	protected byte[] getConfigFile317(String name) {
		CacheLibrary cache = ValkyrCacheLibrary.get();
		Index index = cache.index(0);
		Archive archive = index.archive(2);
		return archive.file(name).getData();
	}

	protected void readConfig(InputBuffer buffer, ConfigExtensionBase definition) {
		for (;;) {
			int opcode = buffer.readUnsignedByte();
			if (opcode == 0)
				break;
			definition.decode(opcode, buffer);
		}
	}
	
}
