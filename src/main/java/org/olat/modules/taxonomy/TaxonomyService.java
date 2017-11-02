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
package org.olat.modules.taxonomy;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TaxonomyService {
	
	public Taxonomy createTaxonomy(String identifier, String displayName, String description, String externalId);
	
	/**
	 * Reload a taxonomy
	 * @param ref The reference of the taxonomy
	 * @return A freshly loaded taxonomy with its base group
	 */
	public Taxonomy getTaxonomy(TaxonomyRef ref);
	
	public Taxonomy updateTaxonomy(Taxonomy taxonomy);
	
	/**
	 * @return The list of taxonomy trees available in the system.
	 */
	public List<Taxonomy> getRootTaxonomyList();
	

	public TaxonomyLevel createTaxonomyLevel(String identifier, String displayName, String description, String externalId,
			TaxonomyLevelManagedFlag[] flags, TaxonomyLevel parent, Taxonomy taxonomy);
	
	/**
	 * @param ref The root taxonomy (optional)
	 * @return A list of levels
	 */
	public List<TaxonomyLevel> getTaxonomyLevels(TaxonomyRef ref);
	
	public TaxonomyLevel getTaxonomyLevel(TaxonomyLevelRef ref);
	
	public List<TaxonomyLevel> getTaxonomyLevelParentLine(TaxonomyLevel taxonomyLevel, Taxonomy taxonomy);
	
	public TaxonomyLevel updateTaxonomyLevel(TaxonomyLevel level);
	
	
	public VFSContainer getDocumentsLibrary(TaxonomyLevel level);
	
	public VFSContainer getDocumentsLibrary(Taxonomy taxonomy);
	
	public VFSContainer getTaxonomyInfoPageContainer(Taxonomy taxonomy);
	
	

	public TaxonomyLevelType createTaxonomyLevelType(String identifier, String displayName, String description, String externalId, Taxonomy taxonomy);
	
	public TaxonomyLevelType getTaxonomyLevelType(TaxonomyLevelTypeRef ref);

	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType levelType);
	
	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType levelType, List<TaxonomyLevelType> allowSubTypes);
	
	/**
	 * Add directly an allowed taxonomy level type to the specified taxonomy level type.
	 * @param levelType The taxonomy level type to enhance
	 * @param allowSubType The taxonomy level type to allow
	 */
	public void taxonomyLevelTypeAllowSubType(TaxonomyLevelType levelType, TaxonomyLevelType allowSubType);
	
	/**
	 * Remove directly an allowed sub type.
	 * 
	 * @param levelType The parent taxonomy level type
	 * @param disallowSubType The taxonomy level type to remove from the allowed list
	 */
	public void taxonomyLevelTypeDisallowSubType(TaxonomyLevelType levelType, TaxonomyLevelType disallowSubType);
	
	/**
	 * The available types for a specific taxonomy.
	 * 
	 * @param taxonomy The taxonomy (mandatory)
	 * @return A list of taxonomy level types
	 */
	public List<TaxonomyLevelType> getTaxonomyLevelTypes(TaxonomyRef taxonomy);
	
	/**
	 * Has some competence in a taxonomy.
	 * 
	 * @param taxonomy The taxonomy (mandatory)
	 * @return true if some competence was found.
	 */
	public boolean hasTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity);
	

	public TaxonomyCompetence getTaxonomyCompetence(TaxonomyCompetenceRef competence);
	
	public List<TaxonomyCompetence> getTaxonomyCompetences(IdentityRef identity, TaxonomyCompetenceTypes... types);
	
	/**
	 * Get the competences in a taxonomy tree of the specified user.
	 * 
	 * @param taxonomy The taxonomy (mandatory)
	 * @return true if some competence was found.
	 */
	public List<TaxonomyCompetence> getTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity);
	
	/**
	 * @param taxonomy The taxonomy (mandatory)
	 * @param identity The user to check (mandatory)
	 * @param competences The list of competences to search
	 * @return true if the user has some of the specified competence in the taxonomy tree
	 */
	public boolean hasCompetence(TaxonomyRef taxonomy, IdentityRef identity, TaxonomyCompetenceTypes... competences);
	
	/**
	 * The competence at a specified level of the taxonomy tree.
	 * @param taxonomyLevel The taxonomy level (mandatory)
	 * @return A list of competences
	 */
	public List<TaxonomyCompetence> getTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel);
	
	/**
	 * The competences at a specific level for the specified user.
	 * 
	 * @param taxonomyLevel The taxonomy level (mandatory)
	 * @param identity The user (mandatory)
	 * @return A list of taxonomy competences
	 */
	public List<TaxonomyCompetence> getTaxonomyLevelCompetences(TaxonomyLevelRef taxonomyLevel, IdentityRef identity);
	
	/**
	 * Add a specific competence to a user.
	 * 
	 * @param taxonomyLevel
	 * @param identities
	 * @param comptence
	 */
	public TaxonomyCompetence addTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel, Identity identity, TaxonomyCompetenceTypes competence);

	/**
	 * Delete the competence
	 * 
	 * @param competence The competence to remove
	 */
	public void removeTaxonomyLevelCompetence(TaxonomyCompetence competence);
	
	/**
	 * 
	 * @param action
	 * @param before
	 * @param after
	 * @param message
	 * @param taxonomy
	 * @param competence
	 * @param assessedIdentity
	 * @param author
	 */
	public void auditLog(TaxonomyCompetenceAuditLog.Action action, String before, String after, String message,
			TaxonomyRef taxonomy, TaxonomyCompetence competence,
			IdentityRef assessedIdentity, IdentityRef author);
	
	public String toAuditXml(TaxonomyCompetence competence);

}