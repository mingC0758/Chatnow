package momingqi.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import momingqi.util.AboutPanel;
import momingqi.util.Util;

/**
 * 服务器界面，有启动和运行记录功能，维护在线用户列表
 * @author mingC
 *
 */
@SuppressWarnings("serial")
public class Server extends JFrame
{
	public JLabel statusLabel;			//服务器状态标签
	public JTextField portTextField;	//端口输入框
	public JTextArea logTextArea;	//记录输出框
	public JButton startButton;		//启动按钮
	public ServerSocket serverSocket;	//服务器serversocket
	public LinkedList<User> onlineList;				//在线用户列表
	
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
		Font font2 = new Font("Dialog.plain", Font.PLAIN, 18);	
		Font font3 = new Font("Dialog.plain", Font.PLAIN, 15);	
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
		JLabel label1 = new JLabel("  服务器状态:");//  服务器状态:
		JLabel label2 = new JLabel("         端口号:");//
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
		
		logTextArea = new JTextArea(20, 33);
		logTextArea.setFont(font3);
		logTextArea.setEditable(false);
		logTextArea.setLineWrap(true); 	//自动换行
		logTextArea.setWrapStyleWord(true);
		JScrollPane logscrollpane = new JScrollPane(logTextArea);	//服务器运行记录输出面板
		logscrollpane.setAutoscrolls(true);
		JPanel logPanel = new JPanel();
		logPanel.add(logscrollpane);
		
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
		this.add(logPanel, BorderLayout.CENTER);
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
			for (User user : onlineList)
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
	 * 添加用户user到在线用户列表中，并发送新增用户消息给“其他”在线用户
	 * @throws IOException 
	 */
	public synchronized void addOnlineUser(User user)
	{
		this.onlineList.add(user);
		String xml = String.format("<addonlineuser id=\"%s\"/>", user.id);
		
		for (User u : onlineList)	//发送user用户给u
		{
			try
			{
				if (u.id.equals(user.id))
					continue;
				u.socket.getOutputStream().write(xml.getBytes()); // 发送列表
				log("send to all:" + xml);
			}
			catch (IOException e1)
			{
				// 出现流错误，将此用户移出在线用户列表
				e1.printStackTrace();
				removeUser(u);
				return;
			}
		}
		String str = new String("");
		for(User u: onlineList)
		{
			str += (u.id + "  ");
		}
		log("onlinelist: " + str);
	}
	
	/**
	 * 根据id获得在线用户的User对象，若该用户不在线则返回null
	 * @param id
	 * @return 找到则返回User对象，否则返回null
	 */
	public User getUser(String id)
	{
		for (User u : onlineList)
		{
			if(u.id.equals(id))
			{
				return u;
			}
		}
		return null;
	}
	
	/**
	 * 从用户列表中删除用户，并发送消息通知其他在线用户
	 * @throws IOException 
	 */
	public synchronized void removeUser(User user)
	{
		this.onlineList.remove(user);
		String xml = String.format("<removeonlineuser id=\"%s\"/>", user.id);
		for (User u: onlineList)	//向所有用户发送移除在线用户消息
		{
			if(u == user) continue;	//不发给退出的用户
			try
			{
				u.socket.getOutputStream().write(xml.getBytes());
			}
			catch (IOException e)
			{
				e.printStackTrace();	//此异常可忽略
			}
		}
		log("client id:" + user.id + "exit");
		log("send to all：" + xml);
	}
	
	/**
	 * 发送当前在线用户列表给user
	 * @throws IOException 
	 */
	public synchronized void sendonlinelist(User user) throws IOException
	{
		/**
		 * 构造用户列表xml
		 */
		StringBuffer xml = new StringBuffer("<onlinelist>");
		for(User u: onlineList)
		{
			xml.append(String.format("<user id=\"%s\"/>", u.id));
		}
		xml.append("</onlinelist>");
		String str_xml = new String(xml);
		/**
		 * 发送用户列表
		 */
		user.getOutputStream().write(str_xml.getBytes());
		log("send to " + user.id + ": " + xml);
	}
	
	/**
	 * 记录服务器的运行消息，显示在logTextArea中
	 */
	public void log(String text)
	{
		this.logTextArea.append(text + "\n");
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
	
	public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		@SuppressWarnings("unused")
		Server server = new Server();
		
	}
}
