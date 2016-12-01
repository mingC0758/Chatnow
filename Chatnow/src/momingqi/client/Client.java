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

@SuppressWarnings("serial")
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
	public String id;	//此用户id
	
	public Client()
	{
		initComponent();
	}
	
	/**
	 * 界面组件初始化
	 */
	private void initComponent()
	{
		Font font2 = new Font("Dialog.plain", Font.BOLD, 21);					//字体
		Font font1 = new Font("Dialog.plain", Font.PLAIN, 21);					//字体
		
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
		
		JLabel idLabel = new JLabel("  账号");
		JLabel pwdLabel = new JLabel("  密码");
		JLabel ipLabel = new JLabel("ip地址");
		JLabel portLabel = new JLabel("  端口");
		JButton loginButton = new JButton("登录");
		JButton resetButton = new JButton("重置");
		JButton defaultButton = new JButton("默认");
		
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
		northPanel.setBorder(BorderFactory.createTitledBorder("用户登录"));
		southPanel.setBorder(BorderFactory.createTitledBorder("服务器配置"));
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
	 * 获取所需信息，创建发送线程，发送账号密码到服务器检验
	 */
	public void login()
	{
		id = idTextField.getText();
		ClientConnectionThread cct = new ClientConnectionThread(this);
		cct.start();
	}

	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		Client client = new Client();
	}

	public void showError(String error)
	{
		this.tipLabel.setText(error);
	}
}
