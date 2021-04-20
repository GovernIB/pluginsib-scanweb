
call mvn clean install -DskipTests %*

xcopy /Y  scanweb-tester\scanweb-ear\target\scanweb.ear D:\dades\dades\CarpetesPersonals\ProgramacioPortaFIB2\portafib-2.0-jboss-5.1.0.GA\server\default\deployscanweb

