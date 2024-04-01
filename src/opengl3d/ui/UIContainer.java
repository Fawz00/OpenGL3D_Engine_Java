package opengl3d.ui;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import opengl3d.utils.XMLParser;

public class UIContainer {
	Object root;
	
	public UIContainer(Element main) {
		main.getNodeName();
		NodeList mainC = main.getChildNodes();
	}
	public UIContainer(String e) {
		this(XMLParser.parse(e));
	}

	public static void render() {
		
	}

}
