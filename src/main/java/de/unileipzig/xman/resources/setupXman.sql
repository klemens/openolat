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

create table `o_xman_archived_protocol` (
  `id` bigint not null,
  `student_id` varchar(255) not null,
  `name` varchar(255) not null,
  `date` datetime not null,
  `location` varchar(255) not null,
  `comment` varchar(255) not null,
  `result` varchar(255) not null,
  `study_path` varchar(255) not null,
  primary key (`id`)
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

CREATE TABLE `o_xman_commentEntry` (
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
  `identity_id` bigint(20) DEFAULT NULL,
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
  `isMultiSubscription` tinyint(1) DEFAULT NULL,
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `comments` text,
  PRIMARY KEY (`exam_id`)
);

CREATE TABLE `o_xman_protocol` (
  `protocol_id` bigint(20) NOT NULL DEFAULT '0',
  `parent_id` bigint(20) DEFAULT NULL,
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `appointment_id` bigint(20) DEFAULT NULL,
  `exam_id` bigint(20) DEFAULT NULL,
  `identity_id` bigint(20) DEFAULT NULL,
  `earmarked` tinyint(1) DEFAULT NULL,
  `comments` text,
  `grade` text,
  `study_path` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`protocol_id`)
);

CREATE TABLE `o_xman_studyPath` (
  `studyPath_id` bigint(20) NOT NULL DEFAULT '0',
  `lastmodified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `creationdate` datetime DEFAULT NULL,
  `name` text,
  PRIMARY KEY (`studyPath_id`)
);
