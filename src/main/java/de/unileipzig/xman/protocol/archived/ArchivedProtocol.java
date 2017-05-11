package de.unileipzig.xman.protocol.archived;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * This class represents an archived protocol.
 * It has no references to other xman objects and thus is suited for export.
 * The identifier is used to find the right object for a student and
 * should be filled with the student number.
 */
@Entity(name="archivedprotocol")
@Table(name="o_xman_archived_protocol")
@NamedQueries({
	@NamedQuery(name = "loadArchivedProtocolsByStudent", query = "SELECT proto FROM archivedprotocol proto WHERE proto.studentId = :studentId"),
	@NamedQuery(name = "loadArchivedProtocols", query = "SELECT proto FROM archivedprotocol proto"),
	@NamedQuery(name = "countArchivedProtocols", query = "SELECT count(proto) FROM archivedprotocol proto"),
	@NamedQuery(name = "deleteArchivedProtocolsByStudent", query = "DELETE FROM archivedprotocol proto WHERE proto.studentId = :studentId")
})
public class ArchivedProtocol implements Persistable {
	private static final long serialVersionUID = -6985508337654400770L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id")
	@XStreamOmitField
	private Long key;

	@Column(name="student_id")
	@XStreamAsAttribute
	@XStreamAlias("id")
	private String studentId;

	@Column
	private String name;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	@Column
	private String location;
	
	@Column
	private String comment;
	
	@Column
	private String result;
	
	@Column(name="study_path")
	private String studyPath;


	@Override
	public Long getKey() {
		return key;
	}

	/**
	 * hibernate setter
	 */
	protected void setKey(Long key) {
		this.key = key;
	}
	
	public String getIdentifier() {
		return studentId;
	}

	public void setIdentifier(String identifier) {
		this.studentId = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getStudyPath() {
		return studyPath;
	}

	public void setStudyPath(String studyPath) {
		this.studyPath = studyPath;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable other) {
		return getKey().equals(other.getKey());
	}
}
