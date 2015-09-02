This directory contains the database schemas needed for autolat and
the configuration for the autotool servers.

# Database

MySQL: autolat-mysql.sql
PostgreSQL: autolat-postgresql.sql

Use autolat-delete.sql to remove autolat. Note however that courses with
autolat nodes will no longer work and throw exception. So you first have to
delete these courses or at least remove the autolat nodes.

# Autotool

autolatProperties.xml contains all servers that will be selectable via the
task configuration. Note that this file can ba changes on the fly in production
without reloading openolat.
You should avoid removing servers from this list, as tasks do not save the url
of the selected server, but the name and version, and so will no longer work
if thier server is removed (this can be easily fixed however by selecting another
server).
