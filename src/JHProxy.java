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
import filter.*;

import name.pham.anh.util.*;

public final class JHProxy
{
	DataProperties	propMain;
	boolean			isRunning;

	ThreadPool		threadPool = new ThreadPool();

	String			targetProxyHost;
	int				targetProxyPort;

	ServerSocket	listeningSocket;
	int				listeningPort;

	Filter			filter;

	public JHProxy()
	{
		propMain = new DataProperties();

		try
		{
			if (new File ("jhproxy.xml").exists())
				propMain.loadFromXML ("jhproxy.xml");
			else
				propMain.loadFromXML ("../jhproxy.xml");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		targetProxyHost = propMain.getProperty ("target_proxy.host");
		targetProxyPort = propMain.getInt ("target_proxy.port");

		listeningPort = propMain.getInt ("listening_port");

		loadSettings (propMain);
	}

	public void loadSettings (DataProperties prop)
	{
		String[] urlFilters = prop.getStringArray ("filter.url", null);
		filter = new HTTPRequestFilter (urlFilters);
	}

	public void start()
	{
		try
		{
			listeningSocket = new ServerSocket (listeningPort);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		System.out.println ("Starting Proxy at port: " + listeningPort);

		if (targetProxyHost != null)
			System.out.println ("Connections established through target proxy: "
					+ targetProxyHost + ":" + targetProxyPort);

		this.run();
	}

	public void run()
	{
		try
		{
			while (true)
			{
				Socket incSocket = listeningSocket.accept();
				threadPool.handle (new ConnectionHandler (this, incSocket));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getTargetProxyHost()
	{
		return targetProxyHost;
	}

	public int getTargetProxyPort()
	{
		return targetProxyPort;
	}

	public static void main (String[] args)
	{
		System.out.println();
		System.out.println ("JHProxy  Copyright (C) 2010  PHAM Tuan Anh <cs.PhamTuanAnh@gmail.com>");
		System.out.println();
		System.out.println ("This program comes with ABSOLUTELY NO WARRANTY.");
		System.out.println ("This is free software, and you are welcome to");
		System.out.println ("redistribute it under certain conditions.");
		System.out.println();

		new JHProxy().start();
	}
}
