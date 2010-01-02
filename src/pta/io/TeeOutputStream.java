/*
 * pta - PTA's Java Library Package
 * Copyright (C) 2010  PHAM Tuan Anh <cs.PhamTuanAnh@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pta.io;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

public final class TeeOutputStream extends FilterOutputStream
{
	private OutputStream	out1;
	private OutputStream	out2;

	public TeeOutputStream (OutputStream stream1, OutputStream stream2)
	{
		super (stream1);
		out1 = stream1;
		out2 = stream2;
	}

	public void write (int b) throws IOException
	{
		out1.write (b);
		out2.write (b);
	}

	public void write (byte[] data, int offset, int length) throws IOException
	{
		out1.write (data, offset, length);
		out2.write (data, offset, length);
	}

	public void flush() throws IOException
	{
		out1.flush();
		out2.flush();
	}

	public void close() throws IOException
	{
		out1.close();
		out2.close();
	}
}
