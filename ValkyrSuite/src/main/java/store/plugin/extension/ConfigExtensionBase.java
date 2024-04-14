/**
 * 
 */
package store.plugin.extension;

import javafx.scene.image.Image;
import javafx.util.Pair;
import lombok.Setter;
import org.displee.cache.index.archive.file.File;
import org.displee.CacheLibrary;
import org.displee.cache.index.Index;
import org.displee.cache.index.archive.Archive;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author ReverendDread Sep 14, 2019
 */
@Setter
public abstract class ConfigExtensionBase implements Cloneable {

	public int id;

	public int previousOpcodeIndex;
	public int[] previousOpcodes;

	public void decode(int opcode, InputBuffer buffer) {}

	public void decode(InputBuffer buffer) {}

	public abstract OutputBuffer encode(OutputBuffer buffer);

	public OutputBuffer[] encodeConfig317(String fileName) {
		return null;
	}

	public byte[] encode317() {
		return null;
	}

	public abstract String toString();

	public abstract Map<Field, Integer> getPriority();

	public List<Integer> getMeshIds() { return null; }

	public List<Pair<Integer, Integer>> getRecolors() { return null; }

	public List<Pair<Integer, Integer>> getRetextures() { return null; }

	public List<Image> getImages() { return null; }

	public void onDecodeFinish() {}

	protected byte[] getConfigFile317(String name) {
		CacheLibrary cache = CacheLibrary.get();
		Index index = cache.getIndex(0);
		Archive archive = index.getArchive(2);
		File file = archive.file(name);
		return file.getData();
	}
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void onCreate() {

	}

	public void copy(Object from) {

	}
	
}
