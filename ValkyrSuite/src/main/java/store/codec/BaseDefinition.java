package store.codec;

import org.displee.CacheLibrary;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;

public class BaseDefinition implements AbstractDefinition, Cloneable {

	int id;
	int count;
	boolean[] shadowed;
	int[] transformations;
	int[][] skin_list;
	int[] settings;

	public BaseDefinition(int id) {
		this.id = id;
	}

	@Override
	public void decode(InputBuffer stream) {
		count = stream.readUnsignedByte();
		transformations = new int[count];
		skin_list = new int[count][];
		shadowed = new boolean[count];
		settings = new int[count];
		for (int opcode = 0; opcode < count; opcode++) {
			transformations[opcode] = stream.readUnsignedByte();
			if (transformations[opcode] == 6)
				transformations[opcode] = 2;
		}
		for (int index = 0; index < count; index++)
			shadowed[index] = stream.readUnsignedByte() == 1;
		for (int setting = 0; setting < count; setting++)
			settings[setting] = stream.readUnsignedShort();
		for (int skin = 0; skin < count; skin++)
			skin_list[skin] = new int[stream.readUnsignedByte()];
		for (int skin = 0; skin < count; skin++) {
			for (int sub_skin = 0; (sub_skin < skin_list[skin].length); sub_skin++)
				skin_list[skin][sub_skin] = stream.readUnsignedByte();
		}
	}

	@Override
	public byte[] encode() {
		OutputBuffer stream = new OutputBuffer(16);
		int count = transformations.length;
		stream.writeByte(count);
		for (int opcode = 0; opcode < count; opcode++)
			stream.writeByte(transformations[opcode]);
		for (int index = 0; index < count; index++)
			stream.writeByte(shadowed[index] ? 1 : 0);
		for (int index = 0; index < count; index++)
			stream.writeShort(settings[index]);
		for (int skin = 0; skin < count; skin++)
			stream.writeByte(skin_list[skin].length);
		for (int skin = 0; skin < count; skin++) {
			for (int sub_skin = 0; (sub_skin < skin_list[skin].length); sub_skin++)
				stream.writeByte(skin_list[skin][sub_skin]);
		}
		byte[] flipped = new byte[stream.getOffset()];
		stream.setOffset(0);
		stream.array();
		return flipped;
	}

	@Override
	public boolean save(CacheLibrary cache) {
		cache.getIndex(1).getArchive(id).add(0, encode());
		return cache.getIndex(1).update();
	}

}
