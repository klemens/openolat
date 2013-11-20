package de.unileipzig.xman.admin.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NamedFileMediaResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.admin.form.UploadArchivedProtocolXml;
import de.unileipzig.xman.protocol.archived.ArchivedProtocolManager;

public class AdminArchiveController extends BasicController {
	VelocityContainer mainVC;

	private Link importButton;
	private Link exportButton;
	private UploadArchivedProtocolXml uploadForm;

	CloseableModalController cmc;

	OLog log = Tracing.createLoggerFor(getClass());

	public AdminArchiveController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ExamAdminSite.class, getLocale()));
		mainVC = new VelocityContainer("archiveProtocols", ExamAdminSite.class, "archive", getTranslator(), this);

		init(ureq);
		putInitialPanel(mainVC);
	}

	private void init(UserRequest ureq) {
		importButton = LinkFactory.createButton("AdminArchiveController.import", mainVC, this);
		exportButton = LinkFactory.createButton("AdminArchiveController.export", mainVC, this);

		String count = NumberFormat.getIntegerInstance(getLocale()).format(ArchivedProtocolManager.getInstance().countAll());
		mainVC.contextPut("message", translate("AdminArchiveController.count", new String[] { count }));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == importButton) {
			uploadForm = new UploadArchivedProtocolXml(ureq, getWindowControl());
			listenTo(uploadForm);

			cmc = new CloseableModalController(getWindowControl(), "AdminArchiveController.import.close", uploadForm.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		} else if(source == exportButton) {
			File tmpFile = new File(WebappHelper.getTmpDir(), "export-" + CodeHelper.getUniqueID());

			OutputStream out = null;
			try {
				out = new FileOutputStream(tmpFile);
				if(ArchivedProtocolManager.getInstance().exportAll(out)) {
					MediaResource mr = new NamedFileMediaResource(tmpFile, "exportArchivedProtocols.xml", "", true);
					ureq.getDispatchResult().setResultingMediaResource(mr);
				} else {
					showError("AdminArchiveController.export.error");
				}
			} catch (FileNotFoundException e) {
				log.error("error while creating tmp file for export of archived protocols", e);
				showError("AdminArchiveController.export.fileError");
				return;
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == uploadForm) {
			if(event == Form.EVNT_VALIDATION_OK) {
				cmc.deactivate();

				int newProtocols = ArchivedProtocolManager.getInstance().importAll(uploadForm.getFileStream());
				if(newProtocols >= 0) {
					showInfo("AdminArchiveController.import.success", String.valueOf(newProtocols));
				} else {
					showError("AdminArchiveController.import.error");
				}

				// refresh view
				init(ureq);

				removeAsListenerAndDispose(cmc);
				cmc = null;
				removeAsListenerAndDispose(uploadForm);
				uploadForm = null;
			}
		}
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(uploadForm);
		removeAsListenerAndDispose(cmc);
	}
}
