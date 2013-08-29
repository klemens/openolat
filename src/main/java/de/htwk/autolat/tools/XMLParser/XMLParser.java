package de.htwk.autolat.tools.XMLParser;


import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.olat.core.CoreSpringFactory;
import org.springframework.core.io.Resource;

public class XMLParser {
	
	public List<AutotoolServer> parsePropertiesFile() throws IOException, JDOMException {
		Resource res = CoreSpringFactory.getResources("classpath*:de/**/autolatProperties.xml")[0];
		Document doc = new SAXBuilder().build(res.getFile());
		Element root = doc.getRootElement();
		if(root.getName().equals("autolat")) {
			List elements = root.getChildren();
			for(int i = 0; i < elements.size(); i++) {
				Element ele = (Element) elements.get(i);
				if(ele.getName().equals("autotoolservers")) {
					return buildServer(ele.getChildren(), null, null); 
				}
			}			 
		} else {
			return null;
		}
		return null;
	} 
	
	public List<AutotoolServer> getServerListByNameAndVersion(String name, String version) throws JDOMException, IOException {
		Resource res = CoreSpringFactory.getResources("classpath*:de/**/autolatProperties.xml")[0];
		Document doc = new SAXBuilder().build(res.getFile());
		
		Element root = doc.getRootElement(); 
		 
		if(root.getName().equals("autolat")) {
			List elements = root.getChildren();
			for(int i = 0; i < elements.size(); i++) {
				Element ele = (Element) elements.get(i);
				if(ele.getName().equals("autotoolservers")) {
					return(buildServer(ele.getChildren(), name, version));
				}
			}			
		} else {
			return null;
		}
		return null;
	}
	
	private List<AutotoolServer> buildServer(List children,String name, String version) throws MalformedURLException {
		List<AutotoolServer> serverList = new ArrayList<AutotoolServer>();

		for(int i = 0; i < children.size(); i++) {
			Element aServer = (Element) children.get(i);
			AutotoolServer autotoolServer = new AutotoolServer();
			for(int j = 0; j < aServer.getChildren().size(); j++) {
				Element aChild = (Element)aServer.getChildren().get(j);
				if(aChild.getName().equals("version")) {
					autotoolServer.setVersion(aChild.getTextTrim());
				}
				if(aChild.getName().equals("url")) {
					autotoolServer.setUrl(aChild.getTextTrim());
				} 
				if(aChild.getName().equals("name")) {
					autotoolServer.setName(aChild.getTextTrim());
				}
				if(aChild.getName().equals("alturl")) {
					autotoolServer.addAlternativURL(aChild.getTextTrim());
				}
			}
			if( (autotoolServer.getVersion().equals(version) && autotoolServer.getName().equals(name)) || (version == null && name == null) ) {   
				serverList.add(autotoolServer); 
			}
		}
		return serverList;
	}
	
	
	
	public OutputObject parseFile(String fileName) throws IOException, JDOMException {
		Document doc = new SAXBuilder().build(fileName);
		Element root = doc.getRootElement();
		ArrayList<Element> templist = new ArrayList<Element>();
		templist.add(root);
		return buildObject(new OutputObject(), templist);	
	}
	
	
	public OutputObject parseString(String xmlString) throws IOException, JDOMException {				
		StringReader xmlStringReader = new StringReader(xmlString);
		Document doc = new SAXBuilder().build( xmlStringReader );
		//get root node
		Element root = doc.getRootElement();		
		// quick (and dirty) workaround to pass a list of just the root-element 
		ArrayList<Element> templist = new ArrayList<Element>();
		templist.add(root);
		return buildObject(new OutputObject(), templist);
	}
	
	private OutputObject buildObject(OutputObject object, List elements) throws MalformedURLException {
		
		for(int i = 0; i < elements.size(); i++) {
			Element ele = (Element)elements.get(i);
			if(ele.getName().equals("Pre")) {
				object.addPre(new Pre(ele.getText()));
			}
			if(ele.getName().equals("Text")) {
				object.addText(ele.getText());
			}
			if(ele.getName().equals("Link")) {
				object.addLink(new Link( ele.getAttributeValue("href"), ele.getText(), ele.getAttributeValue("alt") ) );
			}
			if(ele.getName().equals("Space")) {
				object.setSpace(new Space( Double.valueOf(ele.getAttributeValue("height")), Double.valueOf(ele.getAttributeValue("width")), ele.getAttributeValue("unit")));
			}
			if(ele.getName().equals("Above")) {
				object.addAbove(new Above(buildObject(new OutputObject(), ele.getChildren())));
			}
			if(ele.getName().equals("Beside")) {
				object.addBeside(new Beside(buildObject(new OutputObject(), ele.getChildren())));
			}
			if(ele.getName().equals("Itemize")) {
				object.addItemize(new Itemize(buildObject(new OutputObject(), ele.getChildren())));
			}
			if(ele.getName().equals("Figure")) {
				Figure fig = buildFigure(ele.getChildren());
				object.addFigure(fig);
			}
		}
		return object;
	}

	private Figure buildFigure(List elements) throws MalformedURLException {
		Figure fig = new Figure();
		for(int i = 0; i < elements.size(); i++) {
			Element ele = (Element)elements.get(i);
			if(ele.getName().equals("Pre")) {
				fig.setDescription(new OutputObject());
				fig.getDescription().addPre(new Pre(ele.getText()));
			}
			if(ele.getName().equals("Text")) {
				fig.setDescription(new OutputObject());
				fig.getDescription().addText(ele.getText());
			}
			if(ele.getName().equals("Link")) {
				fig.setDescription(new OutputObject());
				fig.getDescription().addLink(new Link( ele.getAttributeValue("href"), ele.getText(), ele.getAttributeValue("alt") ) );
			}
			if(ele.getName().equals("Space")) {
				fig.setDescription(new OutputObject());
				fig.getDescription().setSpace(new Space( Double.valueOf(ele.getAttributeValue("height")), Double.valueOf(ele.getAttributeValue("width")), ele.getAttributeValue("unit")));
			}
			if(ele.getName().equals("Above")) {
				fig.setDescription(new OutputObject());
				fig.getDescription().addAbove(new Above(buildObject(new OutputObject(), ele.getChildren())));
			}
			if(ele.getName().equals("Beside")) {
				fig.setDescription(new OutputObject());
				fig.getDescription().addBeside(new Beside(buildObject(new OutputObject(), ele.getChildren())));
			}
			if(ele.getName().equals("Itemize")) {
				fig.setDescription(new OutputObject());
				fig.getDescription().addItemize(new Itemize(buildObject(new OutputObject(), ele.getChildren())));
			}
			if(ele.getName().equals("Image")) {
				fig.setPic(PictureFactory.getInstance().getPicture( new Picture(ele.getAttributeValue("type"),
						ele.getText(), 
						ele.getAttributeValue("alt"),
						Double.valueOf(ele.getAttributeValue("width")),
						Double.valueOf(ele.getAttributeValue("height")), 
						ele.getAttributeValue("unit"))));
			}
		}
		return fig;
	}
	
}