package momingqi.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JOptionPane;

import momingqi.util.XMLUtil;

/**
 * 处理添加好友的任务，由主界面的addButton触发
 * @author mingC
 *
 */
public class AddFriendHandler implements ActionListener
{
	private MainFrame mf;
	
	public AddFriendHandler(MainFrame mf)
	{
		this.mf = mf;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String targetID;
		//弹出对话框让用户输入id
		targetID = JOptionPane.showInputDialog(mf, "请输入好友的id", "添加好友", JOptionPane.INFORMATION_MESSAGE);
		//判断该用户是否在线
		if(mf.getFriend(targetID) != null)
		{
			JOptionPane.showMessageDialog(mf, "您已添加他为好友！");
			return;
		}
		//发送xml给服务器
		String xml = XMLUtil.constructAddFriendXML(targetID);
		new SendXMLThread(mf, xml).start();
	}
}

class SendXMLThread extends Thread
{
	private MainFrame mf;
	private String xml;
	
	public SendXMLThread(MainFrame mf, String xml)
	{
		this.mf = mf;
		this.xml = xml;
	}

	@Override
	public void run()
	{
		try
		{
			OutputStream out = mf.getOutputStream();
			out.write(xml.getBytes());
			System.out.println("向服务器发送命令：" + xml);
			out.flush();
		}
		catch (IOException e)
		{
			//发送失败
			JOptionPane.showMessageDialog(mf, "添加好友失败！");
		}
	}
}
