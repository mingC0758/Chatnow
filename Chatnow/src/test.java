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
		String str = "���ܵ����� "+"����Ϣ���Ƿ�鿴��";
		Icon icon = new ImageIcon(Util.ClientImagePath + "Image1.jpg");
		String[] options = {"�鿴", "����", "ȷ��"};	//�Ի���ѡ��
		//�����Ի�������
		int result = JOptionPane.showOptionDialog(jf, str, "����Ϣ", JOptionPane.INFORMATION_MESSAGE, (Integer) null, icon, options, "�鿴");
		if(result == 0)
		{
			System.out.println("YES");
		}
		else System.out.println("NO");
		System.out.println("test");
	}
}
