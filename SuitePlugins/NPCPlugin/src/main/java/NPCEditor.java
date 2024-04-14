

import lombok.extern.slf4j.Slf4j;
import store.plugin.PluginType;
import suite.controller.ConfigEditor;
import utility.*;

@Slf4j
public class NPCEditor extends ConfigEditor {

	@Override
	public ConfigEditorInfo getInfo() {
		return ConfigEditorInfo.builder().index(2).archive(9).type(PluginType.NPC).build();
	}

}
