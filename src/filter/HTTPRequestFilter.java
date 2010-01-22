package filter;

import name.pham.anh.net.http.HTTPRequest;

public class HTTPRequestFilter implements Filter
{
	public boolean blocks (Object obj)
	{
		if (!(obj instanceof HTTPRequest))
			return false;

		return true;
	}
}
