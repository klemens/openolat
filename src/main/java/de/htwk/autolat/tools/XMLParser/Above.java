package de.htwk.autolat.tools.XMLParser;


public class Above extends OutputObject {
	
	public Above(OutputObject object) {
		super(object);
	}
	
	public String toString() {
		if(elements == null) {
			return "";
		}
		if(this.space == null) {
			return buildString();
		}
		return "<div style=\" margin-left: " + space.getWidth() + space.getUnit() + "; magin-top: " + space.getHeight() + space.getUnit() + "\">" + buildString() + "</div>";
	}
	
	private String buildString() {
		String output = "";
		for(int i = 0; i < elements.size(); i++) {
			//Fallunterscheidungen
			//Link
			if(elements.get(i) instanceof Link) {
				output += "<p>";
				output += ((Link)elements.get(i)).toString();
				output += "</p>";
			}
			//Text
			if(elements.get(i) instanceof String) {
				output += "<p>";
				output += (String) elements.get(i);
				output += "</p>";
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
		return output;
	}
}
