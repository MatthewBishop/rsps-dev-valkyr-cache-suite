/**
 * 
 */
package store.codec;

import com.displee.cache.CacheLibrary;
import com.displee.io.impl.InputBuffer;

/**
 * @author ReverendDread Jan 14, 2019
 */
public class FluDefinition implements AbstractDefinition {

	/* (non-Javadoc)
	 * @see com.alex.definition.AbstractDefinition#decode(com.alex.io.InputStream)
	 */
	@Override
	public void decode(InputBuffer stream) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.alex.definition.AbstractDefinition#encode()
	 */
	@Override
	public byte[] encode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.alex.definition.AbstractDefinition#save(com.alex.store.FileStore)
	 */
	@Override
	public boolean save(CacheLibrary cache) {
		// TODO Auto-generated method stub
		return false;
	}

}
