package de.unileipzig.xman.studyPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.type.Type;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;

import de.unileipzig.xman.esf.DuplicateObjectException;

public class StudyPathManager {
	
	private static StudyPathManager INSTANCE = null;
	private OLog log = Tracing.createLoggerFor(this.getClass());
	public static final String DEFAULT_STUDY_PATH = "studyPath.name.default";

	private StudyPathManager() {
		// singleton
	}
	
	/**
	 * @return Singleton.
	 */
	public static StudyPathManager getInstance() { 
		
		if ( INSTANCE == null ) {
			
			INSTANCE = new StudyPathManager();
			INSTANCE.init();
		}
		
		return INSTANCE;
	}
	
	private void init() {
		
		StudyPath defaultStudyPath = this.findStudyPathByI18nKey(StudyPathManager.DEFAULT_STUDY_PATH);
		if (defaultStudyPath == null) {
			
			defaultStudyPath = this.createStudyPath();
			defaultStudyPath.setI18nKey(StudyPathManager.DEFAULT_STUDY_PATH);
			try {
				
				StudyPathManager.getInstance().saveStudyPath(defaultStudyPath);
			}
			// can never happen
			catch (DuplicateObjectException doe) { Tracing.createLoggerFor(StudyPathManager.class).
				info("There is already a studyPath with the name " + defaultStudyPath.getI18nKey() + " in the database");
			}
			
			// remove this
			this.TEMPcreateAllStudyPaths();
		}		
	}
	
	private void TEMPcreateAllStudyPaths() {
		
		String[] keys = {
				"studyPath.name.Diplom-Informatik",
				"studyPath.name.Informatik-Bachelor.(alt)",
				"studyPath.name.Informatik-Bachelor.(neu)",
				"studyPath.name.Informatik-Master.(alt)",
				"studyPath.name.Informatik-Master.(neu)",
				"studyPath.name.Informatik-Lehramt.(alt)",
				"studyPath.name.Bachelorstudiengang.Lehramt.Informatik",
				"studyPath.name.Magister.mit.Hauptfach.Informatik",
				"studyPath.name.Magister.mit.Nebenfach.Informatik",
				"studyPath.name.Wirtschaftsmathematik",
				"studyPath.name.Mathematik-Diplom",
				"studyPath.name.Mathematik-Lehramt.(alt)",
				"studyPath.name.Bachelorstudiengang.Lehramt.Mathematik",
				"studyPath.name.Magister.mit.Hauptfach.Mathematik",
				"studyPath.name.Magister.mit.Nebenfach.Mathematik",
				"studyPath.name.Wirtschaftsinformatik-Diplom",
				"studyPath.name.Wirtschaftsinformatik-Bachelor",
				"studyPath.name.Wirtschaftspädagogik",
				"studyPath.name.Nebenfach.Informatik",
			};
		
			for (String i18nKey : keys ) {
				
				StudyPath studyPath = this.createStudyPath();
				studyPath.setI18nKey(i18nKey);
				DBFactory.getInstance().saveObject(studyPath);
			}
	}
	

	/**
	 * Creates a new studyPath and returns it.
	 * @return the new created studyPath
	 */
	public StudyPath createStudyPath() {
		
		return new StudyPathImpl();
	}
	
	/**
	 * Persists the given StudyPath in the database.

	 * @param studyPath - the studyPath, which is to be saved
	 * @throws DuplicateObjectException if a studyPath with the same key was found
	 */
	public void saveStudyPath(StudyPath newStudyPath) throws DuplicateObjectException {
		
		Translator translator = Util.createPackageTranslator(StudyPath.class, I18nManager.getInstance().getLocaleOrDefault(null));
		
		if ( this.findStudyPathByI18nKey(newStudyPath.getI18nKey()) == null && this.checkTranslationsForDuplicatedEntries(translator.translate(newStudyPath.getI18nKey()), translator)) {
			
			// delete all spaces
			newStudyPath.setI18nKey(newStudyPath.getI18nKey().replaceAll(" ", "."));
			DBFactory.getInstance().saveObject(newStudyPath);
			Tracing.createLoggerFor(StudyPathManager.class).info("New studyPath with id " + newStudyPath.getKey() + " was created.");
		}
		else throw new DuplicateObjectException("There is already a studyPath with the name " + newStudyPath.getI18nKey() + " in the database");
		
	}
	
	private boolean checkTranslationsForDuplicatedEntries(String newName, Translator translator) {
		
		List<StudyPath> list = this.findAllStudyPaths();
		
		
		for ( StudyPath sp : list ) {
			
			if ( translator.translate(sp.getI18nKey()).equals(newName) ) return false;
		}
		return true;
	}
	
	/**
	 * Updates an existing studyPath.
	 * @param studyPath - the studyPath, which is to be updated
	 * @throws DuplicateObjectException 
	 */
	public void updateStudyPath(StudyPath studyPath, String name, String oldName) throws DuplicateObjectException, FileNotFoundException {
		
		this.createLocalFiles(studyPath, name, oldName);
	}
	
