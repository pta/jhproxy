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

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

public class TelnetDaemon extends Thread
{
	JHProxy proxy;
	int port;

	ServerSocket telnetSocket;

	public TelnetDaemon (JHProxy proxy, int port)
	{
		this.proxy = proxy;
		this.setDaemon (true);
		this.port = port;
	}

	public TelnetDaemon (JHProxy proxy)
	{
		this (proxy, 23);
	}

	public void run()
	{
		try
		{
			System.out.println ("Starting Telnet service at port: " + port);
			telnetSocket = new ServerSocket (port);

			while (true)
			{
				Socket socket = telnetSocket.accept();

				new TelnetHandler (proxy, socket).start();
			}
		}
		catch (SocketException se)
		{
			System.out.println ("Telnet socket has been closed.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void close()
	{
		try {telnetSocket.close();} catch (IOException ioe) {ioe.printStackTrace();}
	}
}
