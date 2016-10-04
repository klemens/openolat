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
package org.olat.selenium.page.portfolio;

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 3 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EntryPage {
	
	private final By editFragmentBy = By.cssSelector("div.o_page_fragment_edit");
	
	private final WebDriver browser;

	public EntryPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EntryPage assertOnPage(String title) {
		By metaTitleBy = By.xpath("//div[contains(@class,'o_page_lead')]//h2[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(metaTitleBy, 5, browser);
		return this;
	}
	
	public EntryPage addTitle(String title) {
		By addTitleBy = By.cssSelector("a#o_coadd_htitle");
		browser.findElement(addTitleBy).click();
		OOGraphene.waitElement(editFragmentBy, 5, browser);
		OOGraphene.tinymce(title, ".o_page_part.o_page_edit", browser);
		return this;
	}
	
	/**
	 * Change the size of the title.
	 * 
	 * @param size A value between 1 and 6
	 * @return
	 */
	public EntryPage setTitleSize(int size) {
		By titleSize = By.xpath("//div[contains(@class,'o_page_edit_toolbar')]//a[span[contains(text(),'h" + size + "')]]");
		browser.findElement(titleSize).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Check that the title is on the page with the right size.
	 * 
	 * @param title The title
	 * @param size Its size (between 1 and 6)
	 * @return Itself
	 */
	public EntryPage assertOnTitle(String title, int size) {
		By titleBy = By.xpath("//div[contains(@class,'o_pf_content')]//h" + size + "[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(titleBy, 5, browser);
		return this;
	}
	
	public EntryPage addImage(String title, File image) {
		By addImageBy = By.cssSelector("a#o_coadd_image");
		browser.findElement(addImageBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.cssSelector("fieldset.o_sel_pf_collect_image_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, image, browser);
		OOGraphene.waitingALittleLonger();//wait event
		
		By titleBy = By.cssSelector("fieldset.o_sel_pf_collect_image_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_pf_collect_image_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EntryPage assertOnImage(File image) {
		String filename = image.getName();
		By titleBy = By.xpath("//div[contains(@class,'o_image')]//img[contains(@src,'" + filename + "')]");
		OOGraphene.waitElement(titleBy, 5, browser);
		return this;
	}
	
	public EntryPage addDocument(String title, File document) {
		By addDocumentBy = By.cssSelector("a#o_coadd_bc");
		browser.findElement(addDocumentBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.cssSelector("fieldset.o_sel_pf_collect_document_form .o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, document, browser);
		OOGraphene.waitingALittleLonger();//wait event
		
		By titleBy = By.cssSelector("fieldset.o_sel_pf_collect_document_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_pf_collect_document_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EntryPage assertOnDocument(File image) {
		String filename = image.getName();
		By downloadLinkBy = By.xpath("//div[contains(@class,'o_download')]//a[contains(text(),'" + filename + "')]");
		OOGraphene.waitElement(downloadLinkBy, 5, browser);
		return this;
	}
	
	public EntryPage addCitation(String title, String citation) {
		By addCitationBy = By.cssSelector("a#o_coadd_citation");
		browser.findElement(addCitationBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector("fieldset.o_sel_pf_collect_citation_form .o_sel_pf_collect_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		String citationSelector = ".o_sel_pf_collect_citation_form .o_sel_pf_collect_citation";
		OOGraphene.tinymce(citation, citationSelector, browser);
		
		//ok save
		By saveBy = By.cssSelector("fieldset.o_sel_pf_collect_citation_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EntryPage assertOnCitation(String citation) {
		By citationBy = By.xpath("//blockquote[contains(@class,'o_quote')]/p[contains(text(),'" + citation + "')]");
		OOGraphene.waitElement(citationBy, 5, browser);
		return this;
	}
	
	/**
	 * Publish the entry in the page view.
	 * 
	 * @return Itself
	 */
	public EntryPage publishEntry() {
		By publishBy = By.cssSelector("a.o_sel_pf_publish_entry");
		OOGraphene.waitElement(publishBy, 5, browser);
		browser.findElement(publishBy).click();
		OOGraphene.waitBusy(browser);
		confirm();
		By publishedBy = By.cssSelector("div.o_portfolio_status i.o_icon_pf_entry_published");
		OOGraphene.waitElement(publishedBy, 5, browser);
		return this;
	}
	
	public EntryPage toggleEditor() {
		By closeBy = By.cssSelector("a.o_sel_pf_edit_page");
		OOGraphene.waitElement(closeBy, 5, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Close the fragment editor.
	 * 
	 * @return Itself
	 */
	public EntryPage closeEditFragment() {
		By closeBy = By.cssSelector("div.o_page_others_above a.o_sel_save_element");
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Yes in a dialog box controller.
	 */
	private void confirm() {
		By confirmButtonBy = By.xpath("//div[contains(@class,'modal-dialo')]//div[contains(@class,'modal-footer')]/a[contains(@onclick,'link_0')]");
		OOGraphene.waitElement(confirmButtonBy, 5, browser);
		OOGraphene.waitScrollTop(browser);
		browser.findElement(confirmButtonBy).click();
		OOGraphene.waitBusy(browser);
	}
}
