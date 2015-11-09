package de.unileipzig.xman.protocol.tables;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.protocol.Protocol;

public class ProtocolTableModel extends DefaultTableDataModel<Protocol> {
	
	private int columnCount = 0;
	private Locale locale;
	private Translator translator;

	public static final String EXAM_LAUNCH = "launch.exam";

	/**
	 * @param protocols - the list of protocols to display
	 * @param locale - the local of the user
	 */
	public ProtocolTableModel(List<Protocol> protocols, Locale locale) {
		super(protocols);
		
		this.locale = locale;
		translator = Util.createPackageTranslator(ElectronicStudentFile.class, locale);
	}
	
	public int getColumnCount() {
		return columnCount;
	}

	public Object getValueAt(int row, int col) {
		Protocol protocol = getObject(row);
		
		switch(col) {
			case 0:
				String name = protocol.getExam().getName();
				if(ExamDBManager.getInstance().isClosed(protocol.getExam())) {
					name += " [" + translator.translate("ProtocolTableModel.status.archived") + "]";
				}
				return name;
			
			case 1:
				return protocol.getAppointment().getDate();
			
			case 2:
				return protocol.getAppointment().getPlace();
			
			case 3:
				return protocol.getComments();
			
			case 4:
				return protocol.getGrade();
			
			case 5:
				if(protocol.getEarmarked()) {
					return translator.translate("ProtocolTableModel.status.earmarked");
				} else {
					return translator.translate("ProtocolTableModel.status.subscribed");
				}
		}
		
		return "";
	}
	
	/**
	 * initializes the table
	 */
	public void setTable(TableController tableCtr) {
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.examName", columnCount++, EXAM_LAUNCH, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.date", columnCount++, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.loc", columnCount++, null, locale));
		DefaultColumnDescriptor comments = new DefaultColumnDescriptor("ProtocolTableModel.header.comment", columnCount++, null, locale);
		comments.setEscapeHtml(EscapeMode.none);
		tableCtr.addColumnDescriptor(comments);
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolTableModel.header.mark", columnCount++, null, locale));
		
		DefaultColumnDescriptor status = new DefaultColumnDescriptor("ProtocolTableModel.header.status", columnCount++, null, locale);
		status.setEscapeHtml(EscapeMode.none);
		tableCtr.addColumnDescriptor(status);
		
	}
}