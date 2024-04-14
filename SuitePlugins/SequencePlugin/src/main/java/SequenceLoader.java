import org.displee.CacheLibrary;
import org.displee.cache.index.Index;
import org.displee.cache.index.archive.file.File;
import org.displee.io.impl.InputStream;
import store.plugin.extension.LoaderExtensionBase;
import suite.controller.Selection;

/**
 * 
 */

/**
 * @author ReverendDread
 * Oct 6, 2019
 */
public class SequenceLoader extends LoaderExtensionBase {

	@Override
	public boolean load() {
		try {
			Index index = CacheLibrary.get().getIndex(getIndex());
			int[] files = index.getArchive(getArchive()).getFileIds();
			for (int id : files) {
				File file = index.getArchive(getArchive()).getFile(id);
				if (file == null || file.getData() == null)
					continue;
				SequenceConfig definition = new SequenceConfig();
				definition.id = id;
				InputStream buffer = new InputStream(file.getData());
				readConfig(buffer, definition);
				definitions.put(id, definition);
				Selection.progressListener.pluginNotify("(" + id + "/" + files.length + ")");
			}
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int getFile() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public int getArchive() {
		// TODO Auto-generated method stub
		return 12;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 2;
	}

}
