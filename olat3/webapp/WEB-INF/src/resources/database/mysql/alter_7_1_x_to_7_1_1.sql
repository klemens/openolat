-- mail preferences
alter table o_user add column receiverealmail varchar(16);



-- mail system
-- mail
create table if not exists o_mail (
  mail_id bigint NOT NULL,
  meta_mail_id varchar(64),
  creationdate datetime,
	lastmodified datetime,
	resname varchar(50),
  resid bigint,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  subject varchar(512),
  body longtext,
  fk_from_id bigint,
  primary key (mail_id)
);
alter table o_mail type = InnoDB;

-- mail recipient
create table if not exists o_mail_to_recipient (
  pos mediumint NOT NULL default 0,
  fk_mail_id bigint,
  fk_recipient_id bigint
);
alter table o_mail_to_recipient type = InnoDB;
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DE foreign key (fk_mail_id) references o_mail (mail_id);

create table if not exists o_mail_recipient (
  recipient_id bigint NOT NULL,
  recipientvisible bit,
  deleted bit,
  mailread bit,
  mailmarked bit,
  email varchar(255),
  recipientgroup varchar(255),
  creationdate datetime,
  fk_recipient_id bigint,
  primary key (recipient_id)
);
alter table o_mail_recipient type = InnoDB;
alter table o_mail_recipient add constraint FKF86663165A4FA5DG foreign key (fk_recipient_id) references o_bs_identity (id);
alter table o_mail add constraint FKF86663165A4FA5DC foreign key (fk_from_id) references o_mail_recipient (recipient_id);
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DD foreign key (fk_recipient_id) references o_mail_recipient (recipient_id);

-- mail attachments
create table o_mail_attachment (
	attachment_id bigint NOT NULL,
  creationdate datetime,
	datas mediumblob,
	datas_size bigint,
	datas_name varchar(255),
	mimetype varchar(255),
  fk_att_mail_id bigint,
	primary key (attachment_id)
);
alter table o_mail_attachment type = InnoDB;
alter table o_mail_attachment add constraint FKF86663165A4FA5DF foreign key (fk_att_mail_id) references o_mail (mail_id);
