package momingqi.client;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import momingqi.util.Util;
import momingqi.util.XMLUtil;

import org.xml.sax.SAXException;

/**
 * 用户主界面，负责：维护在线好友列表，维护好友面板，获得向服务器的输出流，显示错误信息
 * @author mingC
 *
 */
public class MainFrame extends JFrame
{
	private Socket socket;
	private String id;
	private String nickname;
	private String photo;
	private Map<String, Friend> friendMap;			//好友id，Friend对象键值对
	private FriendPanel[] friendPanel;				//装有每一个好友面板的数组
	private Map<String, ChatFrame> chatFrameMap;	//好友id，聊天窗口键值对
	
	final Color PanelHighlight = Color.lightGray;
	final Color PanelBackground = Color.GRAY;
	final Color PanelClick = Color.yellow;
	final Font BoldFont = new Font("Dialog.plain", Font.BOLD, 18);
	final Font PlainFont = new Font("Dialog.plain", Font.PLAIN, 15);
	
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
		userPanel.add(new JLabel(icon));	//澶村儚鏍囩
		userPanel.add(rightPanel);
		
		JPanel listPanel = new JPanel(new GridLayout(friendMap.size(), 1, 3, 3));//
		listPanel.setBorder(BorderFactory.createTitledBorder("好友列表"));	
		friendPanel = new FriendPanel[Util.MAXUSERNUM];
		int i = 0;
		
		for(Friend f: friendMap.values())
		{
			friendPanel[i] = createNewPanel(f);
			listPanel.add(friendPanel[i]);
			i++;
		}
		JButton addButton = new JButton("添加好友");	//添加好友按钮
		addButton.addActionListener(new AddFriendHandler(this));
		
		this.setLayout(new FlowLayout());
		this.add(userPanel);
		this.add(listPanel);
		this.add(addButton);
		this.setVisible(true);
		this.setSize(400, 800);
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
	 * 创建一个好友面板并返回
	 * @param f
	 * @return
	 */
	private FriendPanel createNewPanel(Friend f)
	{
		FriendPanel newPanel = new FriendPanel();
		newPanel.id = f.id;	//封装id标签
		newPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		JPanel infoPanel = new JPanel((new GridLayout(2,1)));
		//昵称
		JLabel label = new JLabel(f.nickname);
		label.setFont(BoldFont);
		infoPanel.add(label);
		//在线状态
		JLabel onlineLabel = new JLabel("  离线");
		onlineLabel.setFont(PlainFont);
		infoPanel.add(onlineLabel);
		newPanel.statusLabel = onlineLabel;	//封装在线状态标签
		//id
		label = new JLabel(f.id);
		label.setFont(PlainFont);
		infoPanel.add(label);
		//包装在friendPanel数组里
		newPanel.add(new JLabel(
				new ImageIcon("resources/ImageResources/" + f.photo)));
		newPanel.add(infoPanel);
		newPanel.addMouseListener(new MouseAdapter()	//鐩戝惉闈㈡澘
		{
			@Override
			public void mouseEntered(MouseEvent e)	//鼠标进入时设置为明亮色
			{
				e.getComponent().setBackground(PanelHighlight);
			}
			
			@Override
			public void mouseExited(MouseEvent e)
			{
				e.getComponent().setBackground(PanelBackground);	//鼠标离开时将面板设置为背景色
			}
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				e.getComponent().setBackground(PanelClick); // 设置为特定颜色
				if (e.getClickCount() == 2) // 双击打开聊天窗口
				{
					JPanel jp = (JPanel) ((JPanel) e.getComponent())
							.getComponent(1); // 获得包含id标签的面板
					String id = ((JLabel) jp.getComponent(2)).getText(); // 获得id
					ChatFrame cf = chatFrameMap.get(id); // 获取聊天框
					if (cf == null) // 若聊天框不存在则创建
					{
						// 创建聊天面板并自动添加到chatpanelmap中
						createChatFrame(friendMap.get(id));
					}
					else
					{
						cf.requestFocus();
					}
					e.getComponent().setBackground(PanelBackground);
				}
			}

		});
		return newPanel;
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
	public ChatFrame createChatFrame(Friend f)
	{
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
	 * 
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
				friendPanel[i].statusLabel.setText("离线");
				break;
			}
		}
		Friend offlineFriend = friendMap.get(id);
		if(offlineFriend != null)
		{
			offlineFriend.online = false;
		}
	}
	
	/**
	 * 
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
		Friend onlineFriend = friendMap.get(id);
		if(onlineFriend != null)	//存在一种情况，该上线用户不是好友，此时跳过
		{
			onlineFriend.online = true;
		}
	}
	
	/**
	 * 使用ids数组初始化在线好友列表
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
			Friend onlineFriend = friendMap.get(ids[k]);
			if (onlineFriend != null)
			{
				onlineFriend.online = true;
			}
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
	 * 获得对服务端的输出流
	 * @return
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}
	
	/**
	 * 获得id对应的好友的FriendPanel面板对象
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
	
	public String getNickName()
	{
		return this.nickname;
	}
	
	/**
	 * 创建聊天框，独立创建一个线程
	 * @param id
	 * @param cf
	 */
	private void buildChatFrame(String id, ChatFrame cf)
	{
		new buildChatFrameThread(this, cf, id).start();
		System.out.println("------------------------");
	}

	/**
	 * 给好友面板增加好友
	 */
	public void addFriendToPanel(Friend f)
	{
		for(int i = 0; i < friendPanel.length; i++)
		{
			JPanel newPanel = new JPanel();
			if(friendPanel[i] == null)
			{
				friendPanel[i] = createNewPanel(f);
			}
		}
	}

	

}


class buildChatFrameThread extends Thread
{
	private MainFrame mf;
	private ChatFrame cf;
	private String id;
	
	public buildChatFrameThread(MainFrame mf, ChatFrame cf, String id)
	{
		this.mf = mf;
		this.cf = cf;
		this.id = id;
	}

	@Override
	public void run()
	{
		cf = mf.createChatFrame(mf.getFriend(id));
	}
}

class FriendPanel extends JPanel
{
	public String id;
	public JLabel statusLabel;
}
