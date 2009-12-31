/*
 * @(#)HTTPResponse.java 1.0 2009/12/29
 * Copyright 2009 PHẠM Tuấn Anh. All rights reserved.
 */
package pta.net.http;

import java.io.*;
import java.util.*;
import java.text.*;
import pta.util.FreeByteArrayOutputStream;

public final class HTTPResponse
{
	private byte[]	response;
	private int		statusCode;
	private String	httpVersion;
	private int		messageBodyOffset	= -1;
	private String	contentType;

	private Date	lastModified;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

	public long size()
	{
		return response.length + 4 + httpVersion.length() * 2 + 12 + 4
				+ contentType.length() * 2 + 12;
	}

	public void parse (InputStream in, HTTPRequest request) throws IOException
	{
		FreeByteArrayOutputStream data = new FreeByteArrayOutputStream (1500);
		StringBuffer line = new StringBuffer (128);

		int inInt;
		boolean chunkedEncoding = false;
		int lineCnt = 0;
		int length = -1;

		main: while ((inInt = in.read()) != -1)
		{
			byte b = (byte) inInt;
			line.append ((char) b);
			data.write(b);
			int size = data.size();

			if (b == '\r')
			{
				if (size > 1)
				{
					// "\r\r" then must be the end
					if (data.get(-2) == '\r')
					{
						break main;
					}

					String str = line.toString().trim();

					if (lineCnt == 0)
					{
						// HTTP/1.0 200 OK
						String[] parts = str.split (" ");
						this.httpVersion = parts[0];
						this.statusCode = Integer.parseInt (parts[1]);
						++lineCnt;
					}
					else if (str.startsWith ("Content-Length: "))
					{
						length = Integer.parseInt (str.substring (16));
					}
					else if (str.startsWith ("Transfer-Encoding: chunked"))
					{
						chunkedEncoding = true;
					}
					else if (str.startsWith ("Content-Type: "))
					{
						contentType = str.substring (14);
					}
					else if (str.startsWith ("Last-Modified: "))
					{
						String strDate = str.substring (15);

						try
						{
							lastModified = DATE_FORMAT.parse (strDate);
						}
						catch (ParseException pe)
						{
							System.out.println (DATE_FORMAT.toPattern());
							pe.printStackTrace();
							System.exit (0);
						}
					}
				}

				line.setLength (0); // reset the line buffer
			}
			else if (b == '\n'
					&& data.get(-2) == '\r'
					&& data.get(-3) == '\n'
					&& data.get(-4) == '\r')
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
				if ((request != null && request.getMethod().equals ("HEAD"))
						|| statusCode == 204 || statusCode == 304
						|| (100 <= statusCode && statusCode < 200))
				{
					break main;
				}

				if (length == 0) // no response-body at all
				{
					break main;
				}

				messageBodyOffset = data.size();

				if (length == -1) // message-body contains zero or more chunks data...
				{
					if (!chunkedEncoding)
					{
						while ((inInt = in.read()) != -1)
						{
							data.write (inInt);
						}

						break main;
					}

					// read all the data chunks..
					StringBuffer sb = new StringBuffer(8);

					while (true)
					{
						// chunk = chunk-size [ chunk-extension ] CRLF
						// chunk-data CRLF
						sb.setLength (0);

						while ((inInt = in.read()) != -1)
						{
							data.write (inInt);
							if (inInt == ';' || inInt == '\r')
								break;
							sb.append ((char) inInt);
						}

						length = Integer.valueOf (sb.toString(), 16).intValue();

						// skip rest of this line
						do
						{
							data.write (inInt = in.read());
						}
						while (inInt != '\n');

						if (length != 0)
						{
							for (int i = 0; i < length; i++)
							{
								data.write (in.read());
							}
						}

						// skip 1 line
						do
						{
							data.write (inInt = in.read());
						}
						while (inInt != '\n');

						if (length == 0)
						{
							break main;
						}
					}
				}

				size = data.size();

				this.response = new byte[length + size];
				System.arraycopy (data.getByteArray(), 0, response, 0, size);

				while (length > 0)
				{
					int r = in.read (this.response, size, length);

					if (r == -1) break;

					size += r;
					length -= r;
				}

				return;
			}
		}// main while

		this.response = data.getByteArray();
	}

	public byte[] getData() {return response;}
	public int getStatusCode() {return statusCode;}
	public Date getLastModified() {return lastModified;}
	public String getContentType() {return contentType;}

	public String getContentText()
	{
		if (contentType == null) return null;
		if (!contentType.startsWith ("text/")) return null;
		if (messageBodyOffset < 0) return null;
		try
		{
			return new String (response, messageBodyOffset, response.length
					- messageBodyOffset, "UTF-8");
		}
		catch (UnsupportedEncodingException uee)
		{
			uee.printStackTrace();
			return null;
		}
	}
}
