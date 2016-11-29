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
			//����xml
			String login_xml = XMLUtil.constructLoginXML(id, pwd);
			System.out.println(login_xml);
			
			client.socket.getOutputStream().write(login_xml.getBytes());
			//������֤���
			client.in = client.socket.getInputStream();
			String result = Util.readFromInputStream(client.in);
			System.out.println("���յ���½�����" + result);
			
			if(result.equals("succeed"))	//��½�ɹ�
			{
				System.out.println("��½�ɹ�");
				MainFrame mf = new MainFrame(id, client.socket);
				client.setVisible(false);
				ReceiveMsgThread rmt = new ReceiveMsgThread(mf, client.in);
				rmt.start();
			}
			else if(result.equals("error"))	//��½ʧ��
			{
				client.tipLabel.setText("�˻����������");
				//client.socket.close();
				return;
			}
		}
		catch (Exception e)
		{
			System.out.println("����ʧ��!");
			client.tipLabel.setText("���ӷ�����ʧ�ܣ��������������������Ƿ���ȷ��");
			return;
		}
	}
	
}
