package momingqi.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import momingqi.util.AboutPanel;
import momingqi.util.Util;

public class Server extends JFrame
{
	public JLabel statusLabel;			//服务器状态标签
	public JTextField portTextField;	//端口输入框
	public JButton startButton;		//启动按钮
	public ServerSocket serverSocket;	//服务器serversocket
	public LinkedList<User> userList;				//在线用户列表
	
	public Server()
	{
		this.initComponent();
	}
	
	/**
	 * 初始化界面组件
	 */
	private void initComponent()
	{
		/**
		 * 实例化
		 */
		Font font = new Font("Dialog.plain", Font.BOLD, 18);					//字体
		Font font2 = new Font("Dialog.plain", Font.PLAIN, 18);					//字体
		JPanel mainPanel = new JPanel(new GridLayout(3, 2, 0, 10));	//主面板
		JPanel aboutPanel = new AboutPanel();					//作者信息面板
		this.statusLabel = new JLabel("未启动");
		this.statusLabel.setForeground(Color.RED);
		this.portTextField = new JTextField("10010");
		this.startButton = new JButton("启动");
		this.startButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				start();
			}

		});
		Label label1 = new Label("  服务器状态:");
		Label label2 = new Label("         端口号:");
		/**
		 * 设置字体
		 */
		this.statusLabel.setFont(font);
		this.portTextField.setFont(font2);
		this.startButton.setFont(font);
		label1.setFont(font);
		label2.setFont(font);
		mainPanel.add(label1);
		mainPanel.add(this.statusLabel);
		mainPanel.add(label2);
		mainPanel.add(this.portTextField);
		mainPanel.add(this.startButton);
		
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				windowClosingPerformed();
			}
		});
		this.setTitle("Chatnow——服务器");
		this.add(mainPanel, BorderLayout.NORTH);
		this.add(aboutPanel, BorderLayout.SOUTH);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	/**
	 * 启动服务器
	 */
	private void start()
	{
		int port = getPort();
		if(port == -1) 
		{
			JOptionPane.showMessageDialog(this, "端口号必须在1024与65535之间！", "警告", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		ServerConnectThread sct = new ServerConnectThread(this, port);
		sct.start();
		
	}
	
	/**
	 * 关闭服务器时
	 */
	private void windowClosingPerformed()
	{
		int option = JOptionPane.showConfirmDialog(this, "关闭服务器将使所有已连接的客户端下线，是否继续？", "警告", 
				JOptionPane.OK_CANCEL_OPTION);
		
		if(option == JOptionPane.OK_OPTION)
		{
			System.exit(0);
			closeAllSocket();
		}
		else return;
	}
	
	/**
	 * 关闭资源
	 */
	private void closeAllSocket()
	{
		try
		{
			for (User user : userList)
			{
				user.socket.close();
			}
			serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 添加用户到用户列表
	 * @throws IOException 
	 */
	public void addUser(User user)
	{
		this.userList.add(user);
		sendOnlineUser(user);
	}
	
	/**
	 * 从用户列表中删除用户
	 * @throws IOException 
	 */
	public synchronized void removeUser(User user)
	{
		this.userList.remove(user);
		removeOnlineUser(user);
		System.out.println("有用户退出 id：" + user.id);
	}
	
	/**
	 * 向在线用户发送新增的在线用户id
	 * @throws IOException 
	 */
	public synchronized void sendOnlineUser(User user)
	{
		String xml = String.format("<addOnlineUser id=\"%s\"/>", user.id);
		System.out.println("服务端发送消息：" + xml);
		
		for (User u : userList)
			{
				try
				{
					if(u.id.equals(user.id)) continue;
					u.socket.getOutputStream().write(xml.getBytes()); // 发送列表
				}
				catch (IOException e1)
				{
					// 出现流错误，将此用户移出在线用户列表
					e1.printStackTrace();
					removeUser(u);
					return;
				}
			}
	}
	
	public synchronized void removeOnlineUser(User user)
	{
		String xml = String.format("<removeOnlineUser id=\"%s\"/>", user.id);
		for (User u: userList)	//向所有用户发送移除在线用户消息
		{
			try
			{
				u.socket.getOutputStream().write(xml.getBytes());
			}
			catch (IOException e)
			{
				removeUser(u);
				e.printStackTrace();
			}
		}
		System.out.println("服务端发送消息：" + xml);
	}
	
	/**
	 * 获取端口号，并校验
	 * @return 端口错误时返回-1
	 */
	private int getPort()
	{
		String str_port = this.portTextField.getText();
		if(Util.isCorrectPort(str_port))	//判断端口是否合法
		{
			int int_port = Integer.parseInt(str_port);
			return int_port;
		}
		else return -1;
	}
	
	public static void main(String[] args)
	{
		Server server = new Server();
	}

	/**
	 * 发送所有用户id给user
	 * @throws IOException 
	 */
	public synchronized void sendUserlist(Socket socket) throws IOException
	{
		/**
		 * 构造用户列表xml
		 */
		StringBuffer xml = new StringBuffer("<userlist>");
		for(User user: userList)
		{
			xml.append(String.format("<user id=\"%s\"/>", user.id));
		}
		xml.append("</userlist>");
		String str_xml = new String(xml);
		/**
		 * 发送用户列表
		 */
		socket.getOutputStream().write(str_xml.getBytes());
	}
}
