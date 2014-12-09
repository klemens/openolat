package de.unileipzig.xman.exam.controllers;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;

import de.unileipzig.xman.exam.Exam;

public interface ExamController extends Controller {
	/**
	 * Call this when the exam has changed so that the view can be updated
	 */
	void updateExam(UserRequest ureq, Exam newExam);
}
