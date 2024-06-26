/**
 * 
 */
package store.codec.osrs;

import java.util.LinkedList;

import com.displee.cache.CacheLibrary;
import com.displee.io.impl.InputBuffer;

/**
 * @author ReverendDread Sep 8, 2018
 */
public class FrameSet {

	int sequenceId;
	int archive;
	private FrameDefinition[] frameType;
	private byte[][] frameData;
	private CacheLibrary cache;

	public FrameSet(CacheLibrary cache, int archive, int sequenceId) {
		this.cache = cache;
		this.archive = archive;
		this.sequenceId = sequenceId;
		decodeOSRS();
	}

	/**
	 * 
	 */
	private void decodeOSRS() {

	}

	/**
	 * Decodes a sequence frame set.
	 * 
	 * @return
	 */
	public boolean decode718() {

		// If frames are null, return false.
		if (frameType != null)
			return true;

		// Get the frames data for the set.
		if (frameData == null) {
			if (cache.index(0).archive(archive) == null)
				return false;
			int[] frames = cache.index(0).archive(archive).fileIds();
			frameData = new byte[frames.length][];
			for (int index = 0; index < frames.length; index++) {
				frameData[index] = cache.index(0).archive(archive).file(frames[index]).getData();
			}
		}

		// Check if the base exists for each frame
		boolean exists = true;
		for (int index = 0; index < frameData.length; index++) {
			byte[] data = frameData[index];
			InputBuffer stream = new InputBuffer(data);
			int id = stream.readUnsignedShort();
			exists &= cache.index(1).archive(id).file(0) != null;
		}

		// if the base doesn't exist, return false.
		if (!exists) {
			return false;
		}

		LinkedList<BaseDefinition> list = new LinkedList<BaseDefinition>();

		int[] frameFiles;
		int length = cache.index(0).archive(archive).files().length;
		frameType = new FrameDefinition[length];
		frameFiles = cache.index(0).archive(archive).fileIds();
		for (int index = 0; index < frameFiles.length; index++) {
			byte[] data = frameData[index];
			InputBuffer stream = new InputBuffer(data);
			int id = stream.readUnsignedShort();
			BaseDefinition type = null;
			for (BaseDefinition baseDefinition = list.peek(); baseDefinition != null; baseDefinition = list.poll()) {
				if (id == (baseDefinition.id)) {
					type = baseDefinition;
					break;
				}
			}
			if (type == null)
				type = new BaseDefinition(id, cache.index(1).archive(id).file(0).getData());
			frameType[frameFiles[index]] = new FrameDefinition(data, type);
		}
		frameData = null;
		return true;
	}

}
