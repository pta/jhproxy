import java.net.*;
import java.io.*;
import pta.util.*;

public final class JHProxy extends Thread
{
	DataProperties	propMain;
	boolean			isRunning;

	ThreadPool		threadPool = new ThreadPool();

	String			proxyHost;
	int				proxyPort;

	ServerSocket	svrSocket;

	public JHProxy()
	{
		propMain = new DataProperties();

		try
		{
			propMain.loadFromXML ("jhproxy.xml");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit (1);
		}

		proxyHost = propMain.getString ("proxy.host");

		if (proxyHost != null && proxyHost.length() == 0)
		{
			proxyHost = null;
		}

		proxyPort = propMain.getInt ("proxy.port");
		/*
		urlFilter = new URLFilter();
		urlFilter.load (propMain, "urlFilter.");
		htmlFilter = new HTMLFilter();
		htmlFilter.load (propMain, "htmlFilter.");
		cacheManager = new CacheManager();
		cacheManager.load(propMain,"cache.");
		*/
	}

	public void start()
	{
		int port = propMain.getInt ("listenning.port");
		try
		{
			svrSocket = new ServerSocket (port);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		System.out.println ("Starting Proxy at port: " + port);
		if (proxyHost != null)
			System.out.println ("Connections established through proxy: "
					+ proxyHost + ":" + proxyPort);
		isRunning = true;
		super.start();
	}

	public void run()
	{
		try
		{
			while (isRunning)
			{
				Socket incSocket = svrSocket.accept();
				threadPool.handle (new ConnectionHandler (this, incSocket));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getProxyHost()
	{
		return proxyHost;
	}

	public int getProxyPort()
	{
		return proxyPort;
	}

	public static void main (String[] args)
	{
		new JHProxy().start();
	}
}
