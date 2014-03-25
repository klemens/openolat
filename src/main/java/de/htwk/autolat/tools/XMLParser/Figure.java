package de.htwk.autolat.tools.XMLParser;


public class Figure {
	
	private Picture pic;
	private OutputObject description;
	
	public Figure() {
	}
	
	public Figure(Picture picture) {
		setDescription(null);
		setPic(picture);
	}

	public Figure(Picture picture, OutputObject description) {
		setDescription(description);
		setPic(picture);
	}

	public void setDescription(OutputObject description) {
		this.description = description;
	}

	public OutputObject getDescription() {
		return description;
	}

	public void setPic(Picture pic) {
		this.pic = pic;
	}

	public Picture getPic() {
		return pic;
	}
	
	public String toString() {
		if(description != null) {
			return "<center><OLATPIC>" + pic.getName() + "</OLATPIC>" + description.toString() + "</center>";
		} else {
			return "<center><OLATPIC>" + pic.getName() + "</OLATPIC></center>";
		}
	}
	
	
	
}
