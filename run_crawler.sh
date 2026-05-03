#!/bin/bash
# Get the directory where the script is located
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Install dependencies
pip install -r "$PROJECT_ROOT/crawler/requirements.txt"

# Check for API Key (it will also look in .env automatically)
# We don't exit here because main.py handles the check better

# Run the crawler for different categories
# We run it from the project root
python3 "$PROJECT_ROOT/crawler/main.py" --query "jewelry" --limit 10
python3 "$PROJECT_ROOT/crawler/main.py" --query "diamond ring" --limit 10
python3 "$PROJECT_ROOT/crawler/main.py" --query "gold necklace" --limit 10
python3 "$PROJECT_ROOT/crawler/main.py" --query "bracelet" --limit 10
python3 "$PROJECT_ROOT/crawler/main.py" --query "earrings" --limit 10
