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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package de.htwk.autolat.TaskModule;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.htwk.autolat.BBautOLAT.EditConnectionController;
import de.htwk.autolat.ServerConnection.ServerConnectionTableModel;

/**
 * Description:<br>
 * TODO: Tom Class Description for ServerConnectionTableModel
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public class TaskModuleTableModel extends DefaultTableDataModel implements TableDataModel {
	
	private static final String PACKAGE = Util.getPackageName(TaskModuleTableModel.class);
	
	private final int COLUMN_COUNT = 7;
	
	private PackageTranslator translator;
	
	/**
	 * @param objects
	 */
	public TaskModuleTableModel(List objects, Locale locale) {
		
		super(objects);
		this.setLocale(locale);
		this.translator = new PackageTranslator(PACKAGE, locale);
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		
		Object[] wrapper = (Object[]) objects.get(row);		
		TaskModule taskModule = (TaskModule) wrapper[0];
		
		switch(col) {
			case 0:
				return taskModule.getKey();
			case 1:
				return row+1;
			case 2:
				return (taskModule.getTaskDuration() == 0 ? translator.translate("label.tablemodel.taskmodule.notset") :
					TaskModuleManagerImpl.getInstance().getDurationValueWithLabels(taskModule.getTaskDuration(), getLocale()));
			case 3:
				return (taskModule.getTaskEndDate() == null ? translator.translate("label.tablemodel.taskmodule.notset") :
					taskModule.getTaskEndDate());
			case 4:
				return (taskModule.getMaxCount() == 0 ? translator.translate("label.tablemodel.taskmodule.notset") :
					taskModule.getMaxCount());
			case 5:
				return translator.translate("label.tablemodel.taskmodule.edit");
			case 6:
				return translator.translate("label.tablemodel.taskmodule.delete");
			default:
				return "unknown";
		}
	}
	
	public void setEntries (List objects) {
		this.objects = objects;
	}
	
	public TaskModule getTaskModuleAtRow (int row) {
		
		Object[] wrapper = (Object[]) objects.get(row);		
		TaskModule taskModule = (TaskModule) wrapper[0];
		return taskModule;
	}

}
