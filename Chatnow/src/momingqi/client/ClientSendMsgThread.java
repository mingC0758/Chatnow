package momingqi.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

/**
 * 发送消息msg给cf聊天框对应的好友，若发送失败则在cf上显示相关信息
 * @author mingC
 *
 */
public class ClientSendMsgThread extends Thread
{
	private ChatFrame cf;
	private String msg;
	
	public ClientSendMsgThread(ChatFrame cf, String msg)
	{
		this.cf = cf;
		this.msg = msg;
	}


	@Override
	public void run()
	{
		OutputStream out;
		try
		{
			out = cf.getOutputStream();
			out.write(msg.getBytes());
			out.flush();
			System.out.println("向服务器发送消息：" + msg);
		}
		catch (IOException e)
		{
			System.out.println("发送失败！");
			cf.showError("发送失败！请检查网络状况。");
			JOptionPane.showMessageDialog(cf, "与服务器断开连接！请重新登陆");
			return;
		}
	}
}
