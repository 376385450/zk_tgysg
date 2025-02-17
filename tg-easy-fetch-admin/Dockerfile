FROM 192.168.16.55/library/openjdk:8-jdk-font

ENV TZ=Asia/Shanghai
RUN apk --no-cache add tzdata && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN mkdir -p /app
RUN mkdir -p /data
RUN apk add tini && mkdir -p /apps/logs/fetch/
RUN apk add bash openssh-client
ADD tg-easy-fetch-admin.jar /app/tg-easy-fetch-admin.jar
EXPOSE 8081
ENV OPS ""
ENTRYPOINT /sbin/tini java $OPS -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Djdk.tls.useExtendedMasterSecret=false -Ddruid.mysql.usePingMethod=false  -Dfile.encoding=UTF-8 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/apps/logs/tg-easy.hprof  /app/tg-easy-fetch-admin.jar
