@echo off


cmd /C mvn clean install -DskipTests %* 

if %errorlevel% EQU 0 (

	@echo off
	IF DEFINED SCANWEBTESTER_DEPLOY_DIR (
      
	  @echo on
	  echo --------- COPIANT EAR SCANWEBTESTER ---------

	  xcopy /Y scanweb-ear\target\scanweb.ear %SCANWEBTESTER_DEPLOY_DIR%

	) ELSE (
	  echo  =================================================================
	  echo    Definex la variable d'entorn SCANWEBTESTER_DEPLOY_DIR apuntant al
	  echo    directori de deploy del JBOSS  i automaticament s'hi copiara
	  echo    l'ear generat.
	  echo  =================================================================
	) 

)