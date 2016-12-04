package momingqi.client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import momingqi.util.Util;
import momingqi.util.XMLUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 * 用户主界面，负责：维护所有好友列表，维护好友面板，维护聊天窗口，获得向服务器的输出流，显示错误信息
 * @author mingC
 *
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame
{
	private Socket socket;								//与服务器通信的socket对象
	private String id;									//本客户端信息
	private String nickname;
	private String photo;
	private Map<String, Friend> friendMap;				//好友id，Friend对象键值对
	private Map<String, FriendPanel> friendPanelMap;	//好友id，面板键值对
	private Map<String, ChatFrame> chatFrameMap;		//好友id，聊天窗口键值对
	JPanel listPanel;	//好友列表面板
	
	final Color PanelHighlight = new Color(184,247,136);
	final Color PanelBackground = null;
	final Color PanelClick = new Color(232,237,81);
	final Font BoldFont = new Font("微软雅黑", Font.BOLD, 18);
	final Font PlainFont = new Font("微软雅黑", Font.PLAIN, 15);
	
	public MainFrame(String id, Socket socket)
	{
		this.id = id;
		this.socket = socket;
		applyConfig();
		initComponent();
	}

	/**
	 * 配置
	 */
	private void applyConfig()
	{
		chatFrameMap = new HashMap<String, ChatFrame>();
		
		File friendsxml = new File("./resources/friends.xml");
		try
		{
			friendMap = XMLUtil.parseFriends(friendsxml);
			Friend me = friendMap.get(id);	//获取自己的个人信息
			nickname = me.nickname;
			photo = me.photo;
			friendMap.remove(id);	//把自己移出好友列表
			System.out.println(nickname + "  " + photo + "  ");
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

	private void initComponent()
	{
		Icon icon = new ImageIcon("resources/ImageResources/" + photo);
		
		JPanel userPanel = new JPanel();
		userPanel.setBorder(BorderFactory.createTitledBorder("个人信息"));

		JLabel label;
		JPanel rightPanel = new JPanel(new GridLayout(2,1));
		
		label = new JLabel(nickname);
		label.setFont(BoldFont);
		rightPanel.add(label);
		
		label = new JLabel(id);
		label.setFont(PlainFont);
		rightPanel.add(label);
		userPanel.add(new JLabel(icon));	
		userPanel.add(rightPanel);
		
		listPanel = new JPanel(new GridLayout(friendMap.size(), 1, 3, 3));
		listPanel.setBorder(BorderFactory.createTitledBorder("好友列表"));	
		friendPanelMap = new HashMap<String, FriendPanel>(Util.MAXUSERNUM);
		
		for(Friend f: friendMap.values())
		{
			FriendPanel newPanel = new FriendPanel(this, f);
			friendPanelMap.put(f.id, newPanel);
			listPanel.add(newPanel);
		}
		JButton addButton = new JButton("添加好友");	//添加好友按钮
		addButton.addActionListener(new AddFriendHandler(this));
		
		rightPanel.setBackground(PanelBackground);
		userPanel.setBackground(PanelBackground);
		listPanel.setBackground(PanelBackground);
		
		this.getContentPane().setBackground(PanelBackground);
		this.setLayout(new FlowLayout());
		this.add(userPanel);
		this.add(listPanel);
		this.add(addButton);
		this.setVisible(true);
		this.pack();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				windowClosingPerformed();
			}
		});
	}

	/**
	 * 当窗口关闭时调用此方法
	 */
	public void windowClosingPerformed()
	{
		int result = JOptionPane.showConfirmDialog(this, "是否退出客户端？");
		if(result == JOptionPane.YES_OPTION)
		{
			//向服务端发送退出消息
			String xml = String.format("<close id=\"%s\"/>", this.id);
			OutputStream out;
			try
			{
				out = this.getOutputStream();
				out.write(xml.getBytes());
				out.flush();
			}
			catch (IOException e)
			{
			}
			System.exit(0);
		}
		else
		{
			return;
		}
	}
	
	/**
	 * 弹出提示框显示错误信息
	 * @param error
	 */
	public void showError(String error)
	{
		JOptionPane.showMessageDialog(this, error);
	}


	/**
	 * 创建好友f的聊天窗口，并添加到聊天窗口列表中
	 * @param f
	 * @return
	 */
	public ChatFrame createChatFrame(String id)
	{
		Friend f = this.getFriend(id);
		ChatFrame cf = new ChatFrame(this, f);
		this.chatFrameMap.put(f.id, cf);
		return cf;
	}
	

	public void removeChatFrame(String id)
	{
		chatFrameMap.remove(id);
	}
	
	/**
	 * 获得id对应好友的聊天窗口
	 * @param id
	 * @return
	 */
	public ChatFrame getChatFrame(String id)
	{
		return this.chatFrameMap.get(id);
	}

	/**
	 * 移除在线好友
	 * @param id
	 */
	public void removeOnlineUser(String id)
	{
		FriendPanel fp = friendPanelMap.get(id);
		if (fp == null) return;
		if (fp.id.equals(id))
		{
			fp.statusLabel.setText("离线");
		}

		Friend offlineFriend = friendMap.get(id);
		if(offlineFriend != null)
		{
			offlineFriend.online = false;
		}
	}
	
	/**
	 * 增加在线好友
	 * @param id
	 */
	public void addOnlineUser(String id)
	{
		FriendPanel fp = friendPanelMap.get(id);
		if (fp != null)
		{
			fp.statusLabel.setText("在线");
		}

		Friend offlineFriend = friendMap.get(id);
		if(offlineFriend != null)
		{
			offlineFriend.online = true;
		}
	}
	
	/**
	 * 使用ids数组初始化在线好友列表
	 * @param ids
	 */
	public void initOnlineUser(String[] ids)
	{
		for(int i = 0; i < ids.length && ids[i] != null; i++)
		{
			FriendPanel fp = friendPanelMap.get(ids[i]);
			if(fp == null) continue;
			fp.statusLabel.setText("在线");
			friendMap.get(ids[i]).online = true;
			break;
		}
		
	}

	/**
	 * 根据id获得特定好友的Friend对象
	 * @param id
	 * @return
	 */
	public Friend getFriend(String id)
	{
		return friendMap.get(id);
	}
	
	
	/**
	 * 获得id对应的好友的FriendPanel面板对象
	 * @param id
	 * @return 
	 */
	public FriendPanel getFriendPanel(String id)
	{
		return friendPanelMap.get(id);
	}

	public String getID()
	{
		return this.id;
	}
	
	public String getNickName()
	{
		return this.nickname;
	}
	

	/**
	 * 删除好友：从面版中删除，从在线列表中删除，从文件中删除
	 */
	public void deleteFriend(Friend f)
	{
		listPanel.remove(friendPanelMap.get(f.id));
		this.pack();
		this.validate();
		friendPanelMap.remove(f.id);
		friendMap.remove(f.id);
		removeOnlineUser(f.id);
		
		try
		{
			File file = new File("./resources/friends.xml");
			Document doc = new SAXReader().read(file);
			Element elem = (Element)doc.selectSingleNode("/friends/user[@id='" + f.id + "']");
			doc.getRootElement().remove(elem);
			OutputFormat format = OutputFormat.createPrettyPrint();
			FileOutputStream out = new FileOutputStream(file);
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
	 * 增加好友:添加到好友面板，添加到在线好友列表，添加到friends.xml文件中
	 */
	public void addNewFriend(Friend f)
	{
		FriendPanel newPanel = new FriendPanel(this, f);
		friendPanelMap.put(f.id, newPanel);
		this.listPanel.add(newPanel);
		this.pack();
		this.validate();
		addOnlineUser(f.id);
		
		try
		{
			File file = new File("./resources/friends.xml");
			Document doc = new SAXReader().read(file);
			Element elem = doc.getRootElement().addElement("user");
			elem.addAttribute("id", id);
			elem.addAttribute("nickname", f.nickname);
			elem.addAttribute("photo", f.photo);
			OutputFormat format = OutputFormat.createPrettyPrint();
			FileOutputStream out = new FileOutputStream(file);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获得服务端的输入流
	 */
	public InputStream getInputStream() throws IOException
	{
		return this.socket.getInputStream();
	}
	
	/**
	 * 获得对服务端的输出流
	 * @return
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}

}



