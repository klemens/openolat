Interceptor-Extension v0.4

OLAT-Extension to ensure entering of first name, surname and the institutional
identifier by students of the University of Leipzig.  Since those users were
created via LDAP, these mandatory values are missing.

Installation instructions:
- Get and setup an instance of OpenOlat 8.0.2 (or higher?)
- Link folder LoginInterceptor to src/main/java/de/unileipzig
- run maven to build it (e.g. mvn install)
- enjoy

History:
v0.1 Proof of concept
v0.2 Functionality given
v0.3 Springtest added and integrated into OLATs testframework
v0.4 Adapted for use in openolat 8.0.2. Springtests were not touched.

TODO-List:

SINCE v0.2: Calling of the Controller for specific users only hardly possible. 
The constructor has to be evoked and will be called from within a
CloseableModalController. This one (respectively the panel it displays) can
only be closed by firing a DONE_EVENT.  To give us an opportunity to do so,
the user will at least have to click a button. (That is if one doesn't want to
touch the original OLAT code and stop other afterLoginInterceptionControllers
from working properly) Yet, for users who do not match the criteria (student,
LDAP-user, no institutional identifier entered), i made the dialogue as short
and pleasant as possible.

v0.4: The SupportsAfterLoginInterceptor Interface now provides a method to
determine whether the AfterLogin interruption is necessary.

SINCE v0.2: UserConstants.INSTITUTIONALNAME at the moment automatically set to
"Universität Leipzig". Not sure if this is correct.
