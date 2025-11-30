FROM rust:1.75-alpine AS build
WORKDIR /app
COPY . .
RUN apk add --no-cache musl-dev cmake make g++
RUN cargo build --release

FROM alpine:latest
WORKDIR /app
COPY --from=build /app/target/release/notification-service-rust .
EXPOSE 8084
CMD ["./notification-service-rust"]
