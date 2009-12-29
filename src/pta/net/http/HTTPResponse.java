/*
 * @(#)HTTPResponse.java 1.0 2009/12/29
 * Copyright 2009 PHẠM Tuấn Anh. All rights reserved.
 */
package pta.net.http;

import java.io.*;
import java.util.*;
import java.text.*;

public final class HTTPResponse
{
	private byte[]	response;
	private int		statusCode;
	private String	httpVersion;
	private Date	lastModified		= new Date();
	private int		messageBodyOffset	= -1;
	private String	contentType;

	public long size()
	{
		return response.length + 4 + httpVersion.length() * 2 + 12 + 4
				+ contentType.length() * 2 + 12;
	}

	private transient boolean	isInterrupted	= false;

	public void interrupt()
	{
		isInterrupted = true;
	}

	public boolean isInterrupted()
	{
		return isInterrupted;
	}

	/**
	 * bugs: isInterrupted never occurs if there no synchronized block in the
	 * loops since isInterrupted can only change by the other threads, this
	 * thread must synchronized to make the new value really commited. Java's
	 * transient keyword has nothing to do with thread.
	 */
	public void parse(InputStream in, HTTPRequest request) throws IOException
	{
		StringBuffer line = new StringBuffer(128);
		ArrayList<Byte> data = new ArrayList<Byte>(10240);
		int inInt;
		boolean chunkedEncoding = false;
		int size;
		byte b = -1;
		int lineCnt = 0;
		int length = -1;

		main: while (!isInterrupted && ( (inInt = in.read()) != -1))
		{

			b = (byte) inInt;
			line.append((char) b);
			data.add(new Byte(b));
			size = data.size();

			if (b == '\r')
			{
				if (size > 1)
				{
					// "\r\r" then must be the end
					if ( ((Byte) data.get(size - 2)).byteValue() == '\r')
					{
						break main;
					}
					String str = line.toString().trim();
					// System.out.println('\t'+str);
					if (lineCnt == 0)
					{
						// HTTP/1.0 200 OK
						String[] parts = str.split(" ");
						this.httpVersion = parts[0];
						this.statusCode = Integer.parseInt(parts[1]);
						lineCnt++;
					}
					else if (str.startsWith("Content-Length: "))
					{
						length = Integer.parseInt(str.substring(16));
					}
					else if (str.startsWith("Transfer-Encoding: chunked"))
					{
						chunkedEncoding = true;
					}
					else if (str.startsWith("Content-Type: "))
					{
						contentType = str.substring(14);
					}
					else if (str.startsWith("Last-Modified: "))
					{
						String strDate = str.substring(15);
						SimpleDateFormat dateFormat;
						if (strDate.endsWith("GMT"))
						{
							if (0 <= strDate.indexOf('-'))
							{ // Monday, 06-Jun-1984 23:59:59 GMT
								dateFormat = new SimpleDateFormat(
										"EEEEEEEE, dd-MMM-yyyy HH:mm:ss 'GMT'");
							}
							else
							{ // Mon, 06 Jun 1984 23:59:59 GMT
								dateFormat = new SimpleDateFormat(
										"EEE, dd MMM yyyy HH:mm:ss 'GMT'");
							}
						}
						else
						{ // asctime-strDate
							// isAsctimeDate = true;
							if (strDate.charAt(8) == ' ')
							{ // Mon Jun 6 23:59:59 1984
								dateFormat = new SimpleDateFormat(
										"EEE MMM  d HH:mm:ss yyyy");
							}
							else
							{ // Mon Jun 13 23:59:59 1984
								dateFormat = new SimpleDateFormat(
										"EEE MMM dd HH:mm:ss yyyy");
							}
						}
						dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
						try
						{
							lastModified = dateFormat.parse(strDate);
							// System.out.println(lastModified);
						}
						catch (ParseException pe)
						{
							System.out.println(dateFormat.toPattern());
							pe.printStackTrace();
							System.exit(0);
						}
					}
				}
				line.setLength(0); // reset the line buffer
			}
			else if (b == '\n')
			{
				if ( ( ((Byte) data.get(size - 4)).byteValue() == '\r')
						&& ( ((Byte) data.get(size - 3)).byteValue() == '\n')
						&& ( ((Byte) data.get(size - 2)).byteValue() == '\r'))
				{
					/*
					 * 4.3 Message Body: ...All responses to the HEAD request
					 * method MUST NOT include a message-body, even though the
					 * presence of entity- header fields might lead one to
					 * believe they do. All 1xx (informational), 204 (no
					 * content), and 304 (not modified) responses MUST NOT
					 * include a message-body. All other responses do include a
					 * message-body, although it MAY be of zero length.
					 */
					if ( (request != null && /**/request.getMethod().equals(
							"HEAD"))
							|| statusCode == 204
							|| statusCode == 304
							|| (100 <= statusCode && statusCode < 200))
					{
						break main;
					}
					if (length == 0)
					{ // no response-body at all
						break main;
					}
					messageBodyOffset = data.size();
					if (length == -1)
					{ // message-body contains zero or more chunks data...
						if (!chunkedEncoding)
						{
							while (!isInterrupted
									&& ( (inInt = in.read()) != -1))
							{
								data.add(new Byte((byte) inInt));
							}
							break main;
						}
						// read all the data chunks..
						StringBuffer sb = new StringBuffer(8);
						while (!isInterrupted)
						{
							// chunk = chunk-size [ chunk-extension ] CRLF
							// chunk-data CRLF
							sb.setLength(0);
							while (!isInterrupted && (inInt = in.read()) != -1)
							{
								data.add(new Byte((byte) inInt));
								if (inInt == ';' || inInt == '\r') break;
								sb.append((char) inInt);
							}
							// System.out.println("\t\t"+sb.toString());
							length = Integer.valueOf(sb.toString(), 16)
									.intValue();
							// skip rest of this line
							while (!isInterrupted && (inInt = in.read()) != -1)
							{
								data.add(new Byte((byte) inInt));
								if (inInt == '\n') break;
							}
							if (length != 0)
							{
								for (int i = 0; i < length; i++)
								{
									data.add(new Byte((byte) in.read()));
								}
							}
							// skip 1 line
							while (!isInterrupted && (inInt = in.read()) != -1)
							{
								data.add(new Byte((byte) inInt));
								if (inInt == '\n') break;
							}
							if (length == 0)
							{
								break main;
							}
						}
						break main;
					}
					size = data.size();
					this.response = new byte[length + size];
					for (int i = 0; i < size; i++)
					{
						this.response[i] = ((Byte) data.get(i)).byteValue();
					}
					int r = 0;
					while ( (r = in.read(this.response, size, length)) != -1
							&& length > 0)
					{
						size += r;
						length -= r;
					}
					return;
				}
			}
		}// main while
		size = data.size();
		this.response = new byte[size];
		for (int i = 0; i < size; i++)
		{
			this.response[i] = ((Byte) data.get(i)).byteValue();
		}
	}

	public byte[] getData()
	{
		return response;
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public Date getLastModified()
	{
		return lastModified;
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getContentText()
	{
		if (contentType == null) return null;
		if (!contentType.startsWith("text/")) return null;
		if (messageBodyOffset < 0) return null;
		try
		{
			return new String(response, messageBodyOffset, response.length
					- messageBodyOffset, "UTF-8");
		}
		catch (UnsupportedEncodingException uee)
		{
			uee.printStackTrace();
			return null;
		}
	}
}
