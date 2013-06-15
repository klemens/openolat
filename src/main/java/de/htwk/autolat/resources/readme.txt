To run autOlat, you have to add the modifications given in
updateautOLAT-8.sql to your database:

mysql yourdb -uyouruser -pyourpasswd <updateautOLAT-8.sql

Use deleteautOLAT.sql in the same way to remove the additional tables. Note
that this destroys all information about Autolat, and any course that contains
Autolat nodes will no more work properly. So it is probably not a good idea to
really do that on a productive system. 

For Upgrades: We have not yet tested that. Note that the Hibernate mappings are
now locally to Autolat and need not to be added to
/org/olat/core/commons/persistence/_spring/databaseCorecontext.xml
Please remove all legacy stuff from that location if any.
