import sys
sys.path.append('/')
import airavata_magics

def load_extension():
    try:
        airavata_magics.load_ipython_extension(get_ipython())
        print("Airavata magics successfully loaded!")
    except Exception as e:
        print(f"Failed to load Airavata magics: {e}")

load_extension()
