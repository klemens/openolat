# SimpleShibboleth authenticator for OpenOLAT #

Allows simple authentication using shibboleth. The mapping of shibboleth
attributes to OO attributes is done in the spring config. At least a unique
username must be provided.

This authenticator can also migrate existing OO users to shibboleth
when usernames match. This feature must be enabled.<br />
__Warning:__ This can be dangerous, because the matching is only done by
username without further checks!

For security reasons, this authenticator only uses attributes passed by environment
variable. Passing by header is __not__ supported, because these can be set by the
user and are not trustworthy without further checks.<br />
So make sure your reverse proxy (eg Apache) is passing the right attributes!

## Installing automatically ##

If you are using git to maintain your OpenOLAT instance, simply merge the branch
`shibboleth` into your working branch. This will replace the integrated Shibboleth
authenticator. (by using its url, see below for more details)

## Installing manually ##

Include or link unileipzig/shibboleth into your source tree. Because OpenOLAT does
not provide extension points to insert authenticators via spring, you have to change
the following spring configuration files manually:

### java/org/olat/core/_spring/mainCorecontext.xml ###

Add the following import to the other ones:
```
<import resource="classpath:/de/unileipzig/shibboleth/_spring/simpleShibbolethContext.xml" />
```

### java/org/olat/login/_spring/loginContext.xml ###

Add the following entry to the property map `authenticaionProviders`:
```
<entry key="SimpleShibboleth">
    <ref bean="SimpleShibbolethAuthenticationProvider" />
</entry>
```

### resources/serviceconfig/org/olat/_spring/brasatoconfigpart.xml ###

Add the following entry to the property map `dispatchers` (or replace the
existing `/shib/` if you want to use this url):
```
<entry key="${simpleShibboleth.dispatcherPath:/shib/}">
        <ref bean="simpleShibbolethDispatcher" />
</entry>
```

## Configuration ##

You can provide the following options in olat.local.properties to customize
this library. (You must set simpleShibboleth.enable to true to use it!)

```
# enable lib and set as default login
simpleShibboleth.enable=true
simpleShibboleth.default=true

# path of the dispatcher, relative to the OO context (where the login happens after shibboleth authentication)
simpleShibboleth.dispatcherPath=/shib/

# migrate existing users by matching username
simpleShibboleth.migrateUsers=false

# generate email from username when no email given by shibboleth
simpleShibboleth.emailTemplate=%s@student.my.uni

# url of the session initiator of the service provider
simpleShibboleth.sessionInitiatorPath=/Shibboleth.sso/Login
```

To control the mapping between Shibboleth and OpenOLAT attributes, see the
included `_spring/simpleShibbolethContext.xml`. There are some examples and the
mapping `uid` -> `username` is activated by default.

## Licence ##

This library by Klemens Schölhorn is licenced under MIT licence.
