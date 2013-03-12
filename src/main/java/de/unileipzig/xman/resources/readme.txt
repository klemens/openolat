To build and install the Xman extension several preprarations are required:

1) roll out OpenOlat sources.

2) patch open OpenOlat sources with the supplied diff-xxx.patch: Go to the
OpenOlat main dir and run 

   patch -E -l -p1 < (path_to)/diff-xxx.patch

diff-xxx.patch is the patch for the dedicated OpenOlat version that can be
installed from the repo with 'hg update -u xxx' but shuold also work in wide
ranges.  Try 

  patch --dry-run -E -l -p1 < (path_to)/diff-xxx.patch

for y "dry run" of the changes.

Note that the diffs should be cleaned up just more, since they contain certain
stuff from the core OpenOlat changes, too. 

3) Place (or link) the xman dir as main/src/de/unileipzig/xman in the OpenOlat
source tree. 

4) Compile and install OpenOlat as usual. 

5) Create tables from resources/setupXman.sql in your data base

6) add lines

??

to olat.local.properties

7) Run OpenOlat as usual and (hopefully) enjoy the Xman extension. 

