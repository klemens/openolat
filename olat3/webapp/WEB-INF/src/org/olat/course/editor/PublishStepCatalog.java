package org.olat.course.editor;

import java.util.ArrayList;
import java.util.List;

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.catalog.ui.CatalogAjaxAddController;
import org.olat.catalog.ui.CatalogEntryAddController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Description:<br>
 * Step to set the course in the catalog
 * 
 * <P>
 * Initial Date:  16 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://wwww.frentix.com
 */
class PublishStepCatalog extends BasicStep {
	
	private final PrevNextFinishConfig prevNextConfig;
	private final CourseEnvironment courseEnv;
	private final CourseNode rootNode;
	
	public PublishStepCatalog(UserRequest ureq, ICourse course, boolean hasPublishableChanges) {
		super(ureq);
		
		this.courseEnv = course.getCourseEnvironment();
		this.rootNode = course.getRunStructure().getRootNode();
		setI18nTitleAndDescr("publish.catalog.header", null);
		
		if(hasPublishableChanges) {
			setNextStep(new PublishStep00a(ureq));
			prevNextConfig = PrevNextFinishConfig.BACK_NEXT_FINISH;
		} else {
			setNextStep(Step.NOSTEP);
			prevNextConfig = PrevNextFinishConfig.BACK_FINISH;
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return prevNextConfig;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		return new PublishStepCatalogForm(ureq, wControl, form, stepsRunContext, courseEnv, rootNode);
	}
	
	class PublishStepCatalogForm extends StepFormBasicController {
		
		private FormLink addToCatalog;
		private SingleSelection catalogBox;
		private CloseableModalController cmc;
		private Controller catalogAddController;
		private List<FormLink> deleteLinks;
		
		private final RepositoryEntry repositoryEntry;
		private final CatalogManager catalogManager;
		private final CourseEnvironment courseEnv;
		private final CourseNode rootNode;
		
		public PublishStepCatalogForm(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext,
				CourseEnvironment courseEnv, CourseNode rootNode) {
			super(ureq, control, rootForm, runContext, LAYOUT_CUSTOM, "publish_catalog");
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, courseEnv.getCourseResourceableId());
			repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
			catalogManager = CatalogManager.getInstance();
			this.courseEnv = courseEnv;
			this.rootNode = rootNode;
			
			initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
			
			Property prop = cpm.findCourseNodeProperty(rootNode, null, null, "catalog-choice");
			String value = prop == null ? null : prop.getStringValue();
			
			FormItemContainer fc = FormLayoutContainer.createDefaultFormLayout("catalogSettings", getTranslator());
			fc.setRootForm(mainForm);
			formLayout.add("catalogSettings", fc);
			
			final String[] keys = new String[]{"yes","no"};
			final String[] values = new String[] {
					translate("yes"),
					translate("no")
				};
			catalogBox = uifactory.addDropdownSingleselect("catalogBox", "publish.catalog.box", fc, keys, values, null);
			catalogBox.addActionListener(this, FormEvent.ONCHANGE);
			if(!StringHelper.containsNonWhitespace(value)) {
				value = "yes";
			}
			catalogBox.select(value, true);
			updateCategories();
		}
		
		private void updateCategories() {
			boolean activate = catalogBox.isOneSelected() && "yes".equals(catalogBox.getSelectedKey());
			if(addToCatalog == null) {
				addToCatalog = uifactory.addFormLink("publish.catalog.add", flc, Link.BUTTON_SMALL);
			}
			addToCatalog.setVisible(activate);
			
			deleteLinks = new ArrayList<FormLink>();
			if(activate) {
				List<CatalogEntry> catalogEntries = catalogManager.getCatalogCategoriesFor(repositoryEntry);
				for(CatalogEntry entry:catalogEntries) {
					FormLink link = uifactory.addFormLink(entry.getKey().toString(), "delete", null, flc, Link.LINK);
					link.setUserObject(new CategoryLabel(entry.getKey(), getPath(entry)));
					deleteLinks.add(link);
				}
			}
			flc.contextPut("categories", deleteLinks);
		}
		
		private String getPath(CatalogEntry entry) {
			String path = "";
			CatalogEntry tempEntry = entry;
			while (tempEntry != null) {
				path = "/" + tempEntry.getName() + path;
				tempEntry = tempEntry.getParent();
			}
			return path;
		}
		
		private void deleteCategory(Long categoryKey) {
			CatalogEntry category = catalogManager.loadCatalogEntry(categoryKey);
			List<CatalogEntry> children = catalogManager.getChildrenOf(category);
			for (CatalogEntry child : children) {
				RepositoryEntry childRepoEntry = child.getRepositoryEntry();
				if (childRepoEntry != null && childRepoEntry.equalsByPersistableKey(repositoryEntry)) {
					// remove from catalog
					catalogManager.deleteCatalogEntry(child);
					break;
				}
			}
		}
		
		private void doAddCatalog(UserRequest ureq) {
			removeAsListenerAndDispose(catalogAddController);
			if (getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled()) {
				catalogAddController = new CatalogAjaxAddController(ureq, getWindowControl(), repositoryEntry);
			} else {
				catalogAddController = new CatalogEntryAddController(ureq, getWindowControl(), repositoryEntry);
			}

			listenTo(catalogAddController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", catalogAddController.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = true;
			catalogBox.clearError();
			if(catalogBox.isOneSelected() && catalogBox.isSelected(0) && 
					(deleteLinks == null || deleteLinks.isEmpty())) {
				catalogBox.setErrorKey("publish.catalog.error", null);
				allOk &= false;
			}
			return allOk && super.validateFormLogic(ureq);
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(source == cmc) {
				removeAsListenerAndDispose(catalogAddController);
				removeAsListenerAndDispose(cmc);
				catalogAddController = null;
				cmc = null;
			} else if (catalogAddController == source) {
				if(event == Event.DONE_EVENT) {
					updateCategories();
				}
				cmc.deactivate();
				removeAsListenerAndDispose(catalogAddController);
				removeAsListenerAndDispose(cmc);
				catalogAddController = null;
				cmc = null;
			} else {
				super.event(ureq, source, event);
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			if(catalogBox.isOneSelected()) {
				String val = catalogBox.getSelectedKey();
				CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
				Property prop = cpm.findCourseNodeProperty(rootNode, null, null, "catalog-choice");
				if(prop == null) {
					prop = cpm.createCourseNodePropertyInstance(rootNode, null, null, "catalog-choice", null, null, val, null);
					cpm.saveProperty(prop);
				} else {
					prop.setStringValue(val);
					cpm.updateProperty(prop);
				}
			}
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(catalogBox == source) {
				updateCategories();
				if(catalogBox.isOneSelected() && catalogBox.isSelected(0)) {
					if(deleteLinks == null || deleteLinks.isEmpty()) {
						doAddCatalog(ureq);
					}
				}
			} else if (source == addToCatalog) {
				doAddCatalog(ureq);
			} else if (deleteLinks.contains(source)) {
				CategoryLabel label = (CategoryLabel)source.getUserObject();
				deleteCategory(label.getCategoryKey());
				updateCategories();
			} else {
				super.formInnerEvent(ureq, source, event);
			}
		}
	}
	
	public class CategoryLabel {
		private final Long categoryKey;
		private final String path;
		
		public CategoryLabel(Long categoryKey, String path) {
			this.categoryKey = categoryKey;
			this.path = path;
		}

		public Long getCategoryKey() {
			return categoryKey;
		}

		public String getPath() {
			return path;
		}
	}
}