package de.htwk.autolat.tools.XMLParser;


public class Space {
	
	public final String UNIT_EM = "em";
	public final String UNIT_PX = "px";
	
	private double height;
	private double width;
	private String unit;
	
	public Space() {
	// TODO Auto-generated constructor stub
	}
	
	public Space(double height, double width, String unit) {
		setHeight(height);
		setWidth(width);
		setUnit(unit);
	}
	
	public void setUnit(String unit) {
		if(unit.equals(UNIT_EM) || unit.equals(UNIT_PX)) {
			this.unit = unit;
		}
		else {
			System.err.println(" [Error] Illegal unit");
		}
	}
	
	public void setHeight(double height) {
		this.height = height;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public double getHeight() {
		return height;
	}
	
	public double getWidth() {
		return width;
	}
}
