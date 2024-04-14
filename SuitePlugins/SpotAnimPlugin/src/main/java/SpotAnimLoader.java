import store.ValkyrCacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.archive.file.File;
import com.displee.io.impl.InputBuffer;
import store.plugin.extension.LoaderExtensionBase;
import suite.controller.Selection;

/**
 * @author ReverendDread on 5/21/2020
 * https://www.rune-server.ee/members/reverenddread/
 * @project ValkyrCacheSuite
 */
public class SpotAnimLoader extends LoaderExtensionBase {

    @Override
    public boolean load() {
        try {
            Index index = ValkyrCacheLibrary.get().index(getIndex());
            int[] filesIds = index.archive(getArchive()).fileIds();
            for (int fileId : filesIds) {
                File file = index.archive(getArchive()).file(fileId);
                if (file == null || file.getData() == null)
                    continue;
                SpotAnimConfig definition = new SpotAnimConfig();
                definition.setId(fileId);
                InputBuffer buffer = new InputBuffer(file.getData());
                readConfig(buffer, definition);
                definitions.put(fileId, definition);
                Selection.progressListener.pluginNotify("(" + fileId + "/" + filesIds.length + ")");
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public int getFile() {
        return -1;
    }

    @Override
    public int getArchive() {
        return 13;
    }

    @Override
    public int getIndex() {
        return 2;
    }

}
