package momingqi.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

@SuppressWarnings("serial")
public class FriendPanel extends JPanel implements ActionListener
{
	public MainFrame mf;
	public Friend f;
	public JPopupMenu menu;
	public String id;
	public JLabel statusLabel;
	JMenuItem item1;
	JMenuItem item2;
	JMenuItem item3;
	JMenuItem item4;
	JMenuItem item5;
	JMenuItem item6;
	JFrame historyFrame;
	JTextArea historyArea;
	
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

	/**
	 * 初始化组件
	 */
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
		menu = new JPopupMenu();
		item1 = new JMenuItem("发送消息");
		item2 = new JMenuItem("查看备注");
		item3 = new JMenuItem("查看聊天记录");
		item4 = new JMenuItem("修改备注");
		item5 = new JMenuItem("删除好友");
		item6 = new JMenuItem("删除聊天记录");
		item1.addActionListener(this);
		item2.addActionListener(this);
		item3.addActionListener(this);
		item4.addActionListener(this);
		item5.addActionListener(this);
		menu.add(item1);
		menu.add(item2);
		menu.add(item3);
		menu.add(item4);
		menu.add(item5);
		menu.add(item6);
		historyFrame = new JFrame("与" + f.nickname + "的聊天记录");
		historyArea = new JTextArea(20, 20);
		historyArea.setLineWrap(true);
		historyArea.setFont(mf.PlainFont);
		JScrollPane pane = new JScrollPane(historyArea);
		historyFrame.add(pane);
		historyFrame.pack();
		historyFrame.setVisible(false);
	}

	/**
	 * 菜单选项事件处理
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if(source == item1)
		{
			item1ActionPerform();
		}
		else if(source == item2)
		{
			item2ActionPerform();
		}
		else if(source == item3)
		{
			item3ActionPerform();
		}
		else if(source == item4)
		{
			item4ActionPerform();
		}
		else if(source == item5)
		{
			item5ActionPerform();
		}
		else if(source == item6)
		{
			item6ActionPerform();
		}
	}


	/**
	 * 发送消息选项
	 */
	private void item1ActionPerform()
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

	/**
	 * 查看备注选项
	 */
	private void item2ActionPerform()
	{
		File file = new File("./resources/friends.xml");
		Document doc;
		try
		{
			doc = new SAXReader().read(file);
			Element elem = (Element)doc.selectSingleNode("/friends/user[@id='" + f.id + "']");
			String comment = elem.attributeValue("comment");
			JOptionPane.showMessageDialog(mf, "备注：" + comment);
		}
		catch(DocumentException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 查看聊天记录选项
	 */
	private void item3ActionPerform()
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
			
			historyFrame.setVisible(true);
			historyArea.setText("");
			for (Element recordElem: recordElems)
			{
				String id2 = recordElem.attributeValue("id");
				String nickname = recordElem.attributeValue("nickname");
				String time = recordElem.attributeValue("time");
				String chatmsg = recordElem.getText();
				historyArea.append(String.format("%s %s(%s)\n%s\n", time, nickname, id2, chatmsg));
			}
			
		}
		catch (DocumentException e1)
		{
			e1.printStackTrace();
		}
	}
	
	/**
	 * 修改备注选项
	 */
	private void item4ActionPerform()
	{
		String comment = JOptionPane.showInputDialog(mf, "添加备注:");
		
		File file = new File("./resources/friends.xml");
		Document doc;
		try
		{
			doc = new SAXReader().read(file);
			Element elem = (Element)doc.selectSingleNode("/friends/user[@id='" + f.id + "']");
			elem.attribute("comment").setValue(comment);
			FileOutputStream out = new FileOutputStream(file);
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(doc);
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 *  删除好友选项
	 */
	private void item5ActionPerform()
	{
		mf.deleteFriend(f);
		//发送给服务端
		String xml = String.format("<deletefriend id=\"%s\"/>", f.id);
		OutputStream out;
		try
		{
			out = mf.getOutputStream();
			out.write(xml.getBytes());
			out.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除聊天记录
	 */
	private void item6ActionPerform()
	{
		String filename = f.id + "xml";
		File file = new File("./resources/history/" + filename);
		if(file.exists())
		{
			file.delete();
		}
	}
}


