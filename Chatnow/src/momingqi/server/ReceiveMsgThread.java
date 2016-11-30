package momingqi.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import momingqi.util.Util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 接受来自特定用户的消息(聊天消息，退出消息，增删好友消息)，并进行处理
 * @author mingC
 *
 */
public class ReceiveMsgThread extends Thread
{
	private Server server;	//服务器对象
	private InputStream in;
	private User user;
	private boolean exit = false;	//退出标志
	
	public ReceiveMsgThread(Server server, InputStream in, User user)
	{
		this.server = server;
		this.in = in;
		this.user = user;
	}
	
	@Override
	public void run()
	{
		while (!exit)
		{
			try
			{
				String msg = Util.readFromInputStream(in);	//方法堵塞
				server.log("accept id：" + user.id + " " + msg);
				parseMsg(msg);
			}
			catch (Exception e)
			{
				server.removeUser(user);
				server.log("Unnomally Exit ! id：" + user.id);
				return;	//退出循环，结束接收消息
			}
		}
	}

	private void parseMsg(String msg)//新增<close>消息，否则有错
	{
		InputStream in = new ByteArrayInputStream(msg.getBytes());
		
		SAXParser parser;
		try
		{
			parser = SAXParserFactory.newInstance()
					.newSAXParser();
			
			parser.parse(in, new DefaultHandler()
			{
				boolean isMsg = false;
				String msgReceiverID;
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException
				{
					if(qName.equals("msg"))
					{
						isMsg = true;
						msgReceiverID = attributes.getValue("receiver");
					}
					else if(qName.equals("close"))
					{
						server.removeUser(user);
						exit = true;
					}
					else if(qName.equals("addfriend"))
					{
						String add_id = attributes.getValue("id");
						Document doc;
						try
						{
							doc = new SAXReader().read(Util.ServerResourcesPath + "users.xml");
							Element e = (Element)doc.selectSingleNode("//[@id='" + user.id + "']");
							String id = e.attribute("id").getValue();
							String nickname = e.attribute("id").getValue();
							String photo = e.attribute("id").getValue();
							String xml = String.format("<addfriend id=\"%s\" nickname=\"%s\" photo=\"%s\">",
									id, nickname, photo);
							try
							{
								new SendMsgThread(server, user.socket.getOutputStream(), xml);
							}
							catch (IOException e1)
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						catch (DocumentException e2)
						{
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						
					}
				}
				
				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException
				{
					if (isMsg)
					{
						String msg = new String(ch, start, length);
						String xml = String.format("<msg id=\"%s\">%s</msg>",
								user.id, msg);
						for (User u : server.onlineList)
						{
							if (u.id.equals(msgReceiverID))
							{
								try
								{
									OutputStream outForReceiver = u.socket
											.getOutputStream();
									outForReceiver.write(xml.getBytes());
									outForReceiver.flush();
								}
								catch (IOException e) //若发送消息给接收者失败时，则返回servererror给发送者
								{
									OutputStream outForSender;
									try
									{
										outForSender = user.socket
												.getOutputStream();
										outForSender
												.write("<servererror type=\"msgsenderror\"/>"
														.getBytes());
										outForSender.flush();
									}
									catch (IOException e1)
									{
										server.removeUser(user);
										e.printStackTrace();
									}

								}
							}
						}
					}
				}
				
				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException
				{
					if(qName.equals("msg"))
					{
						isMsg = false;
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
