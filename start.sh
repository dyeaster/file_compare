#!/bin/bash
projectname=filecompare-0.0.1-SNAPSHOT.jar
logpath=./server.log
pid=`ps -ef | grep ${projectname} | grep -v "grep" | awk '{print $2}'`
if [ ${pid} ]; then
  echo "${projectname} is running and pid=${pid}"
else 
  if [ $# -lt 1 ]; then
    port=8989
    echo "the port is not configured, default port 8989 will be used."
  else
    port=$1
  fi
  echo "begin to start ${projectname}"
  nohup java -jar ./filecompare-0.0.1-SNAPSHOT.jar --server.port=${port} >> ${logpath} 2>&1 &
fi

