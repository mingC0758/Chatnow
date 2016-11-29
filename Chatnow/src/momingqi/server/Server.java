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
	public JLabel statusLabel;			//������״̬��ǩ
	public JTextField portTextField;	//�˿������
	public JButton startButton;		//������ť
	public ServerSocket serverSocket;	//������serversocket
	public LinkedList<User> userList;				//�����û��б�
	
	public Server()
	{
		this.initComponent();
	}
	
	/**
	 * ��ʼ���������
	 */
	private void initComponent()
	{
		/**
		 * ʵ����
		 */
		Font font = new Font("Dialog.plain", Font.BOLD, 18);					//����
		Font font2 = new Font("Dialog.plain", Font.PLAIN, 18);					//����
		JPanel mainPanel = new JPanel(new GridLayout(3, 2, 0, 10));	//�����
		JPanel aboutPanel = new AboutPanel();					//������Ϣ���
		this.statusLabel = new JLabel("δ����");
		this.statusLabel.setForeground(Color.RED);
		this.portTextField = new JTextField("10010");
		this.startButton = new JButton("����");
		this.startButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				start();
			}

		});
		Label label1 = new Label("  ������״̬:");
		Label label2 = new Label("         �˿ں�:");
		/**
		 * ��������
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
		this.setTitle("Chatnow����������");
		this.add(mainPanel, BorderLayout.NORTH);
		this.add(aboutPanel, BorderLayout.SOUTH);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	/**
	 * ����������
	 */
	private void start()
	{
		int port = getPort();
		if(port == -1) 
		{
			JOptionPane.showMessageDialog(this, "�˿ںű�����1024��65535֮�䣡", "����", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		ServerConnectThread sct = new ServerConnectThread(this, port);
		sct.start();
		
	}
	
	/**
	 * �رշ�����ʱ
	 */
	private void windowClosingPerformed()
	{
		int option = JOptionPane.showConfirmDialog(this, "�رշ�������ʹ���������ӵĿͻ������ߣ��Ƿ������", "����", 
				JOptionPane.OK_CANCEL_OPTION);
		
		if(option == JOptionPane.OK_OPTION)
		{
			System.exit(0);
			closeAllSocket();
		}
		else return;
	}
	
	/**
	 * �ر���Դ
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
	 * ����û����û��б�
	 * @throws IOException 
	 */
	public void addUser(User user)
	{
		this.userList.add(user);
		sendOnlineUser(user);
	}
	
	/**
	 * ���û��б���ɾ���û�
	 * @throws IOException 
	 */
	public synchronized void removeUser(User user)
	{
		this.userList.remove(user);
		removeOnlineUser(user);
		System.out.println("���û��˳� id��" + user.id);
	}
	
	/**
	 * �������û����������������û�id
	 * @throws IOException 
	 */
	public synchronized void sendOnlineUser(User user)
	{
		String xml = String.format("<addOnlineUser id=\"%s\"/>", user.id);
		System.out.println("����˷�����Ϣ��" + xml);
		
		for (User u : userList)
			{
				try
				{
					if(u.id.equals(user.id)) continue;
					u.socket.getOutputStream().write(xml.getBytes()); // �����б�
				}
				catch (IOException e1)
				{
					// ���������󣬽����û��Ƴ������û��б�
					e1.printStackTrace();
					removeUser(u);
					return;
				}
			}
	}
	
	public synchronized void removeOnlineUser(User user)
	{
		String xml = String.format("<removeOnlineUser id=\"%s\"/>", user.id);
		for (User u: userList)	//�������û������Ƴ������û���Ϣ
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
		System.out.println("����˷�����Ϣ��" + xml);
	}
	
	/**
	 * ��ȡ�˿ںţ���У��
	 * @return �˿ڴ���ʱ����-1
	 */
	private int getPort()
	{
		String str_port = this.portTextField.getText();
		if(Util.isCorrectPort(str_port))	//�ж϶˿��Ƿ�Ϸ�
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
	 * ���������û�id��user
	 * @throws IOException 
	 */
	public synchronized void sendUserlist(Socket socket) throws IOException
	{
		/**
		 * �����û��б�xml
		 */
		StringBuffer xml = new StringBuffer("<userlist>");
		for(User user: userList)
		{
			xml.append(String.format("<user id=\"%s\"/>", user.id));
		}
		xml.append("</userlist>");
		String str_xml = new String(xml);
		/**
		 * �����û��б�
		 */
		socket.getOutputStream().write(str_xml.getBytes());
	}
}
