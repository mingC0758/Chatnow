package momingqi.client;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import momingqi.util.Util;
import momingqi.util.XMLUtil;

import org.xml.sax.SAXException;

/**
 * 用户主界面，显示好友列表和个人信息，查看聊天记录，调出聊天框
 * @author mingC
 *
 */
public class MainFrame extends JFrame
{
	private Socket socket;
	private String id;
	private String nickname;
	private String photo;
	private Map<String, Friend> friendMap;	//好友列表
	private FriendPanel[] friendPanel;				//好友信息面板数组
//	private LinkedList<String> onlineFriendList;	//在线用户（id）
	private Map<String, ChatFrame> chatFrameMap;	//聊天框列表
	
	public MainFrame(String id, Socket socket)
	{
		this.id = id;
		this.socket = socket;
		applyConfig();
		initComponent();
	}


	private void applyConfig()
	{
		chatFrameMap = new HashMap<String, ChatFrame>();
		
		File friendsxml = new File("./resources/friends.xml");
		try
		{
			friendMap = XMLUtil.parseFriends(friendsxml);
			Friend me = friendMap.get(id);	//通过id获取本客户端用户的个人信息
			nickname = me.nickname;
			photo = me.photo;
			friendMap.remove(id);	//将自己移出好友列表
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
		
		new Thread()
		{
			/**
			 * 每10秒输出一次好友状态，用于调试。
			 */
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(10000);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for (Friend f : friendMap.values())
					{
						System.out.println("id:" + f.id + " 状态:" + f.online);
					}
				}
			}
		}.start();
	}

	private void initComponent()
	{
		final Color PanelHighlight = Color.lightGray;
		final Color PanelBackground = Color.GRAY;
		final Color PanelClick = Color.yellow;
		final Font BoldFont = new Font("Dialog.plain", Font.BOLD, 15);
		final Font PlainFont = new Font("Dialog.plain", Font.PLAIN, 15);
		
		Icon icon = new ImageIcon("resources/ImageResources/" + photo);
		System.out.println(icon);
		
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
		userPanel.add(new JLabel(icon));	//头像标签
		userPanel.add(rightPanel);
		
		JPanel listPanel = new JPanel(new GridLayout(friendMap.size(), 1, 3, 3));//用户列表面板
		listPanel.setBorder(BorderFactory.createTitledBorder("好友列表"));	
		friendPanel = new FriendPanel[Util.MAXUSERNUM];
		int i = 0;
		JPanel infoPanel;
		JLabel onlineLabel;
		/***
		 * 为每一个好友创建一个面板来装载：头像Label，infoLabel（nickname,status, id）
		 */
		for(Friend f: friendMap.values())
		{
			friendPanel[i] = new FriendPanel();
			friendPanel[i].id = f.id;	//把好友id封装进去
			friendPanel[i].setBorder(BorderFactory.createLineBorder(Color.BLUE));
			infoPanel = new JPanel((new GridLayout(2,1)));
			//昵称
			label = new JLabel(f.nickname);
			label.setFont(BoldFont);
			infoPanel.add(label);
			//在线状态
			onlineLabel = new JLabel("  离线");
			onlineLabel.setFont(PlainFont);
			infoPanel.add(onlineLabel);
			friendPanel[i].statusLabel = onlineLabel;
			//账号
			label = new JLabel(f.id);
			label.setFont(PlainFont);
			infoPanel.add(label);
			//把头像和infoPanel装起来
			friendPanel[i].add(new JLabel(
					new ImageIcon("resources/ImageResources/" + f.photo)));
			friendPanel[i].add(infoPanel);
			friendPanel[i].addMouseListener(new MouseAdapter()	//监听面板
			{
				@Override
				public void mouseEntered(MouseEvent e)	//改为明亮色
				{
					e.getComponent().setBackground(PanelHighlight);
				}
				
				@Override
				public void mouseExited(MouseEvent e)
				{
					e.getComponent().setBackground(PanelBackground);	//改为默认背景色
				}
				
				@Override
				public void mouseClicked(MouseEvent e)
				{
					JPanel jp = (JPanel)((JPanel)e.getComponent()).getComponent(1); //获取装载对应id的panel
					e.getComponent().setBackground(PanelClick);	//改为单击时的强调色
					if(e.getClickCount() == 2)	//双击打开聊天框
					{
						System.out.println("双击！");
						String id = ((JLabel)jp.getComponent(2)).getText();	//获取id
						System.out.println("id:" + id);
						createChatFrame(friendMap.get(id));
					}
				}
			});
			
			listPanel.add(friendPanel[i]);
			i++;
		}
		
		this.setLayout(new FlowLayout());
		this.add(userPanel);
		this.add(listPanel);
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
	 * 当用户关闭主界面时提示是否退出客户端
	 */
	public void windowClosingPerformed()
	{
		int result = JOptionPane.showConfirmDialog(this, "确定退出客户端？");
		if(result == JOptionPane.YES_OPTION)
		{
			//给服务器发送退出消息
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
				showError("与服务器断开连接！");
			}
			System.exit(0);
		}
		else
		{
			return;
		}
	}
	
	private void showError(String error)
	{
		JOptionPane.showMessageDialog(this, error);
	}


	public ChatFrame createChatFrame(Friend f)
	{
		ChatFrame cf = new ChatFrame(this, f);
		this.chatFrameMap.put(id, cf);
		
		return cf;
	}

	public void removeChatFrame(String id)
	{
		chatFrameMap.remove(id);
	}
	
	/**
	 * 获取指定id用户的聊天框，若此ChatFrame不存在，则返回null
	 * @param id
	 * @return
	 */
	public ChatFrame getChatFrame(String id)
	{
		return this.chatFrameMap.get(id);
	}

	/**
	 * 从在线用户列表中删除特定id的用户，把该用户的online状态设为false并更新界面
	 * @param id
	 */
	public void removeOnlineUser(String id)
	{
		//onlineFriendList.remove(id);
		for(int i = 0; i < Util.MAXUSERNUM; i++)
		{
			if(friendPanel[i] == null) break;
			if(friendPanel[i].id.equals(id))
			{
				System.out.println("id:" + id + "好友离线");
				friendPanel[i].statusLabel.setText("离线");
				break;
			}
		}
		
		friendMap.get(id).online = false;
	}
	
	/**
	 * 新增在线用户到用户列表中，把该用户的online状态设为true,并更新界面
	 * @param id
	 */
	public void addOnlineUser(String id)
	{
		//onlineFriendList.add(id);
		for(int i = 0; i < friendPanel.length; i++)
		{
			if(friendPanel[i] == null) break;
			if(id.equals(friendPanel[i].id))
			{
				friendPanel[i].statusLabel.setText("在线");
			}
		}
		
		friendMap.get(id).online = true;
	}
	
	/**
	 * 初始化在线用户列表，并把对应的在线用户的online状态设为true
	 * @param ids
	 */
	public void initOnlineUser(String[] ids)
	{
		for(int i = 0; i < ids.length; i++)
		{
			if(ids[i] == null) break;
			for (int j = 0; j < friendPanel.length; j++)
			{
				if(friendPanel[j] == null) break;
				if (friendPanel[j].id.equals(ids[i]))
				{
					friendPanel[j].statusLabel.setText("在线");
					break;
				}
			}
		}
		
		for(int k = 0; k < ids.length; k++)
		{
			if(ids[k] == null) break;
			friendMap.get(ids[k]).online = true;
		}
	}

	/**
	 * 通过id从frinedMap里获取Friend对象
	 * @param id
	 * @return
	 */
	public Friend getFriend(String id)
	{
		return friendMap.get(id);
	}
	
	/**
	 * 获取客户端到服务端的输出流
	 * @return
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}
	
	/**
	 * 获取id对应的好友的面板
	 * @param id
	 * @return 
	 */
	public FriendPanel getFriendPanel(String id)
	{
		for(int i = 0; i <= friendPanel.length && friendPanel[i] != null; i++)
		{
			if(friendPanel[i].id.equals(id))
			{
				return friendPanel[i];
			}
		}
		return null;
	}


	public String getID()
	{
		return this.id;
	}
	
}

class FriendPanel extends JPanel
{
	public String id;
	public JLabel statusLabel;
}
