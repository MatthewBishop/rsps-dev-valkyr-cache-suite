import org.displee.CacheLibrary;
import org.displee.cache.index.Index;
import org.displee.cache.index.archive.file.File;
import com.displee.io.impl.InputBuffer;
import store.plugin.extension.LoaderExtensionBase;
import suite.controller.Selection;

public class BitLoader extends LoaderExtensionBase {
    @Override
    public boolean load() {
        try {
            Index index = CacheLibrary.get().getIndex(getIndex());
            int[] fileIds = index.getArchive(getArchive()).getFileIds();
            for (int id : fileIds) {
                File file = index.getArchive(getArchive()).getFile(id);
                if (file == null || file.getData() == null)
                    continue;
                BitConfig definition = new BitConfig();
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
        return 14;
    }

    @Override
    public int getIndex() {
        return 2;
    }
}
