#!/bin/bash

echo off
cat help.txt

env mvn -DskipTests $@ clean install 

if [ $? == 0 ]; then
  if [ "SCANWEBTESTER_DEPLOY_DIR" == "" ];  then

    echo  =================================================================
    echo    Definex la variable d\'entorn SCANWEBTESTER_DEPLOY_DIR apuntant al
    echo    directori de deploy del JBOSS  i automaticament s\'hi copiara
    echo    l\'ear generat.
    echo  =================================================================
  
  else
  
    echo on
    echo --------- COPIANT EAR ---------
    cp ./scanweb-ear/target/scanweb.ear $SCANWEBTESTER_DEPLOY_DIR

  fi
fi


