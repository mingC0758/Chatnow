import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import momingqi.util.Util;

import org.xml.sax.SAXException;




public class test
{
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, InterruptedException
	{
		JFrame jf = new JFrame();
		String str = "接受到来自 "+"的消息，是否查看？";
		Icon icon = new ImageIcon(Util.ClientImagePath + "Image1.jpg");
		String[] options = {"查看", "忽略", "确认"};	//对话框选项
		//弹出对话框提醒
		int result = JOptionPane.showOptionDialog(jf, str, "新消息", JOptionPane.INFORMATION_MESSAGE, (Integer) null, icon, options, "查看");
		if(result == 0)
		{
			System.out.println("YES");
		}
		else System.out.println("NO");
		System.out.println("test");
	}
}
