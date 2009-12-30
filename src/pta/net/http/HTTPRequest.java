/*
 * @(#)HTTPRequest.java 1.0 2009/12/29 Copyright 2009 PHẠM Tuấn Anh. All rights
 * reserved.
 */
package pta.net.http;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;

public final class HTTPRequest
{
	private byte[]				request;
	private String				method;
	private String				path;
	private String				host;
	private int					port						= 80;
	private String				query;
	private String				httpVersion;
	private URL					url;

	private boolean				isNoCached;
	private boolean				isKeepAlive;

	private Date				ifModifiedSince;

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

	public void parse (InputStream in) throws IOException, SocketException
	{
		StringBuffer line = new StringBuffer (128);
		ArrayList<Byte> data = new ArrayList<Byte> (256);
		int inInt;
		byte b = -1;
		int lineCnt = 0;
		int length = 0;
		int size;

		while ((inInt = in.read()) != -1)
		{

			b = (byte) inInt;
			line.append ((char) b);
			data.add (new Byte (b));
			size = data.size();

			if (b == '\r')
			{
				if (size > 1)
				{
					// "\r\r" then must be the end
					if (((Byte) data.get (size - 2)).byteValue() == '\r')
					{
						break;
					}

					String str = line.toString().trim();

					if (lineCnt == 0)
					{
						// GET http://host/link.ext?key=value HTTP/1.0
						String[] parts = str.split (" ");
						url = new URL (parts[1]);
						this.method = parts[0];
						this.host = url.getHost();
						this.path = url.getPath();
						this.query = url.getQuery();
						this.port = url.getPort();
						if (this.port == -1) this.port = 80;
						this.httpVersion = parts[2];
					}
					else if (str.equals ("Proxy-Connection: keep-alive"))
					{
						this.isKeepAlive = true;
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
							ifModifiedSince = simpleDateFormat.parse (strDate);
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

					lineCnt++;
				}

				line.setLength (0);
			}
			else if (b == '\n')
			{
				if ((((Byte) data.get (size - 4)).byteValue() == '\r')
						&& (((Byte) data.get (size - 3)).byteValue() == '\n')
						&& (((Byte) data.get (size - 2)).byteValue() == '\r'))
				{
					if (length == 0)
					{
						this.request = new byte[size];

						for (int i = 0; i < size; i++)
						{
							this.request[i] = ((Byte) data.get(i)).byteValue();
						}
					}
					else
					{
						this.request = new byte[length + size];

						for (int i = 0; i < size; i++)
						{
							this.request[i] = ((Byte) data.get(i)).byteValue();
						}

						in.read (this.request, size, length);
					}

					break;
				}
			}
		}
	}

	// public byte[] getData() {return (byte[])request.clone();}
	public byte[] getData()
	{
		return request;
	}

	// public void setData(byte[] data) {this.request = (byte[])data.clone();}
	// public void setData(byte[] data) {this.request = data;}

	public String getHost()
	{
		return host;
	}

	// public void setHost(String host) {this.host = host;}

	public String getMethod()
	{
		return method;
	}

	// public void setMethod(String method) {this.method = method;}

	public String getQuery()
	{
		return query;
	}

	// public void setQuery(String query) {this.query = query;}

	public String getPath()
	{
		return path;
	}

	// public void setPath(String path) {this.path = path;}

	public int getPort()
	{
		return port;
	}

	// public void setPort(int port) {this.port = port;}

	public String getHttpVersion()
	{
		return httpVersion;
	}

	// public void setHttpVersion(String httpVersion) {this.httpVersion =
	// httpVersion;}

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
