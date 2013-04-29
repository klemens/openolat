package de.unileipzig.xman.studyPath;


import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;

public interface StudyPath extends ModifiedInfo, CreateInfo, Persistable, OLATResourceable {

	public static String ORES_TYPE_NAME = OresHelper.calculateTypeName(StudyPath.class);
	
	public String getName();
	public void setName(String name);
}
