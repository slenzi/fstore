
_Compile instructions:_

mvn clean package -P lenzi.frontier.pc -DskipTests=true

Compiles the project using the lenzi.frontier.pc profile. This will trigger the "oracle" spring profile
to be active, and will use the OracleClosureRepository class. All unit tests will be skipped. War file
is generated in the /target directory.

mvn clean package -P lenzi.mac -DskipTests=true

Compiles the project using the lenzi.mac profile. This will trigger the "postgresql" spring profile
to be active, and will use the PostgresClosureRepository class. All unit tests will be skipped. War file
is generated in the /target directory.


_Unit Tests:_

mvn test-compile

Compiles the source in the src/test/java path

From eclipse, right-click unit test and select, Run As -> JUnit test

see src/test/resources/META-INF/test-xxxx-persistence.xml for test persistence setup.