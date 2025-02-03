FROM openjdk:17-jdk-slim
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HToolkit"
ENV JAVA_OPTS="-Xmx16g -Xms1g"
ENTRYPOINT ["java", "-Dawt.useSystemAAFontSettings=on -Dswing.aatext=true", "-jar", "-Dspring.profiles.active=prod", "/app.jar"]