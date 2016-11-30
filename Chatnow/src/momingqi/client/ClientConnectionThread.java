package momingqi.client;

import java.io.InputStream;
import java.net.Socket;

import momingqi.util.Util;
import momingqi.util.XMLUtil;

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
			client.in = client.socket.getInputStream();
			String result = Util.readFromInputStream(client.in);
			System.out.println("接收到登陆结果：" + result);
			
			if(result.equals("succeed"))	//登陆成功
			{
				System.out.println("登陆成功");
				MainFrame mf = new MainFrame(id, client.socket);
				client.setVisible(false);
				ClientAcceptMsgThread rmt = new ClientAcceptMsgThread(mf, client.in);
				rmt.start();
			}
			else if(result.equals("error"))	//登陆失败
			{
				client.showError("账户或密码错误！");
				client.socket.close();
				return;
			}
			else if(result.equals("repeat"))	//重复登陆
			{
				client.showError("请勿重复登陆！");
				client.socket.close();
				return;
			}
		}
		catch (Exception e)
		{
			System.out.println("连接失败!");
			client.tipLabel.setText("连接服务器失败！请检查网络或服务器配置是否正确。");
			return;
		}
	}
	
}
