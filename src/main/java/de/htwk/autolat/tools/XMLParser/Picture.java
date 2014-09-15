package de.htwk.autolat.tools.XMLParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

public class Picture {
	
	public final String TYPE_PNG = "png";
	public final String TYPE_JPG = "jpg"; 

	private String type;
	private String base64;
	private String alt;
	private double width;
	private double height;
	private String unit;
	
	private String name;
	
	public Picture(String type, String base64, String alt, double width, double height, String unit) {
		setHeight(height);
		setAlt(alt);
		setBase64(base64);
		setType(type);
		setUnit(unit);
		setWidth(width);
	}
	
	public void setType(String type) {
		if(type.equals(TYPE_JPG) || type.equals(TYPE_PNG)) {
			this.type = type;
		}
		else {
			System.err.println(" [Error] Illegal type");
		}
	}

	public String getMimeType() {
		if(type.equals(TYPE_JPG)) {
			return "image/jpeg";
		} else if(type.equals(TYPE_PNG)) {
			return "image/png";
		}
		return "application/octet-stream";
	}

	public void setBase64(String pic) {
		this.base64 = pic;
	}
	
	public String getBase64() {
		return base64;
	}
	
	public String getType() {
		return type;
	}
	
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public String getAlt() {
		return alt;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getWidth() {
		return width;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	public double getHeight() {
		return height;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getUnit() {
		return unit;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
