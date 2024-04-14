import com.google.common.collect.Lists
import com.google.common.collect.Maps
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import misc.CustomTab
import store.ValkyrCacheLibrary
import org.displee.utilities.GZIPCompressor
import org.slf4j.LoggerFactory
import store.cache.index.OSRSIndices
import store.plugin.extension.FXController
import suite.Constants
import suite.Main
import suite.controller.Selection
import suite.dialogue.Dialogues
import utility.*
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

//import com.google.gson.GsonBuilder;
/**
 * @author ReverendDread & RSPSi on 11/27/2019
 * @project ValkyrCacheSuite
 */
class MapPacker : FXController() {
    private var tab: CustomTab? = null

    @FXML
    private val folderRadio: RadioButton? = null

    @FXML
    private val loadType: ToggleGroup? = null

    @FXML
    private val folderFileBox: VBox? = null

    @FXML
    private val mapFolderText: TextField? = null

    @FXML
    private val mapFolderBrowse: Button? = null

    @FXML
    private val singleFileRadio: RadioButton? = null

    @FXML
    private val singleFileBox: VBox? = null

    @FXML
    private val lsText: TextField? = null

    @FXML
    private val lsBrowse: Button? = null

    @FXML
    private val objText: TextField? = null

    @FXML
    private val objBrowse: Button? = null

    @FXML
    private val regionIdRadio: RadioButton? = null

    @FXML
    private val regionType: ToggleGroup? = null

    @FXML
    private val regionIdBox: HBox? = null

    @FXML
    private val regionID: TextField? = null

    @FXML
    private val regionXYRadio: RadioButton? = null

    @FXML
    private val regionXYBox: HBox? = null

    @FXML
    private val regionXText: TextField? = null

    @FXML
    private val regionYText: TextField? = null

    @FXML
    private val worldRadio: RadioButton? = null

    @FXML
    private val worldXYBox: HBox? = null

    @FXML
    private val worldXText: TextField? = null

    @FXML
    private val worldYText: TextField? = null

    @FXML
    private val xteaText: TextField? = null

    @FXML
    private val genXteas: Button? = null

    @FXML
    private val saveBtn: Button? = null

    @FXML
    private val canvas: Canvas? = null

    @FXML
    private val worldAnchor: AnchorPane? = null

    @FXML
    private val regionIdField: TextField? = null

    @FXML
    private val regionIdSubmit: Button? = null
    private var renderer: WorldMapRenderer? = null
    private val xteaStore: MutableList<XTEA> = Lists.newArrayList()

