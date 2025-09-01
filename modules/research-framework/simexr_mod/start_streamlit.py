#!/usr/bin/env python3
"""
SimExR Streamlit App Launcher

This script starts the Streamlit app with the correct configuration.
Make sure the API server is running before starting the Streamlit app.
"""

import subprocess
import sys
import time
import requests
from pathlib import Path

def check_api_server():
    """Check if the API server is running."""
    try:
        response = requests.get("http://127.0.0.1:8001/health/status", timeout=5)
        return response.status_code == 200
    except:
        return False

def main():
    print("🚀 SimExR Streamlit App Launcher")
    print("=" * 40)
    
    # Check if API server is running
    print("🔍 Checking API server status...")
    if not check_api_server():
        print("❌ API server is not running!")
        print("💡 Please start the API server first with:")
        print("   python start_api.py --host 127.0.0.1 --port 8001")
        print("\n🔄 Starting API server automatically...")
        
        # Try to start the API server
        try:
            api_process = subprocess.Popen([
                sys.executable, "start_api.py", "--host", "127.0.0.1", "--port", "8001"
            ], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            
            # Wait for server to start
            print("⏳ Waiting for API server to start...")
            for i in range(30):  # Wait up to 30 seconds
                time.sleep(1)
                if check_api_server():
                    print("✅ API server started successfully!")
                    break
            else:
                print("❌ Failed to start API server")
                return 1
                
        except Exception as e:
            print(f"❌ Error starting API server: {e}")
            return 1
    else:
        print("✅ API server is running!")
    
    # Check if app.py exists
    if not Path("app.py").exists():
        print("❌ app.py not found!")
        print("💡 Make sure you're in the correct directory")
        return 1
    
    # Start Streamlit app
    print("\n🌐 Starting Streamlit app...")
    print("📱 The app will be available at: http://localhost:8501")
    print("🔗 API server: http://127.0.0.1:8001")
    print("\n" + "=" * 40)
    
    try:
        # Start Streamlit with the app
        subprocess.run([
            sys.executable, "-m", "streamlit", "run", "app.py",
            "--server.port", "8501",
            "--server.address", "localhost",
            "--browser.gatherUsageStats", "false"
        ])
    except KeyboardInterrupt:
        print("\n🛑 Streamlit app stopped by user")
    except Exception as e:
        print(f"❌ Error starting Streamlit app: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main())
