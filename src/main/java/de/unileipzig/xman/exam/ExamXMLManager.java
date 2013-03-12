package de.unileipzig.xman.exam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.user.UserImpl;

import com.thoughtworks.xstream.XStream;

public class ExamXMLManager {
	
	private final String EXAM_PATH = "/exams/";

	private static OlatRootFolderImpl requestContainer;

	private static final XStream mystream;
	
	private OLog log = Tracing.createLoggerFor(ExamXMLManager.class);
	
	/**
	 * 
	 */
	static {
		
		mystream = XStreamHelper.createXStreamInstance();
		mystream.alias("user", UserImpl.class);
		mystream.alias("identity", IdentityImpl.class);
	}
	
	/**
	 * 
	 */
	private static ExamXMLManager INSTANCE = null;

	private ExamXMLManager() {
		
		this.prepareFilesystem();
	}
	
	/**
	 * 
	 */
	static { INSTANCE = new ExamXMLManager(); }
	
	/**
	 * @return Singleton.
	 */
	public static ExamXMLManager getInstance() { return INSTANCE; }

	/**
	 * 
	 */
	private void prepareFilesystem() {

		// generate exam base path

		requestContainer = new OlatRootFolderImpl(EXAM_PATH, null);

		File fBasePath = requestContainer.getBasefile();

		if (!fBasePath.exists() && !fBasePath.mkdirs())

			throw new OLATRuntimeException( ExamXMLManager.class, "Could not create exam base path:" + requestContainer, null);

	}
	
	/**
	 * @return a list of all available exams which are archived in the "EXAM_PATH" 
	 */
	public List<Exam> retrieveExams() {
		
		List<Exam> examList = new ArrayList<Exam>();
		
		// get baseFile and all exams
		File fBaseFile = requestContainer.getBasefile();
		File[] exams = fBaseFile.listFiles();

		for( int i = 0; i < exams.length; i++){

			if( exams[i].isFile()){

				examList.add( (Exam)XStreamHelper.readObject(mystream, exams[i]));
			}
		}
		return examList;
		
	}
	
	/**
	 * Returns the exam specified with the given key. If
	 * no such exam was found null is returned.
	 * 
	 * @param the key of the exam to retrieve
	 * @return the exam specified by the key or null if 
	 * no exam was found
	 */
	public Exam retrieveExamByKey(long key) {
		
		List<Exam> examList = this.retrieveExams();
		
		for ( Exam exam : examList ) {
			
			// the exam was found, return it
			if ( exam.getKey() == key ) {
				
				return exam;
			}
		}
		// nothing was found 
		return null;
	}
	
	/**
	 * Saves an exam in the EXAM_PATH. If the exam is already 
	 * persisted, nothing will be done.
	 * 
	 * @param exam the exam which should be persisted
	 * @return true if the exam could be persisted successfully 
	 * or the exam was already persisted, else false
	 */
	public boolean saveExam(Exam exam) {

		log.info("name: " + exam.getName());
		log.info("key:" + exam.getKey().toString());
		String fileName = exam.getKey() + "_" + exam.getName() + ".xml";
		
		try {
			File f = new File( requestContainer.getBasefile().getCanonicalPath() + "/" + fileName );
			
			// exam not yet archived
			if( !f.exists()){
	
				writeObj( fileName , exam);
			}
			return true;
			
		} catch (IOException e) {
				
			this.log.error("There was an error saving the exam with the id: " + exam.getKey(), e);
			return false;
		}
	}
	
	/**
	 * Writes an object, in this case an exam, to the file system.
	 * 
	 * @param fileName - the name of the object (exam.getKey() + "_" + exam.getName() + ".xml")
	 * @param exam - the exam to be saved
	 */
	private void writeObj(String fileName, Exam exam) {
		
		VFSItem vfsItem = requestContainer.resolve(fileName);

		if (vfsItem == null) {

			vfsItem = requestContainer.createChildLeaf(fileName);

		}
		XStreamHelper.writeObject( mystream, (VFSLeaf)vfsItem, exam);
	}

	/**
	 * Deletes the specified exam from the file system.
	 * 
	 * @param exam - the exam to be deleted
	 * @return true if the exam was deleted successfully, else false
	 */
	public boolean deleteExamByExam(Exam exam) {
		
		List<Exam> examList = this.retrieveExams();
		
		for ( Exam examInList : examList ) {
			
			// exam was found
			if ( examInList.getKey() == exam.getKey() ) {
				
				File file = new File(exam.getKey() + "_" + exam.getName() + ".xml");
				
				// if the file exists, it should be deleted
				if ( file.exists() ) {
					
					file.delete();
					return true;
				}
				else {
					
					this.log.error("An error occured while deleting the exam file: " + exam.getKey() + "_" + exam.getName() + ".xml . No such file available.");
					return false;
				}
			}
		}
		return false;
	}
}
