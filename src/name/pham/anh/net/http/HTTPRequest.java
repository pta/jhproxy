/*
 * name.pham.anh - PTA's Java Library Package
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
package name.pham.anh.net.http;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;

import name.pham.anh.io.FreeByteArrayOutputStream;


public final class HTTPRequest
{
	private byte[]				data;
	private String				method;
	private URL					url;
	private String				host;
	private String				httpVersion;

	private boolean				isNoCached;
	private boolean				isKeepAlive;

	private Date				ifModifiedSince;

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

	public void parse (InputStream in) throws IOException
	{
		FreeByteArrayOutputStream dataOS = new FreeByteArrayOutputStream (1400);
		StringBuffer line = new StringBuffer (128);

		int inInt;
		int lineCnt = 0;
		int length = 0;

		while ((inInt = in.read()) != -1)
		{
			byte b = (byte) inInt;
			line.append ((char) b);
			dataOS.write (b);

			if (b == '\r')
			{
				if (dataOS.size() > 1)
				{
					// "\r\r" then must be the end
					if (dataOS.get(-2) == '\r')
					{
						break;
					}

					String str = line.toString().trim();

					if (lineCnt == 0)
					{
						// GET http://host/link.ext?key=value HTTP/1.0
						String[] parts = str.split (" ");
						this.method = parts[0];
						this.url = new URL (parts[1]);
						this.httpVersion = parts[2];

						int p = parts[1].indexOf ("://");

						// remove protocol://host:port from the first header line
						if (p >= 0)
						{
							p = parts[1].indexOf ('/', p + 3);

							dataOS.reset();
							dataOS.write (parts[0].getBytes());
							dataOS.write (' ');
							dataOS.write (parts[1].substring(p).getBytes());
							dataOS.write (' ');
							dataOS.write (parts[2].getBytes());
							dataOS.write ('\r');
						}
					}
					else if (str.equals ("Proxy-Connection: keep-alive"))
					{
						this.isKeepAlive = true;
					}
					if (str.startsWith ("Host: "))
					{
						this.host = str.substring (6);
					}
					if (str.startsWith ("Content-Length: "))
					{
						length = Integer.parseInt (str.substring (16));
					}
					else if (str.startsWith ("If-Modified-Since: "))
					{
						String strDate = str.substring (19);

						try
						{
							ifModifiedSince = DATE_FORMAT.parse (strDate);
							/*System.out.println(strDate);
							System.out.println(ifModifiedSince);/**/
						}
						catch (ParseException pe)
						{
							pe.printStackTrace();
							System.exit (0);
						}
					}
					else if (str.contains ("no-cache"))
					{
						// HTTP/1.0: Pragma: no-cache
						// HTTP/1.1: Cache-Control: no-cache
						this.isNoCached = true;
					}

					++lineCnt;
				}

				line.setLength (0);
			}
			else if (b == '\n'
					&& dataOS.get(-2) == '\r'
					&& dataOS.get(-3) == '\n'
					&& dataOS.get(-4) == '\r')
			{
				break;
			}
		}

		if (length == 0)
		{
			this.data = dataOS.getByteArray();
		}
		else
		{
			int size = dataOS.size();

			this.data = new byte[length + size];

			System.arraycopy (dataOS.getByteArray(), 0, this.data, 0, size);

			in.read (this.data, size, length);
		}
	}

	// public byte[] getData() {return (byte[])this.data.clone();}
	public byte[] getData()
	{
		return this.data;
	}

	public String getHost()
	{
		return this.host;
	}

	public String getMethod()
	{
		return method;
	}

	public String getQuery()
	{
		return this.url.getQuery();
	}

	public String getPath()
	{
		return this.url.getPath();
	}

	public int getPort()
	{
		int port = this.url.getPort();

		if (port >= 0)
			return port;
		else
			return this.url.getDefaultPort();
	}

	public String getHttpVersion()
	{
		return httpVersion;
	}

	public long getIfModefiedSinceTime()
	{
		return ifModifiedSince.getTime();
	}

	public Date getIfModifiedSince()
	{
		return ifModifiedSince;
	}

	public URL getURL() {return url;}
	public boolean isNoCache() {return isNoCached;}
	public boolean isKeepAlive () {return isKeepAlive;}
}
