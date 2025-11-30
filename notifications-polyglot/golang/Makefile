.PHONY: build run test docker-build swagger

build:
	go build -o main .

run:
	go run main.go

test:
	go test ./...

docker-build:
	docker build -t notification-service-go .

swagger:
	swag init
