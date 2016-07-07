/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.portfolio;

import java.util.Date;
import java.util.List;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Section extends SectionRef, PortfolioElement {
	
	/**
	 * This value is generated
	 * @return
	 */
	public long getPos();
	
	public void setTitle(String title);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public Date getBeginDate();
	
	public void setBeginDate(Date date);
	
	public Date getEndDate();
	
	public void setEndDate(Date date);
	
	public SectionStatus getSectionStatus();
	
	/**
	 * Return the section of the template used to create this section or null.
	 * The section is lazily loaded.
	 * 
	 * @return
	 */
	public Section getTemplateReference();
	
	/**
	 * Return the list of entries / pages of the section. They are lazily loaded.
	 * @return
	 */
	public List<Page> getPages();
	
	/**
	 * The binder is lazily loaded.
	 * 
	 * @return
	 */
	public Binder getBinder();

}