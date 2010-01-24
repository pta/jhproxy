/*
 * JHProxy - A Java HTTP Proxy Server Application
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

import java.net.*;
import java.io.*;

import http.*;

public final class ConnectionHandler implements Runnable
{
	JHProxy			proxy;
	Socket			incSocket;

	/**
	 * constructs an instance of ConnectionHandler.
	 */
	ConnectionHandler (JHProxy proxy, Socket incSocket)
	{
		this.proxy = proxy;
		this.incSocket = incSocket;
	}

	public void run()
	{
		HTTPRequest request = new HTTPRequest();

		try
		{
			InputStream incIS = incSocket.getInputStream();

			try
			{
				request.parse (incIS);
			}
			catch (SocketException se)
			{
				System.err.println ("Incoming connection reset: socket read error");
			}
			catch (IOException ioe)
			{
				System.err.println ("Cannot parse the request");
			}
			finally
			{
				try {incSocket.shutdownInput();} catch (IOException ioe) {}
				incIS = null;
			}

			if (request.getURL() == null) return;

			OutputStream incOS = incSocket.getOutputStream();

			try
			{
				if (proxy.filter.blocks (request))
				{
					incOS.write (HTTPResponse.FORBIDDENT_RESPONSE_DATA);
				}
				else
				{
					Socket outSocket = null;

					try
					{
						if (proxy.getTargetProxyHost() == null)
							outSocket = new Socket (request.getHost(), request.getPort());
						else
							outSocket = new Socket (proxy.getTargetProxyHost(), proxy.getTargetProxyPort());

						getResponse (request, outSocket, incOS);
					}
					catch (UnknownHostException uhe)
					{
						System.err.println ("Target or proxy host not found!");
					}
					catch (SocketException se)
					{
						System.err.println ("Connection reset by peer: socket write error");
					}
					finally
					{
						try {outSocket.close();} catch (IOException ioe) {}
						outSocket = null;
					}
				}
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			finally
			{
				try {incOS.flush();} catch (IOException ioe) {}
				try {incOS.close(); } catch (IOException ioe) {}
				incOS = null;
			}
		}
		catch (IOException ioe)
		{
			System.err.println ("Cannot create incoming socket's I/O stream!");
			ioe.printStackTrace();
		}
		finally
		{
			try {incSocket.close();} catch (IOException ioe) {}
			incSocket = null;
		}
	}

	public HTTPResponse getResponse (HTTPRequest request, Socket outSocket, OutputStream incOS) throws IOException
	{
		OutputStream outOS = outSocket.getOutputStream();

		try
		{
			outOS.write (request.getData());
			outOS.flush();

			InputStream outIS = outSocket.getInputStream();
			try
			{
				HTTPResponse response = new HTTPResponse (request);
				response.parse (outIS, incOS);
				return response;
			}
			finally
			{
				outIS.close();
				outIS = null;
				incOS.flush();
				incOS.close();
			}
		}
		finally
		{
			outOS.close();
			outOS = null;
		}
	}
}
