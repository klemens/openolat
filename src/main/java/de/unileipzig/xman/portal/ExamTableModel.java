/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package de.unileipzig.xman.portal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;

import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.protocol.Protocol;

/**
 * Description:<br>
 * Table model to display a list of exams
 * <P>
 * Initial Date: 12.08.2005 <br>
 * 
 * @author blutz
 */
public class ExamTableModel extends DefaultTableDataModel {

	public static final String CMD_LAUNCH = "cmd.launch";
	private int COLUMN_COUNT;
	private List<Protocol> entries;
	private Locale locale;
	private boolean miniMode;
	
	/**
	 * @param list of efficiencyStatements
	 */
	public ExamTableModel(Locale locale, List<Protocol> protocols, boolean miniMode) {
		super(protocols);
		
		this.locale = locale;
		this.entries = protocols;
		this.miniMode = miniMode;
		this.COLUMN_COUNT = miniMode ? 2 : 3;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		
		return this.COLUMN_COUNT;
	}
	
	public int getRowCount() {
		
		return entries.size();
	}
	
	/**
	 * @param row
	 * @return the exam at the given row
	 */
	public Protocol getEntryAt(int row) {

		return entries.get(row);
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		Protocol proto = (Protocol) objects.get(row);
		
		switch (col) {
			case 0:	return ExamDBManager.getInstance().getExamName(proto.getExam());
			
			case 1:	return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(proto.getAppointment().getDate())
											+ ", "
											+ proto.getAppointment().getPlace();
			
			case 2:	return proto.getGrade();
			
			default: return "ERROR";
		}
	}
	
	/**
	 * Return a list of protocols for this bitset
	 * @param objectMarkers
	 * @return the list of protocols
	 */
	public List<Protocol> getProtocols(BitSet objectMarkers) {
		
		List<Protocol> results = new ArrayList<Protocol>();
		for(int i=objectMarkers.nextSetBit(0); i >= 0; i=objectMarkers.nextSetBit(i+1)) {
			Object elem = (Object) getObject(i);
			results.add((Protocol)elem);
		}
		return results;
	}
	
	/**
	 * Initializes the table and sets the table controller.
	 * @param tableCtr
	 */
	public void setTable(TableController tableCtr) {
		
		if ( miniMode ) tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.header.exam", 0, CMD_LAUNCH, locale));
		else tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.header.exam", 0, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.header.appointment", 1, null, locale));
		if ( !miniMode ) tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.header.mark", 2, null, locale));
	}
}