	/**
	 * Deletes the given studyPath.
	 * 
	 * @param studyPath - the studyPath, which is to be deleted
	 */
	public void deleteStudyPath(StudyPath studyPath) {
	
		if ( studyPath != null ) {
			
			DBFactory.getInstance().deleteObject(studyPath);
			Tracing.createLoggerFor(StudyPathManager.class).info("The studyPath with id " + studyPath.getKey() + " was deleted.");
		}
	}
	
	/**
	 * @param id - a Long object representing the id of the studyPath
	 * @return the studyPath with the given id or null, if no studyPaths were found
	 */
	public StudyPath findStudyPathById(Long id) {
		
		String query = "from de.unileipzig.xman.studyPath.StudyPathImpl as spi where spi.key = " + id;
		List<StudyPath> studyPathList = DBFactory.getInstance().find(query);
		if ( studyPathList.size() > 0 ) return (StudyPath)studyPathList.get(0);
		else {
			Tracing.createLoggerFor(StudyPathManager.class).info("No studyPath with id :" + id + " could be found!");
			return null;
		}
	}
	
	/**
	 * @param i18nKey - a string representing the i18nKey of the studyPath
	 * @return the studyPath with the given i18nKey or null, if no studyPaths were found
	 */
	public StudyPath findStudyPathByI18nKey(String i18nKey) {
		
		String query = "from de.unileipzig.xman.studyPath.StudyPathImpl as spi where spi.i18nKey = '" + i18nKey + "'";
		List<StudyPath> studyPathList = DBFactory.getInstance().find(query);
		if ( studyPathList.size() == 1 ) return (StudyPath) studyPathList.get(0);
		else {
			Tracing.createLoggerFor(StudyPathManager.class).info("No studyPath with i18nKey :" + i18nKey + " could be found!");
			return null;
		}
	}

	/**
	 * Creates the local-files. Saves the studyPath in the db.
	 * if the name is already given a duplicateobjectexveption is thrown
	 * 
	 * @param sp - null if you want update the i18n-key, else the studypath to save
	 * @param name - the new translatio
	 * @param oldi18nkey - the old i18n key of the studypath
	 * @throws DuplicateObjectException
	 */
	public void createLocalFiles(StudyPath sp, String newName, String oldI18nKey) throws DuplicateObjectException, FileNotFoundException {

		if ( sp != null ) {
			// save the new study path in the db
			// should only be called by creating the studypath
			this.saveStudyPath(sp);
		}
		
		Set<String> localeList = I18nModule.getEnabledLanguageKeys();
		
		// add the new property to all property files
		for ( String locale : localeList ) {
			
			// log.info(StudyPath.class.getPackage().getName());
			Properties prop = I18nManager.getInstance().getPropertiesWithoutResolvingRecursively(new Locale(locale), StudyPath.class.getPackage().getName());
			
			// why is it possible that prop == null ?
			if (prop != null) {
				
				Set<Object> keys = prop.keySet();
				
				Iterator iter = keys.iterator();
				
				// the new properties file, has to be filled with new name+value and old name+value (remove the old translation) 
				Properties properties = new Properties();
				
				// until no more translation left
				while (iter.hasNext()) {
					
					String key = (String)iter.next();
					properties.put(key, prop.getProperty(key));
				}
				
				// remove only when the studypath gets updated, when created there is no old i18n key
				if ( oldI18nKey != null ) {
					properties.remove(oldI18nKey);
				}
				
				// add the new kay+value 
				properties.put(sp != null ? sp.getI18nKey().replaceAll(" ", ".") : oldI18nKey.replaceAll(" ", "."), newName);

				// save the properties file, will be reloaded if chaching is enabled (only in devel mode)
				I18nManager.getInstance().saveOrUpdateProperties(properties, new Locale(locale), StudyPath.class.getPackage().getName());
				// -----------> Redscreen
				
				
			}
			else {
				throw new FileNotFoundException("The file with the translation of the study paths for the locale: " + locale.toString() +" could not be found: ");
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<StudyPath> findAllStudyPaths() {
		
		List<StudyPath> res = (List<StudyPath>) DBFactory.getInstance().find("from de.unileipzig.xman.studyPath.StudyPathImpl");
		return res;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] createKeysArray() {
		
		List<String> res = (List<String>) DBFactory.getInstance().find("select i18nKey from de.unileipzig.xman.studyPath.StudyPathImpl");
				//new Object[] {}, new Type[] {});
		
		String[] keys = new String[res.size()];
		for (int i = 0; i < res.size(); i++){keys[i] = res.get(i);}
		
		return keys;
	}
	
	/**
	 * 
	 * @param loc
	 * @return
	 */
	public String[] translateKeyArray(Locale loc) {
		
		Translator translator = Util.createPackageTranslator(StudyPath.class, loc);
		
		String[] keys = createKeysArray();
		String[] values = new String[keys.length];
		
		for (int i = 0; i < keys.length; i++) {
			
			values[i] = translator.translate(keys[i]);
			log.info(values[i]);
		}		
		return values;
	}
}