FROM eclipse-temurin:8-jdk AS builder
WORKDIR /workspace
COPY . .
ARG apiVersion
ARG buildFlags=""
RUN ./gradlew --no-daemon ${buildFlags} -PapiVersion=${apiVersion} build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError"
COPY --from=builder /workspace/build/deps/external/*.jar /data/
COPY --from=builder /workspace/build/deps/fint/*.jar /data/
COPY --from=builder /workspace/build/libs/fint-consumer-skeleton-*.jar /data/fint-consumer-skeleton.jar
CMD ["/data/fint-consumer-skeleton.jar"]
