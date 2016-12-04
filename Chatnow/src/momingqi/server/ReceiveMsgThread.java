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
	private Server server;			//服务器对象
	private InputStream in;			//客户端的输入流
	private User user;				//此客户端对应的用户
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
				String msg = Util.readFromInputStream(in);			//方法堵塞，从输入流中读取字节并转换成字符串
				InputStream xml_in = new ByteArrayInputStream(msg.getBytes());
				Document doc = new SAXReader().read(xml_in);
				server.log("accept id：" + user.id + " " + msg);
				Element root = doc.getRootElement();				//获得xml的根节点
				String tag = root.getName();
				if(tag.equals("chatmsg"))
					handleChatMsg(root);
				else if(tag.equals("close"))
					handleClose(root);
				else if(tag.equals("addfriend"))
					handleAddFriend(root);
				else if(tag.equals("deletefriend"))
					handleDeleteFriend(root);
			}
			catch (DocumentException e)
			{
				server.log("parse XML error");
				e.printStackTrace();
				try
				{
					in.close();
				}
				catch (IOException e1)
				{
				}
				server.removeUser(user);
				break;
			}
			catch (IOException e)
			{
				server.removeUser(user);
				break;
			}
		}
	}

	/**
	 * 处理删除好友的消息
	 * @param root
	 */
	private void handleDeleteFriend(Element root)
	{
		User u = server.getUser(root.attributeValue("id"));
		try
		{
			OutputStream out = u.getOutputStream();
			String xml = String.format("<deletefriend id=\"%s\"/>", user.id);
			out.write(xml.getBytes());
			out.flush();
			server.log("send to " + user.id + xml);
		}
		catch (IOException e)
		{
			server.removeUser(u);
		}
	}

	/**
	 * 处理添加好友的消息
	 * @param root
	 */
	private void handleAddFriend(Element root)
	{
		try
		{
			String type = root.attributeValue("type");
			String receiver = root.attributeValue("id"); // 获取接收者id
			User receiverUser = server.getUser(receiver);
			if (receiverUser == null) // 找不到此用户
			{
				String none_xml = String
						.format("<addfriend type=\"result\" id=\"%s\">none</addfriend>",
								receiver);
				OutputStream outToSender = user.getOutputStream();
				
				outToSender.write(none_xml.getBytes());
				outToSender.flush();
				server.log(none_xml);
				return;
			}
			OutputStream outToReceiver = receiverUser.getOutputStream();

			if (type.equals("request"))
			{
				String request_xml = String
						.format("<addfriend type=\"request\" id=\"%s\" nickname=\"%s\" photo=\"%s\"></addfriend>",
								user.id, user.nickname, user.photo);
				outToReceiver.write(request_xml.getBytes());
				outToReceiver.flush();
				server.log("send to " + receiver + request_xml);
			}
			else if (type.equals("result"))
			{
				String result = root.getText();
				User receiver_user = server.getUser(root.attributeValue("id"));
				OutputStream out = receiver_user.socket.getOutputStream();
				if (result.equals("accept"))
				{
					String xml = String
							.format("<addfriend type=\"result\" id=\"%s\" nickname=\"%s\" photo=\"%s\">accept</addfriend>",
									user.id, user.nickname, user.photo);
					out.write(xml.getBytes());
					out.flush();
					server.log("send to " + receiver_user.id + xml);
				}
				else if (result.equals("refuse"))
				{
					String xml = String
							.format("<addfriend type=\"result\" id=\"%s\">refuse</addfriend>",
									user.id);

					out.write(xml.getBytes());
					out.flush();
					server.log(xml);
				}
			}
		}
		catch (IOException e)
		{
		}
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
				user.id, time, msg);							//发送给接受者的xml
		System.out.println(msg);
		User receiver = server.getUser(receiverID);
		try
		{
			OutputStream outForReceiver = receiver.socket.getOutputStream();
			outForReceiver.write(xml.getBytes());
			outForReceiver.flush();
			server.log("send to id:" + receiver.id + " " + xml);
		}
		catch (IOException e) 						// 若发送消息给接收者失败时，则返回msgrerror给发送者
		{
			OutputStream outForSender;
			try
			{
				outForSender = user.socket.getOutputStream();
				String error = "<msgerror id=\""+ receiver.id +"\"/>";
				outForSender.write(error.getBytes());			
				outForSender.flush();
				server.log("send to id:" + user.id + "<msgerror/>");
			}
			catch (IOException e1)
			{
			}

		}
			
	}
}
