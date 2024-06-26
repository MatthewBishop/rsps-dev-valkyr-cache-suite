import store.ValkyrCacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.archive.file.File;
import com.displee.io.impl.InputBuffer;
import store.plugin.extension.LoaderExtensionBase;
import suite.controller.Selection;

/**
 * 
 */

/**
 * @author ReverendDread
 * Oct 6, 2019
 */

public class ObjectLoader extends LoaderExtensionBase {

	@Override
	public boolean load() {
		try {
			Index index = ValkyrCacheLibrary.get().index(getIndex());
			int[] files = index.archive(getArchive()).fileIds();
			for (int id : files) {
				File file = index.archive(getArchive()).file(id);
				if (file == null || file.getData() == null)
					continue;
				ObjectConfig definition = new ObjectConfig();
				definition.setId(id);
				InputBuffer buffer = new InputBuffer(file.getData());
				readConfig(buffer, definition);
				definition.onDecodeFinish();
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
		return 6;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 2;
	}

}
