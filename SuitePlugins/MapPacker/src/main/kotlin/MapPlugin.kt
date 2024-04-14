import store.plugin.Plugin
import store.plugin.PluginType
import suite.annotation.PluginDescriptor

/**
 * @author Andrew on 11/25/2019
 * @project ValkyrCacheSuite
 */
@PluginDescriptor(
    author = "ReverendDread",
    description = "Pack's maps into the cache.",
    version = "183",
    type = PluginType.MAP
)
class MapPlugin : Plugin() {
    override fun load(): Boolean {
        setController(MapPacker())
        underlays = UnderlayLoader().load()
        overlays = OverlayLoader().load()
        return true
    }

    override fun getFXML(): String {
        return "main.fxml"
    }

    companion object {
        var underlays: UnderlayLoader? = null
        var overlays: OverlayLoader? = null
    }
}