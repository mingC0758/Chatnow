package momingqi.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import momingqi.util.Util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ClientAcceptMsgThread extends Thread
{
	public MainFrame mf;
	public InputStream in;
	
	public ClientAcceptMsgThread(MainFrame mf, InputStream in)
	{
		this.mf = mf;
		this.in = in;
	}

	@Override
	public void run()
	{
		String msg = null;
		InputStream msg_in = null;
		Document doc = null;
		SAXReader reader = new SAXReader();
		while(true)
		{
			try
			{
				msg = Util.readFromInputStream(in);	//方法堵塞，从输入流中读取字节并转换成字符串
				msg_in = new ByteArrayInputStream(msg.getBytes());
				doc = reader.read(msg_in);
				
				Element root = doc.getRootElement();	//获得xml的根节点
				dispatch(root);
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(mf, "与服务器断开连接！");//断线处理
				break;
			}
			catch (DocumentException e)	//出现多条消息连接在一起接收时出现此异常
			{
				String cor_msg ="<msg>" + msg + "</msg>";	//用一个根标签打包起来
				InputStream cor_msg_in = new ByteArrayInputStream(cor_msg.getBytes());
				try
				{
					doc = reader.read(cor_msg_in);
					List<Element> list = (List<Element>)doc.getRootElement().elements();
					for(Element elem: list)
					{
						dispatch(elem);	//分派消息
					}
				}
				catch (DocumentException e1)
				{
					e1.printStackTrace();
				}
			}
			
		}
	}

	/**
	 * 分派消息让对应的方法进行处理
	 * @param root
	 */
	private void dispatch(Element root)
	{
		switch(root.getName())
		{
			case "chatmsg":
				handleChatMsg(root); break;
			case "addonlineuser":
				handleAddOnlineUser(root); break;
			case "removeonlineuser":
				handleRemoveOnlineUser(root); break;
			case "onlinelist":
				handleOnlineList(root); break;
			case "addfriend":
				handleAddFriend(root); break;
			case "removefriend":
				handleRemoveFriend(root); break;
		}
	}

	/**
	 * 处理服务端传来的在线用户下线xml消息：调用MainFrame的removeOnlineUser方法
	 * @param root
	 */
	private void handleRemoveOnlineUser(Element root)
	{
		mf.removeOnlineUser(root.attributeValue("id"));
	}

	/**
	 * 处理服务端传来的离线用户上线xml消息
	 * @param root
	 */
	private void handleAddOnlineUser(Element root)
	{
		mf.addOnlineUser(root.attributeValue("id"));
	}

	private void handleRemoveFriend(Element root)
	{
		// TODO Auto-generated method stub
		
	}

	private void handleAddFriend(Element root)
	{
		// TODO Auto-generated method stub
	}

	/**
	 * 处理服务端传来的在线用户列表xml消息
	 * @param root
	 */
	private void handleOnlineList(Element root)
	{
		String[] ids = new String[Util.MAXUSERNUM];
		@SuppressWarnings("unchecked")
		List<Element> list = (List<Element>)root.elements("user");
		int i = 0;
		for(Element e: list)
		{
			ids[i++] = e.attributeValue("id");
		}
		System.out.println("初始化在线列表");
		mf.initOnlineUser(ids);	//初始化在线用户列表
	}

	/***
	 * 处理服务端传来的聊天消息
	 * @param root
	 */
	private void handleChatMsg(Element root)
	{
		String sender_id = root.attributeValue("sender");	//获取发送者的id
		String time = root.attributeValue("time");			//获取发送时间
		String msgText = root.getText();					//获得信息文本
		ChatFrame cf = mf.getChatFrame(sender_id);			//获取该id的聊天框，若不存在则cf==null
		
		if(cf == null)	//窗口没有打开
		{
			//询问用户是否打开聊天窗口
			Friend f = mf.getFriend(sender_id);	//获得发送者的Friend对象
			String str = "接受到来自 " + f.nickname + "的消息，是否查看？";		//对话框提示
			Icon icon = new ImageIcon(Util.ClientImagePath + f.photo);	//发送者的头像图标
			String[] options = {"查看", "忽略"};	//对话框选项
			//弹出对话框提醒用户是否打开聊天窗口，堵塞
			int result = JOptionPane.showOptionDialog(mf, str, "新消息",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, options, "查看");
			if (result == 0)
			{
				cf = mf.createChatFrame(f);
				cf.setMsgText(time, msgText);		//显示消息
				cf.logMsg(msgText);	//记录消息
			}
		}
		else
		{
			cf.setMsgText(time, msgText);	//显示聊天信息
			cf.logMsg(msgText);	//记录消息
		}
	}

//	private void parseMsg(String msg)
//	{
//		InputStream in = new ByteArrayInputStream(msg.getBytes());
//		
//		SAXParser parser;
//		try
//		{
//			parser = SAXParserFactory.newInstance()
//					.newSAXParser();
//			
//			parser.parse(in, new DefaultHandler()
//			{
//				boolean isOnlineList = false;
//				boolean isChatMsg = false;
//				String msg_id;	//发送者的id
//				String msg;		//聊天消息
//				String[] ids = new String[20];
//				int id_index = 0;
//				
//				@Override
//				public void startElement(String uri, String localName,
//						String qName, Attributes attributes) throws SAXException
//				{
//					if(qName.equals("addonlineuser"))	//新增在线用户
//					{
//						mf.addOnlineUser(attributes.getValue("id"));
//					}
//					else if(qName.equals("removeonlineuser"))
//					{
//						mf.removeOnlineUser(attributes.getValue("id"));
//					}
//					else if(qName.equals("onlinelist"))
//					{
//						isOnlineList = true;
//					}
//					else if(qName.equals("user") && isOnlineList == true)
//					{
//						ids[id_index] = attributes.getValue("id");
//						System.out.println("新上线好友，id:"+ids[id_index]);
//						id_index++;
//					}
//					else if(qName.equals("chatmsg"))
//					{
//						isChatMsg = true;
//						msg_id = attributes.getValue("sender");
//					}
//					else if(qName.equals("addfriend"))
//					{
//						final String add_id = attributes.getValue("id");
//						final String add_nickname = attributes.getValue("nickname");
//						final String add_photo = attributes.getValue("photo");
//						String show = "来自id:" + add_id + "的用户请求添加您为好友，是否同意？";
//						int result = 
//								JOptionPane.showConfirmDialog(null, show, "请求", JOptionPane.YES_NO_OPTION);
//						if (result == JOptionPane.NO_OPTION) return;
//						new Thread()
//						{
//							public void run() 
//							{
//								try
//								{	//发送接收命令给服务器
//									OutputStream out = mf.getOutputStream();
//									out.write(("<accept id=\"" + add_id + "\"/>").getBytes());
//									out.flush();
//								}
//								catch (IOException e)
//								{
//									mf.showError("发送失败！");
//								}
//								
//							};
//						}.start();
//						//添加到friends.xml中
//						try
//						{
//							Document doc = new SAXReader().read(Util.ClientResourcesPath + "friends.xml");
//							doc.getRootElement().addAttribute("id", add_id);
//							doc.getRootElement().addAttribute("nickname", add_nickname);
//							doc.getRootElement().addAttribute("photo", add_photo);
//							XMLWriter w = new XMLWriter(new FileOutputStream(Util.ClientResourcesPath + "friends.xml"));
//							w.write(doc);
//						}
//						catch (Exception e)
//						{
//							e.printStackTrace();
//						}
//						//增加好友到friendPanel
//						Friend f = new Friend(add_id, add_nickname, add_photo);
//						mf.addFriendToPanel(f);
//						//增加到上线好友列表
//						mf.addOnlineUser(f.id);
//					}
//				}
//				
//				@Override
//				public void characters(char[] ch, int start, int length)
//						throws SAXException
//				{
//					if(isChatMsg == true)
//					{
//						//获取消息
//						msg = new String(ch, start, length);
//					}
//				}
//				
//				@Override
//				public void endElement(String uri, String localName,
//						String qName) throws SAXException
//				{
//					if(qName.equals("onlinelist"))
//					{
//						isOnlineList = false;
//						System.out.println("初始化在线列表");
//						mf.initOnlineUser(ids);
//					}
//					else if(qName.equals("chatmsg"))
//					{
//						isChatMsg = false;
//						ChatFrame cf = mf.getChatFrame(msg_id);
//						
//						if(cf == null)	//窗口没有打开
//						{
//							//询问用户是否打开聊天窗口
//							Friend f = mf.getFriend(msg_id);	//获得发送者的Friend对象
//							String str = "接受到来自 " + f.nickname + "的消息，是否查看？";
//							Icon icon = new ImageIcon(Util.ClientImagePath + f.photo);
//							String[] options = {"查看", "忽略"};	//对话框选项
//							//弹出对话框提醒用户是否打开聊天窗口，堵塞
//							int result = JOptionPane.showOptionDialog(mf, str, "新消息",
//									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, options, "查看");
//							if (result == 0)
//							{
//								cf = mf.createChatFrame(f);
//								cf.setMsgText(msg);		//显示消息
//								cf.logMsg(msg);	//记录消息
//							}
//						}
//						else
//						{
//							cf.setMsgText(msg);	//显示聊天信息
//						}
//						
//					}
//				}
//			});
//		}
//		catch (ParserConfigurationException e)
//		{
//			e.printStackTrace();
//		}
//		catch (SAXException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		
//		
//	}
}
