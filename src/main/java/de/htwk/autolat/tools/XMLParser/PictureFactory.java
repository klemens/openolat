package de.htwk.autolat.tools.XMLParser;

public class PictureFactory {
	
	private static PictureFactory INSTANCE;
	
	private PictureFactory() {
	}
	
	public synchronized static PictureFactory getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new PictureFactory();
		}
		return INSTANCE;
	}
	
	public Picture getPicture(Picture pic) {
		pic.setName("image" + Math.round(pic.getBase64().hashCode() * Math.random()) + "." + pic.getType());
		return pic;
	}
}
