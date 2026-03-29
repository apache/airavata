#!/usr/bin/env python3
"""
OpenAI API Key Configuration Manager

This module ensures the OpenAI API key is properly set and available throughout the application.
"""

import os
import openai
from .config import settings

def ensure_openai_api_key():
    """
    Ensure the OpenAI API key is set globally.
    This function should be called at startup and whenever the API key needs to be refreshed.
    """
    # Get API key from config
    api_key = settings.openai_api_key
    
    if not api_key:
        raise ValueError("No OpenAI API key found in configuration")
    
    # Set in environment
    os.environ["OPENAI_API_KEY"] = api_key
    
    # Set in openai module
    openai.api_key = api_key
    
    print(f"🔑 OpenAI API key configured globally: {api_key[:20]}...")
    
    return api_key

def get_openai_api_key():
    """
    Get the current OpenAI API key.
    """
    return openai.api_key or os.environ.get("OPENAI_API_KEY")

def is_openai_configured():
    """
    Check if OpenAI is properly configured.
    """
    api_key = get_openai_api_key()
    return bool(api_key and api_key.startswith("sk-"))

# Initialize at module import
try:
    ensure_openai_api_key()
except Exception as e:
    print(f"⚠️  Warning: Could not initialize OpenAI API key: {e}")
