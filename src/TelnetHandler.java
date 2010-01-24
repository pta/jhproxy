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
import java.io.PrintStream;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.net.Socket;
import java.net.SocketAddress;

public class TelnetHandler extends Thread
{
	JHProxy proxy;
	Socket socket;

	public TelnetHandler (JHProxy proxy, Socket socket)
	{
		this.proxy = proxy;
		this.socket = socket;
		this.setDaemon (true);
	}

	public void run()
	{
		SocketAddress socketAddress = socket.getRemoteSocketAddress();
		System.out.println ("Receive a telnet session from " + socketAddress);

		try
		{
			Scanner scn = new Scanner (socket.getInputStream());
			PrintStream ps = new PrintStream (socket.getOutputStream());

			ps.println ("JHProxy Telnet Service");

			/*
			ps.print ("Login: ");
			ps.flush();
			String login = scn.nextLine();
			ps.print ("Password: ");
			ps.flush();
			String password = scn.nextLine();
			*/

			while (true)
			{
				ps.println ("\n\n   Main Menu\n");
				ps.println ("11. Reload Settings");
				ps.println ("12. Reset Proxy");
				ps.println ("13. Exit");
				ps.print (" -> ");
				ps.flush();

				int cmd = scn.nextInt();
				ps.println();
				ps.flush();

				switch (cmd)
				{
					case 11:
						ps.print ("Reload settings.. ");
						ps.flush();
						proxy.loadSettings();
						ps.println ("done.");
						ps.flush();
						break;

					case 12:
						ps.println ("Reseting proxy..");
						ps.flush();
						proxy.close();
						return;

					case 13:
						ps.println ("Bye bye. Have a nice day!!!");
						return;
				}
			}
		}
		catch (NoSuchElementException nsee)
		{
			System.out.println ("Telnet connection closed by remote client from " + socketAddress);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			System.out.println ("Close telnet session from " + socketAddress);
			try {socket.close();} catch (IOException ioe) {}
		}
	}
}
