package store.codec.map;

import java.util.concurrent.ConcurrentHashMap;

import com.displee.cache.CacheLibrary;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;

public class OverlayDefinition {

	private static ConcurrentHashMap<Integer, OverlayDefinition> cached_definition = new ConcurrentHashMap<Integer, OverlayDefinition>();

	public int id;
	public int rgbColor;
	public int textureId;
	public boolean hideUnderlay;
	public int blendColor;
	public int opcode8;
	public int opcode9;
	public boolean opcode10;
	public int opcode11;
	public boolean opcode12;
	public int opcode13;
	public int opcode14;
	public int opcode16;
	public int opcode20;
	public int opcode21;
	public int opcode22;

	public OverlayDefinition() {
		rgbColor = -1;
		textureId = -1;
		hideUnderlay = true;
		blendColor = -1;
		opcode8 = -1;
		opcode9 = -1;
		opcode10 = true;
		opcode11 = -1;
		opcode12 = false;
		opcode13 = -1;
		opcode14 = -1;
		opcode16 = -1;
		opcode20 = -1;
		opcode21 = -1;
		opcode22 = -1;
	}

	public static OverlayDefinition getOverlayDefinition(CacheLibrary cache, int id) {
		OverlayDefinition definition = cached_definition.get(id);
		if (definition != null)
			return definition;
		byte[] data = cache.index(2).archive(4).file(id).getData();
		definition = new OverlayDefinition();
		definition.id = id;
		if (data != null)
			definition.decode(new InputBuffer(data));
		definition.method4603();
		cached_definition.put(id, definition);
		return definition;
	}

	private void decode(InputBuffer stream) {
		for (;;) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
				break;
			decode(stream, opcode);
		}
	}

	private void decode(InputBuffer stream, int opcode) {
		if (opcode == 1)
			rgbColor = stream.read24BitInt();
		else if (opcode == 2)
			textureId = stream.readUnsignedByte();
		else if (opcode == 3) {
			textureId = stream.readUnsignedShort();
			if (textureId == 65565)
				textureId = -1;
		} else if (opcode == 5)
			hideUnderlay = true;
		else if (opcode == 7)
			blendColor = stream.read24BitInt();
		else if (opcode == 8) {
			;
		} else if (opcode == 9)
			opcode9 = stream.readUnsignedShort() << 2;
		else if (opcode == 10)
			opcode10 = false;
		else if (opcode == 11)
			opcode11 = stream.readUnsignedByte();
		else if (opcode == 12)
			opcode12 = true;
		else if (opcode == 13)
			opcode13 = stream.read24BitInt();
		else if (opcode == 14)
			opcode14 = stream.readUnsignedByte() << 2;
		else if (opcode == 16)
			opcode16 = stream.readUnsignedByte();
		else if (opcode == 20)
			opcode20 = stream.readUnsignedShort();
		else if (opcode == 21)
			opcode21 = stream.readUnsignedByte();
		else if (opcode == 22)
			opcode22 = stream.readUnsignedShort();
	}

	void method4603() {
		opcode11 = (opcode11 << 8 | id);
	}

	public void write(CacheLibrary cache) {
		cache.index(2).add(4).add(this.id, this.encode());
	}

	private byte[] encode() {
		OutputBuffer stream = new OutputBuffer(16);

		if (rgbColor != -1) {
			stream.writeByte(1);
			stream.write24BitInt(rgbColor);
		}

		if (textureId != -1) {
			stream.writeByte(3);
			stream.writeShort(textureId);
		}

		if (hideUnderlay)
			stream.writeByte(5);

		if (blendColor != -1) {
			stream.writeByte(7);
			stream.write24BitInt(blendColor);
		}

		if (opcode9 != -1) {
			stream.writeByte(9);
			stream.writeShort(opcode9 >> 2);
		}

		if (!opcode10)
			stream.writeByte(10);

		if (opcode11 != -1) {
			stream.writeByte(11);
			stream.writeByte(opcode11);
		}

		if (opcode12)
			stream.writeByte(12);

		if (opcode13 != -1) {
			stream.writeByte(13);
			stream.write24BitInt(opcode13);
		}

		if (opcode14 != -1) {
			stream.writeByte(14);
			stream.writeByte(opcode14 >> 2);
		}

		if (opcode16 != -1) {
			stream.writeByte(16);
			stream.writeByte(opcode16);
		}

		if (opcode20 != -1) {
			stream.writeByte(20);
			stream.writeShort(opcode20);
		}

		if (opcode21 != -1) {
			stream.writeByte(21);
			stream.writeByte(opcode21);
		}

		if (opcode22 != -1) {
			stream.writeByte(22);
			stream.writeShort(opcode22);
		}

		stream.writeByte(0);
		return stream.array();
	}

}
