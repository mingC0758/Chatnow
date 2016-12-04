package momingqi.client;

import java.net.Socket;
import momingqi.util.XMLUtil;

/**
 * 连接服务器的请求线程
 * @author mingC
 *
 */
public class ClientConnectionThread extends Thread
{
	private Client client;
	
	public ClientConnectionThread(Client client)
	{
		this.client = client;
	}
	
	@Override
	public void run()
	{
		try
		{
			String host = client.ipTextField.getText();
			String port = client.portTextField.getText();
			
			System.out.println(host + port);
			client.socket = new Socket(host, Integer.parseInt(port));
			String id = client.idTextField.getText();
			String pwd = client.pwdTextField.getText();
			//构建xml
			String login_xml = XMLUtil.constructLoginXML(id, pwd);
			System.out.println(login_xml);
			
			client.socket.getOutputStream().write(login_xml.getBytes());
			//接收验证结果
			
			ClientAcceptMsgThread rmt = new ClientAcceptMsgThread(client, client.socket);
			rmt.start();
		}
		catch (Exception e)
		{
			System.out.println("连接失败!");
			client.tipLabel.setText("连接服务器失败！请检查网络或服务器配置是否正确。");
			return;
		}
	}
	
}
