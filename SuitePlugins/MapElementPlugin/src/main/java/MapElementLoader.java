import store.ValkyrCacheLibrary;
import org.displee.cache.index.Index;
import org.displee.cache.index.archive.file.File;
import com.displee.io.impl.InputBuffer;
import store.plugin.extension.LoaderExtensionBase;
import suite.controller.Selection;

public class MapElementLoader extends LoaderExtensionBase {
    @Override
    public boolean load() {
        try {
            Index index = ValkyrCacheLibrary.get().getIndex(getIndex());
            int[] fileIds = index.getArchive(getArchive()).fileIds();
            for (int id : fileIds) {
                File file = index.getArchive(getArchive()).file(id);
                if (file == null || file.getData() == null)
                    continue;
                MapElementConfig definition = new MapElementConfig();
                definition.id = id;
                InputBuffer buffer = new InputBuffer(file.getData());
                readConfig(buffer, definition);
                definitions.put(id, definition);
                Selection.progressListener.pluginNotify("(" + id + "/" + fileIds.length + ")");
            }
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public int getFile() {
        return -1;
    }

    @Override
    public int getArchive() {
        return 35;
    }

    @Override
    public int getIndex() {
        return 2;
    }
}
