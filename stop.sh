#!/bin/bash
projectname=filecompare-0.0.1-SNAPSHOT.jar
pid=`ps -ef | grep ${projectname} | grep -v "grep" | awk '{print $2}'`
if [ ${pid} ]; then
  echo "${projectname} is running and pid=${pid}"
  kill -9 ${pid}
  if [[ $? -eq 0 ]]; then
    echo "success stop ${projectname}"
  else
    echo "failed to stop ${projectname}"
  fi
fi

