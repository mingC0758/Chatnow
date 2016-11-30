package momingqi.client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import momingqi.util.Util;

public class ChatFrame extends JFrame
{
	public MainFrame mf;
	private Friend f;
	public JTextArea msgArea;
	public JTextArea inputArea;
	public JLabel tipLabel;
	final Font PlainFont = new Font("Dialog.plain", Font.PLAIN, 15);
	
	public ChatFrame(MainFrame mf, Friend f)
	{
		this.mf = mf;
		this.f= f;
		initComponent();
	}

	/**
	 * 初始化组件
	 */
	private void initComponent()
	{
		msgArea = new JTextArea(20, 10);
		inputArea = new JTextArea(20, 10);
		tipLabel = new JLabel("  ");
		msgArea.setFont(PlainFont);
		inputArea.setFont(PlainFont);
		tipLabel.setFont(PlainFont);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,2));	//按钮面板
		JPanel infoPanel = new JPanel();	//对方的个人信息面板，包括id，nickname和头像
		
		JLabel nicknameLabel = new JLabel(f.nickname);
		JLabel idLabel = new JLabel(f.id);
		JLabel photoLabel = new JLabel(new ImageIcon(Util.ClientImagePath + f.photo));
		JPanel idnamePanel = new JPanel(new GridLayout(2,1));
		idnamePanel.add(idLabel);
		idnamePanel.add(nicknameLabel);
		
		infoPanel.add(photoLabel);
		infoPanel.add(idnamePanel);
		
		JButton sendButton = new JButton("发送");
		JButton resetButton = new JButton("重置");
		buttonPanel.add(tipLabel);
		buttonPanel.add(sendButton);
		buttonPanel.add(resetButton);
		
		//当输入框失去焦点时，消除提示信息
		inputArea.addFocusListener(new FocusListener()	
		{
			
			@Override
			public void focusLost(FocusEvent e)
			{
				tipLabel.setText("  ");
			}
			
			@Override
			public void focusGained(FocusEvent e)
			{
			}
		});
		
		sendButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				sendChatMsg();
			}

		});
		
		tipLabel.setForeground(Color.RED);
		
		this.setLayout(new FlowLayout());
		this.add(infoPanel);
		this.add(msgArea);
		this.add(inputArea);
		this.add(buttonPanel);
		this.setTitle("正在与" + f.nickname + "聊天");
		this.pack();
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
				mf.removeChatFrame(f.id); 	//移除此窗口
				System.gc();				//GC回收
			}
		});
	}
	
	/**
	 * 点击发送按钮时
	 */
	private void sendChatMsg()
	{
		String msg = inputArea.getText();
		if(msg.equals(""))	//输出框为空
		{
			tipLabel.setText("请输入内容！");
			return;
		}
		if(f.online == false)	//对方不在线
		{
			showError("对方处于离线状态，无法发送信息。");
			return;
		}
		//构建xml
		String msg_xml = String.format("<chatmsg receiver=\"%s\">%s</chatmsg>", f.id, msg);
		//创建发送消息线程
		SendMsgThread smt = new SendMsgThread(this, msg_xml);
		smt.start();
	}
	
	/**
	 * 在聊天框中显示msg
	 * @param msg
	 */
	public void setMsgText(String msg)
	{
		//获取系统时间time
		Date now = new Date();
		DateFormat df = DateFormat.getTimeInstance();
		String time = df.format(now);
		//构建message显示到msgArea文本框中
		String message = String.format("%s  %s(%s)\n%s\n\n", time, f.nickname, f.id, msg);//获取系统当前时间
		msgArea.append(message);
		
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		return mf.getOutputStream();
	}

	/**
	 * 显示错误信息
	 * @param string
	 */
	public void showError(String error)
	{
		msgArea.append("\n" + error);
	}

	public void logMsg(String msg)
	{
		
	}
}
