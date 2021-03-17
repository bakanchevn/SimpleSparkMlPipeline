FROM bde2020/spark-worker:3.0.2-hadoop3.2
# FROM openjdk:8

COPY ./StreamingProject /app

# ENV SBT_VERSION 1.4.8

#RUN \
#  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
#  dpkg -i sbt-$SBT_VERSION.deb && \
#  rm sbt-$SBT_VERSION.deb && \
#  apt-get update && \
#  apt-get install sbt 

WORKDIR /app

#RUN sbt assembly && /bin/bash 


#CMD tail -f /dev/null
#CMD sh spark_model_run.sh 
#CMD sh spark_stream_run.sh
