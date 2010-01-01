/*
 * @(#)HTTPResponse.java 1.0 2009/12/29
 * Copyright 2009 PHẠM Tuấn Anh. All rights reserved.
 */
package pta.net.http;

import java.io.*;
import java.util.*;
import java.text.*;

import pta.io.FreeByteArrayOutputStream;
import pta.io.TeeOutputStream;

public final class HTTPResponse
{
	private HTTPRequest request;
	private byte[]	data;

	private int		statusCode;
	private String	httpVersion;
	private int		headerLength;
	private String	contentType;

	private Date	lastModified;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

	public HTTPResponse (HTTPRequest request)
	{
		this.request = request;
	}

	public void parse (InputStream in) throws IOException
	{
		this.parse (in, null);
	}

	public void parse (InputStream in, OutputStream out) throws IOException
	{
		FreeByteArrayOutputStream dataOS = new FreeByteArrayOutputStream (1500);
		OutputStream teeOS = (out != null) ?
				new TeeOutputStream (dataOS, out) : dataOS;
		StringBuffer line = new StringBuffer (128);

		int inInt;
		boolean chunkedEncoding = false;
		int lineCnt = 0;
		int contentLength = -1;

		while ((inInt = in.read()) != -1)
		{
			byte b = (byte) inInt;
			line.append ((char) b);
			teeOS.write(b);
			int size = dataOS.size();

			if (b == '\r')
			{
				if (size > 1)
				{
					// "\r\r" then must be the end
					if (dataOS.get(-2) == '\r')
					{
						break;
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
						contentLength = Integer.parseInt (str.substring (16));
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
					&& dataOS.get(-2) == '\r'
					&& dataOS.get(-3) == '\n'
					&& dataOS.get(-4) == '\r')
			{
				break;
			}
		}

		/*
		 * 4.3 Message Body: ...All responses to the HEAD this.request
		 * method MUST NOT include a message-body, even though the
		 * presence of entity-header fields might lead one to
		 * believe they do. All 1xx (informational), 204 (no
		 * content), and 304 (not modified) responses MUST NOT
		 * include a message-body. All other responses do include a
		 * message-body, although it MAY be of zero length.
		 */
		if (contentLength == 0 || this.request.getMethod().equals ("HEAD")
				|| statusCode == 204 || statusCode == 304
				|| (100 <= statusCode && statusCode < 200))
		{
			this.data = dataOS.getByteArray();
			headerLength = this.data.length;
		}
		else if (contentLength > 0)
		{
			headerLength = dataOS.size();

			/*
			int size = dataOS.size();

			this.data = new byte[contentLength + size];
			System.arraycopy (dataOS.getByteArray(), 0, this.data, 0, size);

			while (contentLength > 0)
			{
				int r = in.read (this.data, size, contentLength);

				if (r == -1) break;

				size += r;
				contentLength -= r;
			}
			/**/

			for (int i = 0; i < contentLength; ++i)
			{
				inInt = in.read();

				if (inInt == -1) break;

				teeOS.write (inInt);
			}

			this.data = dataOS.getByteArray();
		}
		else // message-body contains zero or more chunks dataOS...
		{
			headerLength = dataOS.size();

			if (!chunkedEncoding)
			{
				while ((inInt = in.read()) != -1)
				{
					teeOS.write (inInt);
				}
			}
			else
			{
				// read all the dataOS chunks..
				StringBuffer sb = new StringBuffer(8);
				int length;

				do
				{
					// chunk = chunk-size [ chunk-extension ] CRLF
					// chunk-dataOS CRLF
					sb.setLength (0);

					while ((inInt = in.read()) != -1)
					{
						teeOS.write (inInt);
						if (inInt == ';' || inInt == '\r')
							break;
						sb.append ((char) inInt);
					}

					length = Integer.valueOf (sb.toString().trim(), 16).intValue();

					// skip rest of this line
					do
					{
						teeOS.write (inInt = in.read());
					}
					while (inInt != '\n');

					if (length > 0)
					{
						for (int i = 0; i < length; ++i)
						{
							teeOS.write (in.read());
						}
					}

					// skip 1 line
					do
					{
						teeOS.write (inInt = in.read());
					}
					while (inInt != '\n');
				}
				while (length > 0);
			}

			this.data = dataOS.getByteArray();
		}
	}

	public byte[] getData() {return this.data;}
	public int getStatusCode() {return statusCode;}
	public Date getLastModified() {return lastModified;}
	public String getContentType() {return contentType;}
	public String getHttpVersion () {return httpVersion;}

	public String getContentText()
	{
		if (contentType.startsWith ("text/")
				&& this.data.length > headerLength)
		{
			try
			{
				return new String (this.data, headerLength,
						this.data.length - headerLength, "UTF-8");
			}
			catch (UnsupportedEncodingException uee)
			{
				uee.printStackTrace();
			}
		}

		return null;
	}
}
