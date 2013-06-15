package de.htwk.autolat.tools.XMLParser;


public class Pre {
	
	private String preText;
	
	public Pre() {
		//nothing to do
	}
	
	public Pre(String pre) {
		setPre(pre);
	}
	
	public void setPre(String pre) {
		this.preText = pre;
	}
	
	public String getPre() {
		return this.preText;
	}
	
	public String toString() {
		return "<pre>" + preText + "</pre>";
	}

}
