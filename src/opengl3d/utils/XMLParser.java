package opengl3d.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

public class XMLParser {

	public XMLParser() {}

	public static Element parse(String xmlData) {
		try {
			System.out.println("Parsing XML...");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			ByteArrayInputStream input = new ByteArrayInputStream(xmlData.getBytes());
			Document document = builder.parse(input);

			Element root = document.getDocumentElement();
			System.out.println("Parsing XML finished.");
			return root;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Metode rekursif untuk memproses elemen XML
	public static void showAll(Element element) {
		showContents(element, 0, Integer.MAX_VALUE);
	}
	public static void showAll(Element element, int limit) {
		showContents(element, 0, limit);
	}
	private static void showContents(Element element, int count, int limit) {
		System.out.println();

		// Menampilkan nama elemen
		System.out.println("Element: " + element.getNodeName());
		
		// Mendapatkan atribut elemen
		if (element.hasAttributes()) {
			System.out.println("Attributes:");
			for (int i = 0; i < element.getAttributes().getLength(); i++) {
				Node attribute = element.getAttributes().item(i);
				System.out.println("\t" + attribute.getNodeName() + ": " + attribute.getNodeValue());
			}
		}

		// Mendapatkan anak-anak elemen
		NodeList children = element.getChildNodes();
		
		// Memproses setiap anak elemen
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			
			// Memeriksa apakah elemen anak adalah elemen yang valid
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				// Memanggil metode rekursif untuk memproses elemen anak
				if(count<limit) showContents((Element) child, ++count, limit);
			}
		}
	}

}
