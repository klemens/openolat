package de.unileipzig.xman.protocol.archived.tables;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;

import de.unileipzig.xman.protocol.archived.ArchivedProtocol;

public class ArchivedProtocolTableModel extends DefaultTableDataModel<ArchivedProtocol> {
	private int columnCount = 0;

	public ArchivedProtocolTableModel(List<ArchivedProtocol> objects, Locale locale) {
		super(objects);
		
		setLocale(locale);
	}
	
	public void initTable(TableController table) {
		table.addColumnDescriptor(new DefaultColumnDescriptor("ArchivedProtocolTableModel.header.name", columnCount++, null, getLocale()));
		table.addColumnDescriptor(new DefaultColumnDescriptor("ArchivedProtocolTableModel.header.date", columnCount++, null, getLocale()));
		table.addColumnDescriptor(new DefaultColumnDescriptor("ArchivedProtocolTableModel.header.location", columnCount++, null, getLocale()));
		DefaultColumnDescriptor comments = new DefaultColumnDescriptor("ArchivedProtocolTableModel.header.comment", columnCount++, null, getLocale());
		comments.setEscapeHtml(EscapeMode.antisamy);
		table.addColumnDescriptor(comments);
		table.addColumnDescriptor(new DefaultColumnDescriptor("ArchivedProtocolTableModel.header.result", columnCount++, null, getLocale()));
		table.addColumnDescriptor(new DefaultColumnDescriptor("ArchivedProtocolTableModel.header.studyPath", columnCount++, null, getLocale()));
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public Object getValueAt(int row, int col) {
		ArchivedProtocol protocol = getObject(row);

		switch(col) {
			case 0:
				return protocol.getName();

			case 1:
				return protocol.getDate();

			case 2:
				return protocol.getLocation();

			case 3:
				return protocol.getComment();

			case 4:
				return protocol.getResult();

			case 5:
				return protocol.getStudyPath();
		}

		return "";
	}
}
