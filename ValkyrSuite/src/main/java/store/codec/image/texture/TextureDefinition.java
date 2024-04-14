/**
 * 
 */
package store.codec.image.texture;

import lombok.Getter;
import lombok.Setter;
import org.displee.CacheLibrary;
import store.codec.AbstractDefinition;
import com.displee.io.impl.InputBuffer;

/**
 * @author ReverendDread Jan 10, 2019
 */
@Getter
@Setter
public class TextureDefinition implements AbstractDefinition, Cloneable {

	public final int id;

	/**
	 * Creates a new texture definition.
	 * 
	 * @param id
	 */
	public TextureDefinition(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see com.alex.definition.AbstractDefinition#decode(com.alex.io.InputStream)
	 */
	@Override
	public void decode(InputBuffer stream) {

	}

	/* (non-Javadoc)
	 * @see com.alex.definition.AbstractDefinition#encode()
	 */
	@Override
	public byte[] encode() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.alex.definition.AbstractDefinition#save(com.alex.store.FileStore)
	 */
	@Override
	public boolean save(CacheLibrary cache) {
		cache.getIndex(9).addArchive(id).add(id, encode());
		return cache.getIndex(9).update();
	}

	@Override
	public String toString() {
		return "" + this.id;
	}

}
