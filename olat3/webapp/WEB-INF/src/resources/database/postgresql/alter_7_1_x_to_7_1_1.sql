-- mail preferences
alter table o_user add column receiverealmail varchar(16);



-- mail system
-- mail
create table o_mail (
  mail_id int8 not null,
  meta_mail_id varchar(64),
  creationdate timestamp,
	lastmodified timestamp,
	resname varchar(50),
  resid int8,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  subject varchar(512),
  body text,
  fk_from_id int8,
  primary key (mail_id)
);

-- mail recipient
create table o_mail_to_recipient (
  pos int4 NOT NULL default 0,
  fk_mail_id int8,
  fk_recipient_id int8
);
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DE foreign key (fk_mail_id) references o_mail (mail_id);

create table o_mail_recipient (
  recipient_id int8 NOT NULL,
  recipientvisible boolean,
  deleted boolean,
  mailread boolean,
  mailmarked boolean,
  email varchar(255),
  recipientgroup varchar(255),
  creationdate timestamp,
  fk_recipient_id int8,
  primary key (recipient_id)
);
alter table o_mail_recipient add constraint FKF86663165A4FA5DG foreign key (fk_recipient_id) references o_bs_identity (id);
alter table o_mail add constraint FKF86663165A4FA5DC foreign key (fk_from_id) references o_mail_recipient (recipient_id);
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DD foreign key (fk_recipient_id) references o_mail_recipient (recipient_id);

-- mail attachments
create table o_mail_attachment (
	attachment_id int8 NOT NULL,
  creationdate timestamp,
	datas bytea,
	datas_size int8,
	datas_name varchar(255),
	mimetype varchar(255),
  fk_att_mail_id int8,
	primary key (attachment_id)
);
alter table o_mail_attachment add constraint FKF86663165A4FA5DF foreign key (fk_att_mail_id) references o_mail (mail_id);
