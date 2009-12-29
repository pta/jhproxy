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
	private Date				ifModifiedSince				= new Date (0);
	private SimpleDateFormat	ifModifiedSinceDateFormat	= new SimpleDateFormat (
																	"EEEEEEEE, dd-MMM-yyyy HH:mm:ss 'GMT'");
	private int					ifModifiedSinceOffset;
	private int					ifModifiedSinceLength;
	private boolean				isAsctimeDate;

	private boolean				isInterrupted				= false;

	public void interrupt()
	{
		isInterrupted = true;
	}

	public boolean isInterrupted()
	{
		return isInterrupted;
	}

	public void parse (InputStream in) throws IOException, SocketException
	{
		StringBuffer line = new StringBuffer (128);
		ArrayList<Byte> data = new ArrayList<Byte> (256);
		int inInt;
		byte b = -1;
		int lineCnt = 0;
		int length = 0;
		int size;

		while (!isInterrupted && ((inInt = in.read()) != -1))
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
					if (str.startsWith ("Content-Length: "))
					{
						length = Integer.parseInt (str.substring (16));
					}
					else if (str.contains (": no-cache"))
					{
						// HTTP/1.0: Pragma: no-cache
						// HTTP/1.1: Cache-Control: no-cache
						this.isNoCached = true;
					}
					else if (str.startsWith ("If-Modified-Since: "))
					{
						String strDate = str.substring (19);
						ifModifiedSinceLength = strDate.length();
						ifModifiedSinceOffset = data.size()
								- ifModifiedSinceLength - 1;
						if (strDate.endsWith ("GMT"))
						{
							if (0 <= strDate.indexOf ('-'))
							{ // Monday, 06-Jun-1984 23:59:59 GMT
								ifModifiedSinceDateFormat = new SimpleDateFormat (
										"EEEEEEEE, dd-MMM-yyyy HH:mm:ss 'GMT'");
							}
							else
							{ // Mon, 06 Jun 1984 23:59:59 GMT
								ifModifiedSinceDateFormat = new SimpleDateFormat (
										"EEE, dd MMM yyyy HH:mm:ss 'GMT'");
							}
						}
						else
						{ // asctime-strDate
							isAsctimeDate = true;
							if (strDate.charAt (8) == ' ')
							{ // Mon Jun 6 23:59:59 1984
								ifModifiedSinceDateFormat = new SimpleDateFormat (
										"EEE MMM  d HH:mm:ss yyyy");
							}
							else
							{ // Mon Jun 13 23:59:59 1984
								ifModifiedSinceDateFormat = new SimpleDateFormat (
										"EEE MMM dd HH:mm:ss yyyy");
							}
						}
						ifModifiedSinceDateFormat
								.setTimeZone (new SimpleTimeZone (0, "GMT"));
						try
						{
							ifModifiedSince = ifModifiedSinceDateFormat
									.parse (strDate);
							/*
							 * System.out.println(strDate);
							 * System.out.println(ifModifiedSince);/*
							 */
						}
						catch (ParseException pe)
						{
							System.out.println (ifModifiedSinceDateFormat
									.toPattern());
							pe.printStackTrace();
							System.exit (0);
						}
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
							this.request[i] = ((Byte) data.get (i))
									.byteValue();
						}
						break;
					}
					this.request = new byte[length + size];
					for (int i = 0; i < size; i++)
					{
						this.request[i] = ((Byte) data.get (i)).byteValue();
					}
					in.read (this.request, size, length);
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

	public void setIfModifiedSince (Date newDate)
	{
		if (ifModifiedSince == null || ifModifiedSinceOffset == 0) return;
		Calendar newCal = new GregorianCalendar();
		newCal.setTime (newDate);
		Calendar oldCal = new GregorianCalendar();
		oldCal.setTime (ifModifiedSince);
		SimpleDateFormat dateFormat = ifModifiedSinceDateFormat;
		if (isAsctimeDate)
		{
			if (newCal.get (Calendar.DAY_OF_MONTH) < 10)
			{ // EEE MMM d HH:mm:ss yyyy
				if (10 <= oldCal.get (Calendar.DAY_OF_MONTH))
				{
					dateFormat = new SimpleDateFormat (
							"EEE MMM  d HH:mm:ss yyyy");
				}
			}
			else
			{ // EEE MMM dd HH:mm:ss yyyy
				if (oldCal.get (Calendar.DAY_OF_MONTH) < 10)
				{
					dateFormat = new SimpleDateFormat (
							"EEE MMM dd HH:mm:ss yyyy");
				}
			}
		}
		dateFormat.setTimeZone (new SimpleTimeZone (0, "GMT"));
		String strDate = dateFormat.format (newDate);
		try
		{
			byte[] strData = strDate.getBytes ("ISO-8859-1");
			if (strData.length == ifModifiedSinceLength)
			{
				System.arraycopy (strData, 0, request, ifModifiedSinceOffset,
						ifModifiedSinceLength);
			}
			else
			{
				byte[] old = request;
				request = new byte[request.length + strData.length
						- ifModifiedSinceLength];
				System.arraycopy (old, 0, request, 0, ifModifiedSinceOffset);
				System.arraycopy (strData, 0, request, ifModifiedSinceOffset,
						strData.length);
				System.arraycopy (old, ifModifiedSinceOffset
						+ ifModifiedSinceLength, request, ifModifiedSinceOffset
						+ strData.length, request.length
						- ifModifiedSinceOffset - strData.length);/**/
				ifModifiedSinceLength = strData.length;
			}
			ifModifiedSince = newDate;
		}
		catch (UnsupportedEncodingException uee)
		{
			uee.printStackTrace();
			System.exit (0);
		}
	}

	public URL getURL()
	{
		return url;
	}

	public boolean isNoCache()
	{
		return isNoCached;
	}
}
