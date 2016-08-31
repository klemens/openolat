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

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 14.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioV2Page {
	
	public static final By portfolioBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation");

	private final WebDriver browser;

	public PortfolioV2Page(WebDriver browser) {
		this.browser = browser;
	}
	
	public PortfolioV2Page assertOnBinder() {
		By navigationBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation");
		OOGraphene.waitElement(navigationBy, browser);
		WebElement navigationEl = browser.findElement(navigationBy);
		Assert.assertTrue(navigationEl.isDisplayed());
		return this;
	}
	
	public PortfolioV2Page assertOnSectionTitleInEntries(String title) {
		By sectionTitleBy = By.xpath("//div[contains(@class,'o_portfolio_section')]//h3[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(sectionTitleBy, 5, browser);
		return this;
	}
	
	public PortfolioV2Page assertOnPageInEntries(String title) {
		By sectionTitleBy = By.xpath("//div[contains(@class,'o_portfolio_page')]//h4[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(sectionTitleBy, 5, browser);
		return this;
	}
	
	public PortfolioV2Page assertOnPageInToc(String title) {
		By sectionTitleBy = By.xpath("//a[contains(@class,'o_pf_open_entry')]/span[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(sectionTitleBy, 5, browser);
		return this;
	}
	
	public PortfolioV2Page selectTableOfContent() {
		By tocBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation .o_sel_pf_toc");
		browser.findElement(tocBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PortfolioV2Page selectEntries() {
		By tocBy = By.cssSelector("li.o_tool .o_sel_pf_binder_navigation .o_sel_pf_entries");
		browser.findElement(tocBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PortfolioV2Page createSection(String title) {
		//click create button
		By createBy = By.className("o_sel_pf_new_section");
		WebElement createButton = browser.findElement(createBy);
		createButton.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_pf_edit_section_form");
		OOGraphene.waitElement(popupBy, 5, browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_pf_edit_section_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(title);
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_edit_section_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PortfolioV2Page createEntry(String title) {
		//click create button
		By createBy = By.className("o_sel_pf_new_entry");
		WebElement createButton = browser.findElement(createBy);
		createButton.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		By popupBy = By.cssSelector("div.modal-content fieldset.o_sel_pf_edit_entry_form");
		OOGraphene.waitElement(popupBy, 5, browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_pf_edit_entry_title input[type='text']");
		WebElement nameEl = browser.findElement(nameBy);
		nameEl.sendKeys(title);
		
		//save
		By submitBy = By.cssSelector(".o_sel_pf_edit_entry_form button.btn-primary");
		WebElement submitButton = browser.findElement(submitBy);
		submitButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	

}
