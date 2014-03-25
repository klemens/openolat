package de.htwk.autolat.tools.XMLParser;


public class Beside extends OutputObject {
	
	public Beside(OutputObject object) {
		super(object);
	}
	
	public String toString() {
		if(elements == null) {
			return "";
		}
		if(this.space == null) {
			return buildString("<div>");
		}
		return buildString("<div style=\" margin-left: " + space.getWidth() + space.getUnit() + "; magin-top: " + space.getHeight() + space.getUnit() + "\">" );
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
			if(elements.get(i) instanceof Above) {
				output += ((Above)elements.get(i)).toString();
			}
			if(elements.get(i) instanceof Beside) {
				output += ((Beside)elements.get(i)).toString();
			}
			if(elements.get(i) instanceof Itemize) {
				output += ((Itemize)elements.get(i)).toString();
			}
			if(elements.get(i) instanceof Figure) {
				output += ((Figure)elements.get(i)).toString();
			}
		}
		output += "</div>";
		return output;
	}
}
