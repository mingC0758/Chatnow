package momingqi.server;

import java.awt.Color;
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

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

/**
 * 线程：用于处理客户端的连接
 * 接受用户端的连接，获得id和pwd，在user.xml中查找相关用户，若失败则返回error，成功返回succeed。
 * 成功后将获得该用户信息封装到User中，并调用server的addOnlineUser()方法
 * @author mingC
 *
 */
public class ServerConnectThread extends Thread
{
	private Server server;
	private int port;
	
	public ServerConnectThread(Server server, int port)
	{
		this.server = server;
		this.port = port;
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
		server.onlineList = new LinkedList<User>();
		
		server.log("Server Running..");
		while (true)
		{
			try
			{
				Socket socket = server.serverSocket.accept();	//接收连接请求，创建socket
				InputStream in = socket.getInputStream();		//获得输入流
				OutputStream out = socket.getOutputStream();	//获取输出流
				
				//从输入流读取登陆的xml消息
				server.log("Someone Connecting..");
				String logxml = Util.readFromInputStream(in);	
				
				//解析xml获取用户提交的log_id和log_pwd
				server.log("accept login:" + logxml);
				String[] loginfo = XMLUtil.parseLoginXML(logxml);	//获取用户提交的id和密码
				String log_id = loginfo[0];
				String log_pwd = loginfo[1];
				
				//从数据库users.xml中获取正确的密码cor)pwd
				String[] userInfo = XMLUtil.parseUsersXML(log_id);//从users.xml中获取正确密码
				String cor_pwd = null;
				if(userInfo != null)
				{	
					cor_pwd = userInfo[1];	//正确密码
				}
				if (userInfo != null && cor_pwd.equals(log_pwd))	//存在此用户且密码正确
				{
					User user = new User(socket, userInfo[0], userInfo[2], userInfo[3]);	//创建User对象
					server.log("correct password:"+ cor_pwd + " submitted password:" + log_pwd);
					server.log("id"+user.id+"  " + user.nickname);
					if(server.getUser(user.id) != null)	//判断此用户是否在线
					{
						out.write("<login result=\"repeat\"/>".getBytes());	//重复登陆
						out.flush();
						server.log("REPEAT!");
						socket.close();		//关闭socket
					}
					else
					{
						//发送登陆成功的消息给客户端
						out.write("<login result=\"succeed\"/>".getBytes());
						out.flush();
						server.log("SUCCEED!");
						server.sendonlinelist(user);	//把当前的在线用户列表发给新登陆的用户
						server.addOnlineUser(user);		//登陆成功，将此用户添加到用户列表
						
						//开启监听此客户端消息的线程
						ReceiveMsgThread rmt = new ReceiveMsgThread(server, in, user);
						rmt.start();
					}
				}
				else						//密码或id错误
				{
					out.write("<login result=\"error\"/>".getBytes());
					out.flush();
					server.log("ERROR ID OR PASSWORD!");
					socket.close();	//关闭socket
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
			catch (DocumentException e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
