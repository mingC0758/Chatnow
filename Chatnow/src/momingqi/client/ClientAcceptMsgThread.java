package momingqi.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import momingqi.util.Util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 接收服务端的消息，并进行相应的处理
 * @author mingC
 *
 */
public class ClientAcceptMsgThread extends Thread
{
	public Client client;
	public MainFrame mf;
	public Socket socket;
	public InputStream in;
	public OutputStream out;
	
	public ClientAcceptMsgThread(Client client, Socket socket)
	{
		this.client = client;
		this.socket = socket;
		try
		{
			in = socket.getInputStream();
			out = socket.getOutputStream();
		}
		catch (IOException e)
		{
			this.interrupt();
		}
		
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
					@SuppressWarnings("unchecked")
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
		String tag = root.getName();
		if (tag.equals("chatmsg"))
			handleChatMsg(root);

		else if (tag.equals("addonlineuser"))
			handleAddOnlineUser(root);

		else if (tag.equals("removeonlineuser"))
			handleRemoveOnlineUser(root);

		else if (tag.equals("onlinelist"))
			handleOnlineList(root);

		else if (tag.equals("addfriend"))
			handleAddFriend(root);
		
		else if (tag.equals("deletefriend"))
			handleDeleteFriend(root);
		
		else if (tag.equals("login"))
			handleLoginResult(root);
		
		else if(tag.equals("msgerror"))
			handleMsgError(root);
	}

	/**
	 * 发送上一条聊天消息时失败
	 * @param root
	 */
	private void handleMsgError(Element root)
	{
		String time = Util.presentTime();
		String id = root.attributeValue("id");
		ChatFrame cf = mf.getChatFrame(id);
		if(cf == null)
		{
			mf.createChatFrame(id);
		}
		cf.setMsgText(time, "发送消息失败！");
	}

	/**
	 * 登陆结果处理
	 * @param root
	 */
	private void handleLoginResult(Element root)
	{
		String result = root.attributeValue("result");
		System.out.println("接收到登陆结果：" + result);

		if(result.equals("succeed"))	//登陆成功
		{
			System.out.println("登陆成功");
			mf = new MainFrame(client.id, client.socket);
			client.setVisible(false);
		}
		else if(result.equals("error"))	//登陆失败
		{
			client.showError("账户或密码错误！");
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
			}
			return;
		}
		else if(result.equals("repeat"))	//重复登陆
		{
			client.showError("请勿重复登陆！");
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
			}
			return;
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

	private void handleDeleteFriend(Element root)
	{
		String id = root.attributeValue("id");
		JOptionPane.showMessageDialog(mf, id + "与你脱离好友关系");
		Friend f = mf.getFriend(id);
		mf.deleteFriend(f);
	}

	/**
	 * 添加好友消息处理
	 * @param root
	 */
	private void handleAddFriend(Element root)
	{
		String type = root.attributeValue("type");
		String receiver = root.attributeValue("id");
		if(type.equals("request"))
		{
			//对方请求添加好友
			int choose = JOptionPane.showConfirmDialog(mf, receiver+"请求添加您为好友，是否同意？");
			if(choose == JOptionPane.YES_OPTION)
			{
				String xml = String.format("<addfriend type=\"result\" id=\"%s\">accept</addfriend>", 
						receiver);
				try
				{
					out.write(xml.getBytes());
					out.flush();
				}
				catch (IOException e)
				{
					//发送消息失败
				}
				String nickname = root.attributeValue("nickname");
				String photo = root.attributeValue("photo");
				Friend f = new Friend(receiver, nickname, photo);
				mf.addNewFriend(f);
			}
			else if(choose == JOptionPane.NO_OPTION)
			{
				String xml = String.format("<addfriend type=\"result\" id=\"%s\">refuse</addfriend>", receiver);
				try
				{
					out.write(xml.getBytes());
					out.flush();
				}
				catch (IOException e)
				{
					//发送消息失败
				}
			}
		}
		else if(type.equals("result"))
		{
			String result = root.getText();
			if(result.equals("accept"))
			{
				//对方接受请求
				//获取对方的id，nickname，photo
				String id, nickname, photo;
				id = root.attributeValue("id");
				nickname = root.attributeValue("nickname");
				photo = root.attributeValue("photo");
				Icon icon = new ImageIcon("./resources/ImageResources/"+photo);
				Friend newFriend = new Friend(id, nickname, photo);
				JOptionPane.showMessageDialog(mf, id+"同意添加您为好友！", "消息", 
						JOptionPane.INFORMATION_MESSAGE, icon);
				mf.addNewFriend(newFriend);
			}
			else if(result.equals("refuse"))
			{
				String id = root.attributeValue("id");
				JOptionPane.showMessageDialog(mf, id+"拒绝添加您为好友！");
			}
			else if(result.equals("none"))
			{
				String id = root.attributeValue("id");
				JOptionPane.showMessageDialog(mf, "不存在此用户：" + id);
			}
		}
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
		Friend f = mf.getFriend(sender_id);					//获得发送者的Friend对象
		ChatFrame.logMsg(f, time, msgText);			//记录消息
		
		if(cf == null)	//窗口没有打开
		{
			//询问用户是否打开聊天窗口
			String str = "接受到来自 " + f.nickname + "的消息，是否查看？";		//对话框提示
			Icon icon = new ImageIcon(Util.ClientImagePath + f.photo);	//发送者的头像图标
			String[] options = {"查看", "忽略"};	//对话框选项
			//弹出对话框提醒用户是否打开聊天窗口，堵塞
			int result = JOptionPane.showOptionDialog(mf, str, "新消息",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, options, "查看");
			if (result == 0)
			{
				cf = mf.createChatFrame(sender_id);
				cf.setMsgText(time, msgText);		//显示消息
			}
		}
		else
		{
			cf.setMsgText(time, msgText);	//显示聊天信息
		}
	}


}
