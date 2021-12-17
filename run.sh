#!/bin/bash

if [ $# -lt 3 ]
  then
    echo "Usage: ./run.sh <module> <docker_repo> <PAT>"
    echo ""
    echo "module: either BUILD, ISSUE or BOTH"
    echo "docker_repo: e.g. friwidev/jcefmavenbot"
    echo "PAT: your GitHub token"
    exit 1
fi

case $1 in
  BUILD)
    build=true
    issue=false
    ;;

  ISSUE)
    build=false
    issue=true
    ;;

  BOTH)
    build=true
    issue=true
    ;;

  *)
    echo "Err: Unknown module!"
    exit 1
    ;;
esac

docker run --name jcefmavenbot -e TOKEN=$3 -e BUILD=$build -e ISSUE=$issue $2:linux-latest