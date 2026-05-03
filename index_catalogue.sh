#!/bin/bash
# =============================================================
# Bulk Index Script for Image2Jewel
# Indexes all images from the catalogue into Elasticsearch
# via the Spring Boot /api/jewelry/index endpoint.
# =============================================================

BACKEND_URL="http://localhost:8080"
CATALOGUE_DIR="/home/shivesh/Desktop/Image2Jewel/catalogue/images"

# Check backend is running
echo "🔍 Checking backend at $BACKEND_URL..."
if ! curl -s --max-time 3 "$BACKEND_URL" > /dev/null 2>&1; then
    echo "❌ Backend is not running at $BACKEND_URL"
    echo "   Start it first: cd backend && ./gradlew bootRun"
    exit 1
fi
echo "✅ Backend is running."
echo ""

SUCCESS=0
FAIL=0
TOTAL=$(ls "$CATALOGUE_DIR"/*.jpg 2>/dev/null | wc -l)

echo "📦 Found $TOTAL images to index."
echo "============================================="

for FILE in "$CATALOGUE_DIR"/*.jpg; do
    FILENAME=$(basename "$FILE")
    
    # Extract category from filename (e.g., "bracelet_abc123.jpg" -> "Bracelet")
    RAW_CATEGORY=$(echo "$FILENAME" | sed 's/_[^_]*$//' | sed 's/_/ /g')
    
    # Capitalize first letter of each word
    CATEGORY=$(echo "$RAW_CATEGORY" | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) tolower(substr($i,2))}1')
    
    # Generate a readable name
    NAME="$CATEGORY $(echo $FILENAME | grep -oP '[A-Za-z0-9]+(?=\.jpg)' | tail -1)"
    
    # Description based on category
    DESCRIPTION="A beautiful $CATEGORY piece from the Image2Jewel catalogue."

    echo -n "  [$((SUCCESS + FAIL + 1))/$TOTAL] Indexing: $FILENAME ($CATEGORY)... "
    
    RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/index_response.txt \
        -X POST "$BACKEND_URL/api/jewelry/index" \
        -F "image=@$FILE" \
        -F "name=$NAME" \
        -F "category=$CATEGORY" \
        -F "description=$DESCRIPTION" \
        --max-time 60)
    
    if [ "$RESPONSE" = "200" ]; then
        echo "✅"
        SUCCESS=$((SUCCESS + 1))
    else
        echo "❌ (HTTP $RESPONSE)"
        cat /tmp/index_response.txt 2>/dev/null
        echo ""
        FAIL=$((FAIL + 1))
    fi
done

echo ""
echo "============================================="
echo "📊 Indexing complete!"
echo "   ✅ Success: $SUCCESS"
echo "   ❌ Failed:  $FAIL"
echo "   📦 Total:   $TOTAL"
echo "============================================="
