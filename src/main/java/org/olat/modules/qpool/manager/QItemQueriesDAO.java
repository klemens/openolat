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
package org.olat.modules.qpool.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionItemView.OrderBy;
import org.olat.modules.qpool.model.ItemWrapper;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service do only the main queries for the question pool and mapped them
 * to the standardized QuestionItemView
 * 
 * 
 * Initial date: 12.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qItemQueriesDAO")
public class QItemQueriesDAO {
	
	@Autowired
	private DB dbInstance;
	
	public int countFavoritItems(SearchQuestionItemParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item.key)")
		  .append(" from questionitem item")
		  .append(" where exists (")
		  .append("   select mark.key from ").append(MarkImpl.class.getName()).append(" as mark")
		  .append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" )");
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", params.getIdentity().getKey());
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		return query.getSingleResult().intValue();
	}
	
	public List<QuestionItemView> getFavoritItems(SearchQuestionItemParams params, Collection<Long> inKeys,
			int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
		  .append(" (select count(sgmi.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=ownerGroup")
		  .append(" ) as owners,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		  .append(" ) as teacher,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		  .append(" ) as manager,")
		  .append(" (select count(pool2item.key) from qpool2item pool2item")
		  .append("    where pool2item.item.key=item.key")
		  .append("      and pool2item.editable is true")
		  .append(" ) as pools,")
		  .append(" (select count(shareditem.key) from qshareitem shareditem")
		  .append("    where shareditem.item.key=item.key")
		  .append("      and shareditem.editable is true")
		  .append(" ) as groups,")
		  .append(" (select count(rating.key) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append("     and rating.creator.key=:identityKey")
		  .append(" ) as ratings,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from questionitem item")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where exists (")
		  .append("   select mark.key from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" )");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null && !OrderBy.marks.name().equals(orderBy[0].getKey())) {
			appendOrderBy(sb, "item", orderBy);
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", params.getIdentity().getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			ItemWrapper itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor((Number)result[1])
					.setTeacher((Number)result[2])
					.setManager((Number)result[3])
					.setEditableInPool((Number)result[4])
					.setEditableInShare((Number)result[5])
					.setRatedByIdentity((Number)result[6])
					.setRating((Double)result[7])
					.setMarked(true)
					.create();
			views.add(itemWrapper);
		}
		return views;
	}
	
	public List<QuestionItemView> getItemsOfCollection(Identity identity, QuestionItemCollection collection, Collection<Long> inKeys,
			String format, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
		  .append(" (select count(sgmi.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=ownerGroup")
		  .append(" ) as owners,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		  .append(" ) as teacher,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		  .append(" ) as manager,")
		  .append(" (select count(pool2item.key) from qpool2item pool2item")
		  .append("    where pool2item.item.key=item.key")
		  .append("      and pool2item.editable is true")
		  .append(" ) as pools,")
		  .append(" (select count(shareditem.key) from qshareitem shareditem")
		  .append("    where shareditem.item.key=item.key")
		  .append("      and shareditem.editable is true")
		  .append(" ) as groups,")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" ) as marks,")
		  .append(" (select count(rating.key) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append("     and rating.creator.key=:identityKey")
		  .append(" ) as ratings,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from qcollection2item coll2item")
		  .append(" inner join coll2item.item item")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where coll2item.collection.key=:collectionKey");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(format)) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("collectionKey", collection.getKey())
				.setParameter("identityKey", identity.getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(format)) {
			query.setParameter("format", format);
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			ItemWrapper itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor((Number)result[1])
					.setTeacher((Number)result[2])
					.setManager((Number)result[3])
					.setEditableInPool((Number)result[4])
					.setEditableInShare((Number)result[5])
					.setMarked((Number)result[6])
					.setRatedByIdentity((Number)result[7])
					.setRating((Double)result[8])
					.create();
			views.add(itemWrapper);
		}
		return views;
	}
	
	public List<QuestionItemView> getItemsOfTaxonomyLevel(SearchQuestionItemParams params, Collection<Long> inKeys,
			int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
			.append(" (select count(sgmi.key) from ") .append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
			.append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=ownerGroup")
			.append(" ) as owners,")
		    .append(" (select count(competence.key) from ctaxonomycompetence competence")
		    .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		    .append("     and competence.identity.key=:identityKey")
		    .append("     and competence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		    .append(" ) as teacher,")
		    .append(" (select count(competence.key) from ctaxonomycompetence competence")
		    .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		    .append("     and competence.identity.key=:identityKey")
		    .append("     and competence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		    .append(" ) as manager,")
			.append(" (select count(pool2item.key) from qpool2item pool2item")
			.append("    where pool2item.item.key=item.key")
			.append("      and pool2item.editable is true")
			.append(" ) as pools,")
			.append(" (select count(shareditem.key) from qshareitem shareditem")
			.append("    where shareditem.item.key=item.key")
			.append("      and shareditem.editable is true")
			.append(" ) as groups,")
			.append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
			.append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
			.append(" ) as marks,")
			.append(" (select count(rating.key) from userrating as rating")
			.append("   where rating.resId=item.key and rating.resName='QuestionItem'")
			.append("     and rating.creator.key=:identityKey")
			.append(" ) as ratings,")
			.append(" (select avg(rating.rating) from userrating as rating")
			.append("   where rating.resId=item.key and rating.resName='QuestionItem'")
			.append(" ) as rating")
			.append(" from questionitem item")
			.append(" inner join fetch item.ownerGroup ownerGroup")
			.append(" inner join fetch item.taxonomyLevel taxonomyLevel")
			.append(" left join fetch item.type itemType")
			.append(" left join fetch item.educationalContext educationalContext")
			.append(" where taxonomyLevel.key=:taxonomyLevelKey");
		// Exclude QTI1.2 in review process.
		// This query is only used in review process, so exclude them always.
		sb.append(" and item.format<>'IMS QTI 1.2'");
		if (params.getQuestionStatus() != null) {
			sb.append(" and item.status=:questionStatus");
		}
		if (params.getOnlyAuthor() != null) {
			sb.append(" and exists (").append("select sgmi.key from ")
					.append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
					.append("   where sgmi.identity.key=:onlyAuthorKey and sgmi.securityGroup=item.ownerGroup")
					.append(" )");
		}
		if (params.getExcludeAuthor() != null) {
			sb.append(" and not exists (").append("select sgmi.key from ")
					.append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
					.append("   where sgmi.identity.key=:excludeAuthorKey and sgmi.securityGroup=item.ownerGroup")
					.append(" )");
		}
		if (params.isExcludeRated()) {
			sb.append(" and not exists (");
			sb.append(" select rating.key from userrating as rating");
			sb.append("  where rating.resId=item.key and rating.resName='QuestionItem'");
			sb.append("    and rating.creator.key=:identityKey)");
		}
		if (inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if (StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("taxonomyLevelKey", params.getTaxonomyLevelKey())
				.setParameter("identityKey", params.getIdentity().getKey());
		if (params.getQuestionStatus() != null) {
			query.setParameter("questionStatus", params.getQuestionStatus().toString());
		}
		if (params.getOnlyAuthor() != null) {
			query.setParameter("onlyAuthorKey", params.getOnlyAuthor().getKey());
		}
		if (params.getExcludeAuthor() != null) {
			query.setParameter("excludeAuthorKey", params.getExcludeAuthor().getKey());
		}
		if (inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if (StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		if (firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if (maxResults > 0) {
			query.setMaxResults(maxResults);
		}

		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for (Object[] result : results) {
			ItemWrapper itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor((Number)result[1])
					.setTeacher((Number)result[2])
					.setManager((Number)result[3])
					.setEditableInPool((Number)result[4])
					.setEditableInShare((Number)result[5])
					.setMarked((Number)result[6])
					.setRatedByIdentity((Number)result[7])
					.setRating((Double)result[8])
					.create();
			views.add(itemWrapper);
		}
		return views;
	}
	
	public int countItemsOfTaxonomyLevel(SearchQuestionItemParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item ")
		  .append(" inner join item.taxonomyLevel taxonomyLevel")
		  .append(" where taxonomyLevel.key=:taxonomyLevelKey");
		if(params.getQuestionStatus() != null) {
			sb.append(" and item.status=:questionStatus");
		}
		if (params.getOnlyAuthor() != null) {
			sb.append(" and exists (").append("select sgmi.key from ")
					.append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
					.append("   where sgmi.identity.key=:onlyAuthorKey and sgmi.securityGroup=item.ownerGroup")
					.append(" )");
		}
		if (params.getExcludeAuthor() != null) {
			sb.append(" and not exists (").append("select sgmi.key from ")
					.append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
					.append("   where sgmi.identity.key=:excludeAuthorKey and sgmi.securityGroup=item.ownerGroup")
					.append(" )");
		}
		if (params.isExcludeRated()) {
			sb.append(" and not exists (");
			sb.append(" select rating.key from userrating as rating");
			sb.append("  where rating.resId=item.key and rating.resName='QuestionItem'");
			sb.append("    and rating.creator.key=:identityKey)");
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("taxonomyLevelKey", params.getTaxonomyLevelKey());
		if(params.getQuestionStatus() != null) {
			query.setParameter("questionStatus", params.getQuestionStatus().toString());
		}
		if (params.getOnlyAuthor() != null) {
			query.setParameter("onlyAuthorKey", params.getOnlyAuthor().getKey());
		}
		if (params.getExcludeAuthor() != null) {
			query.setParameter("excludeAuthorKey", params.getExcludeAuthor().getKey());
		}
		if (params.isExcludeRated()) {
			query.setParameter("identityKey", params.getIdentity().getKey());
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		return query.getSingleResult().intValue();
	}

	public int countItemsByAuthor(SearchQuestionItemParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(item) from questionitem item ")
		  .append(" where exists (")
		  .append("   select sgmi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:authorKey and sgmi.securityGroup=item.ownerGroup")
		  .append(" )");	
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("authorKey", params.getAuthor().getKey());
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		return query.getSingleResult().intValue();
	}
	
	public List<QuestionItemView> getItemsByAuthor(SearchQuestionItemParams params,
			Collection<Long> inKeys, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		  .append(" ) as teacher,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		  .append(" ) as manager,")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" ) as marks,")
		  .append(" (select count(rating.key) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append("     and rating.creator.key=:identityKey")
		  .append(" ) as ratings,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from questionitem item")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where exists (")
		  .append("   select sgmi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:authorKey and sgmi.securityGroup=ownerGroup")
		  .append(" )");
		
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("authorKey", params.getAuthor().getKey())
				.setParameter("identityKey", params.getIdentity().getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			ItemWrapper itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor(true)
					.setTeacher((Number)result[1])
					.setManager((Number)result[2])
					.setMarked((Number)result[3])
					.setRatedByIdentity((Number)result[4])
					.setRating((Double)result[5])
					.create();
			views.add(itemWrapper);
		}
		return views;
	}
	
	public List<QuestionItemView> getSharedItemByResource(Identity identity, OLATResource resource, List<Long> inKeys,
			String format, int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item,  ")
		  .append(" (select count(sgmi.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=ownerGroup")
		  .append(" ) as owners,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		  .append(" ) as teacher,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		  .append(" ) as manager,")
		  .append(" shareditem.editable,")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" ) as marks,")
		  .append(" (select count(rating.key) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append("     and rating.creator.key=:identityKey")
		  .append(" ) as ratings,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from qshareitem shareditem")
		  .append(" inner join shareditem.item item")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where shareditem.resource.key=:resourceKey");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(format)) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("identityKey", identity.getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(format)) {
			query.setParameter("format", format);
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			ItemWrapper itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor((Number)result[1])
					.setTeacher((Number)result[2])
					.setManager((Number)result[3])
					.setEditableInShare((Boolean)result[4])
					.setMarked((Number)result[5])
					.setRatedByIdentity((Number)result[6])
					.setRating((Double)result[7])
					.create();
			views.add(itemWrapper);
		}
		return views;
	}
	
	public List<QuestionItemView> getItemsOfPool(SearchQuestionItemParams params, Collection<Long> inKeys,
			int firstResult, int maxResults, SortKey... orderBy) {
		StringBuilder sb = new StringBuilder();
		sb.append("select item,")
		  .append(" (select count(sgmi.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=ownerGroup")
		  .append(" ) as owners,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		  .append(" ) as teacher,")
		  .append(" (select count(competence.key) from ctaxonomycompetence competence")
		  .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		  .append("     and competence.identity.key=:identityKey")
		  .append("     and competence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		  .append(" ) as manager,")
		  .append("pool2item.editable,")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
		  .append(" ) as marks,")
		  .append(" (select count(rating.key) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append("     and rating.creator.key=:identityKey")
		  .append(" ) as ratings,")
		  .append(" (select avg(rating.rating) from userrating as rating")
		  .append("   where rating.resId=item.key and rating.resName='QuestionItem'")
		  .append(" ) as rating")
		  .append(" from qpool2item pool2item")
		  .append(" inner join pool2item.item item")
		  .append(" inner join fetch item.ownerGroup ownerGroup")
		  .append(" left join fetch item.type itemType")
		  .append(" left join fetch item.taxonomyLevel taxonomyLevel")
		  .append(" left join fetch item.educationalContext educationalContext")
		  .append(" where pool2item.pool.key=:poolKey");
		if(inKeys != null && inKeys.size() > 0) {
			sb.append(" and item.key in (:inKeys)");
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.append(" and item.format=:format");
		}
		appendOrderBy(sb, "item", orderBy);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("poolKey", params.getPoolKey())
				.setParameter("identityKey", params.getIdentity().getKey());
		if(inKeys != null && inKeys.size() > 0) {
			query.setParameter("inKeys", inKeys);
		}
		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			ItemWrapper itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor((Number)result[1])
					.setTeacher((Number)result[2])
					.setManager((Number)result[3])
					.setEditableInPool((Boolean)result[4])
					.setMarked((Number)result[5])
					.setRatedByIdentity((Number)result[6])
					.setRating((Double)result[7])
					.create();
			views.add(itemWrapper);
		}
		return views;
	}
	
	private void appendOrderBy(StringBuilder sb, String dbRef, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			switch(sortKey) {
				case "itemType":
					sb.append(dbRef).append(".type.type ");
					appendAsc(sb, asc);
					break;
				case "marks":
					sb.append("marks");
					appendAsc(sb, asc);
					break;
				case "rating":
					sb.append("rating");
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;
				case "keywords":
				case "coverage":
				case "additionalInformations":
					sb.append("lower(").append(dbRef).append(".").append(sortKey).append(")");
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;	
				default:
					sb.append(dbRef).append(".").append(sortKey);
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;
			}
		} else {
			sb.append(" order by item.key asc ");
		}
	}
	
	private final StringBuilder appendAsc(StringBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}
	
	public QuestionItemView getItem(Long itemKey, Identity identity, Long restrictToPoolKey, Long restrictToGroupKey) {
		if (itemKey == null || identity == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
			.append(" (select count(sgmi.key) from ") .append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
			.append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=ownerGroup")
			.append(" ) as owners,")
		    .append(" (select count(competence.key) from ctaxonomycompetence competence")
		    .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		    .append("     and competence.identity.key=:identityKey")
		    .append("     and competence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		    .append(" ) as teacher,")
		    .append(" (select count(competence.key) from ctaxonomycompetence competence")
		    .append("   where competence.taxonomyLevel.key = taxonomyLevel.key")
		    .append("     and competence.identity.key=:identityKey")
		    .append("     and competence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		    .append(" ) as manager,")
			.append(" (select count(pool2item.key) from qpool2item pool2item")
			.append("    where pool2item.item.key=item.key")
			.append("      and pool2item.editable is true");
		if (restrictToPoolKey != null) {
			sb.append(" and pool2item.pool.key=:restrictToPoolKey");
		}
		sb.append(" ) as pools,")
			.append(" (select count(shareditem.key) from qshareitem shareditem")
			.append("    where shareditem.item.key=item.key")
			.append("      and shareditem.editable is true");
		if (restrictToGroupKey != null) {
			sb.append(" and shareditem.resource=:restrictToGroupKey");
		}
		sb.append(" ) as groups,")
			.append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
			.append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
			.append(" ) as marks,")
			.append(" (select count(rating.key) from userrating as rating")
			.append("   where rating.resId=item.key and rating.resName='QuestionItem'")
			.append("     and rating.creator.key=:identityKey")
			.append(" ) as ratings,")
			.append(" (select avg(rating.rating) from userrating as rating")
			.append("   where rating.resId=item.key and rating.resName='QuestionItem'")
			.append(" ) as rating")
			.append(" from questionitem item")
			.append(" left join fetch item.ownerGroup ownerGroup")
			.append(" left join fetch item.taxonomyLevel taxonomyLevel")
			.append(" left join fetch item.type itemType")
			.append(" left join fetch item.educationalContext educationalContext")
			.append(" where item.key=:itemKey");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("itemKey", itemKey)
				.setParameter("identityKey", identity.getKey());
		if (restrictToPoolKey != null) {
			query.setParameter("restrictToPoolKey", restrictToPoolKey);
		}
		if (restrictToGroupKey != null) {
			query.setParameter("restrictToGroupKey", restrictToPoolKey);
		}

		ItemWrapper itemWrapper = null;
		List<Object[]> results = query.getResultList();
		if (!results.isEmpty()) {
			Object[] result = results.get(0);
			itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor((Number)result[1])
					.setTeacher((Number)result[2])
					.setManager((Number)result[3])
					.setEditableInPool((Number)result[4])
					.setEditableInShare((Number)result[5])
					.setMarked((Number)result[6])
					.setRatedByIdentity((Number)result[7])
					.setRating((Double)result[8])
					.create();
		}
		return itemWrapper;
	}

}
