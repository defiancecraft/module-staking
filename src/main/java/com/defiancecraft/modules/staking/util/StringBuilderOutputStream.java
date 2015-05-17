package com.defiancecraft.modules.staking.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream wrapping a StringBuilder. This is useful for
 * functions which accept an OutputStream, such as {@link java.lang.Exception#printStackTrace(java.io.PrintStream)}
 * (the OutputStream can easily be made into a PrintStream).
 */
public class StringBuilderOutputStream extends OutputStream {

	protected StringBuilder builder;
	
	/**
	 * Constructs a new StringBuilderOuputStream with the given builder.
	 * @param builder Builder to use
	 */
	public StringBuilderOutputStream(StringBuilder builder) {
		this.builder = builder;
	}
	
	@Override
	public void write(int b) throws IOException {
		this.builder.append((char) b);
	}

}
