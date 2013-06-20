# SimpleShibboleth authenticator for OpenOLAT #

Allows simple authentication using shibboleth. The mapping of shibboleth
attributes to OO attributes is done in the spring config. At least a unique
username must be provided.

This authenticator can also migrate existing OO users to shibboleth
when usernames match. This feature must be enabled.
_Warning:_ This can be dangerous, because the matching is only done by
username!

Also make sure, OO is not accessible directly on the net, only through
a shibboleth proxy, because otherwise anyone could login as any user
simply by providing the right username as http header parameter!

## Configuration ## 

You can provide the following options in olat.local.properties to customize
this library. (You must set simpleShibboleth.enable to true to use it!)

    # enable lib and set as default login
    simpleShibboleth.enable=true
    simpleShibboleth.default=true
    # customize the path used for shib authentication
    simpleShibboleth.path=shib
    # migrate existing users by matching username
    simpleShibboleth.migrateUsers=false

## Licence ##

This library by Klemens Schölhorn is licenced under MIT licence.
