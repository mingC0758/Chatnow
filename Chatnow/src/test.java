import java.awt.Dialog;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import momingqi.util.Util;

import org.xml.sax.SAXException;




public class test
{
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, InterruptedException
	{
		JFrame f = new JFrame();
		f.setVisible(true);
		f.pack();
		Thread.sleep(4000);
		f.add(new JLabel("123"));
		f.validate();
		f.pack();
	}
}
