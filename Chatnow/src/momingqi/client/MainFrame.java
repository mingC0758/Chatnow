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
 * �û������棬��ʾ�����б�͸�����Ϣ���鿴�����¼�����������
 * @author mingC
 *
 */
public class MainFrame extends JFrame
{
	private Socket socket;
	private String id;
	private String nickname;
	private String photo;
	private Map<String, Friend> friendMap;	//�����б�
	private FriendPanel[] friendPanel;				//������Ϣ�������
//	private LinkedList<String> onlineFriendList;	//�����û���id��
	private Map<String, ChatFrame> chatFrameMap;	//������б�
	
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
			Friend me = friendMap.get(id);	//ͨ��id��ȡ���ͻ����û��ĸ�����Ϣ
			nickname = me.nickname;
			photo = me.photo;
			friendMap.remove(id);	//���Լ��Ƴ������б�
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
			 * ÿ10�����һ�κ���״̬�����ڵ��ԡ�
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
						System.out.println("id:" + f.id + " ״̬:" + f.online);
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
		userPanel.setBorder(BorderFactory.createTitledBorder("������Ϣ"));

		JLabel label;
		JPanel rightPanel = new JPanel(new GridLayout(2,1));
		
		label = new JLabel(nickname);
		label.setFont(BoldFont);
		rightPanel.add(label);
		
		label = new JLabel(id);
		label.setFont(PlainFont);
		rightPanel.add(label);
		userPanel.add(new JLabel(icon));	//ͷ���ǩ
		userPanel.add(rightPanel);
		
		JPanel listPanel = new JPanel(new GridLayout(friendMap.size(), 1, 3, 3));//�û��б����
		listPanel.setBorder(BorderFactory.createTitledBorder("�����б�"));	
		friendPanel = new FriendPanel[Util.MAXUSERNUM];
		int i = 0;
		JPanel infoPanel;
		JLabel onlineLabel;
		/***
		 * Ϊÿһ�����Ѵ���һ�������װ�أ�ͷ��Label��infoLabel��nickname,status, id��
		 */
		for(Friend f: friendMap.values())
		{
			friendPanel[i] = new FriendPanel();
			friendPanel[i].id = f.id;	//�Ѻ���id��װ��ȥ
			friendPanel[i].setBorder(BorderFactory.createLineBorder(Color.BLUE));
			infoPanel = new JPanel((new GridLayout(2,1)));
			//�ǳ�
			label = new JLabel(f.nickname);
			label.setFont(BoldFont);
			infoPanel.add(label);
			//����״̬
			onlineLabel = new JLabel("  ����");
			onlineLabel.setFont(PlainFont);
			infoPanel.add(onlineLabel);
			friendPanel[i].statusLabel = onlineLabel;
			//�˺�
			label = new JLabel(f.id);
			label.setFont(PlainFont);
			infoPanel.add(label);
			//��ͷ���infoPanelװ����
			friendPanel[i].add(new JLabel(
					new ImageIcon("resources/ImageResources/" + f.photo)));
			friendPanel[i].add(infoPanel);
			friendPanel[i].addMouseListener(new MouseAdapter()	//�������
			{
				@Override
				public void mouseEntered(MouseEvent e)	//��Ϊ����ɫ
				{
					e.getComponent().setBackground(PanelHighlight);
				}
				
				@Override
				public void mouseExited(MouseEvent e)
				{
					e.getComponent().setBackground(PanelBackground);	//��ΪĬ�ϱ���ɫ
				}
				
				@Override
				public void mouseClicked(MouseEvent e)
				{
					JPanel jp = (JPanel)((JPanel)e.getComponent()).getComponent(1); //��ȡװ�ض�Ӧid��panel
					e.getComponent().setBackground(PanelClick);	//��Ϊ����ʱ��ǿ��ɫ
					if(e.getClickCount() == 2)	//˫���������
					{
						System.out.println("˫����");
						String id = ((JLabel)jp.getComponent(2)).getText();	//��ȡid
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
	 * ���û��ر�������ʱ��ʾ�Ƿ��˳��ͻ���
	 */
	public void windowClosingPerformed()
	{
		int result = JOptionPane.showConfirmDialog(this, "ȷ���˳��ͻ��ˣ�");
		if(result == JOptionPane.YES_OPTION)
		{
			//�������������˳���Ϣ
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
				showError("��������Ͽ����ӣ�");
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
	 * ��ȡָ��id�û������������ChatFrame�����ڣ��򷵻�null
	 * @param id
	 * @return
	 */
	public ChatFrame getChatFrame(String id)
	{
		return this.chatFrameMap.get(id);
	}

	/**
	 * �������û��б���ɾ���ض�id���û����Ѹ��û���online״̬��Ϊfalse�����½���
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
				System.out.println("id:" + id + "��������");
				friendPanel[i].statusLabel.setText("����");
				break;
			}
		}
		
		friendMap.get(id).online = false;
	}
	
	/**
	 * ���������û����û��б��У��Ѹ��û���online״̬��Ϊtrue,�����½���
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
				friendPanel[i].statusLabel.setText("����");
			}
		}
		
		friendMap.get(id).online = true;
	}
	
	/**
	 * ��ʼ�������û��б����Ѷ�Ӧ�������û���online״̬��Ϊtrue
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
					friendPanel[j].statusLabel.setText("����");
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
	 * ͨ��id��frinedMap���ȡFriend����
	 * @param id
	 * @return
	 */
	public Friend getFriend(String id)
	{
		return friendMap.get(id);
	}
	
	/**
	 * ��ȡ�ͻ��˵�����˵������
	 * @return
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}
	
	/**
	 * ��ȡid��Ӧ�ĺ��ѵ����
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
