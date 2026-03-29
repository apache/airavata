"""
Main FastAPI application for SimExR.
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from db import Database, DatabaseConfig
from core.services import SimulationService, ReasoningService, DataService, ServiceConfiguration
from core.patterns import DIContainer
from .routers import simulation, reasoning, database, health
from .config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan management."""
    # Startup
    print("🚀 Starting SimExR API...")
    
    # Initialize configuration
    service_config = ServiceConfiguration(
        database_path=settings.database_path,
        models_directory=settings.models_dir,
        results_directory=settings.results_media_dir,
        default_timeout=settings.max_simulation_timeout,
        max_batch_size=settings.max_batch_size,
        enable_logging=True
    )
    
    # Initialize dependency injection container
    di_container = DIContainer()
    
    # Register services
    di_container.register_singleton("simulation_service", 
                                  lambda: SimulationService(service_config))
    di_container.register_singleton("reasoning_service", 
                                  lambda: ReasoningService(service_config))
    di_container.register_singleton("data_service", 
                                  lambda: DataService(service_config))
    
    # Initialize database (for backward compatibility)
    db_config = DatabaseConfig(database_path=settings.database_path)
    db = Database(db_config)
    di_container.register_instance("database", db)
    
    # Store in app state
    app.state.db = db
    app.state.di_container = di_container
    app.state.service_config = service_config
    
    print("✅ Services initialized")
    print(f"📊 Database path: {settings.database_path}")
    print(f"🔧 Models directory: {settings.models_dir}")
    
    yield
    
    # Shutdown
    print("🛑 Shutting down SimExR API...")
    
    # Cleanup services
    try:
        simulation_service = di_container.get("simulation_service")
        simulation_service.cleanup()
    except:
        pass


# Create FastAPI app
app = FastAPI(
    title="SimExR API",
    description="Simulation Execution and Reasoning API",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure appropriately for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router, prefix="/health", tags=["Health"])
app.include_router(simulation.router, prefix="/simulation", tags=["Simulation"])
app.include_router(reasoning.router, prefix="/reasoning", tags=["Reasoning"])
app.include_router(database.router, prefix="/database", tags=["Database"])


@app.get("/", summary="Root endpoint")
async def root():
    """Root endpoint with API information."""
    return {
        "message": "Welcome to SimExR API",
        "version": "1.0.0",
        "docs": "/docs",
        "health": "/health/status"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "api.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
