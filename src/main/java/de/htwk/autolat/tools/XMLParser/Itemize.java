package de.htwk.autolat.tools.XMLParser;


public class Itemize extends OutputObject {
	
	public Itemize(OutputObject object) {
		super(object);
	}
	
	public String toString() {
		if(elements == null) {
			return "";
		}
		if(this.space == null) {
			return buildString("<div>");
		}
		return buildString("<div style=\" margin-left: " + space.getWidth() + space.getUnit() + "; magin-top: " + space.getHeight() + space.getUnit() + "\"" );
	}
	
	private String buildString(String output) {
		output += "<ol>";
		for(int i = 0; i < elements.size(); i++) {
			//Fallunterscheidungen
			//Link
			if(elements.get(i) instanceof Link) {
				output += "<li>";
				output += ((Link)elements.get(i)).toString();
				output += "</li>";
			}
			//Text
			if(elements.get(i) instanceof String) {
				output += "<li>";
				output += (String) elements.get(i);
				output += "</li>";
			}
			//Pre
			if(elements.get(i) instanceof Pre) {
				output += "<li>";
				output += ((Pre)elements.get(i)).toString();
				output += "</li>";
			}
			if(elements.get(i) instanceof Above) {
				output += "<li>";
				output += ((Above)elements.get(i)).toString();
				output += "</li>";
			}
			if(elements.get(i) instanceof Beside) {
				output += "<li>";
				output += ((Beside)elements.get(i)).toString();
				output += "</li>";
			}
			if(elements.get(i) instanceof Itemize) {
				output += "<li>";
				output += ((Itemize)elements.get(i)).toString();
				output += "</li>";
			}
			if(elements.get(i) instanceof Figure) {
				output += ((Figure)elements.get(i)).toString();
			}
		}
		output += "</ol></div>";
		return output;
	}
}
