#!/usr/bin/env bash
# Build and start Systelm ERP (Linux/macOS)
# 1. cd frontend && npm run build
# 2. cp -r frontend/dist/* backend/src/main/resources/static/
# 3. cd backend && mvn package -DskipTests
# 4. java -jar backend/target/backend-0.1.0.jar

cd "$(dirname "$0")/.."
java -jar backend/target/backend-0.1.0.jar
