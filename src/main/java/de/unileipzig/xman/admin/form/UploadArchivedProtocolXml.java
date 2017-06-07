package de.unileipzig.xman.admin.form;

import java.io.File;
import java.io.InputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.util.Util;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.protocol.archived.ArchivedProtocol;

public class UploadArchivedProtocolXml extends FormBasicController {
	private FileElement xmlFile;
	private FormLink xsdLink;

	public UploadArchivedProtocolXml(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(ExamAdminSite.class, getLocale()));

		initForm(ureq);
	}

	public InputStream getFileStream() {
		return xmlFile.getUploadInputStream();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		xmlFile = uifactory.addFileElement(getWindowControl(), "AdminArchiveController.uploadForm.file", formLayout);
		xmlFile.setMandatory(true, "AdminArchiveController.uploadForm.file.error");
		xsdLink = uifactory.addFormLink("AdminArchiveController.uploadForm.link.downloadSchema", formLayout);

		uifactory.addFormSubmitButton("AdminArchiveController.uploadForm.submit", formLayout);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == xsdLink) {
			File xsdFile = new File(ArchivedProtocol.class.getResource("_static/archivedProtocols.xsd").getFile());
			if(xsdFile.exists()) {
				FileMediaResource mr = new FileMediaResource(xsdFile, true);
				ureq.getDispatchResult().setResultingMediaResource(mr);
			} else {
				showError("AdminArchiveController.uploadForm.link.downloadSchema.error");
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}
