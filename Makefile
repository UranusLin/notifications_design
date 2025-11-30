.PHONY: test-all test-java test-kotlin test-go test-ts test-rust help

help:
	@echo "Available commands:"
	@echo "  make test-all      - Run tests for all languages"
	@echo "  make test-java     - Run Java tests"
	@echo "  make test-kotlin   - Run Kotlin tests"
	@echo "  make test-go       - Run Go tests"
	@echo "  make test-ts       - Run TypeScript tests"
	@echo "  make test-rust     - Run Rust tests"

test-all: test-java test-kotlin test-go test-ts test-rust
	@echo "✅ All tests passed!"

test-java:
	@echo "Testing Java..."
	@cd notifications-polyglot/java && ./gradlew test

test-kotlin:
	@echo "Testing Kotlin..."
	@cd notifications-polyglot/kotlin && ./gradlew test

test-go:
	@echo "Testing Go..."
	@cd notifications-polyglot/golang && go test ./...

test-ts:
	@echo "Testing TypeScript..."
	@cd notifications-polyglot/typescript && npm test

test-rust:
	@echo "Testing Rust..."
	@cd notifications-polyglot/rust && cargo test
