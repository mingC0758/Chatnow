package momingqi.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import momingqi.util.Util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

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
				String msg = Util.readFromInputStream(in);	//方法堵塞，从输入流中读取字节并转换成字符串
				InputStream xml_in = new ByteArrayInputStream(msg.getBytes());
				Document doc = new SAXReader().read(xml_in);
				server.log("accept id：" + user.id + " " + msg);
				Element root = doc.getRootElement();	//获得xml的根节点
//				switch(root.getName())
//				{
//					case "chatmsg":
//						handleChatMsg(root); break;
//					case "close":
//						handleClose(root); break;
//					case "addfriend":
//						handleAddFriend(root); break;
//					case "removefriend":
//						handleRemoveFriend(root); break;
//				}
				String tag = root.getName();
				if(tag.equals("chatmsg"))
					handleChatMsg(root);
				else if(tag.equals("close"))
					handleClose(root);
				else if(tag.equals("addfriend"))
					handleAddFriend(root);
				else if(tag.equals("removefriend"))
					handleRemoveFriend(root);
			}
			catch (DocumentException e)
			{
				server.log("parse XML error");// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			catch (IOException e)
			{
				break;
			}
		}
	}

	private void handleRemoveFriend(Element root)
	{
		// TODO Auto-generated method stub
		
	}

	private void handleAddFriend(Element root)
	{
		//TODO
	}

	private void handleClose(Element root)
	{
		server.removeUser(user);
		exit = true;
	}

	/**
	 * 处理聊天消息
	 * @param root
	 */
	private void handleChatMsg(Element root)
	{
		String receiverID = root.attributeValue("receiver");	//获得接收者id
		String time = root.attributeValue("time");
		String msg = root.getText();							//获得聊天文本
		String xml = String.format("<chatmsg sender=\"%s\" time=\"%s\">%s</chatmsg>",
				user.id, time, msg);									//发送给接受者的xml
		System.out.println(msg);
		User receiver = server.getUser(receiverID);
		try
		{
			OutputStream outForReceiver = receiver.socket.getOutputStream();
			outForReceiver.write(xml.getBytes());
			outForReceiver.flush();
			server.log("send to id:" + receiver.id + " " + xml);
		}
		catch (IOException e) // 若发送消息给接收者失败时，则返回servererror给发送者
		{
			OutputStream outForSender;
			try
			{
				outForSender = user.socket.getOutputStream();
				outForSender.write("<msgerror/>" // 由于对方下线导致发送消息失败
						.getBytes());
				outForSender.flush();
				server.log("send to id:" + user.id + "<msgerror/>");
			}
			catch (IOException e1)
			{
			}

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
//				boolean isChatMsg = false;
//				String msgReceiverID;
//				
//				@Override
//				public void startElement(String uri, String localName,
//						String qName, Attributes attributes)
//						throws SAXException
//				{
//					if(qName.equals("chatmsg"))
//					{
//						isChatMsg = true;
//						msgReceiverID = attributes.getValue("receiver");
//					}
//					else if(qName.equals("close"))
//					{
//						server.removeUser(user);
//						exit = true;
//					}
//					else if(qName.equals("addfriend"))
//					{
//						String add_id = attributes.getValue("id");
//						Document doc;
//						try
//						{
//							doc = new SAXReader().read(Util.ServerResourcesPath + "users.xml");
//							Element e = (Element)doc.selectSingleNode("//[@id='" + user.id + "']");
//							String id = e.attribute("id").getValue();
//							String nickname = e.attribute("id").getValue();
//							String photo = e.attribute("id").getValue();
//							String xml = String.format("<addfriend id=\"%s\" nickname=\"%s\" photo=\"%s\">",
//									id, nickname, photo);
//							try
//							{
//								new SendMsgThread(server, user.socket.getOutputStream(), xml);
//							}
//							catch (IOException e1)
//							{
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
//						}
//						catch (DocumentException e2)
//						{
//							// TODO Auto-generated catch block
//							e2.printStackTrace();
//						}
//						
//					}
//				}
//				
//				@Override
//				public void characters(char[] ch, int start, int length)
//						throws SAXException
//				{
//					if (isChatMsg)
//					{
//						String msg = new String(ch, start, length);
//						String xml = String.format("<chatmsg sender=\"%s\">%s</chatmsg>",
//								user.id, msg);
//						
//						for (User u : server.onlineList)
//						{
//							if (u.id.equals(msgReceiverID))
//							{
//								try
//								{
//									OutputStream outForReceiver = u.socket
//											.getOutputStream();
//									outForReceiver.write(xml.getBytes());
//									outForReceiver.flush();
//									server.log("send to id:" + u.id + xml);
//								}
//								catch (IOException e) //若发送消息给接收者失败时，则返回servererror给发送者
//								{
//									OutputStream outForSender;
//									try
//									{
//										outForSender = user.socket
//												.getOutputStream();
//										outForSender
//												.write("<msgerror/>"	//由于对方下线导致发送消息失败
//														.getBytes());
//										outForSender.flush();
//										server.log("send to id:" + user.id + "<msgerror/>");
//									}
//									catch (IOException e1)
//									{
//									}
//
//								}
//							}
//						}
//					}
//				}
//				
//				@Override
//				public void endElement(String uri, String localName,
//						String qName) throws SAXException
//				{
//					if(qName.equals("msg"))
//					{
//						isChatMsg = false;
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
//	}
	
}
