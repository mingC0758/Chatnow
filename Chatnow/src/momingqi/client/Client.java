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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import momingqi.util.AboutPanel;

/**
 * 客户端登陆界面
 * @author mingC
 *
 */
@SuppressWarnings("serial")
public class Client extends JFrame implements ActionListener
{
	public JTextField idTextField;
	public JTextField pwdTextField;
	public JTextField ipTextField;
	public JTextField portTextField;
	JButton loginButton = new JButton("登录");
	JButton resetButton = new JButton("重置");
	JButton defaultButton = new JButton("默认");
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
		Font font2 = new Font("微软雅黑", Font.BOLD, 18);					//字体
		Font font1 = new Font("微软雅黑", Font.PLAIN, 18);					//字体
		
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
		loginButton = new JButton("登录");
		resetButton = new JButton("重置");
		defaultButton = new JButton("默认");
		
		loginButton.addActionListener(this);
		resetButton.addActionListener(this);
		defaultButton.addActionListener(this);
		
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

	public void showError(String error)
	{
		this.tipLabel.setText(error);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if(source == loginButton)
		{
			login();
		}
		else if(source == resetButton)
		{
			idTextField.setText("");
			pwdTextField.setText("");
		}
		else if(source == defaultButton)
		{
			ipTextField.setText("127.0.0.1");
			portTextField.setText("10010");
		}
	}
	
	/**
	 * 主函数，设置UI视感为windows视感
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws UnsupportedLookAndFeelException
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		Client client = new Client();
	}
}
