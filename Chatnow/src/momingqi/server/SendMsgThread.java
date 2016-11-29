package momingqi.server;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JOptionPane;

public class SendMsgThread extends Thread
{
	private Server server;
	private OutputStream out;
	private String msg;
	
	public SendMsgThread(Server server, OutputStream out, String msg)
	{
		this.server = server;
		this.out = out;
		this.msg = msg;
	}


	@Override
	public void run()
	{
		try
		{
			out.write(msg.getBytes());
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(server, "发送消息失败");
			return;
		}
	}
}