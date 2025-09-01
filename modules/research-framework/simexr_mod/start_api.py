#!/usr/bin/env python3
"""
SimExR API Startup Script

This script starts the SimExR API server with proper configuration.
"""

import sys
import os
import argparse
from pathlib import Path

# Add project root to Python path
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))


def main():
    # Set OpenAI API key globally at startup
    # Check if API key is already set in environment
    if os.environ.get("OPENAI_API_KEY"):
        print(f"🔑 OpenAI API key already set in environment: {os.environ['OPENAI_API_KEY'][:10]}...")
        # Ensure this environment variable is used and not overridden by config
        api_key = os.environ["OPENAI_API_KEY"]
    else:
        try:
            from utils.config import settings
            api_key = settings.openai_api_key
            if api_key:
                os.environ["OPENAI_API_KEY"] = api_key
                print(f"🔑 OpenAI API key set from config: {api_key[:10]}...")
            else:
                print("⚠️  Warning: No OpenAI API key found in config")
                api_key = None
        except Exception as e:
            print(f"⚠️  Warning: Could not load OpenAI API key from config: {e}")
            api_key = None
    
    # Force set the environment variable to ensure it's used
    if api_key:
        os.environ["OPENAI_API_KEY"] = api_key
        print(f"🔑 Final OpenAI API key set: {api_key[:10]}...")
    
    parser = argparse.ArgumentParser(description="Start SimExR API Server")
    parser.add_argument("--host", default="0.0.0.0", help="Host to bind to")
    parser.add_argument("--port", type=int, default=8000, help="Port to bind to")
    parser.add_argument("--reload", action="store_true", help="Enable auto-reload for development")
    parser.add_argument("--workers", type=int, default=1, help="Number of worker processes")
    parser.add_argument("--log-level", default="info", help="Log level")
    parser.add_argument("--db-path", help="Path to database file")
    
    args = parser.parse_args()
    
    # Set environment variables
    if args.db_path:
        os.environ["SIMEXR_DATABASE_PATH"] = args.db_path
    
    # Import after setting environment
    import uvicorn
    from api.main import app
    
    print("🚀 Starting SimExR API Server")
    print(f"📡 Host: {args.host}:{args.port}")
    print(f"🔄 Reload: {args.reload}")
    print(f"👥 Workers: {args.workers}")
    print(f"📊 Database: {os.environ.get('SIMEXR_DATABASE_PATH', 'default')}")
    print(f"📖 Docs: http://{args.host}:{args.port}/docs")
    print()
    
    # Start server
    uvicorn.run(
        "api.main:app",
        host=args.host,
        port=args.port,
        reload=args.reload,
        workers=args.workers if not args.reload else 1,  # reload mode requires single worker
        log_level=args.log_level,
        access_log=True
    )


if __name__ == "__main__":
    main()
