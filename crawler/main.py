import os
import requests
import time
import argparse
from pathlib import Path
from dotenv import load_dotenv

try:
    from tqdm import tqdm
except ImportError:
    def tqdm(iterable, **kwargs):
        return iterable

# Load environment variables
load_dotenv()

def fetch_with_retry(url, headers=None, params=None, stream=False, retries=3):
    """
    Helper to fetch data with a simple retry mechanism for transient network errors.
    """
    for i in range(retries):
        try:
            response = requests.get(url, headers=headers, params=params, stream=stream, timeout=15)
            return response
        except (requests.exceptions.ConnectionError, requests.exceptions.Timeout) as e:
            if i == retries - 1:
                raise e
            print(f"\nNetwork issue, retrying in 2s... ({i+1}/{retries})")
            time.sleep(2)
    return None

def download_jewelry_images(query="jewelry", limit=10, output_dir=None, access_key=None, use_random=False):
    """
    Downloads jewelry images using the official Unsplash API with retries and robust pathing.
    """
    # Resolve output directory relative to the script's location if not provided
    if output_dir is None:
        base_dir = Path(__file__).resolve().parent.parent
        output_path = base_dir / "catalogue" / "images"
    else:
        output_path = Path(output_dir)

    if not output_path.exists():
        output_path.mkdir(parents=True, exist_ok=True)

    key = access_key or os.getenv("UNSPLASH_ACCESS_KEY")
    if not key:
        print("Error: Unsplash Access Key is missing.")
        print("Please set UNSPLASH_ACCESS_KEY in your .env file.")
        return

    # Setup API request
    if use_random:
        api_url = "https://api.unsplash.com/photos/random"
        params = {"query": query, "count": min(limit, 30), "orientation": "squarish"}
    else:
        api_url = "https://api.unsplash.com/search/photos"
        params = {"query": query, "per_page": min(limit, 30), "orientation": "squarish"}

    headers = {"Authorization": f"Client-ID {key}", "Accept-Version": "v1"}

    print(f"Searching for '{query}' via Unsplash API ({'Random' if use_random else 'Search'} mode)...")
    
    try:
        response = fetch_with_retry(api_url, headers=headers, params=params)
        if response.status_code == 401:
            print("Error: 401 Unauthorized. Check your Access Key.")
            return
        elif response.status_code != 200:
            print(f"API Error {response.status_code}: {response.text}")
            return
        
        data = response.json()
        photos = data if use_random else data.get('results', [])
        
    except Exception as e:
        print(f"Connection failed for query '{query}': {e}")
        return

    if not photos:
        print(f"No photos found for '{query}'.")
        return

    print(f"Found {len(photos)} images. Starting download...")
    downloaded_count = 0

    for photo in tqdm(photos, desc=f"Downloading {query}"):
        photo_id = photo['id']
        download_url = photo['urls']['regular']
        
        try:
            img_res = fetch_with_retry(download_url, stream=True)
            if img_res and img_res.status_code == 200:
                file_name = f"{query.replace(' ', '_')}_{photo_id}.jpg"
                file_full_path = output_path / file_name
                with open(file_full_path, 'wb') as f:
                    for chunk in img_res.iter_content(chunk_size=8192):
                        f.write(chunk)
                downloaded_count += 1
            else:
                status = img_res.status_code if img_res else "Failed"
                print(f"\nFailed to download {photo_id}. Status: {status}")
        except Exception as e:
            print(f"\nError downloading {photo_id}: {e}")

    print(f"Successfully downloaded {downloaded_count} images to {output_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Robust Jewelry Image Crawler")
    parser.add_argument("--query", type=str, default="jewelry")
    parser.add_argument("--limit", type=int, default=10)
    parser.add_argument("--output", type=str, default=None)
    parser.add_argument("--key", type=str)
    parser.add_argument("--random", action="store_true")

    args = parser.parse_args()
    
    download_jewelry_images(
        query=args.query, 
        limit=args.limit, 
        output_dir=args.output, 
        access_key=args.key,
        use_random=args.random
    )
