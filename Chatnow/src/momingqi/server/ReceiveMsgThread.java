package momingqi.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import momingqi.util.Util;

/**
 * ���������ض��û�����Ϣ(������Ϣ���˳���Ϣ����ɾ������Ϣ)�������д���
 * @author mingC
 *
 */
public class ReceiveMsgThread extends Thread
{
	private Server server;	//����������
	private InputStream in;
	private User user;
	private boolean exit = false;	//�˳���־
	
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
				String msg = Util.readFromInputStream(in);	//��������
				System.out.println("����˽��ܵ�����id��" + user.id + "����Ϣ��" + msg);
				parseMsg(msg);
			}
			catch (Exception e)
			{
				server.removeUser(user);
				System.out.println("���û��������˳���id��" + user.id);
				break;	//�˳�ѭ��������������Ϣ
			}
		}
	}

	private void parseMsg(String msg)//����<close>��Ϣ�������д�
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
						for (User u : server.userList)
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
								catch (IOException e) //��������Ϣ��������ʧ��ʱ���򷵻�servererror��������
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
