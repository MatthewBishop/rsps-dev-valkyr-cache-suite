import store.ValkyrCacheLibrary;
import com.displee.cache.index.Index;
import com.displee.cache.index.archive.file.File;
import com.displee.io.impl.InputBuffer;
import store.plugin.PluginType;
import store.plugin.extension.LoaderExtensionBase;
import suite.annotation.LoaderDescriptor;
import suite.controller.Selection;

/**
 * @author ReverendDread on 12/11/2019
 * https://www.rune-server.ee/members/reverenddread/
 * @project ValkyrCacheSuite
 */
@LoaderDescriptor(author = "ReverendDread", description = "Loads textures from the cache", version = "183", type = PluginType.TEXTURE)
public class TextureLoader extends LoaderExtensionBase {

    @Override
    public boolean load() {
        Index index = ValkyrCacheLibrary.get().index(getIndex());
        int[] fileIds = index.archive(getArchive()).fileIds();
        for (int id : fileIds) {
            File file = index.archive(getArchive()).file(id);
            if (file == null || file.getData() == null)
                continue;
            TextureConfig def = new TextureConfig();
            InputBuffer buffer = new InputBuffer(file.getData());
            def.setId(id);
            def.decode(-1, buffer);
            definitions.put(def.id, def);
            Selection.progressListener.pluginNotify("(" + id + "/" + fileIds.length + ")");
        }
        return true;
    }

    @Override
    public int getFile() {
        return -1;
    }

    @Override
    public int getArchive() {
        return 0;
    }

    @Override
    public int getIndex() {
        return 9;
    }

}
