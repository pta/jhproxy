package pta.io;

import java.io.OutputStream;

public final class NullOutputStream extends OutputStream
{
	/**
     * A singleton.
     */
    public static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

	public void write (int b)
	{
	}

	public void write (byte[] data, int offset, int length)
	{
	}

	public void write (byte[] b)
	{
	}
}
