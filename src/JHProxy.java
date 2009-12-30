import java.net.*;
import java.io.*;
import pta.util.*;

public final class JHProxy
{
	DataProperties	propMain;
	boolean			isRunning;

	ThreadPool		threadPool = new ThreadPool();

	String			targetProxyHost;
	int				targetProxyPort;

	ServerSocket	listeningSocket;
	int				listeningPort;

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
			System.exit(1);
		}

		targetProxyHost = propMain.getProperty ("target_proxy.host");
		targetProxyPort = propMain.getInt ("target_proxy.port");

		listeningPort = propMain.getInt ("listening_port");

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
		new JHProxy().start();
	}
}
