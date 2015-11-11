CREATE TABLE o_xman_appointment (
  appointment_id bigint PRIMARY KEY,
  date timestamp,
  duration integer,
  place character varying(255),
  occupied boolean,
  lastmodified timestamp NOT NULL,
  creationdate timestamp,
  exam_id bigint
);

create table o_xman_archived_protocol (
  id bigint PRIMARY KEY,
  student_id character varying(255) NOT NULL,
  name character varying(255) NOT NULL,
  date timestamp NOT NULL,
  location character varying(255) NOT NULL,
  comment character varying(255) NOT NULL,
  result character varying(255) NOT NULL,
  study_path character varying(255) NOT NULL
);

CREATE TABLE o_xman_commentEntry (
  parent_id bigint,
  commentEntry_id bigint PRIMARY KEY,
  lastmodified timestamp NOT NULL,
  creationdate timestamp,
  comment text,
  author_id bigint
);

CREATE TABLE o_xman_esf (
  esf_id bigint PRIMARY KEY,
  lastmodified timestamp NOT NULL,
  creationdate timestamp,
  identity_id bigint
);

CREATE TABLE o_xman_exam (
  exam_id bigint PRIMARY KEY,
  name character varying(255),
  regStartDate timestamp,
  regEndDate timestamp,
  identity_id bigint,
  signOffDate timestamp,
  earmarkedEnabled boolean,
  isOral boolean,
  isMultiSubscription boolean,
  lastmodified timestamp NOT NULL,
  creationdate timestamp,
  comments text
);

CREATE TABLE o_xman_protocol (
  protocol_id bigint PRIMARY KEY,
  parent_id bigint,
  lastmodified timestamp NOT NULL,
  creationdate timestamp,
  appointment_id bigint,
  exam_id bigint,
  identity_id bigint,
  earmarked boolean,
  comments text,
  grade text,
  study_path character varying(255)
);

CREATE TABLE o_xman_studypath (
  studyPath_id bigint PRIMARY KEY,
  lastmodified timestamp NOT NULL,
  creationdate timestamp,
  name text
);
