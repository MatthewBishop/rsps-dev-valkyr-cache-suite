import store.ValkyrCacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.archive.file.File;
import com.displee.io.impl.InputBuffer;
import store.plugin.extension.LoaderExtensionBase;
import suite.controller.Selection;

/**
 * @author ReverendDread on 5/17/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project ValkyrCacheSuite
 */
public class EnumLoader extends LoaderExtensionBase {

    @Override
    public boolean load() {
        try {
            Index index = ValkyrCacheLibrary.get().index(getIndex());
            int[] fileIds = index.archive(getArchive()).fileIds();
            for (int id : fileIds) {
                File file = index.archive(getArchive()).file(id);
                if (file == null || file.getData() == null)
                    continue;
                EnumConfig definition = new EnumConfig();
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
        return 8;
    }

    @Override
    public int getIndex() {
        return 2;
    }
}
