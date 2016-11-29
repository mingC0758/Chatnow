package momingqi.client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import momingqi.util.AboutPanel;

public class Client extends JFrame
{
	public JTextField idTextField;
	public JTextField pwdTextField;
	public JTextField ipTextField;
	public JTextField portTextField;
	public JLabel tipLabel;
	public Socket socket;
	public InputStream in;
	public OutputStream out;
	
	public Client()
	{
		initComponent();
	}
	
	/**
	 * ���������ʼ��
	 */
	private void initComponent()
	{
		Font font2 = new Font("Dialog.plain", Font.BOLD, 21);					//����
		Font font1 = new Font("Dialog.plain", Font.PLAIN, 21);					//����
		
		idTextField = new JTextField(12);
		pwdTextField = new JTextField(12);
		ipTextField = new JTextField(12);
		portTextField = new JTextField(12);
		tipLabel = new JLabel("");
		
		tipLabel.setForeground(Color.red);
		idTextField.setFont(font1);
		pwdTextField.setFont(font1);
		ipTextField.setFont(font1);
		portTextField.setFont(font1);
		
		JLabel idLabel = new JLabel("  �˺�");
		JLabel pwdLabel = new JLabel("  ����");
		JLabel ipLabel = new JLabel("ip��ַ");
		JLabel portLabel = new JLabel("  �˿�");
		JButton loginButton = new JButton("��¼");
		JButton resetButton = new JButton("����");
		JButton defaultButton = new JButton("Ĭ��");
		
		loginButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				login();
			}

		});
		
		idLabel.setForeground(Color.blue);
		idLabel.setFont(font2);
		pwdLabel.setForeground(Color.blue);
		pwdLabel.setFont(font2);
		ipLabel.setFont(font2);
		portLabel.setFont(font2);
		
		JPanel northPanel = new JPanel(new FlowLayout());
		JPanel southPanel = new JPanel(new FlowLayout());
		northPanel.setBorder(BorderFactory.createTitledBorder("�û���¼"));
		southPanel.setBorder(BorderFactory.createTitledBorder("����������"));
		northPanel.add(idLabel);
		northPanel.add(idTextField);
		northPanel.add(pwdLabel);
		northPanel.add(pwdTextField);
		northPanel.add(loginButton);
		northPanel.add(resetButton);
		northPanel.add(tipLabel);
		southPanel.add(ipLabel);
		southPanel.add(ipTextField);
		southPanel.add(portLabel);
		southPanel.add(portTextField);
		southPanel.add(defaultButton);
		
		idTextField.setText("10001");
		pwdTextField.setText("12345");
		ipTextField.setText("127.0.0.1");
		portTextField.setText("10010");
		
		this.setLayout(new GridLayout(3, 1));
		this.getContentPane().add(northPanel);
		this.add(southPanel);
		this.add(new AboutPanel());
		this.setTitle("Chatme");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(320, 510);
		this.setAlwaysOnTop(true);
		this.setResizable(false);
		this.setVisible(true);
	}

	/**
	 * ��ȡ������Ϣ�����������̣߳������˺����뵽����������
	 */
	public void login()
	{
		ClientConnectionThread cct = new ClientConnectionThread(this);
		cct.start();
	}

	public static void main(String[] args)
	{
		Client client = new Client();
	}
}
