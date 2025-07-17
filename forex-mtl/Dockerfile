FROM hseeberger/scala-sbt:17.0.2_1.6.2_2.13.8 as builder

WORKDIR /app
COPY . .

RUN sbt stage

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/universal/stage .

CMD ["./bin/forex"]