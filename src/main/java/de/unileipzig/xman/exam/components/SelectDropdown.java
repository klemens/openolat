package de.unileipzig.xman.exam.components;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Provides a Dropdown with links of which exactly is the selected one.
 * By default this first link is selected.
 * @see #select(int)
 * @see #select(String)
 */
public class SelectDropdown extends Dropdown implements ComponentEventListener {
	private String[] names;
	private String[] I18nKeys;
	private String[] cssClasses;

	private Link[] links;

	public SelectDropdown(String name, String[] names, String[] I18nKeys, String[] cssClasses, Translator translator) {
		super(name, "", false, translator);

		if(names.length == 0 || names.length != I18nKeys.length || names.length != cssClasses.length) {
			throw new IllegalArgumentException("Provided arrays are of different length or empty");
		}

		this.names = names;
		this.I18nKeys = I18nKeys;
		this.cssClasses = cssClasses;

		links = new Link[names.length];
		for(int i = 0; i < links.length; ++i) {
			links[i] = LinkFactory.createToolLink(names[i], getTranslator().translate(I18nKeys[i]), this, cssClasses[i]);
			addComponent(links[i]);
		}

		select(0);
	}

	/**
	 * Selects a link by index. The previously selected one is deselected.
	 */
	public void select(int selectedIndex) {
		if(selectedIndex < 0 || selectedIndex >= links.length) {
			throw new IllegalArgumentException("Invalid selectedIndex");
		}

		setI18nKey(I18nKeys[selectedIndex]);
		setIconCSS("o_icon " + cssClasses[selectedIndex]);

		for(int i = 0; i < links.length; ++i) {
			if(i == selectedIndex) {
				links[i].setEnabled(false);
			} else {
				links[i].setEnabled(true);
			}
		}
	}

	/**
	 * Selects a link by its name. The previously selected one is deselected.
	 */
	public void select(String name) {
		for(int i = 0; i < names.length; ++i) {
			if(name.equals(names[i])) {
				select(i);
				return;
			}
		}
		throw new IllegalArgumentException("Link with given name does not exist");
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if(enabled) {
			select(0);
		} else {
			for(Link link : links) {
				link.setEnabled(false);
			}
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		if(isEnabled()) {
			return super.getHTMLRendererSingleton();
		} else {
			return new DefaultComponentRenderer() {
				@Override
				public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
					StringOutput sb2 = new StringOutput();
					SelectDropdown.super.getHTMLRendererSingleton().render(renderer, sb2, source, ubu, translator, renderResult, args);
					// o_disabled provides the disabled look and the removal of data-toggle='dropdown' disabled the dropdown menu
					sb.append(sb2.toString().replaceFirst("class='", "class='o_disabled ").replaceFirst("data-toggle='dropdown'", ""));
				}
			};
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		for(int i = 0; i < links.length; ++i) {
			if(links[i] == source) {
				fireEvent(ureq, new Event(names[i]));
				break;
			}
		}
	}
}
