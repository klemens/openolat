# Autolat

Autotool integration for OpenOLAT.

## Installation

Autolat uses a fork of the xml-rpc library [redstone], which you have to install
locally before building OpenOLAT with autolat support. You may also have to
[adjust the pom.xml][pom], because it expects a hg repository by default and
fails when built from a git repository.

### Using git

Just merge the `autolat` branch into your project.

### Manually

Include the `src/main/java/de/htwk/autolat` folder and the documentation file
`src/main/webapp/static/msg/autolat-help.html` into your OpenOLAT build.

You also have to add the spring context and the hibernate persistence config:

`src/main/java/org/olat/core/_spring/mainCorecontext.xml`
```
<import resource="classpath:/de/htwk/autolat/_spring/mainCorecontext.xml"/>
```

`src/main/resources/META-INF/persistence.xml`
```
<mapping-file>de/htwk/autolat/LivingTaskInstance/LivingTaskInstanceImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/tools/Scoring/ScoreObjectImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/TaskSolution/TaskSolutionImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/TaskModule/TaskModuleImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/TaskConfiguration/TaskConfigurationImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/Student/StudentImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/Configuration/ConfigurationImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/TaskResult/TaskResultImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/TaskType/TaskTypeImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/TaskInstance/TaskInstanceImpl.hbm.xml</mapping-file>
<mapping-file>de/htwk/autolat/ServerConnection/ServerConnectionImpl.hbm.xml</mapping-file>
```

### Database

Use the files in the `resources` folder to import the tables needed. SQL-files
for MySQL and PostgreSQL are provided.

Use `autolat-delete.sql` to remove the tables. Note however that courses with
autolat nodes will no longer work and throw exception. So you first have to
delete these courses or at least remove the autolat nodes.

## Autotool server configuration

The available autotool servers have to be configured initially during build
time in `autolatProperties.xml`. However, this file can also be changed while
the server is running without restarting OpenOLAT.

You should avoid removing servers from this list, as tasks do not save the url
of the selected server, but the name and version, and so will no longer work
if their server is removed (this can be easily fixed however by selecting another
server).

[redstone]: https://github.com/klemens/redstone/tree/autolat
[pom]: https://github.com/klemens/openolat/wiki/Change_repository_type_to_git.patch
