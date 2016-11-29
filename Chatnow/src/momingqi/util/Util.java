package momingqi.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * ͨ���࣬��װ�˺ܶ�ʵ�÷�����������
 * @author mingC
 *
 */
public final class Util
{
	public final static int MAXUSERNUM = 20;	//����û���
	public final static String ClientImagePath = "./resources/ImageResources/";	//����û���
	/**
	 * �ж�str�Ƿ��Ǵ�����
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
	 * �ж϶˿��Ƿ�Ϸ�
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
	 * ���������ж�ȡ�ַ�������
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readFromInputStream(InputStream in) throws IOException
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
}
