package org.displee.cache;

import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;

/**
 * A class serving as a container where we can read from and write to.
 * 
 * @author Displee
 */
public interface Container {

	/**
	 * Used to read data from an index, an archive or a file.
	 */
	public boolean read(InputBuffer inputBuffer);

	/**
	 * Write data to an index, archive or file.
	 */
	public byte[] write(OutputBuffer outputBuffer);

}