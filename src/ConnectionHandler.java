import java.net.*;
import java.io.*;
import pta.net.http.*;

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
				System.out.println ("Connection reset");
			}
			catch (IOException ioe)
			{
				System.out.println ("Cannot parse the request");
				ioe.printStackTrace();
				return;
			}
			finally
			{
				incIS = null;

				try
				{
					incSocket.shutdownInput();
					// inRequest.close();
				}
				catch (IOException ioe) {}
			}

			if (request.getURL() == null) return;

			{ // passed the URLFilter
				Socket outSocket;
				try
				{
					if (proxy.getTargetProxyHost() == null)
						outSocket = new Socket (request.getHost(), request.getPort());
					else
						outSocket = new Socket (proxy.getTargetProxyHost(), proxy.getTargetProxyPort());

					try
					{
						HTTPResponse response = getResponse (request, outSocket);

						OutputStream incOS = incSocket.getOutputStream();
						try
						{
							try
							{
								incOS.write (response.getData());
							}
							catch (SocketException se)
							{
								System.out.println ("Connection reset by peer: socket write error");
							}

							incOS.flush();
						}
						finally
						{
							try
							{
								incOS.flush();
							}
							catch (IOException ioe) {}
							try
							{
								incOS.close();
							}
							catch (IOException ioe) {}

							incOS = null;
						}
						// if (response.getStatusCode()==304) { // Not
						// Modified
						// OutputStream os =new BufferedOutputStream(new
						// FileOutputStream("J:\\out.data"));
						// os.write(response.getData());
						// os.flush();
						// System.exit(0);
						// }
					}
					finally
					{
						try
						{
							outSocket.close();
						}
						catch (IOException ioe) {}

						outSocket = null;
					}
				}
				catch (UnknownHostException uhe)
				{
					System.out.println ("request or proxy host not found!");
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
		catch (IOException ioe)
		{
			System.out
					.println ("Cannot create request socket input stream!");
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				incSocket.close();
			}
			catch (IOException ioe)
			{}
			incSocket = null;
		}
	}

	public HTTPResponse getResponse (HTTPRequest request, Socket outSocket) throws IOException
	{
		OutputStream outOS = outSocket.getOutputStream();

		try
		{
			outOS.write (request.getData());
			outOS.flush();
			// System.out.println(new
			// String(request.getData(),"ISO-8859-1"));

			InputStream outIS = outSocket.getInputStream();
			try
			{
				HTTPResponse response = new HTTPResponse();

				try
				{
					response.parse (outIS, request);
					return response;
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
					return null;
				}
			}
			finally
			{
				outIS.close();
				outIS = null;
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
		finally
		{
			outOS.close();
			outOS = null;
		}
	}
}
