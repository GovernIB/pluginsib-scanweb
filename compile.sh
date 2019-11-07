#!/bin/bash

mvn -DskipTests clean  install $@ 

if [ $? == 0 ]; then
  if [ "SCANWEB_DEPLOY_DIR" == "" ];  then

    echo  =================================================================
    echo    Definex la variable d\'entorn SCANWEB_DEPLOY_DIR apuntant al
    echo    directori de deploy del JBOSS  i automaticament s\'hi copiara
    echo    l\'ear generat.
    echo  =================================================================

  else

    echo on
    echo --------- COPIANT EAR ---------
    cp ./scanweb-tester/scanweb-ear/target/scanweb.ear $SCANWEB_DEPLOY_DIR

  fi
fi


