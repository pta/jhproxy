/*
 * pta - PTA's Java Library Package
 * Copyright (C) 2010  PHAM Tuan Anh
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
package pta.util;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Color;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

/**
 * @see Properties
 * @author Phạm Tuấn Anh
 * @version 1.0
 */
public final class DataProperties extends Properties
{
	private static final long	serialVersionUID	= 1149422039950187037L;

	/**
	 * Construct a empty {@link DataProperties} with no defaults
	 * {@link Properties}.
	 * 
	 * @see Properties#Properties()
	 */
	public DataProperties()
	{
		super();
	}

	/**
	 * Construct a empty {@link DataProperties} with the given defaults
	 * {@link Properties}.
	 * 
	 * @see Properties#Properties(Properties)
	 */
	public DataProperties (Properties defaults)
	{
		super (defaults);
	}

	/**
	 * Loads properties from the file specified by <code>inputFile</code>.
	 * 
	 * @param inputFile
	 *        name of the properties file to load.
	 * @throws IOException
	 *         If an I/O error occurs.
	 * @exception IllegalArgumentException
	 *            If <code>inputFile</code> contains a malformed Unicode escape
	 *            sequence.
	 * @exception NullPointerException
	 *            If <code>inputFile</code> is <code>null</code>.
	 * @see #load(InputStream)
	 * @see #load(String,String)
	 */
	public void load (String inputFile) throws IOException
	{
		BufferedInputStream bis = new BufferedInputStream (
				new FileInputStream (inputFile)); 
		this.load (bis);
		bis.close();
	}

	public void loadFromXML (String inputFile) throws IOException, InvalidPropertiesFormatException
	{
		BufferedInputStream bis = new BufferedInputStream (
				new FileInputStream (inputFile)); 
		this.loadFromXML (bis);
		bis.close();
	}
	
	public void storeToXML (String outputFile, String comment) throws IOException
	{
		BufferedOutputStream bos = new BufferedOutputStream (
				new FileOutputStream (outputFile)); 
		this.storeToXML (bos, comment);
		bos.flush();
		bos.close();
	}

	/**
	 * @param key
	 *        the key specify the property
	 * @param delimExp
	 *        {@link java.util.regex.Pattern regular expression} delimit to
	 *        split the properties value. If zero-lenth or null, the default
	 *        delimit is (\s);
	 * @return an array of {@link String} split from the value of the key and
	 *         delimit
	 * @see java.util.regex.Pattern
	 */
	public String[] getStringArray (String key, String delimExp)
	{
		String all = getProperty(key).trim();
		if (all == null) return null;
		if (delimExp == null || delimExp.length() == 0)
			return all.split ("\\s");
		else
			return all.split (delimExp);
	}

	/**
	 * set the value to the key as an properties. If <code>defaults</code>
	 * defined, the method search for the key in the <code>defaults</code>, it
	 * make sure that no duplicate key-value pairs in the {@link DataProperties}
	 * itself and its <code>defaults</code>
	 * 
	 * @param key
	 *        the key specify the property
	 * @param value
	 *        value of the property to set to the key.
	 * @see Properties#setProperty(String,String)
	 * @see Properties#Properties(Properties)
	 * @return the value {@link Object} it self
	 */
	public Object setProperty (String key, String value)
	{
		if (defaults == null) return super.setProperty (key, value);
		if (defaults.getProperty (key).equals (value)) return remove (key);
		else return super.setProperty (key, value);
	}

	/**
	 * get the time distance measured in milliseconds in the following format:
	 * key : [<hours>h][<minutes>'][<seconds>"][<miliseconds>]
	 * 
	 * @return an long integer of time distance in milliceconds, <code>-1</code>
	 *         if invalid format
	 */
	public long getTime (String key)
	{
		String time = getProperty(key);
		if (time == null) return -1;
		if (time.length() == 0) return 0;
		long ret = 0;
		try
		{
			int pos;
			if (0 < (pos = time.indexOf ('h')))
			{
				ret = 60 * 60 * 1000 * Long.parseLong (time.substring (0, pos));
				time = time.substring (++pos);
			}
			if (0 < (pos = time.indexOf ('m')))
			{
				ret += 60 * 1000 * Long.parseLong (time.substring (0, pos));
				time = time.substring (++pos);
			}
			if (0 < (pos = time.indexOf ('s')))
			{
				ret += 1000 * Long.parseLong (time.substring (0, pos));
				time = time.substring (++pos);
			}
			if (0 < time.length())
			{
				ret += Long.parseLong (time);
			}
		}
		catch (NumberFormatException nfe)
		{
			return -1;
		}
		return ret;
	}

