"""
FastAPI dependency injection for shared resources.
"""

from fastapi import Request, HTTPException
from typing import Annotated

from db import Database
from core.services import SimulationService, ReasoningService, DataService
from core.patterns import DIContainer


def get_database(request: Request) -> Database:
    """
    Get database instance from application state.
    
    This dependency provides access to the database instance
    that was initialized during application startup.
    """
    if not hasattr(request.app.state, 'db'):
        raise HTTPException(
            status_code=500,
            detail="Database not initialized"
        )
    
    return request.app.state.db


def get_di_container(request: Request) -> DIContainer:
    """Get dependency injection container."""
    if not hasattr(request.app.state, 'di_container'):
        raise HTTPException(
            status_code=500,
            detail="DI Container not initialized"
        )
    
    return request.app.state.di_container


def get_simulation_service(request: Request) -> SimulationService:
    """Get simulation service."""
    container = get_di_container(request)
    return container.get("simulation_service")


def get_reasoning_service(request: Request) -> ReasoningService:
    """Get reasoning service."""
    container = get_di_container(request)
    return container.get("reasoning_service")


def get_data_service(request: Request) -> DataService:
    """Get data service."""
    container = get_di_container(request)
    return container.get("data_service")


# Type aliases for dependency injection
DatabaseDep = Annotated[Database, get_database]
DIContainerDep = Annotated[DIContainer, get_di_container]
SimulationServiceDep = Annotated[SimulationService, get_simulation_service]
ReasoningServiceDep = Annotated[ReasoningService, get_reasoning_service]
DataServiceDep = Annotated[DataService, get_data_service]
