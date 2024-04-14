import com.displee.io.impl.InputBuffer
import store.ValkyrCacheLibrary
import store.plugin.PluginType
import suite.annotation.LoaderDescriptor
import suite.controller.Selection
import java.util.*

/**
 *
 * @project ValkyrCacheSuite
 * @author ReverendDread on 3/22/2020
 * https://www.rune-server.ee/members/reverenddread/
 */
@LoaderDescriptor(author = "ReverendDread", version = "317/OSRS/742", type = PluginType.FLO)
class OverlayLoader {

    val overlays: HashMap<Int, OverlayConfig> = hashMapOf()

    fun load(): OverlayLoader {
        try {
            val index = ValkyrCacheLibrary.get().index(index)
            val files = index.archive(archive).fileIds()
            for (id in files) {
                val file = index.archive(archive).file(id)
                file?.apply {
                    val definition = OverlayConfig()
                    val buffer = InputBuffer(file.data)
                    buffer.raw().apply {
                        while (true) {
                            val opcode = buffer.readUnsignedByte()
                            if (opcode == 0) break
                            definition.decode(opcode, buffer)
                        }
                    }
                    overlays[id] = definition
                    Selection.progressListener.pluginNotify("($id/${files.size})")
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return this
    }

    companion object {
        val index = 2;
        val archive = 4;
    }

}