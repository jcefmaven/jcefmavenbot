#!/bin/bash

if [ $# -lt 2 ]
  then
    echo "Usage: ./run.sh <docker_repo> <PAT>"
    echo ""
    echo "docker_repo: e.g. friwidev/jcefmavenbot"
    echo "PAT: your GitHub token"
    exit 1
fi

docker run --name jcefmavenbot -e TOKEN=$2 $1:linux-latest