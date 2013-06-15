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
		pic.setName(getName(pic.getMIMEString().hashCode()));
		return pic;
	}

	private String getName(int hash) {
		return "image"+Math.round(hash*Math.random());
	}
}
