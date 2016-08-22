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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CategoryToElement;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.component.TimelinePoint;
import org.olat.modules.portfolio.ui.model.PageRow;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderPageListController extends AbstractPageListController {
	
	private Link newSectionLink, newEntryLink;
	private FormLink previousSectionLink, nextSectionLink, showAllSectionsLink;
	
	private CloseableModalController cmc;
	private SectionEditController newSectionCtrl;
	private PageMetadataEditController newPageCtrl;
	private AssignmentEditController newAssignmentCtrl;

	private final Binder binder;
	private final List<Identity> owners;
	
	@Autowired
	private UserManager userManager;
	
	public BinderPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl, stackPanel, secCallback, config, "binder_pages", true);
		this.binder = binder;
		owners = portfolioService.getMembers(binder, PortfolioRoles.owner.name());
		
		initForm(ureq);
		loadModel(null);
	}

	@Override
	public void initTools() {
		if(secCallback.canAddPage(null)) {
			newEntryLink = LinkFactory.createToolLink("new.page", translate("create.new.page"), this);
			newEntryLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(newEntryLink, Align.right);
		}
		
		if(secCallback.canAddSection()) {
			newSectionLink = LinkFactory.createToolLink("new.section", translate("create.new.section"), this);
			newSectionLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(newSectionLink, Align.right);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			StringBuilder ownerSb = new StringBuilder();
			for(Identity owner:owners) {
				if(ownerSb.length() > 0) ownerSb.append(", ");
				ownerSb.append(userManager.getUserDisplayName(owner));
			}
			layoutCont.contextPut("owners", ownerSb.toString());
			layoutCont.contextPut("binderKey", binder.getKey());
			layoutCont.contextPut("binderTitle", StringHelper.escapeHtml(binder.getTitle()));
		}

		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setFromColumnModel(false);
		options.setDefaultOrderBy(new SortKey(null, false));
		tableEl.setSortSettings(options);
		
		previousSectionLink = uifactory.addFormLink("section.paging.previous", formLayout, Link.BUTTON | Link.NONTRANSLATED);
		previousSectionLink.setVisible(false);
		previousSectionLink.setIconLeftCSS("o_icon o_icon_move_left");
		nextSectionLink = uifactory.addFormLink("section.paging.next", formLayout, Link.BUTTON | Link.NONTRANSLATED);
		nextSectionLink.setVisible(false);
		nextSectionLink.setIconRightCSS("o_icon o_icon_move_right");
		showAllSectionsLink = uifactory.addFormLink("section.paging.all", formLayout, Link.BUTTON);
		showAllSectionsLink.setVisible(false);
	}

	@Override
	protected void loadModel(String searchString) {
		List<Section> sections = portfolioService.getSections(binder);
		
		List<CategoryToElement> categorizedElements = portfolioService.getCategorizedSectionsAndPages(binder);
		Map<OLATResourceable,List<Category>> categorizedElementMap = new HashMap<>();
		Map<Section,Set<String>> sectionAggregatedCategoriesMap = new HashMap<>();
		for(CategoryToElement categorizedElement:categorizedElements) {
			List<Category> categories = categorizedElementMap.get(categorizedElement.getCategorizedResource());
			if(categories == null) {
				categories = new ArrayList<>();
				categorizedElementMap.put(categorizedElement.getCategorizedResource(), categories);
			}
			categories.add(categorizedElement.getCategory());
		}
		
		//comments
		Map<Long,Long> numberOfCommentsMap = portfolioService.getNumberOfComments(binder);
		
		//assessment sections
		List<AssessmentSection> assessmentSections = portfolioService.getAssessmentSections(binder, getIdentity());
		Map<Section,AssessmentSection> sectionToAssessmentSectionMap = assessmentSections.stream()
				.collect(Collectors.toMap(as -> as.getSection(), as -> as));
		
		//assignments
		List<Assignment> assignments = portfolioService.getAssignments(binder);
		Map<Section,List<Assignment>> sectionToAssignmentMap = new HashMap<>();
		for(Assignment assignment:assignments) {
			List<Assignment> assignmentList;
			if(sectionToAssignmentMap.containsKey(assignment.getSection())) {
				assignmentList = sectionToAssignmentMap.get(assignment.getSection());
			} else {
				assignmentList = new ArrayList<>();
				sectionToAssignmentMap.put(assignment.getSection(), assignmentList);
			}
			assignmentList.add(assignment);
		}

		List<Page> pages = portfolioService.getPages(binder, searchString);
		List<PageRow> rows = new ArrayList<>(pages.size());
		for (Page page : pages) {
			if(!secCallback.canViewElement(page)) {
				continue;
			}
			
			boolean first = false;
			Section section = page.getSection();
			if (sections.remove(section)) {
				first = true;
			}
			
			PageRow pageRow = forgeRow(page, sectionToAssessmentSectionMap.get(section), sectionToAssignmentMap.get(section),
					first, categorizedElementMap, numberOfCommentsMap);
			rows.add(pageRow);
			if(secCallback.canAddPage(section)) {
				FormLink newEntryButton = uifactory.addFormLink("new.entry." + (++counter), "new.entry", "create.new.page", null, flc, Link.BUTTON);
				newEntryButton.setCustomEnabledLinkCSS("btn btn-primary");
				newEntryButton.setUserObject(pageRow);
				pageRow.setNewEntryLink(newEntryButton);
			}
			
			if(secCallback.canNewAssignment() && section != null) {
				FormLink newAssignmentButton = uifactory.addFormLink("new.assignment." + (++counter), "new.assignment", "create.new.assignment", null, flc, Link.BUTTON);
				newAssignmentButton.setCustomEnabledLinkCSS("btn btn-primary");
				newAssignmentButton.setUserObject(pageRow);
				pageRow.setNewAssignmentLink(newAssignmentButton);
			}
			
			if(section != null) {
				Set<String> categories = sectionAggregatedCategoriesMap.get(section);
				if(categories == null) {
					categories = new HashSet<>();
					sectionAggregatedCategoriesMap.put(section, categories);
				}
				if(pageRow.getPageCategories() != null && pageRow.getPageCategories().size() > 0) {
					categories.addAll(pageRow.getPageCategories());
				}
				
				pageRow.setSectionCategories(categories);
			}
		}
		
		//sections without pages
		if(!StringHelper.containsNonWhitespace(searchString)) {
			for(Section section:sections) {
				if(!secCallback.canViewElement(section)) {
					continue;
				}
				
				PageRow pageRow = forgeRow(section, sectionToAssessmentSectionMap.get(section), sectionToAssignmentMap.get(section),
						true, categorizedElementMap);
				rows.add(pageRow);

				if(secCallback.canAddPage(section)) {
					FormLink newEntryButton = uifactory.addFormLink("new.entry." + (++counter), "new.entry", "create.new.page", null, flc, Link.BUTTON);
					newEntryButton.setCustomEnabledLinkCSS("btn btn-primary");
					newEntryButton.setUserObject(pageRow);
					pageRow.setNewEntryLink(newEntryButton);
				}
				
				if(secCallback.canNewAssignment() && section != null) {
					FormLink newAssignmentButton = uifactory.addFormLink("new.assignment." + (++counter), "new.assignment", "create.new.assignment", null, flc, Link.BUTTON);
					newAssignmentButton.setCustomEnabledLinkCSS("btn btn-primary");
					newAssignmentButton.setUserObject(pageRow);
					pageRow.setNewAssignmentLink(newAssignmentButton);
				}
			}
		}

		model.setObjects(rows);
		tableEl.reloadData();
		updateTimeline();
	}
	
	private void updateTimeline() {
		List<PageRow> pages = model.getObjects();
		List<TimelinePoint> points = new ArrayList<>(pages.size());
		for(PageRow page:pages) {
			if(page.isPage()) {
				String s = page.getPageStatus() == null ? "draft" : page.getPageStatus().name();
				points.add(new TimelinePoint(page.getKey().toString(), page.getTitle(), page.getCreationDate(), s));
			}
		}
		timelineEl.setPoints(points);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableRenderEvent) {
				FlexiTableRenderEvent re = (FlexiTableRenderEvent)event;
				if(re.getRendererType() == FlexiTableRendererType.custom) {
					tableEl.sort(null, false);
				}
			} else if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select-page".equals(cmd)) {
					PageRow row = model.getObject(se.getIndex());
					doOpenPage(ureq, row);
				}
			}
		} else if(previousSectionLink == source) {
			Section previousSection = (Section)previousSectionLink.getUserObject();
			doFilterSection(previousSection);
		} else if(nextSectionLink == source) {
			Section nextSection = (Section)nextSectionLink.getUserObject();
			doFilterSection(nextSection);
		} else if(showAllSectionsLink == source) {
			doShowAll();
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("new.entry".equals(cmd)) {
				PageRow row = (PageRow)link.getUserObject();
				doCreateNewPage(ureq, row.getSection());
			} else if("new.assignment".equals(cmd)) {
				PageRow row = (PageRow)link.getUserObject();
				doCreateNewAssignment(ureq, row.getSection());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(newEntryLink == source) {
			doCreateNewPage(ureq, null);
		} else if(newSectionLink == source) {
			doCreateNewSection(ureq);
		} 
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Section".equalsIgnoreCase(resName)) {
			Long resId = entries.get(0).getOLATResourceable().getResourceableId();
			
			PageRow activatedRow = null;
			for(PageRow row :model.getObjects()) {
				if(row.getSection() != null && row.getSection().getKey().equals(resId)) {
					activatedRow = row;
					break;
				}
			}
			
			if(activatedRow != null) {
				doFilterSection(activatedRow.getSection());
			}
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(newSectionCtrl == source || newAssignmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newSectionCtrl == source || newPageCtrl == source || newAssignmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(null);
				Page newPage = newPageCtrl.getPage();
				for(PageRow row:model.getObjects()) {
					if(row.getPage() != null && row.getPage().equals(newPage)) {
						doOpenPage(ureq, row);
						break;
					}
				}
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newAssignmentCtrl);
		removeAsListenerAndDispose(newSectionCtrl);
		removeAsListenerAndDispose(newPageCtrl);
		removeAsListenerAndDispose(cmc);
		newAssignmentCtrl = null;
		newSectionCtrl = null;
		newPageCtrl = null;
		cmc = null;
	}
	
	private void doShowAll() {
		model.filter(null);
		tableEl.reloadData();
		updateTimeline();
		
		previousSectionLink.setVisible(false);
		nextSectionLink.setVisible(false);
		showAllSectionsLink.setVisible(false);
	}
	
	protected void doFilterSection(Section section) {
		List<Section> currentSections = model.filter(section);
		tableEl.reloadData();
		updateTimeline();
		
		int index = currentSections.indexOf(section);

		previousSectionLink.setEnabled(index > 0);
		if(index > 0) {
			String previousTitle = currentSections.get(index - 1).getTitle();
			previousSectionLink.setI18nKey(translate("section.paging.with.title", new String[]{ previousTitle }));
			previousSectionLink.setUserObject(currentSections.get(index - 1));
		} else {
			previousSectionLink.setI18nKey(translate("section.paging.previous"));
		}
		
		if(index >= 0 && index + 1 < currentSections.size()) {
			String nextTitle = currentSections.get(index + 1).getTitle();
			nextSectionLink.setI18nKey(translate("section.paging.with.title", new String[]{ nextTitle }));
			nextSectionLink.setEnabled(true);
			nextSectionLink.setUserObject(currentSections.get(index + 1));
		} else {
			nextSectionLink.setI18nKey(translate("section.paging.next"));
			nextSectionLink.setEnabled(false);
		}
		
		boolean visible = currentSections.size() > 1;
		previousSectionLink.setVisible(visible);
		nextSectionLink.setVisible(visible);
		showAllSectionsLink.setVisible(visible);
	}
	
	private void doCreateNewSection(UserRequest ureq) {
		if(newSectionCtrl != null) return;
		
		newSectionCtrl = new SectionEditController(ureq, getWindowControl(), binder, secCallback);
		listenTo(newSectionCtrl);
		
		String title = translate("create.new.section");
		cmc = new CloseableModalController(getWindowControl(), null, newSectionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateNewPage(UserRequest ureq, Section preSelectedSection) {
		if(newPageCtrl != null) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), binder, false, preSelectedSection, true);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateNewAssignment(UserRequest ureq, Section section) {
		if(newAssignmentCtrl != null) return;
		
		newAssignmentCtrl = new AssignmentEditController(ureq, getWindowControl(), section);
		listenTo(newAssignmentCtrl);
		
		String title = translate("create.new.assignment");
		cmc = new CloseableModalController(getWindowControl(), null, newAssignmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}