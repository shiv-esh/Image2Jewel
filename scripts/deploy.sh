#!/bin/bash

# ============================================================
# Image2Jewel - Backend Deployment Script
# ============================================================
# Usage: ./deploy.sh <path-to-pem-file>
# ============================================================

set -e

# --- Configuration ---
EC2_USER="ubuntu"
EC2_HOST="13.204.81.65"
REMOTE_PROJECT_DIR="/home/ubuntu/Image2Jewel"

# --- Setup Paths ---
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

# --- Validate PEM file argument ---
PEM_FILE="${1:-/home/shivesh/Downloads/catharsis.pem}" # Default if not provided
SSH_OPTS="-o StrictHostKeyChecking=no"

if [ -n "$PEM_FILE" ] && [ -f "$PEM_FILE" ]; then
    chmod 400 "$PEM_FILE"
    SSH_OPTS="$SSH_OPTS -i $PEM_FILE"
    echo "🔑 Using PEM file: $PEM_FILE"
else
    echo "⚠️ Warning: No PEM file found at $PEM_FILE. Attempting default SSH key."
fi

echo ""
echo "============================================================"
echo " 🔨 Building Backend (Local)"
echo "============================================================"

cd backend
if [ -f "./gradlew" ]; then
    chmod +x gradlew
    ./gradlew bootJar --no-daemon -q
elif command -v gradle >/dev/null 2>&1; then
    gradle bootJar -q
else
    echo "❌ Error: Neither ./gradlew nor gradle command was found. Please install Gradle!"
    exit 1
fi
echo "✅ Backend build successful."
cd "$PROJECT_ROOT"

echo ""
echo "============================================================"
echo " 🔎 Checking for local build"
echo "============================================================"

# Check if JAR exists
JAR_FILE=$(ls backend/build/libs/*.jar 2>/dev/null | head -n 1)
if [ -n "$JAR_FILE" ]; then
    echo "✅ Found build: $JAR_FILE"
else
    echo "❌ Error: No JAR found in backend/build/libs/. Please build it first!"
    exit 1
fi

echo ""
echo "============================================================"
echo " 📤 Uploading to EC2"
echo "============================================================"

# 1. Sync project structure (excluding local build artifacts)
echo "📦 Syncing project files..."
rsync -avz -e "ssh $SSH_OPTS" \
    --exclude '.git' \
    --exclude '.gradle' \
    --exclude 'build' \
    --exclude 'node_modules' \
    ./ "$EC2_USER@$EC2_HOST:$REMOTE_PROJECT_DIR/"

# 2. Specifically sync the freshly built JAR
echo "🚀 Uploading new JAR ($JAR_FILE)..."
ssh $SSH_OPTS "$EC2_USER@$EC2_HOST" "mkdir -p $REMOTE_PROJECT_DIR/backend/build/libs"
scp $SSH_OPTS "$JAR_FILE" "$EC2_USER@$EC2_HOST:$REMOTE_PROJECT_DIR/backend/build/libs/"

echo ""
echo "============================================================"
echo " 🚀 Restarting Containers on EC2"
echo "============================================================"

ssh $SSH_OPTS "$EC2_USER@$EC2_HOST" << EOF
    cd "$REMOTE_PROJECT_DIR"
    
    echo "🛠️  Rebuilding Image and Restarting Containers..."
    sudo docker compose -f docker-compose.prod.yml up -d --build
    
    echo "🧹 Cleaning up old images..."
    sudo docker image prune -f
    
    echo "✅ Containers started successfully."
    sudo docker ps
EOF

echo ""
echo "============================================================"
echo " ✅ Deployment Complete!"
echo " 📋 Monitor: ssh $SSH_OPTS $EC2_USER@$EC2_HOST 'sudo docker logs -f backend-api'"
echo "============================================================"
