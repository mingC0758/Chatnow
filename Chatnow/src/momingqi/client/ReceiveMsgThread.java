package momingqi.client;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import momingqi.util.Util;
import momingqi.util.XMLUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ReceiveMsgThread extends Thread
{
	public MainFrame mf;
	public InputStream in;
	
	public ReceiveMsgThread(MainFrame mf, InputStream in)
	{
		this.mf = mf;
		this.in = in;
	}

	@Override
	public void run()
	{
		String msg;
		while(true)
		{
			try
			{
				msg = Util.readFromInputStream(in);	//堵塞
				System.out.println("接收到消息:" + msg);
				parseMsg(msg);
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(mf, "与服务器断开连接！");//断线处理
				break;
			}
			
		}
	}

	private void parseMsg(String msg)
	{
		InputStream in = new ByteArrayInputStream(msg.getBytes());
		
		SAXParser parser;
		try
		{
			parser = SAXParserFactory.newInstance()
					.newSAXParser();
			
			parser.parse(in, new DefaultHandler()
			{
				boolean isOnlineList = false;
				boolean isMsg = false;
				String msg_id;	//发送者的id
				String msg;		//聊天消息
				String[] ids = new String[20];
				int id_index = 0;
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes) throws SAXException
				{
					if(qName.equals("addOnlineUser"))	//新增在线用户
					{
						mf.addOnlineUser(attributes.getValue("id"));
					}
					else if(qName.equals("removeOnlineUser"))
					{
						mf.removeOnlineUser(attributes.getValue("id"));
					}
					else if(qName.equals("onlinelist"))
					{
						isOnlineList = true;
					}
					else if(qName.equals("user") && isOnlineList == true)
					{
						ids[id_index] = attributes.getValue("id");
						System.out.println("新上线好友，id:"+ids[id_index]);
						id_index++;
					}
					else if(qName.equals("msg"))
					{
						isMsg = true;
						msg_id = attributes.getValue("id");
					}
					else if(qName.equals("addfriend"))
					{
						final String add_id = attributes.getValue("id");
						final String add_nickname = attributes.getValue("nickname");
						final String add_photo = attributes.getValue("photo");
						String show = "来自id:" + add_id + "的用户请求添加您为好友，是否同意？";
						int result = 
								JOptionPane.showConfirmDialog(null, show, "请求", JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.NO_OPTION) return;
						new Thread()
						{
							public void run() 
							{
								try
								{	//发送接收命令给服务器
									OutputStream out = mf.getOutputStream();
									out.write(("<accept id=\"" + add_id + "\"/>").getBytes());
									out.flush();
								}
								catch (IOException e)
								{
									mf.showError("发送失败！");
								}
								
							};
						}.start();
						//添加到friends.xml中
						try
						{
							Document doc = new SAXReader().read(Util.ClientResourcesPath + "friends.xml");
							doc.getRootElement().addAttribute("id", add_id);
							doc.getRootElement().addAttribute("nickname", add_nickname);
							doc.getRootElement().addAttribute("photo", add_photo);
							XMLWriter w = new XMLWriter(new FileOutputStream(Util.ClientResourcesPath + "friends.xml"));
							w.write(doc);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						//增加好友到friendPanel
						Friend f = new Friend(add_id, add_nickname, add_photo);
						mf.addFriendToPanel(f);
						//增加到上线好友列表
						mf.addOnlineUser(f.id);
					}
				}
				
				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException
				{
					if(isMsg == true)
					{
						//获取消息
						msg = new String(ch, start, length);
					}
				}
				
				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException
				{
					if(qName.equals("onlinelist"))
					{
						isOnlineList = false;
						System.out.println("初始化在线列表");
						mf.initOnlineUser(ids);
					}
					else if(qName.equals("msg"))
					{
						isMsg = false;
						ChatFrame cf = mf.getChatFrame(msg_id);
						if(cf == null)	//窗口没有打开
						{
							//询问用户是否打开聊天窗口
							Friend f = mf.getFriend(msg_id);	//获得发送者的Friend对象
							String str = "接受到来自 " + f.nickname + "的消息，是否查看？";
							Icon icon = new ImageIcon(Util.ClientImagePath + f.photo);
							String[] options = {"查看", "忽略"};	//对话框选项
							//弹出对话框提醒用户是否打开聊天窗口
							int result = JOptionPane.showOptionDialog(mf, str, "新消息",
									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, options, "查看");
							if (result == 0)
							{
								cf = mf.createChatFrame(f);
								cf.setMsgText(msg);		//显示消息
								cf.logMsg(msg);	//记录消息
							}
						}
						else
						{
							cf.setMsgText(msg);	//显示聊天信息
						}
						
					}
				}
			});
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
}
