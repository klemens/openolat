package de.htwk.autolat.tools.XMLParser;


import java.util.ArrayList;
import java.util.List;


/**
 * 
 * Description:<br>
 * This Object represent the XML File.
 * grammar
 *{
 *   start = Output
 *
 *   Output
 *       = element Pre     { text }
 *       | element Text    { text }
 *       | element Image   { attribute type { string "png" | string "jpg" },
 *                           attribute alt  { string },
 *                           xsd:base64Binary }
 *       | element Link    { attribute href { xsd:anyURI },
 *                           text }
 *       | element Above   { Output* }
 *       | element Beside  { Output* }
 *       | element Itemize { Output* }
 *       | element Space   { attribute width  { xsd:double },
 *                           attribute height { xsd:double },
 *                           attribute unit   { string "px" | string "em" } }
 *}
 * <P>
 * Initial Date:  14.11.2009 <br>
 * @author Joerg
 */
public class OutputObject {
	
	@SuppressWarnings("unchecked")
	protected List elements;
	
	protected List<Link> link;
	protected List<Pre> pre;
	protected List<String> text;
	protected List<Figure> figures;
	protected List<Above> above;
	protected List<Beside> beside;
	protected List<Itemize> itemize;
	protected Space space;
	
	private List<Picture> pictures;

	public OutputObject() {
		//nothing to do
	}
	
	public OutputObject(OutputObject output) {
		link = output.getLink();
		pre = output.getPre();
		text = output.getText();
		figures = output.getFigures();
		above = output.getAbove();
		beside = output.getBeside();
		itemize = output.getItemize();
		space = output.getSpace();
		elements = output.getElements();
		
	}
	/*public OutputObject(Link link, List<String> pre, Picture pic, OutputObject above, OutputObject beside, OutputObject itemize, Space space, String text) {
		setLink(link);
		setAbove(above);
		setBeside(beside);
		setItemize(itemize);
		setPic(pic);
		setPre(pre);
		setSpace(space);
		setText(text);
	}*/
	
	/*-----Getter-------------------------------------------*/
	public List getElements() {
		return elements;
	}
	
	public List<Link> getLink() {
		return link;
	}
	
	public List<Pre> getPre() {
		return pre;
	}
	
	public List<String> getText() {
		return text;
	}

	public List<Above> getAbove() {
		return above;
	}
	
	public List<Beside> getBeside() {
		return beside;
	}
	
	public List<Itemize> getItemize() {
		return itemize;
	}
	
	public Space getSpace() {
		return space;
	}
	
	public List<Figure> getFigures() {
		return figures;
	}
	/*-----Setter-------------------------------------------*/
	public void addFigure(Figure fig) {
		if(this.figures == null) {
			this.figures = new ArrayList<Figure>();
		}
		this.figures.add(fig);
		addElement(fig);
	}
	
	public void addLink(Link link) {
		if(this.link == null) {
			this.link = new ArrayList<Link>();
		}
		this.link.add(link);
		addElement(link);
	}
	
	public void addPre(Pre pre) {
		if(this.pre == null) {
			this.pre = new ArrayList<Pre>();
		}
		this.pre.add(pre);
		addElement(pre);
	}
	
	public void setPre(List<Pre> pre) {
		this.pre = pre;
	}

	public void addText(String text) {
		if(this.text == null) {
			this.text = new ArrayList<String>();
		}
		this.text.add(text);
		addElement(text);
	}

	public void addAbove(Above above) {
		if(this.above == null) {
			this.above = new ArrayList<Above>();
		}
		this.above.add(above);
		addElement(above);
	}
	
	public void addBeside(Beside beside) {
		if(this.beside == null) {
			this.beside = new ArrayList<Beside>();
		}
		this.beside.add(beside);
		addElement(beside);
	}
	
	public void addItemize(Itemize itemize) {
		if(this.itemize == null) {
			this.itemize = new ArrayList<Itemize>();
		}
		this.itemize.add(itemize);
		addElement(itemize);
	}
	
	public void setSpace(Space space) {
		this.space = space;
		addElement(space);
	}
	/*-------------------------------------------------------*/
	
	public boolean hasPic() {
		return figures == null ? false : true;
	}
	
	public String toString() {
		if(this.space == null) {
			return buildString("<div class=\"autolat\">");
		}
		return buildString("<div class=\"autolat\" style=\" margin-left: " + space.getWidth() + space.getUnit() + "; magin-top: " + space.getHeight() + space.getUnit() + "\">" );
	}
	
	private String buildString(String output) {
		for(int i = 0; i < elements.size(); i++) {
			//Fallunterscheidungen
			//Link
			if(elements.get(i) instanceof Link) {
				output += ((Link)elements.get(i)).toString();
			}
			//Text
			if(elements.get(i) instanceof String) {
				output += (String) elements.get(i);
			}
			//Pre
			if(elements.get(i) instanceof Pre) {
				output += ((Pre)elements.get(i)).toString();
			}
			//Above
			if(elements.get(i) instanceof Above) {
				output += ((Above)elements.get(i)).toString();
			}
			//Beside
			if(elements.get(i) instanceof Beside) {
				output += ((Beside)elements.get(i)).toString();
			}
			//Itemize
			if(elements.get(i) instanceof Itemize) {
				output += ((Itemize)elements.get(i)).toString();
			}
			//Add placeholder for pictures
			if(elements.get(i) instanceof Figure) {
				output += ((Figure)elements.get(i)).toString();
			}
		}
		output += "</div>";
		return output;
	}
	
	@SuppressWarnings("unchecked")
	private void addElement(Object element) {
		if(this.elements == null) {
			this.elements = new ArrayList();
		}
		this.elements.add(element);
	}

	public List<Picture> getPictures() {
		if(pictures == null) {
			pictures = new ArrayList<Picture>();
			
			if(this.figures != null) {
				for(int i = 0; i < figures.size(); i++) {
					pictures.add(figures.get(i).getPic());
				}
			}
			collectPictures(); 
		}
		return pictures;
	}

	private void collectPictures() {
		//System.out.println(elements.size());
		if(elements != null) {
			for(int i = 0; i < elements.size(); i++) {
				if(elements.get(i) instanceof OutputObject) {
					if( ((OutputObject)elements.get(i)).hasPic() ) {
						for(int j = 0; j < ((OutputObject)elements.get(i)).getPictures().size(); j++) {
							pictures.add(((OutputObject)elements.get(i)).getPictures().get(j));
						}
					}
				}
			}
		}
	}
	
	
}