    //    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    //    public void saveXTeas(){
    //        if(Constants.settings.xteaFile == null || Constants.settings.xteaFile.isEmpty())
    //            return;
    //
    //        try(FileWriter fw = new FileWriter(new File(Constants.settings.xteaFile))){
    //            GSON.toJson(xteaStore, fw);
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }
    override fun initialize(tab: CustomTab, refresh: Boolean, lastId: Int) {
        if (Constants.settings.xteaFile == null || Constants.settings.xteaFile.isEmpty()) {
            val file = RetentionFileChooser.showOpenDialog(
                "Please select xteas.json",
                Main.getSelection().stage.scene.window,
                FilterMode.JSON
            )
            if (file != null) {
                Constants.settings.xteaFile = file.absolutePath
                Constants.settings.save()
            }
        }

//        if(Constants.settings.xteaFile != null && !Constants.settings.xteaFile.isEmpty()){
//            try(FileReader fr = new FileReader(new File(Constants.settings.xteaFile))) {
//                xteaStore = GSON.fromJson(fr, new TypeToken<List<XTEA>>() { }.getType());
//            } catch (Exception ex){
//                ex.printStackTrace();
//            }
//        }
        renderer = WorldMapRenderer(canvas!!)
        Thread(renderer).start()
        XTEASManager.get().setParserType(XTEASManager.XTEAParserType.POLAR)
        regionType!!.selectedToggleProperty()
            .addListener { observable: ObservableValue<out Toggle>?, oldValue: Toggle?, newValue: Toggle ->
                regionIdBox!!.isDisable = newValue !== regionIdRadio
                regionXYBox!!.isDisable = newValue !== regionXYRadio
                worldXYBox!!.isDisable = newValue !== worldRadio
            }
        loadType!!.selectedToggleProperty()
            .addListener { observable: ObservableValue<out Toggle>?, oldValue: Toggle?, newValue: Toggle ->
                folderFileBox!!.isDisable = newValue !== folderRadio
                singleFileBox!!.isDisable = newValue !== singleFileRadio
            }
        this.tab = tab
        mapFolderBrowse!!.onAction = EventHandler {
            val file = RetentionFileChooser.showOpenDialog(Main.getSelection().stage.scene.window, FilterMode.PACK)
            if (file != null) {
                mapFolderText!!.text = file.absoluteFile.toString()
            }
        }
        lsBrowse!!.onAction = EventHandler {
            val file = RetentionFileChooser.showOpenDialog(
                Main.getSelection().stage.scene.window,
                FilterMode.DAT,
                FilterMode.GZIP
            )
            if (file != null) {
                lsText!!.text = file.absoluteFile.toString()
            }
        }
        objBrowse!!.onAction = EventHandler {
            val file = RetentionFileChooser.showOpenDialog(
                Main.getSelection().stage.scene.window,
                FilterMode.DAT,
                FilterMode.GZIP
            )
            if (file != null) {
                objText!!.text = file.absoluteFile.toString()
            }
        }
        saveBtn!!.onAction = EventHandler {
            val regionX: Int
            val regionY: Int
            val regionId = regionID!!.text.toInt()
            if (regionType.selectedToggle === regionIdRadio) {
                regionX = regionId shr 8
                regionY = regionId and 0xFF
            } else if (regionType.selectedToggle === regionXYRadio) {
                regionX = regionXText!!.text.toInt()
                regionY = regionYText!!.text.toInt()
            } else {
                regionX = Math.floor(worldXText!!.text.toInt() / 64.0).toInt()
                regionY = Math.floor(worldYText!!.text.toInt() / 64.0).toInt()
            }
            var xteas = if (xteaText!!.text.isEmpty()) XTEASManager.lookup(regionId) else Arrays.stream(
                xteaText.text.replace(" ", "").split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()).mapToInt { s: String -> s.toInt() }.toArray()
            if (Objects.isNull(xteas)) {
                xteas = generateXTEAS()
                log.info("Generating xteas for region {}, {}", regionId, xteas)
            }
            if (xteas.size < 4) {
                Dialogues.alert(
                    Alert.AlertType.ERROR,
                    "XTEA Format Incorrect",
                    "Please check your xteas format",
                    "needs to be - 0, 0, 0, 0",
                    false
                )
                return@EventHandler
            }
            val xteaKeys: MutableMap<Int, IntArray> = Maps.newConcurrentMap()
            if (loadType.selectedToggle === folderRadio) {
                if (mapFolderText!!.text.isEmpty()) {
                    val alert = Alert(Alert.AlertType.ERROR, "Please select map files!", ButtonType.OK)
                    alert.show()
                    return@EventHandler
                }
                if (!mapFolderText.text.endsWith(".pack")) {
                    val alert = Alert(Alert.AlertType.ERROR, "File needs to be .pack format!", ButtonType.OK)
                    alert.show()
                    return@EventHandler
                }
                try {
                    val chunks = MultiMapEncoder.decode(
                        Files.readAllBytes(
                            File(
                                mapFolderText.text
                            ).toPath()
                        )
                    )
                    val finalXteas = xteas
                    chunks.stream()
                        .map { chunk: MultiMapEncoder.Chunk ->
                            val localRegionId = regionX + chunk.offsetX shl 8 or regionY + chunk.offsetY
                            xteaStore.removeIf { xtea: XTEA -> xtea.region == localRegionId }
                            xteaStore.add(XTEA(localRegionId, finalXteas))
                            packMaps(
                                regionX + chunk.offsetX,
                                regionY + chunk.offsetY,
                                chunk.tileMapData,
                                chunk.objectMapData,
                                finalXteas
                            )
                        }
                        .forEach { map: Map<Int, IntArray> ->
                            map.entries.forEach(
                                Consumer { (key, value): Map.Entry<Int, IntArray> -> xteaKeys[key] = value })
                        }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else {
                if (lsText!!.text.isEmpty() || objText!!.text.isEmpty()) {
                    val alert = Alert(Alert.AlertType.ERROR, "Please select map files!", ButtonType.OK)
                    alert.show()
                    return@EventHandler
                }
                if (!(lsText.text.endsWith(".gz") || lsText.text.endsWith(".dat")) || !(objText!!.text.endsWith(".gz") || objText.text.endsWith(
                        ".dat"
                    ))
                ) {
                    val alert = Alert(Alert.AlertType.ERROR, "Map files need to be .gz or .dat format!", ButtonType.OK)
                    alert.show()
                    return@EventHandler
                }
                try {
                    var tileData = Files.readAllBytes(File(lsText.text).toPath())
                    var objData = Files.readAllBytes(File(objText!!.text).toPath())
                    if (objText.text.endsWith(".gz")) {
                        objData = GZIPCompressor.inflate317(objData)
                    }
                    if (lsText.text.endsWith(".gz")) {
                        tileData = GZIPCompressor.inflate317(tileData)
                    }
                    xteaKeys.putAll(packMaps(regionX, regionY, tileData, objData, xteas))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            if (ValkyrCacheLibrary.getIndex(OSRSIndices.MAPS).update(Selection.progressListener, xteaKeys)) {
                val alert = Alert(Alert.AlertType.INFORMATION, "Maps packed successfully!", ButtonType.OK)
                alert.show()
            }
        }
        genXteas!!.onAction = EventHandler { event: ActionEvent? -> generateXTEAS() }
        canvas.widthProperty().bind(worldAnchor!!.widthProperty())
        canvas.heightProperty().bind(worldAnchor.heightProperty())
        regionIdSubmit!!.onAction = EventHandler { e: ActionEvent? ->
            try {
                val id = regionIdField!!.text.toInt()
                renderer!!.regionId = id
            } catch (ex: Exception) {
                renderer!!.regionId = -1
            }
        }
    }

    fun generateXTEAS(): IntArray {
        val keys: MutableList<Int> = Lists.newArrayList()
        val random = Random()
        keys.add(random.nextInt())
        keys.add(random.nextInt())
        keys.add(random.nextInt())
        keys.add(random.nextInt())
        xteaText!!.text =
            keys.stream().map { obj: Int? -> java.lang.String.valueOf(obj) }.collect(Collectors.joining(","))
        return keys.stream().mapToInt { obj: Int -> obj }.toArray()
    }

    fun packMaps(
        regionX: Int,
        regionY: Int,
        tileData: ByteArray?,
        objData: ByteArray?,
        xteas: IntArray
    ): Map<Int, IntArray> {
        return try {
            val mapArchiveName = "m" + regionX + "_" + regionY
            val landArchiveName = "l" + regionX + "_" + regionY
            var mapArchive = ValkyrCacheLibrary.getIndex(OSRSIndices.MAPS).getArchive(mapArchiveName)
            var exists = Objects.nonNull(mapArchive)
            if (exists) {
                mapArchive.reset()
            } else {
                mapArchive = ValkyrCacheLibrary.getIndex(OSRSIndices.MAPS).addArchive(mapArchiveName)
            }
            mapArchive.add(0, tileData)
            mapArchive.flag()
            var landArchive = ValkyrCacheLibrary.getIndex(OSRSIndices.MAPS).getArchive(landArchiveName)
            exists = Objects.nonNull(landArchive)
            if (exists) {
                landArchive.reset()
            } else {
                landArchive = ValkyrCacheLibrary.getIndex(OSRSIndices.MAPS).addArchive(landArchiveName)
            }
            landArchive.add(0, objData)
            landArchive.flag()
            val xteaKeys: MutableMap<Int, IntArray> = Maps.newHashMap()
            xteaKeys[landArchive.id] = xteas
            xteaKeys
        } catch (e: Exception) {
            e.printStackTrace()
            val alert = Alert(Alert.AlertType.ERROR, "Failed to pack maps!", ButtonType.OK)
            alert.show()
            Maps.newHashMap()
        }
    }

    override fun save() {}
    override fun getInfo(): ConfigEditorInfo? {
        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(MapPacker::class.java)
    }
}