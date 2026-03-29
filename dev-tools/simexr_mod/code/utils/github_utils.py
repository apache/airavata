import requests
from pathlib import Path

def fetch_notebook_from_github(github_url: str, dest_dir: str = "external_models") -> str:
    """
    Downloads a file from a GitHub URL and saves it locally.
    Handles both raw URLs and blob URLs.
    Returns the local path to the saved file.
    """
    # Convert GitHub blob URL to raw URL if needed
    if "github.com" in github_url and "/blob/" in github_url:
        raw_url = github_url.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")
    else:
        raw_url = github_url
    
    print(f"[GITHUB_UTILS] Converting {github_url} to {raw_url}")
    
    resp = requests.get(raw_url)
    resp.raise_for_status()

    Path(dest_dir).mkdir(exist_ok=True, parents=True)
    filename = Path(raw_url).name
    local_path = Path(dest_dir) / filename
    local_path.write_bytes(resp.content)
    print(f"[GITHUB_UTILS] Downloaded file to {local_path}")
    return str(local_path)
