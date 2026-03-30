FROM maven:3.9.14-eclipse-temurin-25 AS builder
ARG SERVICE_NAME=jrebel-license-server
WORKDIR /build
#单模块构建
#COPY pom.xml .
#RUN mvn dependency:go-offline
# 复制整个项目以支持多模块构建
COPY . .
# 构建 jrebel-license-server 及其依赖模块（如common）
RUN mvn clean package -DskipTests -pl ${SERVICE_NAME} -am

# 使用精简的 JRE 镜像
FROM eclipse-temurin:25-jre-alpine
ARG SERVICE_NAME=jrebel-license-server
# 复制jar
COPY --from=builder /build/${SERVICE_NAME}/target/${SERVICE_NAME}-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]