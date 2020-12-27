#!/bin/bash
. HANGMAN_ENV
PID_FILE="${HANGMAN_REP}/pid.txt"

if [ "$1" == "start" ]
then
    if [ -f $PID_FILE ]
    then
        echo "Un process est deja en cours. Son PID est "`cat $PID_FILE`
        exit 1
    else
        echo "Demarrage de hangmanBot en cours"
        nohup ${JAVA_REP}/java -jar ${HANGMAN_REP}/hangmanbot.jar --spring.config.location=${HANGMAN_REP}/config/application.properties >> ${HANGMAN_REP}/log/hangmanbot.log 2>&1 &
        echo $! > $PID_FILE
        echo "Demarrage de hangmanBot OK"
        exit 0
    fi
fi

if [ "$1" == "stop" ]
then
    if [ -f $PID_FILE ]
    then
        kill -SIGTERM `cat $PID_FILE`
        rm $PID_FILE
        echo "Arret du hangmanbot OK"
        exit 0
    else
        echo "Pas de process en cours"
        exit 1
    fi
fi
