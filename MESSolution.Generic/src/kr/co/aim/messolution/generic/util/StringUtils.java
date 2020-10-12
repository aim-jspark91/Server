package kr.co.aim.messolution.generic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class StringUtils extends org.apache.commons.lang.StringUtils{
	
	private static Log log = LogFactory.getLog(StringUtils.class);
	
	public static boolean startWithAny(String source, String... values)
	{
		for(String value : values)
		{
			if(StringUtil.startsWith(source, value))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean equalsIn(String source, String... values)
	{
		for(String value : values)
		{
			if(StringUtil.equals(source, value))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean equalsAll(String source, String... values)
	{
		for(String value : values)
		{
			if(!StringUtil.equals(source, value))
			{
				return false;
			}
		}
		return true;
	}

	public static boolean equalsAllList(String source, List<String> values)
	{
		for(String value : values)
		{
			if(!StringUtil.equals(source, value))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean equalsInList(String source, List<String> values)
	{
		for(String value : values)
		{
			if(StringUtil.equals(source, value))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isEmptyAll(String... values)
	{
		for(String value : values)
		{
			if(StringUtil.isNotEmpty(value))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isNotEmptyAll(String... values)
	{
		for(String value : values)
		{
			if(StringUtil.isEmpty(value))
			{
				return false;
			}
		}
		return true;
	}
	
	public static int countMatches(String source, String... values)
    {
		int count = 0;
		
        for (String value : values)
        {
			try {
				count += StringUtil.countMatches(source, value);
			} catch (Exception e) {
				log.info("CommonUtil.countMatches error.");
			}
		}
        
        return count;
    }
	
	public static boolean regexp_like(String str, String pattern)
	{
		Pattern p = Pattern.compile(pattern);
		java.util.regex.Matcher m = p.matcher(str);
		
		if( m.find() )
		{
			return true;
		}
		
		return false;
	}
	
	public static List<String> regexp_substr(String str, String pattern)
	{
		List<String> returnValue = new ArrayList<String>();
		Pattern p = Pattern.compile(pattern);
		java.util.regex.Matcher m = p.matcher(str);
		
		while(m.find())
		{
			returnValue.add(m.group());
		}

		return returnValue;
	}
	
	public static String regexp_replace(String str, String pattern, String replaceValue)
	{
		/*
		 * Pattern Oracle : Java    
		 *           \2   =  $2
		 */
		if( str == null)
			return "";
		
		return str.replaceAll(pattern, replaceValue);
	}
	
	public static String getNVLString(String... values) throws CustomException
	{
		for (String value : values)
		{
			if(StringUtil.isNotEmpty(value))
			{
				return value;
			}
		}
		
		return StringUtil.EMPTY;
	}
	
	public static String concat(List<String> stringList, String separator) throws CustomException
	{
		return StringUtils.concat(stringList, separator, null, null);
	}
	
	public static String concat(List<String> stringList, String separator, String prefix, String suffix) throws CustomException
	{
		if(CommonUtil.isNullOrEmpty(stringList))
			return "";
		
		StringBuffer buffer = new StringBuffer();
		
		for(String string : stringList)
		{
			if(StringUtils.isNotEmpty(prefix))
			{
				buffer.append(prefix);
			}
			
			buffer.append(string);
			
			if(StringUtils.isNotEmpty(suffix))
			{
				buffer.append(suffix);
			}
			if( StringUtil.isNotEmpty(separator))
			{
				buffer.append(separator);
			}
		}
		
		if( StringUtil.isNotEmpty(separator))
		{
			return StringUtil.removeEnd(buffer.toString(), separator);
		}
		
		return buffer.toString();
	}
	
	public static String concat(List<String> stringList) throws CustomException
	{
		return concat(stringList, null);
	}
	
	public static String lpad(String text, int length, String padder)
	{
		if(length <= 0)
			return "";
		
		int textLength = StringUtils.length(text);
		
		if(StringUtils.length(text) > length)
			return StringUtils.substring(text, 0, length-1);
		
		StringBuffer buffer = new StringBuffer(text);
		buffer.insert(0,StringUtils.pad(padder, length-textLength));
		
		return buffer.toString();
	}
	
	public static String pad(String pattern, int length)
	{
		if(length <= 0)
			return "";
		int textLength = StringUtils.length(pattern);
		if(textLength < length)
		{
			StringBuffer buffer = new StringBuffer();
			int fullTextQty = length / textLength;
			int remain = length % textLength;
			for(int i=0; i<fullTextQty; i++)
			{
				buffer.append(pattern);
			}
			if(remain > 0)
			{
				buffer.insert(0, StringUtil.substring(pattern, 0, remain-1));
			}
			return buffer.toString();
		}
			
		if(textLength > length)
		{
			return StringUtils.substring(pattern, 0, length-1);
		}
		
		return pattern;
	}
	
	
	public static String getSingleCharInPosition(String string, int position) throws CustomException
	{
		try
		{
			return StringUtil.substring(string, position-1, position);
			
		}catch(Exception e)
		{
			log.info("SubString Error : CommonUtil.getSingleCharInPosition ");
			return "";
		}
	}
}
