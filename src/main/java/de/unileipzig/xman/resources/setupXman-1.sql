/* Tabellen fuer OLAT PA - extracted from bulky diff */

CREATE TABLE IF NOT EXISTS o_xman_appointment (
  appointment_id bigint(20) NOT NULL,
  `date` datetime DEFAULT NULL,
  duration int(11) DEFAULT NULL,
  place varchar(255) DEFAULT NULL,
  occupied bit(1) DEFAULT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  exam_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (appointment_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_comment (
  comment_id bigint(20) NOT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  PRIMARY KEY (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_commentEntry (
  idx int(11) DEFAULT NULL,
  parent_id bigint(20) DEFAULT NULL,
  commentEntry_id bigint(20) NOT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  `comment` longtext,
  author_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (commentEntry_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_esf (
  esf_id bigint(20) NOT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  validated tinyint(1) DEFAULT NULL,
  identity_id bigint(20) DEFAULT NULL,
  validator_id bigint(20) DEFAULT NULL,
  illnessReport_id bigint(20) DEFAULT NULL,
  comment_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (esf_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_exam (
  exam_id bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  regStartDate datetime DEFAULT NULL,
  regEndDate datetime DEFAULT NULL,
  identity_id bigint(20) DEFAULT NULL,
  signOffDate datetime DEFAULT NULL,
  earmarkedEnabled bit(1) DEFAULT NULL,
  isOral bit(1) DEFAULT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  comments longtext,
  module_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_illnessReport (
  illnessReport_id bigint(20) NOT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  PRIMARY KEY (illnessReport_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_illnessReportEntry (
  idx int(11) DEFAULT NULL,
  parent_id bigint(20) DEFAULT NULL,
  illnessReportEntry_id bigint(20) NOT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  toDate datetime DEFAULT NULL,
  fromDate datetime DEFAULT NULL,
  PRIMARY KEY (illnessReportEntry_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_module (
  module_id bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  identity_id bigint(20) DEFAULT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  description longtext,
  number longtext,
  PRIMARY KEY (module_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_protocol (
  protocol_id bigint(20) NOT NULL,
  idx int(11) DEFAULT NULL,
  parent_id bigint(20) DEFAULT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  appointment_id bigint(20) DEFAULT NULL,
  exam_id bigint(20) DEFAULT NULL,
  identity_id bigint(20) DEFAULT NULL,
  module_id bigint(20) DEFAULT NULL,
  earmarked bit(1) DEFAULT NULL,
  comments longtext,
  grade longtext,
  PRIMARY KEY (protocol_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS o_xman_studyPath (
  studyPath_id bigint(20) NOT NULL,
  lastmodified datetime NOT NULL,
  creationdate datetime DEFAULT NULL,
  i18nKey longtext,
  PRIMARY KEY (studyPath_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/* Ende PA Tabellen */
