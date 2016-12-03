package momingqi.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import momingqi.client.Friend;
import momingqi.server.User;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
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
	
	
	/**
	 * 解析登陆xml，并返回到字符串id(String[0])和pwd(String[1])数组
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
	 * 从users.xml中获取id对应用户的信息（id, pwd, nickname, photo）
	 * @param id
	 * @return 返回一个字符串数组（id, pwd, nickname, photo）
	 * @throws DocumentException 
	 */
	public final static String[] parseUsersXML(String id) throws DocumentException
	{
		Document doc = new SAXReader().read("./server_resources/users.xml");
		Node node = doc.selectSingleNode("//user[@id='" + id + "']");

		if(node == null) return null;		//查无此人
		Element e_user = (Element) node;
		String pwd = e_user.attributeValue("pwd");
		String nickname = e_user.attributeValue("nickname");
		String photo = e_user.attributeValue("photo");

		String[] result = new String[4];
		result[0] = id;
		result[1] = pwd;
		result[2] = nickname;
		result[3] = photo;
		
		return result;
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