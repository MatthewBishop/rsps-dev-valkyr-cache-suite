/**
 * 
 */
package store.codec.osrs;

import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;

/**
 * @author ReverendDread Sep 6, 2018
 */
public class BaseDefinition {

	public int id;
	public int count;
	public int[] transformations;
	public int[][] skinList;

	public BaseDefinition(int id, byte[] data) {
		this.id = id;
		InputBuffer stream = new InputBuffer(data);
		count = stream.readUnsignedByte();
		transformations = new int[count];
		skinList = new int[count][];
		for (int opcode = 0; opcode < count; opcode++)
			transformations[opcode] = stream.readUnsignedByte();
		for (int skin = 0; skin < count; skin++)
			skinList[skin] = new int[stream.readUnsignedByte()];
		for (int skin = 0; skin < count; skin++) {
			for (int subSkin = 0; (subSkin < skinList[skin].length); subSkin++)
				skinList[skin][subSkin] = stream.readUnsignedByte();
		}
	}

	public byte[] encode() {
		OutputBuffer stream = new OutputBuffer(16);
		int count = transformations.length;
		stream.writeByte(transformations.length);
		for (int opcode = 0; opcode < count; opcode++) {
			stream.writeByte(transformations[opcode]);
		}
		for (int opcode = 0; opcode < count; opcode++) {
			stream.writeByte(0);
		}
		for (int opcode = 0; opcode < count; opcode++) {
			stream.writeShort(-1);
		}
		for (int skin = 0; skin < count; skin++) {
			stream.writeByte(skinList[skin].length);
		}
		for (int skin = 0; skin < count; skin++) {
			for (int subSkin = 0; (subSkin < skinList[skin].length); subSkin++)
				stream.writeByte(skinList[skin][subSkin]);
		}
		return stream.flip();
	}

}
