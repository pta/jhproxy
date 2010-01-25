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
			PrintStream ps = new PrintStream (socket.getOutputStream(), true);

			ps.println ("JHProxy Telnet Service");

			/*
			ps.print ("Login: ");
			String login = scn.nextLine();
			ps.print ("Password: ");
			String password = scn.nextLine();
			*/

			while (true)
			{
				ps.println ("\n\n   Main Menu\n");
				ps.println ("1.  Status");
				ps.println ("2.  Reload Settings");
				ps.println ("3.  Reset Proxy");
				ps.println ("0.  Exit");

				do
				{
					ps.print (" -> ");

					String cmd = scn.nextLine();
					ps.println();

					if (cmd.equals ("1"))
					{
						ps.println (proxy.getSettingsString());
					}
					else if (cmd.equals ("2"))
					{
						ps.print ("Reload settings.. ");
						proxy.loadSettings();
						ps.println ("done.");
					}
					else if (cmd.equals("3"))
					{
						ps.print ("Reseting proxy? [y/n] ");
						cmd = scn.nextLine();
						ps.println();

						if (cmd.toLowerCase().startsWith("y"))
						{
							ps.println ("Reseting proxy..\n");
							proxy.close();
							return;
						}
						else
							ps.println ("Reset canceled.");
					}
					else if (cmd.equals("0"))
					{
						ps.println ("Bye bye. Have a nice day!!!");
						return;
					}
					else
						continue;
				}
				while (false);
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
