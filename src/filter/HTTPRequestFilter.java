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
