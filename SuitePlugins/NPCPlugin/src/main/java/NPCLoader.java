import lombok.extern.slf4j.Slf4j;
import org.displee.CacheLibrary;
import org.displee.cache.index.Index;
import org.displee.cache.index.archive.file.File;
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
@Slf4j
public class NPCLoader extends LoaderExtensionBase {

	@Override
	public boolean load() {
		try {
			Index index = CacheLibrary.get().getIndex(getIndex());
			int[] fileIds = index.getArchive(getArchive()).getFileIds();
			int size = fileIds.length;
			for (int id : fileIds) {
				File file = index.getArchive(getArchive()).getFile(id);
				if (file == null)
					continue;
				NPCConfig definition = new NPCConfig();
				if (file.getData() == null)
					continue;
				definition.id = id;
				InputBuffer buffer = new InputBuffer(file.getData());
				readConfig(buffer, definition);
				definitions.put(id, definition);
				Selection.progressListener.pluginNotify("(" + id + "/" + size + ")");
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
		return 9;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 2;
	}

}
