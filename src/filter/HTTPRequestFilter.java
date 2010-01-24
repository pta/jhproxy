package filter;

import java.util.regex.Pattern;
import name.pham.anh.net.http.HTTPRequest;

public class HTTPRequestFilter implements Filter
{
	private Pattern[] urlPatterns;

	public HTTPRequestFilter (String[] urls)
	{
		int n = urls.length;

		urlPatterns = new Pattern[n];

		for (int i = 0; i < n; ++i)
		{
			urlPatterns[i] = Pattern.compile (urls[i]);
		}
	}

	public boolean blocks (Object obj)
	{
		if (obj instanceof HTTPRequest)
		{
			HTTPRequest request = (HTTPRequest) obj;
			int n = urlPatterns.length;

			for (int i = 0; i < n; ++i)
			{
				if (urlPatterns[i].matcher (request.getURL().toString()).matches())
					return true;
			}
		}

		return false;
	}
}
