package de.htwk.autolat.tools.XMLParser;


import java.net.MalformedURLException;
import java.net.URL;

public class Link {
	
	private String url;
	private String name;
	private String alt;
	
	public Link() {
		//nothing to do
	}

	
	public Link(String url, String name, String alt) {
		setURL(url);
		setName(name);
		setAlt(alt);
	}
	
	public void setURL(String url) {
		this.url = url;
	}
	
	public String getURL() {
		return url;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public String getAlt() {
		return alt;
	}
	
	public String toString() {
		return "<a alt=\""+ alt + "\" href=\"" + url + "\" target=\"_blank\">" + name + "</a>";
	}
}
