"""
SimExR API - FastAPI application for simulation execution and reasoning.

This module provides REST APIs for:
- Simulation execution and batch processing
- Reasoning agent interactions
- Database operations and results management
- System health and testing
"""

from .main import app

__all__ = ["app"]
