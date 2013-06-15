package de.htwk.autolat.tools.XMLParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AutotoolServer {
	
	private String version;
	private URL url;
	private List<URL> alternativURLs;
	private String name;
	
	public AutotoolServer() {
		this(null, null, null, null);
	}
	
	public AutotoolServer(String name, String version, URL url, List<URL> altURL) {
		setUrl(url);
		setVersion(version);
		setName(name);
		if(altURL == null) {
			altURL = new ArrayList<URL>();
		}
		setAlternativURL(altURL);
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}
	
	public void setUrl(String url) throws MalformedURLException {
		this.url = new URL(url);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<URL> getAlternativURL() {
		return alternativURLs;
	}

	public void setAlternativURL(List<URL> alternativURL) {
		this.alternativURLs = alternativURL;
	}
	
	public void addAlternativURL(String alternativURL) throws MalformedURLException {
		this.alternativURLs.add(new URL(alternativURL));
	}
}
