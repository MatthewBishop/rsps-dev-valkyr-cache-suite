/**
 * 
 */
package suite.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import store.ValkyrCacheLibrary;
import store.cache.index.OSRSIndices;
import suite.Main;
import utility.FilterMode;
import utility.RetentionFileChooser;

/**
 * @author ReverendDread
 * Sep 23, 2019
 */
public class TitlescreenReplacer {

	public static void open() {
		try {
			File file = RetentionFileChooser.showOpenDialog("Choose an image...", Main.getPrimaryStage(), FilterMode.JPG);
			byte[] bytes = Files.readAllBytes(file.toPath());
			ValkyrCacheLibrary.getIndex(OSRSIndices.HUFFMAN).add(10).add(0, bytes, "title.jpg".hashCode());
			ValkyrCacheLibrary.getIndex(OSRSIndices.HUFFMAN).update(Main.getSelection().getProgressListener());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
