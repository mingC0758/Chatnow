import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;




public class test
{
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, InterruptedException, DocumentException
	{
		Document doc = new SAXReader().read("./server_resources/users.xml");
		Node n = doc.selectSingleNode("//user[@id='10001']");
		
			Element e_user = (Element)n;
			String nickname = e_user.attributeValue("nickname");
			String photo = e_user.attributeValue("photo");
			System.out.println(nickname + photo);
		
	}
}
