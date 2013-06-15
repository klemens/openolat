package de.htwk.autolat.tools.XMLParser;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

public class Picture {
	
	public final String TYPE_PNG = "png";
	public final String TYPE_JPG = "jpg"; 
	
	
	
	private String type;
	private String pic;
	private String alt;
	private double width;
	private double height;
	private String unit;
	
	private String name;
	
	public Picture() {
	// TODO Auto-generated constructor stub
	}
	public Picture(String type, String pic, String alt, double width, double height, String unit) {
		setHeight(height);
		setAlt(alt);
		setPic(pic);
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
	
	public void setPic(String pic) {
		this.pic = pic;
	}
	
	public String getType() {
		return type;
	}
	
	public String getMIMEString() {
		return pic;
	}
	
	public InputStream getDecodedPictureStream()
	{
		InputStream inputStream = null, pictureStream = null;
		
		try
		{
			inputStream = new ByteArrayInputStream(getMIMEString().getBytes("UTF-8"));
		} 
		catch (UnsupportedEncodingException e)
		{
      e.printStackTrace();
		}
		
		//TODO: Is base64 always the right MIME type?		
		try {
			pictureStream = MimeUtility.decode(inputStream, "base64");
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pictureStream;
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
