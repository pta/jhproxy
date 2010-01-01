package pta.io;

import java.io.ByteArrayOutputStream;

public class FreeByteArrayOutputStream extends ByteArrayOutputStream
{
	public FreeByteArrayOutputStream()
	{
		super();
	}

	public FreeByteArrayOutputStream (int size)
	{
		super (size);
	}

	public int get (int p)
	{
		if (p < 0)
			return buf[count+p];
		else
			return buf[p];
	}

	public byte[] getByteArray()
	{
		return buf;
	}
}
