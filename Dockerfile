FROM openjdk:8u151-jre-slim-stretch

COPY build/install/app /app

EXPOSE 8080
CMD ["/app/bin/app"]
