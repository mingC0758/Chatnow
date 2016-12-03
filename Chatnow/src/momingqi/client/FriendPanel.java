package momingqi.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class FriendPanel extends JPanel
{
	public MainFrame mf;
	public Friend f;
	public JPopupMenu menu;
	public String id;
	public JLabel statusLabel;
	
	final Color PanelHighlight = new Color(184,247,136);
	final Color PanelBackground = null;
	final Color PanelClick = new Color(232,237,81);
	final Font BoldFont = new Font("Dialog.plain", Font.BOLD, 18);
	final Font PlainFont = new Font("Dialog.plain", Font.PLAIN, 15);
	
	public FriendPanel(MainFrame mf, Friend f)
	{
		this.mf = mf;
		this.f = f;
		initComponent();
	}

	private void initComponent()
	{
		JPanel infoPanel = new JPanel((new GridLayout(2,1)));
		
		//昵称
		JLabel label = new JLabel(f.nickname);
		label.setFont(BoldFont);
		infoPanel.add(label);
		//在线状态
		JLabel onlineLabel = new JLabel("  离线");
		onlineLabel.setFont(PlainFont);
		infoPanel.add(onlineLabel);
		this.statusLabel = onlineLabel;	//封装在线状态标签
		//id
		label = new JLabel(f.id);
		label.setFont(PlainFont);
		infoPanel.add(label);
		
		infoPanel.setBackground(PanelBackground);
		//包装在friendPanel数组里
		this.add(new JLabel(
				new ImageIcon("resources/ImageResources/" + f.photo)));
		this.add(infoPanel);
		this.addMouseListener(new MouseAdapter()				//给每个好友面板增加鼠标事件监听器
		{
			@Override
			public void mouseEntered(MouseEvent e)					//鼠标进入时设置为明亮色
			{
				e.getComponent().setBackground(PanelHighlight);
				((JPanel)e.getComponent()).getComponent(1).setBackground(PanelBackground);
			}
			
			@Override
			public void mouseExited(MouseEvent e)
			{
				e.getComponent().setBackground(PanelBackground);	//鼠标离开时将面板设置为背景色
			}
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				e.getComponent().setBackground(PanelClick); 		// 设置为特定颜色
				if(e.getButton() == 3)	//右击时弹出菜单
				{
					System.out.println(e.getSource());
					FriendPanel fp = (FriendPanel)e.getComponent();
					fp.menu.show(fp, e.getX(), e.getY());
					return;
				}
				if (e.getClickCount() == 2) 						// 双击打开聊天窗口
				{
//					JPanel jp = (JPanel) ((JPanel) e.getComponent())
//							.getComponent(1); 						// 获得包含id标签的面板
//					String id = ((JLabel) jp.getComponent(2)).getText(); 	// 获得id
					String id = ((FriendPanel)e.getComponent()).id;
					ChatFrame cf = mf.getChatFrame(id); 			// 获取聊天框
					if (cf == null) 								// 若聊天框不存在则创建
					{
						// 创建聊天面板并自动添加到chatpanelmap中
						
						mf.createChatFrame(id);
					}
					else
					{
						cf.requestFocus();
					}
					e.getComponent().setBackground(PanelBackground);
				}
			}

		});
		
		this.id = f.id;	//封装id标签
		this.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		this.setBackground(PanelBackground);
		
		// 右键好友弹出菜单
		JPopupMenu menu = new JPopupMenu();
		JMenuItem item1 = new JMenuItem("发送消息");
		JMenuItem item2 = new JMenuItem("查看聊天记录");
		JMenuItem item3 = new JMenuItem("修改备注");
		JMenuItem item4 = new JMenuItem("删除好友");
		item1.addActionListener(new Item1Handler(mf, f));
		item2.addActionListener(new Item2Handler(mf, this.f));
		menu.add(item1);
		menu.add(item2);
		this.menu = menu;
	}

}

/**
 * 发送消息选项
 * @author mingC
 *
 */
class Item1Handler implements ActionListener
{
	private MainFrame mf;
	private Friend f;
	
	public Item1Handler(MainFrame mf, Friend f)
	{
		this.mf = mf;
		this.f = f;
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
		ChatFrame cf = mf.getChatFrame(f.id); 			// 获取聊天框
		if (cf == null) 								// 若聊天框不存在则创建
		{
			// 创建聊天面板并自动添加到chatpanelmap中
			mf.createChatFrame(f.id);
		}
		else
		{
			cf.requestFocus();
		}
	}
}

/**
 * 查看聊天记录选项
 * @author mingC
 *
 */
class Item2Handler implements ActionListener
{
	private MainFrame mf;
	private Friend f;
	
	public Item2Handler(MainFrame mf, Friend f)
	{
		this.mf = mf;
		this.f = f;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String filename = f.id + ".xml";
		File file = new File("./resources/history/" + filename);
		if(!file.exists())
		{
			JOptionPane.showMessageDialog(mf, "找不到与此好友的聊天记录");
			return;
		}
		try
		{
			Document doc = new SAXReader().read(file);
			@SuppressWarnings("unchecked")
			List<Element> recordElems = (List<Element>)doc.getRootElement().elements("record");
			
			JFrame frame = new JFrame("与" + f.nickname + "的聊天记录");
			JTextArea area = new JTextArea(20, 20);
			area.setLineWrap(true);
			area.setFont(mf.PlainFont);
			JScrollPane pane = new JScrollPane(area);
			frame.add(pane);
			frame.pack();
			frame.setVisible(true);
			
			for (Element recordElem: recordElems)
			{
				String id2 = recordElem.attributeValue("id");
				String nickname = recordElem.attributeValue("nickname");
				String time = recordElem.attributeValue("time");
				String chatmsg = recordElem.getText();
				area.append(String.format("%s %s(%s)\n%s\n", time, nickname, id2, chatmsg));
			}
			
			
		}
		catch (DocumentException e1)
		{
			e1.printStackTrace();
		}

	}
}
