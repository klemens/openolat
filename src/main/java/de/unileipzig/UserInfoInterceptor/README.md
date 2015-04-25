# UserInfoInterceptor for OpenOLAT #

Allows asking users for their names ane emails during their first login. This
can be useful when these are not provided by the authenticator. (eg. shibboleth)

Additionally, students can be asked or required to enter their student number.
You can also provide a regex to validate the entered student number.

Once the user has entered the requested data, the dialog is not shown again.

## Installing automatically ##

If you are using git to maintain your OpenOLAT instance, simply merge the branch
`userInfoInterceptor` into your working branch.

## Installing manually ##

Copy or link unileipzig/UserInfoInterceptor into your source tree.

### java/org/olat/core/_spring/mainCorecontext.xml ###

Add the following import to the other ones:
```
<import resource="classpath:/de/unileipzig/UserInfoInterceptor/_spring/interceptorContext.xml" />
```

## Configuration ##

You can provide the following options in olat.local.properties to customize
this library. (You must set userInfoInterceptor.enable to true to use it!)

```
# enable the interceptor after user logins
userInfoInterceptor.enable=true

# do not require the user's student number (default)
userInfoInterceptor.studentNumber=ignore
# ask students to enter their student number but do not require it
userInfoInterceptor.studentNumber=ask
# require students to enter their student number
userInfoInterceptor.studentNumber=require

# only accept numerical student numbers
# no check is done if this option is omitted
# (note: you must escape all backslashes)
userInfoInterceptor.studentNumberCheck=\\d+
```

## Licence ##

This library is licenced under MIT licence.<br />
Original idea and code by Sascha Vinz, reworked by Klemens Schölhorn.
