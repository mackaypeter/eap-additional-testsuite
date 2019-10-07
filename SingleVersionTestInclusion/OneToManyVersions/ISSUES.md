Issue 1:
Currently only supports 1 .java file destribution.
A typical test case will include much more than that:
- several java files (let's say for example the test case itself and a servlet that gets deployed and contains the tested behaviour)
- additional resources like web.xml, import.sql, some.properties
Ideally the distribution would work on a "test case as a whole" basis, rather than per file. Being able to specify a root directory would be already an improvement.

Issue 2:
Being able to distribute more advanced things like new arquillian configurations, test configurations, or new modules would be nice to have, although