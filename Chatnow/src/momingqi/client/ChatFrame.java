package momingqi.client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ChatFrame extends JFrame
{
	public MainFrame mf;
	private Friend f;
	public JTextArea msgArea;
	public JTextArea inputArea;
	public JLabel tipLabel;
	
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
		
		JPanel infoPanel = mf.getFriendPanel(f.id);
		JPanel buttonPanel = new JPanel(new GridLayout(1,2));
		
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
				sendMsg();
			}

		});
		
		tipLabel.setForeground(Color.RED);
		
		this.setLayout(new FlowLayout());
		this.add(infoPanel);
		this.add(msgArea);
		this.add(inputArea);
		this.add(buttonPanel);
		this.setTitle("正在与" + f.nickname + "聊天");
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
	private void sendMsg()
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
		String msg_xml = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg sender=\"%s\" receiver=\"%s\">%s</msg>", mf.getID(), f.id, msg);
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
		//获取系统当前时间
		msgArea.append("\n" + msg);
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
