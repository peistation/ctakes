package clear.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class JString
{
	/** @return delim.join(list) as in Python */
	static public String join(ArrayList<String> list, String delim)
	{
		StringBuilder buff = new StringBuilder();
		
		for (int i=0; i<list.size(); i++)
		{
			buff.append(list.get(i));
			if (i+1 < list.size())	buff.append(delim);
		}
		
		return buff.toString();
	}
	
	/** @return delim.join(list) as in Python */
	static public String join(String[] list, String delim)
	{
		StringBuilder buff = new StringBuilder();
		
		for (int i=0; i<list.length; i++)
		{
			buff.append(list[i]);
			if (i+1 < list.length)	buff.append(delim);
		}
		
		return buff.toString();
	}
	
	static public String getUTF8(String str)
	{
		String utf = "";
		
		try
		{
			utf = new String(str.getBytes(), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {System.err.println(e);}
		
		return utf; 
	}
}	
