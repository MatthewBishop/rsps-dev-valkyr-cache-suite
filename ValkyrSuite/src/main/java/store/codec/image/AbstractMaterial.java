package store.codec.image;

import com.displee.io.impl.InputBuffer;

public abstract class AbstractMaterial {

	public AbstractMaterial[] cachedSprites;
	public boolean aBoolean7382;
	public int anInt7381;

	public AbstractMaterial(int size, boolean bool) {
		this.aBoolean7382 = bool;
		this.cachedSprites = new AbstractMaterial[size];
	}

	public int getSpriteGroup() {
		return -1;
	}

	public int getSpriteFrame() {
		return -1;
	}

	public void decodeSprite(int visable, InputBuffer buffer) {

	}

	public void method3133(int i) {

	}

	@Override
	public String toString() {
		return "aBoolean7382: " + aBoolean7382 + ", cachedSprites length: " + cachedSprites.length;
	}

}
