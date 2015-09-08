create table ao_task_solution (
	id bigint not null,
	evaluationtext longtext,
	score double,
	solutiontext longtext,
	solutiondate datetime,
	fk_taskinstance bigint,
	primary key (id)
);

create table ao_task_result(
	id bigint not null,
	maxscoredate datetime,
	maxscore double,
	maxscoretasktext longtext,
	haspassed bit,
	solutiontext longtext,
	primary key(id)
);
	
create table ao_task_module(
	id bigint not null,
	duration bigint,
	enddate datetime,
	maxcount bigint,
	fk_nextmodule bigint,
	fk_configuration bigint,
	primary key(id)
);

create table ao_task_instance(
	id bigint not null,
	scounter bigint,
	fcounter bigint,
	icounter bigint,
	lcounter bigint,
	fk_configuration bigint,
	fk_livinginstance bigint,
	fk_result bigint,
	fk_student bigint,
	fk_taskconfiguration bigint,
	fk_taskmodule bigint,
	primary key(id)
);

create table ao_task_type (
	id bigint not null,
	tasktype varchar(255),
	scoringorder varchar(1),
	primary key (id)
);

create table ao_living_task_instance (
	id bigint not null,
	creationdate datetime,
	tasktext longtext,
	samplesolution longtext,
	sampledocumentation longtext,
	signature varchar(255),
	internaltasktext longtext,
	primary key(id)
);

create table ao_serverconnection (
	id bigint not null,
	name varchar(255),
	lastcontact datetime,
	isactive bit,
	path varchar(255),
	primary key (id)
);

create table ao_student (
	id bigint not null,
	fk_identity bigint,
	primary key (id)
);

create table ao_task_configuration (
	id bigint not null,
	configuration longtext,
	documentation longtext,
	description longtext,
	comment longtext,
	signature varchar(255),
	isaltered bit,
	fk_tasktype bigint,
	primary key (id)
);

create table ao_configuration (
	id bigint not null,
	begindate datetime,
	enddate datetime,
	courseid bigint,
    coursenodeid bigint,
	fk_taskconfiguration bigint,
	fk_serverconnection bigint,
    autolatserver varchar(255),
	primary key (id)
);

create table ao_scorepoints(
	id bigint not null,
	position int not null,
	point int,
	primary key (id, position)
);

create table ao_configuration_taskmodule(
	id bigint not null,
	position int not null,
	fk_taskmodule bigint,
	primary key (id, position)
);
