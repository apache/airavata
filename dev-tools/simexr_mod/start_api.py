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
    # Clear any cached environment variables and set OpenAI API key globally
    print("🧹 Clearing environment variable cache...")
    
    # Clear any existing OpenAI-related environment variables
    openai_vars_to_clear = [
        "OPENAI_API_KEY", "OPENAI_API_KEY_OLD", "OPENAI_API_KEY_CACHE",
        "PYTHONPATH", "PYTHONHOME", "PYTHONUNBUFFERED"
    ]
    
    for var in openai_vars_to_clear:
        if var in os.environ:
            old_value = os.environ.pop(var)
            print(f"🗑️  Cleared {var}: {old_value[:20] if old_value else 'None'}...")
    
    # Force reload any cached modules that might have old API keys
    import importlib
    modules_to_reload = ['openai', 'utils.config']
    for module_name in modules_to_reload:
        if module_name in sys.modules:
            importlib.reload(sys.modules[module_name])
            print(f"🔄 Reloaded module: {module_name}")
    
    # Now set the OpenAI API key from config using the dedicated module
    try:
        from utils.openai_config import ensure_openai_api_key
        api_key = ensure_openai_api_key()
        print("✅ OpenAI API key configuration completed successfully")
    except Exception as e:
        print(f"❌ Error configuring OpenAI API key: {e}")
        api_key = None
    
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
