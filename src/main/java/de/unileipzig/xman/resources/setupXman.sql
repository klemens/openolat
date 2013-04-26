CREATE TABLE `o_xman_appointment` (
  `appointment_id` bigint(20) NOT NULL DEFAULT '0',
  `date` datetime DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `place` varchar(255) DEFAULT NULL,
  `occupied` tinyint(1) DEFAULT NULL,
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `exam_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`appointment_id`)
);

-- wird nicht verwendet? Kein dump da.
CREATE TABLE `o_xman_category` (
  `category_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(255) NOT NULL DEFAULT '',
  `identity_id` bigint(20) DEFAULT NULL,
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`category_id`)
);

CREATE TABLE `o_xman_comment` (
  `comment_id` bigint(20) NOT NULL DEFAULT '0',
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  PRIMARY KEY (`comment_id`)
);

CREATE TABLE `o_xman_commentEntry` (
  `idx` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `commentEntry_id` bigint(20) NOT NULL DEFAULT '0',
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `comment` text,
  `author_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`commentEntry_id`)
);

CREATE TABLE `o_xman_esf` (
  `esf_id` bigint(20) NOT NULL DEFAULT '0',
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `validated` tinyint(1) DEFAULT NULL,
  `identity_id` bigint(20) DEFAULT NULL,
  `validator_id` bigint(20) DEFAULT NULL,
  `illnessReport_id` bigint(20) DEFAULT NULL,
  `comment_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`esf_id`)
);

CREATE TABLE `o_xman_exam` (
  `exam_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(255) DEFAULT NULL,
  `regStartDate` datetime DEFAULT NULL,
  `regEndDate` datetime DEFAULT NULL,
  `identity_id` bigint(20) DEFAULT NULL,
  `signOffDate` datetime DEFAULT NULL,
  `earmarkedEnabled` tinyint(1) DEFAULT NULL,
  `isOral` tinyint(1) DEFAULT NULL,
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `comments` text,
  `module_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`exam_id`)
);

-- Die beiden folgenden Tabellen können perspektivisch rausgeworfen werden,
-- wenn der Code entsprechend abgespeckt ist, da nicht genutzt und über
-- Notizfunktion abbildbar.
CREATE TABLE `o_xman_illnessReport` (
  `illnessReport_id` bigint(20) NOT NULL DEFAULT '0',
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  PRIMARY KEY (`illnessReport_id`)
);

CREATE TABLE `o_xman_illnessReportEntry` (
  `idx` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `illnessReportEntry_id` bigint(20) NOT NULL DEFAULT '0',
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `toDate` datetime DEFAULT NULL,
  `fromDate` datetime DEFAULT NULL,
  PRIMARY KEY (`illnessReportEntry_id`)
);

-- Auch das ist obsolet, wenn der Code entsprechend abgespeckt ist, weil nicht
-- sinnvoll genutzt.
CREATE TABLE `o_xman_module` (
  `module_id` bigint(20) NOT NULL DEFAULT '0',
  `name` varchar(255) NOT NULL DEFAULT '',
  `identity_id` bigint(20) DEFAULT NULL,
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `description` text,
  `number` text,
  PRIMARY KEY (`module_id`)
);

CREATE TABLE `o_xman_protocol` (
  `protocol_id` bigint(20) NOT NULL DEFAULT '0',
  `idx` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `appointment_id` bigint(20) DEFAULT NULL,
  `exam_id` bigint(20) DEFAULT NULL,
  `identity_id` bigint(20) DEFAULT NULL,
  `module_id` bigint(20) DEFAULT NULL,
  `earmarked` tinyint(1) DEFAULT NULL,
  `comments` text,
  `grade` text,
  PRIMARY KEY (`protocol_id`)
);

CREATE TABLE `o_xman_studyPath` (
  `studyPath_id` bigint(20) NOT NULL DEFAULT '0',
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `name` text,
  PRIMARY KEY (`studyPath_id`)
);
