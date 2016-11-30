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
		System.out.println(doc.getRootElement().getName());
		
	}
}
