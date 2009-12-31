/*
 * @(#)HTTPRequest.java 1.0 2009/12/29 Copyright 2009 PHẠM Tuấn Anh. All rights
 * reserved.
 */
package pta.net.http;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;

import pta.util.*;

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

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

	public void parse (InputStream in) throws IOException, SocketException
	{
		FreeByteArrayOutputStream data = new FreeByteArrayOutputStream (1400);
		StringBuffer line = new StringBuffer (128);

		int inInt;
		int lineCnt = 0;
		int length = 0;

		while ((inInt = in.read()) != -1)
		{
			byte b = (byte) inInt;
			line.append ((char) b);
			data.write (b);

			if (b == '\r')
			{
				if (data.size() > 1)
				{
					// "\r\r" then must be the end
					if (data.get(-2) == '\r')
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
					&& data.get(-2) == '\r'
					&& data.get(-3) == '\n'
					&& data.get(-4) == '\r')
			{
				break;
			}
		}

		if (length == 0)
		{
			this.request = data.getByteArray();
		}
		else
		{
			int size = data.size();

			this.request = new byte[length + size];

			System.arraycopy (data.getByteArray(), 0, this.request, 0, size);

			in.read (this.request, size, length);
		}
	}

	// public byte[] getData() {return (byte[])this.request.clone();}
	public byte[] getData()
	{
		return this.request;
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
