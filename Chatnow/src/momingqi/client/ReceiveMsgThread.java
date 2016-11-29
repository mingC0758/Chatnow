package momingqi.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import momingqi.util.Util;

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
				msg = Util.readFromInputStream(in);	//����
				System.out.println("���յ���Ϣ:" + msg);
				parseMsg(msg);
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(mf, "��������Ͽ����ӣ�");//���ߴ���
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
				boolean isUserList = false;
				boolean isMsg = false;
				String msg_id;	//�����ߵ�id
				String msg;		//������Ϣ
				String[] ids = new String[20];
				int id_index = 0;
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes) throws SAXException
				{
					if(qName.equals("addOnlineUser"))	//���������û�
					{
						mf.addOnlineUser(attributes.getValue("id"));
					}
					else if(qName.equals("removeOnlineUser"))
					{
						mf.removeOnlineUser(attributes.getValue("id"));
					}
					else if(qName.equals("userlist"))
					{
						isUserList = true;
					}
					else if(qName.equals("user") && isUserList == true)
					{
						ids[id_index] = attributes.getValue("id");
						System.out.println("id:"+ids[id_index]);
						id_index++;
					}
					else if(qName.equals("msg"))
					{
						isMsg = true;
						msg_id = attributes.getValue("id");
					}
				}
				
				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException
				{
					if(isMsg == true)
					{
						//��ȡ��Ϣ
						msg = new String(ch, start, length);
					}
				}
				
				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException
				{
					if(qName.equals("userlist"))
					{
						isUserList = false;
						System.out.println("��ʼ�������б�");
						mf.initOnlineUser(ids);
					}
					else if(qName.equals("msg"))
					{
						isMsg = false;
						ChatFrame cf = mf.getChatFrame(msg_id);
						if(cf == null)	//����û�д�
						{
							//ѯ���û��Ƿ�����촰��
							Friend f = mf.getFriend(msg_id);	//��÷����ߵ�Friend����
							String str = "���ܵ����� " + f.nickname + "����Ϣ���Ƿ�鿴��";
							Icon icon = new ImageIcon(Util.ClientImagePath + f.photo);
							String[] options = {"�鿴", "����"};	//�Ի���ѡ��
							//�����Ի��������û��Ƿ�����촰��
							int result = JOptionPane.showOptionDialog(mf, str, "����Ϣ",
									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, icon, options, "�鿴");
							if (result == JOptionPane.YES_OPTION)
							{
								cf = mf.createChatFrame(f);
								cf.setMsgText(msg);		//��ʾ��Ϣ
								cf.logMsg(msg);	//��¼��Ϣ
							}
						}
						else
						{
							cf.setMsgText(msg);	//��ʾ������Ϣ
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
