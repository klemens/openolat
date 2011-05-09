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


-- access control
alter table o_repositoryentry add column fk_tutorgroup int8;
alter table o_repositoryentry add column fk_participantgroup int8;

create table o_ac_offer (
	offer_id int8 NOT NULL,
  creationdate timestamp,
	lastmodified timestamp,
	is_valid boolean default true,
	validfrom timestamp,
	validto timestamp,
  version int4 not null,
  resourceid int8,
  resourcetypename varchar(255),
  resourcedisplayname varchar(255),
  token varchar(255),
  fk_resource_id int8,
	primary key (offer_id)
);
create index ac_offer_to_resource_idx on o_ac_offer (fk_resource_id);

create table o_ac_method (
	method_id int8 NOT NULL,
	access_method varchar(32),
  version int4 not null,
  creationdate timestamp,
	lastmodified timestamp,
	is_valid boolean default true,
	validfrom timestamp,
	validto timestamp,
	primary key (method_id)
);

create table o_ac_offer_access (
	offer_method_id int8 NOT NULL,
  version int4 not null,
  creationdate timestamp,
	is_valid boolean default true,
	validfrom timestamp,
	validto timestamp,
  fk_offer_id int8,
  fk_method_id int8,
	primary key (offer_method_id)
);
alter table o_ac_offer_access add constraint off_to_meth_meth_ctx foreign key (fk_method_id) references o_ac_method (method_id);
alter table o_ac_offer_access add constraint off_to_meth_off_ctx foreign key (fk_offer_id) references o_ac_offer (offer_id);


-- access cart
create table o_ac_order (
	order_id int8 NOT NULL,
  version int4 not null,
  creationdate timestamp,
	lastmodified timestamp,
	is_valid boolean default true,
  fk_delivery_id int8,
	primary key (order_id)
);
create index ac_order_to_delivery_idx on o_ac_order (fk_delivery_id);

create table o_ac_order_part (
	order_part_id int8 NOT NULL,
  version int4 not null,
  pos int4,
  creationdate timestamp,
  fk_order_id int8,
	primary key (order_part_id)
);
alter table o_ac_order_part add constraint ord_part_ord_ctx foreign key (fk_order_id) references o_ac_order (order_id);


create table o_ac_order_line (
	order_item_id int8 NOT NULL,
  version int4 not null,
  pos int4,
  creationdate timestamp,
  fk_order_part_id int8,
  fk_offer_id int8,
	primary key (order_item_id)
);
alter table o_ac_order_line add constraint ord_item_ord_part_ctx foreign key (fk_order_part_id) references o_ac_order_part (order_part_id);
alter table o_ac_order_line add constraint ord_item_offer_ctx foreign key (fk_offer_id) references o_ac_offer (offer_id);
  
create table o_ac_transaction (
	transaction_id int8 NOT NULL,
  version int4 not null,
  creationdate timestamp,
  fk_order_part_id int8,
  fk_order_id int8,
  fk_method_id int8,
	primary key (transaction_id)
);
alter table o_ac_transaction add constraint trans_ord_ctx foreign key (fk_order_id) references o_ac_order (order_id);
alter table o_ac_transaction add constraint trans_ord_part_ctx foreign key (fk_order_part_id) references o_ac_order_part (order_part_id);
alter table o_ac_transaction add constraint trans_method_ctx foreign key (fk_method_id) references o_ac_method (method_id);





