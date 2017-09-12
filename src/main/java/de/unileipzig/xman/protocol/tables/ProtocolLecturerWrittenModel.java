package de.unileipzig.xman.protocol.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

public class ProtocolLecturerWrittenModel extends DefaultTableDataModel<Protocol> {

	public static String ACTION_USER = "user";
	public static String ACTION_MULTI_REGISTER = "multi.register";
	public static String ACTION_MULTI_EARMARK = "multi.earmark";
	public static String ACTION_MULTI_UNREGISTER = "multi.unregister";
	public static String ACTION_MULTI_EDIT_RESULT = "multi.edit.result";
	public static String ACTION_MULTI_EDIT_COMMENT = "multi.edit.comment";
	public static String ACTION_MULTI_MAIL = "multi.mail";

	
	private int columnCount;
	
	private Translator translator;
	private Exam exam;

	public ProtocolLecturerWrittenModel(Exam exam, Locale locale) {
		super(new ArrayList<Protocol>());

		setLocale(locale);
		this.translator = Util.createPackageTranslator(Exam.class, locale);
		this.exam = exam;
		
		update();
	}
	
	public void update() {
		setObjects(ProtocolManager.getInstance().findAllProtocolsByExam(exam));
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Protocol proto = getObject(row);
		
		switch(col) {
			case 0: return proto.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + proto.getIdentity().getUser().getProperty(UserConstants.LASTNAME, null);
			case 1: return proto.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
			case 2: return proto.getStudyPath();
			case 3: return proto.getGrade();
			case 4: return proto.getComments();
			case 5:	if(proto.getEarmarked()) {
						return translator.translate("ProtocolLecturerWrittenModel.status.earmarked");
					} else {
						return translator.translate("ProtocolLecturerWrittenModel.status.registered");
					}
		}
		
		return null;
	}
	
	public void createColumns(TableController tableController) {
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolLecturerWrittenModel.header.name", 0, ACTION_USER, getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolLecturerWrittenModel.header.matrikel", 1, null, getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolLecturerWrittenModel.header.studypath", 2, null, getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("ProtocolLecturerWrittenModel.header.result", 3, null, getLocale()));
		DefaultColumnDescriptor comment = new DefaultColumnDescriptor("ProtocolLecturerWrittenModel.header.comment", 4, null, getLocale());
		comment.setEscapeHtml(EscapeMode.antisamy);
		tableController.addColumnDescriptor(comment);
				
		DefaultColumnDescriptor status = new DefaultColumnDescriptor("ProtocolLecturerWrittenModel.header.status", 5, null, getLocale()) {
			/**
			 * Sort order: registered, earmarked
			 */
			@Override
			public int compareTo(final int rowa, final int rowb) {
				Protocol protocolA = getObject(rowa);
				Protocol protocolB = getObject(rowb);
				
				int scoreA = (protocolA.getEarmarked() ? 1 : 0);
				
				int scoreB = (protocolB.getEarmarked() ? 1 : 0);
				
				// Order by matrikel (inverse) if equal
				if(scoreA == scoreB) {
					try {
						int matrikelA = Integer.parseInt(protocolA.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
						int matrikelB = Integer.parseInt(protocolB.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
						return matrikelB - matrikelA;
					} catch(NumberFormatException e) {
						// should not occur
						return 0;
					}
				}
				
				return scoreA - scoreB;
			}
		};
		status.setEscapeHtml(EscapeMode.none);
		tableController.addColumnDescriptor(status);
		
		columnCount = 6;
		
		tableController.addMultiSelectAction("ProtocolLecturerWrittenModel.multi.register", ACTION_MULTI_REGISTER);
		tableController.addMultiSelectAction("ProtocolLecturerWrittenModel.multi.earmark", ACTION_MULTI_EARMARK);
		tableController.addMultiSelectAction("ProtocolLecturerWrittenModel.multi.unregister", ACTION_MULTI_UNREGISTER);
		tableController.addMultiSelectAction("ProtocolLecturerWrittenModel.multi.edit.result", ACTION_MULTI_EDIT_RESULT);
		tableController.addMultiSelectAction("ProtocolLecturerWrittenModel.multi.edit.comment", ACTION_MULTI_EDIT_COMMENT);
		tableController.addMultiSelectAction("ProtocolLecturerWrittenModel.multi.mail", ACTION_MULTI_MAIL);
	}

}
