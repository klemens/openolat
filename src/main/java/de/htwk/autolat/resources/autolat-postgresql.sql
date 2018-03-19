create table ao_task_solution (
	id bigint not null,
	evaluationtext text,
	score double precision,
	solutiontext text,
	solutiondate timestamp,
	fk_taskinstance bigint,
	primary key (id)
);

create table ao_task_result(
	id bigint not null,
	maxscoredate timestamp,
	maxscore double precision,
	maxscoretasktext text,
	haspassed boolean,
	solutiontext text,
	primary key(id)
);

create table ao_task_module(
	id bigint not null,
	duration bigint,
	enddate timestamp,
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
	creationdate timestamp,
	tasktext text,
	samplesolution text,
	sampledocumentation text,
	signature varchar(255),
	internaltasktext text,
	primary key(id)
);

create table ao_serverconnection (
	id bigint not null,
	name varchar(255),
	lastcontact timestamp,
	isactive boolean,
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
	configuration text,
	documentation text,
	description text,
	comment text,
	signature varchar(255),
	isaltered boolean,
	fk_tasktype bigint,
	primary key (id)
);

create table ao_configuration (
	id bigint not null,
	begindate timestamp,
	enddate timestamp,
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
