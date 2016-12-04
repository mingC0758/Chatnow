package momingqi.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * 通用类，包装了很多实用方法，和配置
 * @author mingC
 *
 */
public final class Util
{
	public final static int MAXUSERNUM = 20;	//最大用户数
	public final static String ClientImagePath = "./resources/ImageResources/";	//最大用户数
	public final static String ClientResourcesPath = "./resources/";	//最大用户数
	public final static String ServerResourcesPath = "./server_resources/";
	
	/**
	 * 判断str是否是纯数字
	 * @param str
	 * @return
	 */
	public static final boolean isNumbers(String str)
	{
		int len = str.length();
		for(int i = 0; i < len; i++)
		{
			if(Character.isDigit(str.charAt(i)) == false)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 判断端口是否合法
	 * @param port
	 * @return
	 */
	public static final boolean isCorrectPort(String port)
	{
		if(isNumbers(port))
		{
			int p = Integer.parseInt(port);
			if(p <= 65535 && p >= 1024)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 从输入流中读取字符串内容
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public final static String readFromInputStream(InputStream in) throws IOException
	{
		byte[] b = new byte[1024];
		int length = in.read(b);
		String str = null;
		
		if(length != -1)
		{
			str = new String(b, 0, length);
		}
		
		return str;
	}
	
	/**
	 * 返回当前系统时间
	 * @return
	 */
	public final static String presentTime()
	{
		Date now = new Date();
		DateFormat df = DateFormat.getTimeInstance();
		String time = df.format(now);
		return time;
	}
}
