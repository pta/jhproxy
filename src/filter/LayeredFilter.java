package filter;

import java.util.Iterator;
import java.util.ArrayList;

public class LayeredFilter extends ArrayList<Filter> implements Filter
{
	private static final long	serialVersionUID	= -208961744738610851L;

	public boolean blocks (Object obj)
	{
		for (Iterator<Filter> iter = this.iterator(); iter.hasNext();)
		{
			if (iter.next().blocks (obj))
				return true;
		}

		return false;
	}
}
