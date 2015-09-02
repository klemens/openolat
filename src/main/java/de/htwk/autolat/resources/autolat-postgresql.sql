diff --git a/src/main/java/de/htwk/autolat/resources/updateautOLAT-8.sql b/src/main/java/de/htwk/autolat/resources/updateautOLAT-8.sql
index 250cda4..b05761c 100644
--- a/src/main/java/de/htwk/autolat/resources/updateautOLAT-8.sql
+++ b/src/main/java/de/htwk/autolat/resources/updateautOLAT-8.sql
@@ -1,27 +1,27 @@
-create table ao_task_solution (
+create table ao_task_solution (
 	id bigint not null,
-	evaluationtext longtext,
-	score double,
-	solutiontext longtext,
-	solutiondate datetime,
+	evaluationtext text,
+	score double precision,
+	solutiontext text,
+	solutiondate timestamp,
 	fk_taskinstance bigint,
 	primary key (id)
 );
 
 create table ao_task_result(
 	id bigint not null,
-	maxscoredate datetime,
-	maxscore double,
-	maxscoretasktext longtext,
-	haspassed bit,
-	solutiontext longtext,
+	maxscoredate timestamp,
+	maxscore double precision,
+	maxscoretasktext text,
+	haspassed boolean,
+	solutiontext text,
 	primary key(id)
 );
 	
 create table ao_task_module(
 	id bigint not null,
 	duration bigint,
-	enddate datetime,
+	enddate timestamp,
 	maxcount bigint,
 	fk_nextmodule bigint,
 	fk_configuration bigint,
@@ -52,20 +52,20 @@ create table ao_task_type (
 
 create table ao_living_task_instance (
 	id bigint not null,
-	creationdate datetime,
-	tasktext longtext,
-	samplesolution longtext,
-	sampledocumentation longtext,
+	creationdate timestamp,
+	tasktext text,
+	samplesolution text,
+	sampledocumentation text,
 	signature varchar(255),
-	internaltasktext longtext,
+	internaltasktext text,
 	primary key(id)
 );
 
 create table ao_serverconnection (
 	id bigint not null,
 	name varchar(255),
-	lastcontact datetime,
-	isactive bit,
+	lastcontact timestamp,
+	isactive boolean,
 	path varchar(255),
 	primary key (id)
 );
@@ -78,20 +78,20 @@ create table ao_student (
 
 create table ao_task_configuration (
 	id bigint not null,
-	configuration longtext,
-	documentation longtext,
-	description longtext,
-	comment longtext,
+	configuration text,
+	documentation text,
+	description text,
+	comment text,
 	signature varchar(255),
-	isaltered bit,
+	isaltered boolean,
 	fk_tasktype bigint,
 	primary key (id)
 );
 
 create table ao_configuration (
 	id bigint not null,
-	begindate datetime,
-	enddate datetime,
+	begindate timestamp,
+	enddate timestamp,
 	courseid bigint,
     coursenodeid bigint,
 	fk_taskconfiguration bigint,
