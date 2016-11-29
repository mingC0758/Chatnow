package momingqi.server;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import momingqi.util.Util;
import momingqi.util.XMLUtil;

import org.xml.sax.SAXException;

/**
 * ������տͻ��˵�����
 * @author mingC
 *
 */
public class ServerConnectThread extends Thread
{
	private Server server;
	private int port;
	private File usersxml;
	
	public ServerConnectThread(Server server, int port)
	{
		this.server = server;
		this.port = port;
		this.usersxml = new File("./server_resources/users.xml");
	}
	
	@Override
	public void run()
	{
		try
		{
			server.serverSocket = new ServerSocket(port);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(server, "�˿��ѱ�ռ�ã�");
			return;
		}
		server.portTextField.setEditable(false);
		server.statusLabel.setForeground(Color.YELLOW);
		server.statusLabel.setText("������");
		server.startButton.setEnabled(false);
		server.userList = new LinkedList<User>();
		
		System.out.println("������������..");
		while (true)
		{
			try
			{
				Socket socket = server.serverSocket.accept();
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
				System.out.println("someone connecting..");
				String xml = Util.readFromInputStream(socket.getInputStream());	//��ȡ��½��Ϣ
				System.out.println("���յ��ĵ�½��Ϣ��" + xml);
				String[] u = XMLUtil.parseLoginXML(xml);			//��ȡ�û��ύ��id������
				String id = u[0];
				String pwd = u[1];
				String cor_pwd = XMLUtil.getPwd(usersxml, id);	//��ȡ��ȷ����
				System.out.println("��ȷ���룺"+cor_pwd + " ���û�ʹ�õ����룺" + pwd);
				if (cor_pwd != null && cor_pwd.equals(pwd))	//������ȷ
				{
					//���͵�½�ɹ�����Ϣ���ͻ���
					socket.getOutputStream().write("succeed".getBytes());
					socket.getOutputStream().flush();
					User user = new User(socket, id);
					server.sendUserlist(socket);	//�ѵ�ǰ�������û��б����µ�½���û�
					server.addUser(user);	//��½�ɹ��������û���ӵ��û��б�
					//���������˿ͻ�����Ϣ���߳�
					ReceiveMsgThread rmt = new ReceiveMsgThread(server, in, user);
					rmt.start();
				}
				else						//�������
				{
					socket.getOutputStream().write("error".getBytes());
					socket.getOutputStream().flush();
					System.out.println("���û���½ʱʹ���˴�����˺Ż�����");
					socket.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (SAXException e)
			{
				e.printStackTrace();
			}
			catch (ParserConfigurationException e)
			{
				e.printStackTrace();
			}
		}
	}
}