	/**
	 * set the <code>integer</code> property
	 * 
	 * @see #getInt(String)
	 */
	public void setInt (String key, int value)
	{
		setProperty (key, Integer.toString (value));
	}

	/**
	 * get the <code>integer</code> property
	 * 
	 * @return <code>0</code> if the property not found or not a number
	 * @see #setInt(String,int)
	 */
	public int getInt (String key)
	{
		try
		{
			return Integer.parseInt (getProperty (key));
		}
		catch (NumberFormatException nfe)
		{
			return 0;
		}
		catch (NullPointerException npe)
		{
			return 0;
		}
	}

	/**
	 * set the <code>long</code> property
	 * 
	 * @see #getLong(String)
	 */
	public void setLong (String key, long value)
	{
		setProperty (key, Long.toString (value));
	}

	/**
	 * get the <code>long</code> property
	 * 
	 * @return <code>0</code> if the property not found or not a number
	 * @see #setLong(String,long)
	 */
	public long getLong (String key)
	{
		try
		{
			return Long.parseLong (getProperty (key));
		}
		catch (NumberFormatException nfe)
		{
			return 0;
		}
		catch (NullPointerException npe)
		{
			return 0;
		}
	}

	/**
	 * set the property specified by key to <code>1</code> if value is
	 * <code>true</code>, and <code>0</code> if value is <code>false</code>
	 * 
	 * @see #getBoolean(String)
	 */
	public void setBoolean (String key, boolean value)
	{
		setProperty (key, value ? "1" : "0");
	}

	/**
	 * get the <code>boolean</code> property specified by the key
	 * 
	 * @return <code>true</code> if value is <code>1</code>, <code>false</code>
	 *         if values is <code>0</code> or not found
	 * @see #setBoolean(String,boolean)
	 */
	public boolean getBoolean (String key)
	{
		if (getInt (key) == 1) return true;
		else return false;
	}

	/**
	 * get the color property. The property must in one of the folowing format:
	 * <code>
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;0xRRGGBB
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp; #RRGGBB
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;rr gg bb
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;rgb</code>
	 * 
	 * @param key
	 *        the key to find the property
	 * @param defaults
	 *        {@link Color} object use when property is invalid or not found.
	 * @return the {@link Color} object specified by input key
	 */
	public Color getColor (String key, Color defaults)
	{
		String str = getProperty (key);
		if (str == null) return defaults;
		try
		{
			if (str.startsWith ("0x"))
			{
				return new Color (Integer.parseInt (str.substring (2), 16));
			}
			else if (str.startsWith ("#"))
			{
				return new Color (Integer.parseInt (str.substring (1), 16));
			}
			else if (str.indexOf (' ') != -1)
			{
				StringTokenizer tk = new StringTokenizer (str);
				return new Color (Integer.parseInt (tk.nextToken()), Integer
						.parseInt (tk.nextToken()), Integer.parseInt (tk
						.nextToken()));
			}
			else
			{
				return new Color (Integer.parseInt (str));
			}
		}
		catch (NumberFormatException e)
		{
			System.err.println (key + " invalid");
			return defaults;
		}
	}

	/**
	 * return {@link #getColor(String,Color) getColor(key,}{@link Color#RED}
	 * {@link #getColor(String,Color) )}
	 * 
	 * @param key
	 *        the key to find the property
	 * @return the {@link Color} object specified by input key
	 */
	public Color getColor (String key)
	{
		return getColor (key, Color.RED);
	}

	/**
	 * get the {@link ImageIcon} specified by the property key
	 * 
	 * @param key
	 *        the key to find the property
	 * @return the {@link ImageIcon} object specified by input key
	 */
	public ImageIcon getImageIcon (String key)
	{
		return new ImageIcon (getProperty (key));
	}

	/**
	 * get the scaled instance of {@link ImageIcon} specified by the property
	 * key
	 * 
	 * @return the scaled to <code>(width,height)</code> instance of
	 *         {@link ImageIcon} specified by the input key
	 */
	public ImageIcon getImageIcon (String key, int width, int height)
	{
		return new ImageIcon (getImageIcon (key).getImage().getScaledInstance (
				width, height, Image.SCALE_SMOOTH));
	}

	public static void main (String[] args)
	{
		try
		{
			DataProperties prop = new DataProperties();
			prop.loadFromXML ("/tmp/htproxy.xml");

			for (String s : prop.getStringArray ("cache.duration.filters.url", "\n"))
				System.out.println(s);
			//prop.list(System.out);
			//System.out.println (" = " + prop.getTime ("shit"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
