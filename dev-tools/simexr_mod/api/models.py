"""
Pydantic models for API request/response validation.
"""

from typing import Dict, List, Any, Optional, Union
from pydantic import BaseModel, Field, validator
from datetime import datetime
from enum import Enum


class StatusResponse(BaseModel):
    """Standard status response."""
    status: str
    message: str
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class ErrorResponse(BaseModel):
    """Standard error response."""
    error: str
    detail: str
    timestamp: datetime = Field(default_factory=datetime.utcnow)


# Simulation Models
class SimulationParameters(BaseModel):
    """Parameters for simulation execution."""
    model_config = {"extra": "allow"}  # Allow additional parameters
    
    @validator('*', pre=True)
    def convert_to_number(cls, v):
        """Convert string numbers to float/int."""
        if isinstance(v, str) and v.replace('.', '').replace('-', '').isdigit():
            if '.' in v:
                return float(v)
            return int(v)
        return v


class SingleSimulationRequest(BaseModel):
    """Request for single simulation execution."""
    model_id: str = Field(..., description="ID of the simulation model")
    parameters: SimulationParameters = Field(..., description="Simulation parameters")


class BatchSimulationRequest(BaseModel):
    """Request for batch simulation execution."""
    model_id: str = Field(..., description="ID of the simulation model")
    parameter_grid: List[SimulationParameters] = Field(..., description="List of parameter sets")
    
    @validator('parameter_grid')
    def validate_grid_size(cls, v):
        if len(v) > 1000:  # Max batch size
            raise ValueError("Batch size cannot exceed 1000")
        return v


class SimulationResult(BaseModel):
    """Result from simulation execution."""
    success: bool
    parameters: Dict[str, Any]
    results: Dict[str, Any]
    execution_time: float
    stdout: str = ""
    stderr: str = ""
    error_message: Optional[str] = None


class BatchSimulationResponse(BaseModel):
    """Response for batch simulation."""
    status: str
    total_runs: int
    successful_runs: int
    failed_runs: int
    results: List[SimulationResult]
    execution_time: float


# Reasoning Models
class ReasoningRequest(BaseModel):
    """Request for reasoning agent."""
    model_id: str = Field(..., description="ID of the model to analyze")
    question: str = Field(..., min_length=1, description="Question to ask the reasoning agent")
    max_steps: Optional[int] = Field(20, ge=1, le=50, description="Maximum reasoning steps")


class ReasoningResponse(BaseModel):
    """Response from reasoning agent."""
    answer: str
    model_id: str
    question: str
    history: List[Dict[str, Any]]
    code_map: Dict[int, str]
    images: List[str]
    execution_time: float


# Database Models
class ModelMetadata(BaseModel):
    """Metadata for simulation model."""
    model_name: str
    description: Optional[str] = None
    parameters: Dict[str, Any] = {}
    author: Optional[str] = None
    version: Optional[str] = "1.0"
    tags: List[str] = []


class StoreModelRequest(BaseModel):
    """Request to store a new simulation model."""
    model_name: str = Field(..., min_length=1)
    metadata: ModelMetadata
    script_content: str = Field(..., min_length=1, description="Python script content")


class StoreModelResponse(BaseModel):
    """Response after storing a model."""
    model_id: str
    status: str
    message: str


class ModelInfo(BaseModel):
    """Information about a simulation model."""
    id: str
    name: str
    metadata: Dict[str, Any]
    script_path: str
    created_at: Optional[str] = None


class ResultsQuery(BaseModel):
    """Query parameters for results."""
    model_id: Optional[str] = None
    limit: Optional[int] = Field(100, ge=1, le=10000)
    offset: Optional[int] = Field(0, ge=0)


class ResultsResponse(BaseModel):
    """Response with simulation results."""
    total_count: int
    results: List[Dict[str, Any]]
    model_id: Optional[str] = None


# Health Check Models
class HealthStatus(str, Enum):
    """Health status enumeration."""
    HEALTHY = "healthy"
    UNHEALTHY = "unhealthy"
    DEGRADED = "degraded"


class ComponentHealth(BaseModel):
    """Health status of a component."""
    name: str
    status: HealthStatus
    message: str
    last_check: datetime


class HealthResponse(BaseModel):
    """Overall health response."""
    status: HealthStatus
    components: List[ComponentHealth]
    timestamp: datetime = Field(default_factory=datetime.utcnow)


# Test Models
class TestRequest(BaseModel):
    """Request for testing functionality."""
    test_type: str = Field(..., description="Type of test to run")
    parameters: Dict[str, Any] = Field(default_factory=dict)


class TestResponse(BaseModel):
    """Response from test execution."""
    test_type: str
    success: bool
    message: str
    details: Dict[str, Any] = Field(default_factory=dict)
    execution_time: float
