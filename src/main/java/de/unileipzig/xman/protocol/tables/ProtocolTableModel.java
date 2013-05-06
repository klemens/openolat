package de.unileipzig.xman.protocol.tables;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.studyPath.StudyPath;
import de.unileipzig.xman.studyPath.StudyPathManager;

public class ProtocolTableModel extends DefaultTableDataModel {
	
	private List<Protocol> entries;
	private int COLUMN_COUNT;
	private Locale locale;
	private boolean showScores;
	private boolean showExamName;
	private boolean showEsfLink;

	public static final String COMMAND_VCARD = "show.vcard";
	public static final String EXAM_LAUNCH = "launch.exam";
	public static final String ESF_OPEN = "launch.esf";

	/**
	 * default constructor for this table model
	 * 
	 * @param locale - the local of the user
	 * @param protocols - the list of protocols to display
	 * @param showScores - show the column grades
	 * @param showExamName - the the column examName
	 * @param ureq - the UserRequest to identify the users Roles
	 */
	public ProtocolTableModel(Locale locale, List<Protocol> protocols, boolean showScores, boolean showExamName, boolean showEsfLink) {
		super(protocols);
		
		this.locale = locale;
		this.showScores = showScores;
		this.showExamName = showExamName;
		this.showEsfLink = showEsfLink;
		this.entries = protocols; 
		this.COLUMN_COUNT = showScores ? 7 : 6;
		if ( showExamName ) this.COLUMN_COUNT++;
	}
	
	public int getColumnCount() {
		
		return COLUMN_COUNT;
	}

	public int getRowCount() {
		
		return entries.size();
	}

	/**
	 * 
	 * @param row
	 * @return
	 */
	public Protocol getEntryAt(int row){
		
		return entries.get(row);
	}
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public String getValueAt(int row, int col) {
		
		Protocol proto = this.getEntryAt(row);
		switch(col) {
			case 0: return proto.getIdentity().getName();
			
			case 1: return proto.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER , null);
			
			case 2: return proto.getIdentity().getUser().getProperty(UserConstants.LASTNAME, null) 
							+ ", " + proto.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null);
			
			case 3: return proto.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, locale);
			
			case 4: return DateFormat.getDateTimeInstance(
						DateFormat.SHORT, DateFormat.SHORT, locale).format(proto.getAppointment().getDate())
					+ ", "
					+ proto.getAppointment().getPlace();
			
			case 5: return proto.getGrade();
			
			//s. CommentEntryTableModel
			case 6: return proto.getComments();
			
			case 7: return proto.getExam().getName();
			
			default: return "";
			
		}
	}
	
	/**
	 * sets the entries of the table
	 * @param entries - a list of protocols
	 */
	public void setEntries(List<Protocol> entries){
		
		this.entries = entries;
		this.objects=entries;
	}
	
	/**
	 * initializes the table
	 */
	public void setTable(TableController tableCtr) {
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.login", 0, COMMAND_VCARD, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.matrikel", 1, showEsfLink ? ESF_OPEN : null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.name", 2, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.studyPath", 3, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.appointment", 4, null, locale));
		if ( this.showScores ) tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.mark", 5, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.comment", 6, null, locale));
		if ( this.showExamName ) tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.examName", 7, EXAM_LAUNCH, locale));
	}
	
	/**
	 * Return a list of protocols for this bitset
	 * @param objectMarkers
	 * @return
	 */
	public List<Protocol> getProtocols(BitSet objectMarkers) {
		
		List<Protocol> results = new ArrayList<Protocol>();
		for( int i = objectMarkers.nextSetBit(0) ; i >= 0; i = objectMarkers.nextSetBit( i + 1 ) ) {
			Object elem = (Object) getObject(i);
			results.add((Protocol)elem);
		}
		return results;
	}
	
	/**
	 * @param rowid
	 * @return The Protocol at the given position in the table
	 */
	public Protocol getProtocolAt(int rowid) {
		
		Object[] co = (Object[])getObject(rowid);
		Protocol proto = (Protocol) co[0];
		return proto;
	}
}