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
package http;

import java.io.*;
import java.util.*;
import java.text.*;

import name.pham.anh.io.FreeByteArrayOutputStream;
import name.pham.anh.io.TeeOutputStream;


public final class HTTPResponse
{
	private HTTPRequest request;
	private byte[]	data;

	private int		statusCode;
	private String	httpVersion;
	private int		headerLength;
	private String	contentType;
	private String	charset = "utf-8";
	private boolean	chunkedEncoding = false;
	private int		contentLength = -1;

	private Date	lastModified;

	private FreeByteArrayOutputStream dataOS;

	public static final byte[] FORBIDDENT_URL_RESPONSE =
			(
				"HTTP/1.1 403 Forbidden\r\n" +
				"Content-Length: 220\r\n" +
				"Content-Type: text/html; charset=iso-8859-1\r\n\r\n" +
				"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n" +
				"<html><head>\n" +
				"<title>403 Forbidden</title>\n" +
				"</head><body>\n" +
				"<h1>Forbidden</h1>\n" +
				"<p>This address is blocked by proxy server.</p>\n" +
				"<hr>\n" +
				"<address>JHProxy</address>\n" +
				"</body></html>"
			).getBytes();

	public static final byte[] FORBIDDENT_TXT_RESPONSE =
		(
			"HTTP/1.1 403 Forbidden\r\n" +
			"Content-Length: 220\r\n" +
			"Content-Type: text/html; charset=iso-8859-1\r\n\r\n" +
			"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n" +
			"<html><head>\n" +
			"<title>403 Forbidden</title>\n" +
			"</head><body>\n" +
			"<h1>Forbidden</h1>\n" +
			"<p>This content is blocked by proxy server.</p>\n" +
			"<hr>\n" +
			"<address>JHProxy</address>\n" +
			"</body></html>"
		).getBytes();

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

	public HTTPResponse (HTTPRequest request)
	{
		this.request = request;
	}

	public HTTPResponse (byte[] bytes)
	{
		data = bytes;
	}

	public void parse (InputStream in) throws IOException
	{
		this.parse (in, null);
	}

	public void parse (InputStream in, OutputStream out) throws IOException
	{
		parseHead (in);
		parseBody (in, out);
	}

	public void parseHead (InputStream in) throws IOException
	{
		dataOS = new FreeByteArrayOutputStream (1500);
		StringBuffer line = new StringBuffer (128);

		int inInt;
		int lineCnt = 0;

		while ((inInt = in.read()) != -1)
		{
			byte b = (byte) inInt;
			line.append ((char) b);
			dataOS.write(b);
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
						contentLength = Integer.parseInt (str.substring (16).trim());
					}
					else if (str.startsWith ("Transfer-Encoding: chunked"))
					{
						chunkedEncoding = true;
					}
					else if (str.startsWith ("Content-Type: "))
					{
						int p = str.indexOf (';');

						if (p > 0)
						{
							contentType = str.substring (14, p);

							int s = str.indexOf ("charset=");

							if (s > 0)
							{
								int e = str.indexOf (';', s + 8);

								if (e > 0)
									charset = str.substring (s + 8, e);
								else
									charset = str.substring (s + 8);
							}
						}
						else
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

		headerLength = dataOS.size();
	}

	public void parseBody (InputStream in) throws IOException
	{
		parseBody (in, null);
	}

	public void parseBody (InputStream in, OutputStream out) throws IOException
	{
		if (out != null)
			out.write (dataOS.getByteArray(), 0, headerLength);

		OutputStream teeOS = (out != null) ?
				new TeeOutputStream (dataOS, out) : dataOS;

		if (contentLength == 0 || this.request.getMethod().equals ("HEAD")
				|| statusCode == 204 || statusCode == 304
				|| (100 <= statusCode && statusCode < 200))
		{
			/*
			 * 4.3 Message Body: ...All responses to the HEAD request
			 * method MUST NOT include a message-body, even though the
			 * presence of entity-header fields might lead one to
			 * believe they do. All 1xx (informational), 204 (no
			 * content), and 304 (not modified) responses MUST NOT
			 * include a message-body. All other responses do include a
			 * message-body, although it MAY be of zero length.
			 */
		}
		else if (contentLength > 0)
		{
			for (int i = 0; i < contentLength; ++i)
			{
				int inInt = in.read();

				if (inInt == -1) break;

				teeOS.write (inInt);
			}
		}
		else // message-body contains zero or more chunks dataOS...
		{
			if (!chunkedEncoding)
			{
				int inInt;
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

					int inInt;
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
		}

		this.data = dataOS.getByteArray();
		dataOS = null;
	}

	public byte[] getData() {return this.data;}
	public int getStatusCode() {return statusCode;}
	public Date getLastModified() {return lastModified;}
	public String getContentType() {return contentType;}
	public String getHttpVersion () {return httpVersion;}

	public boolean hasTextBody()
	{
		return (contentType != null && contentType.startsWith ("text/"));
	}

	public String getBodyText()
	{
		if (hasTextBody() && this.data.length > headerLength)
		{
			try
			{
				return new String (this.data, headerLength,
						this.data.length - headerLength, charset);
			}
			catch (UnsupportedEncodingException uee)
			{
				System.out.println ("Unsupport content encoding: " + charset);
			}
		}

		return null;
	}
}
