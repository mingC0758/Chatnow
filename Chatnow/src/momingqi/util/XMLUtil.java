package momingqi.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import momingqi.client.Friend;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class XMLUtil
{
	/**
	 * 构建客户端向服务器端发送的登陆数据的xml字符串（账户，密码）
	 * @return
	 */
	public final static String constructLoginXML(String id, String pwd)
	{
		String xml = String.format("<login id=\"%s\" pwd=\"%s\"/>", id, pwd);
		return xml;
	}
	
	/**解析登陆xml，并返回到字符串id和pwd
	 * 
	 * @param xml
	 * @param id
	 * @param pwd
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public final static String[] parseLoginXML(String xml) throws SAXException, IOException, ParserConfigurationException
	{
		InputStream in = new ByteArrayInputStream(xml.getBytes());
		SAXParser parser = SAXParserFactory.newInstance()
				.newSAXParser();

		LoginXMLHandler h = new LoginXMLHandler();
		parser.parse(in, h);
		
		return h.u;
	}
	
	/**
	 * 从usersxml里获取id对应的pwd
	 * @return 返回pwd
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public final static String getPwd(File usersxml, String id) throws SAXException, IOException, ParserConfigurationException
	{
		SAXParser parser = SAXParserFactory.newInstance()
				.newSAXParser();

		UsersXMLHandler h = new UsersXMLHandler(id);
		parser.parse(usersxml, h);
		
		return h.cor_pwd;
	}
	
	public final static HashMap<String, Friend> parseFriends(File xml) throws ParserConfigurationException, SAXException, IOException
	{
		HashMap<String, Friend> map = new HashMap<String, Friend>();
		SAXParser parser = SAXParserFactory.newInstance()
				.newSAXParser();
		
		parser.parse(xml, new FriendsXMLHandler(map));
		return map;
	}
}

class FriendsXMLHandler extends DefaultHandler
{
	public HashMap<String, Friend> map;
	
	
	public FriendsXMLHandler(HashMap<String, Friend> map)
	{
		this.map = map;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(qName.equals("user"))
		{
			String id = attributes.getValue("id");
			String nickname = attributes.getValue("nickname");
			String photo = attributes.getValue("photo");
			Friend f = new Friend(id, nickname, photo);
			map.put(id, f);
		}
	}
}

class LoginXMLHandler extends DefaultHandler
{
	public String[] u;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if (qName.equals("login"))
		{
			String id = attributes.getValue("id");
			String pwd = attributes.getValue("pwd");
			u = new String[2];
			u[0] = id;
			u[1] = pwd;
		}
	}
}

class UsersXMLHandler extends DefaultHandler
{
	private String id;
	public String cor_pwd;	//正确密码
	
	public UsersXMLHandler(String id)
	{
		this.id = id;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(qName.equals("user"))
		{
			if(attributes.getValue("id").equals(id))
			{
				cor_pwd = attributes.getValue("pwd");
			}
		}
	}
}