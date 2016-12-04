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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import momingqi.util.Util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * 聊天窗口
 * @author mingC
 *
 */
@SuppressWarnings("serial")
public class ChatFrame extends JFrame
{
	public MainFrame mf;
	private Friend f;
	public JTextArea msgArea;
	public JTextField inputField;
	public JLabel tipLabel;
	final Font PlainFont = new Font("微软雅黑", Font.PLAIN, 18);
	
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
		msgArea = new JTextArea(25, 25);
		inputField = new JTextField(25);
		tipLabel = new JLabel("  ");
		msgArea.setFont(PlainFont);
		inputField.setFont(PlainFont);
		tipLabel.setFont(PlainFont);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,2));	//按钮面板
		JPanel infoPanel = new JPanel();	//对方的个人信息面板，包括id，nickname和头像
		
		JLabel nicknameLabel = new JLabel(f.nickname);
		JLabel idLabel = new JLabel(f.id);
		JLabel photoLabel = new JLabel(new ImageIcon(Util.ClientImagePath + f.photo));
		JPanel idnamePanel = new JPanel(new GridLayout(2,1));
		idLabel.setFont(PlainFont);
		nicknameLabel.setFont(PlainFont);
		idnamePanel.add(idLabel);
		idnamePanel.add(nicknameLabel);
		
		infoPanel.add(photoLabel);
		infoPanel.add(idnamePanel);
		
		JButton sendButton = new JButton("发送");
		JButton resetButton = new JButton("清屏");
		buttonPanel.add(tipLabel);
		buttonPanel.add(sendButton);
		buttonPanel.add(resetButton);
		
		//当输入框失去焦点时，消除提示信息
		inputField.addFocusListener(new FocusListener()	
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
		
		//按下回车发送消息
		inputField.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				sendChatMsg();
			}
		});
		//点击发送也可以发送消息
		sendButton.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				sendChatMsg();
			}

		});
		
		//清屏按钮
		resetButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				msgArea.setText("");
			}
		});
		
		tipLabel.setForeground(Color.RED);
		msgArea.setEditable(false);
		msgArea.setLineWrap(true);
		JScrollPane msgScrollPane = new JScrollPane(msgArea);
		msgScrollPane.setAutoscrolls(true);
		
		this.setLayout(new FlowLayout());
		this.add(infoPanel);
		this.add(msgScrollPane);
		this.add(inputField);
		this.add(buttonPanel);
		this.setTitle("正在与" + f.nickname + "聊天");
		this.setSize(470, 830);
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
		String msg = inputField.getText();
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
		String time = Util.presentTime();
		//构建xml
		String msg_xml = String.format("<chatmsg receiver=\"%s\" time=\"%s\">%s</chatmsg>", f.id, time, msg);
		//创建发送消息线程
		ClientSendMsgThread smt = new ClientSendMsgThread(this, msg_xml);
		smt.start();
		//清空发送区
		inputField.setText("");	
		//显示到聊天区
		
		// 构建message显示到msgArea文本框中
		String message = String.format("%s  %s(%s)\n%s\n\n", time, mf.getNickName(),
				mf.getID(), msg);// 获取系统当前时间
		msgArea.append(message);
		logMsg(f, time, msg);
	}
	
	/**
	 * 在聊天框中显示msg
	 * @param msg
	 */
	public void setMsgText(String time, String msg)
	{
		//构建message显示到msgArea文本框中
		String message = String.format("%s  %s(%s)\n%s\n\n", time, f.nickname, f.id, msg);//获取系统当前时间
		msgArea.append(message);
		
	}
	
	/**
	 * 显示错误信息
	 * @param string
	 */
	public void showError(String error)
	{
		msgArea.append("\n" + error);
	}

	/**
	 * 记录msg到消息记录文件history.xml中
	 * @param f
	 * @param time
	 * @param msg
	 */
	public static void logMsg(Friend f, String time, String msg)
	{
		String filename = f.id + ".xml";
		File file = new File("./resources/history/" + filename);
		
		try
		{
			Document doc;
			Element root;
			if (!file.exists())
			{
				System.out.println("------not exist!------");
				// 若聊天记录文件不存在则创建一个
				file.createNewFile();
				doc = DocumentHelper.createDocument();
				root = doc.addElement("historys");
			}
			else
			{
				doc = new SAXReader().read(file);
				root = doc.getRootElement();
			}
			Element recordElem = root.addElement("record");
			recordElem.addAttribute("time", time);
			recordElem.addAttribute("nickname", f.nickname);
			recordElem.addAttribute("id", f.id);
			recordElem.setText((msg));
			FileOutputStream out = new FileOutputStream(file);
			OutputFormat format = OutputFormat.createCompactFormat();
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(doc);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 获得服务端的输出流
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return mf.getOutputStream();
	}

	/**
	 * 获得服务端的输入流
	 */
	public InputStream getInputStream() throws IOException
	{
		return mf.getInputStream();
	}
}
