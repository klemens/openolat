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
package org.olat.modules.lecture.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureStatisticsSearchParameters {
	
	private String login;
	private List<String> bulkIdentifiers;
	private Map<String,String> userProperties;
	
	private Date startDate;
	private Date endDate;
	private RepositoryEntryLifecycle lifecycle;
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public RepositoryEntryLifecycle getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(RepositoryEntryLifecycle lifecycle) {
		this.lifecycle = lifecycle;
	}

	public List<String> getBulkIdentifiers() {
		return bulkIdentifiers;
	}

	public void setBulkIdentifiers(List<String> bulkIdentifiers) {
		this.bulkIdentifiers = bulkIdentifiers;
	}

	public Map<String, String> getUserProperties() {
		return userProperties;
	}

	public void setUserProperties(Map<String, String> userProperties) {
		this.userProperties = userProperties;
	}
}
