package momingqi.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

/**
 * ������Ϣmsg��cf������Ӧ�ĺ��ѣ�������ʧ������cf����ʾ�����Ϣ
 * @author mingC
 *
 */
public class SendMsgThread extends Thread
{
	private ChatFrame cf;
	private String msg;
	
	public SendMsgThread(ChatFrame cf, String msg)
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
		}
		catch (IOException e)
		{
			System.out.println("����ʧ�ܣ�");
			cf.showError("����ʧ�ܣ���������״����");
			JOptionPane.showMessageDialog(cf.mf, "��������Ͽ����ӣ������µ�½");
			return;
		}
	}
}
