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
 * 负责接收客户端的连接
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
			JOptionPane.showMessageDialog(server, "端口已被占用！");
			return;
		}
		server.portTextField.setEditable(false);
		server.statusLabel.setForeground(Color.YELLOW);
		server.statusLabel.setText("运行中");
		server.startButton.setEnabled(false);
		server.userList = new LinkedList<User>();
		
		System.out.println("服务器运行中..");
		while (true)
		{
			try
			{
				Socket socket = server.serverSocket.accept();
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
				System.out.println("someone connecting..");
				String xml = Util.readFromInputStream(socket.getInputStream());	//读取登陆信息
				System.out.println("接收到的登陆信息：" + xml);
				String[] u = XMLUtil.parseLoginXML(xml);			//获取用户提交的id和密码
				String id = u[0];
				String pwd = u[1];
				String cor_pwd = XMLUtil.getPwd(usersxml, id);	//获取正确密码
				System.out.println("正确密码："+cor_pwd + " 该用户使用的密码：" + pwd);
				if (cor_pwd != null && cor_pwd.equals(pwd))	//密码正确
				{
					//发送登陆成功的消息给客户端
					socket.getOutputStream().write("succeed".getBytes());
					socket.getOutputStream().flush();
					User user = new User(socket, id);
					server.sendUserlist(socket);	//把当前的在线用户列表发给新登陆的用户
					server.addUser(user);	//登陆成功，将此用户添加到用户列表
					//开启监听此客户端消息的线程
					ReceiveMsgThread rmt = new ReceiveMsgThread(server, in, user);
					rmt.start();
				}
				else						//密码错误
				{
					socket.getOutputStream().write("error".getBytes());
					socket.getOutputStream().flush();
					System.out.println("有用户登陆时使用了错误的账号或密码");
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
