"""
Database management API endpoints.
"""

import json
import tempfile
from typing import List, Optional
from pathlib import Path
from fastapi import APIRouter, HTTPException, Depends, UploadFile, File

from db import store_simulation_script
from ..models import (
    StoreModelRequest, StoreModelResponse, ModelInfo, ResultsQuery, 
    ResultsResponse, StatusResponse
)
from ..dependencies import get_database


router = APIRouter()


@router.post("/models", response_model=StoreModelResponse, summary="Store new simulation model")
async def store_model(request: StoreModelRequest, db = Depends(get_database)):
    """
    Store a new simulation model in the database.
    
    - **model_name**: Name of the simulation model
    - **metadata**: Model metadata and configuration
    - **script_content**: Python script content for the simulation
    
    Returns the generated model ID.
    """
    try:
        # Create temporary script file
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write(request.script_content)
            temp_script_path = f.name
        
        try:
            # Store in database
            model_id = store_simulation_script(
                model_name=request.model_name,
                metadata=request.metadata.model_dump(),
                script_path=temp_script_path
            )
            
            return StoreModelResponse(
                model_id=model_id,
                status="success",
                message=f"Model {request.model_name} stored successfully"
            )
            
        finally:
            # Clean up temporary file
            Path(temp_script_path).unlink(missing_ok=True)
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to store model: {str(e)}")


@router.post("/models/upload", response_model=StoreModelResponse, summary="Upload simulation model file")
async def upload_model(
    model_name: str,
    metadata: str,  # JSON string
    script_file: UploadFile = File(...),
    db = Depends(get_database)
):
    """
    Upload a simulation model from a file.
    
    - **model_name**: Name of the simulation model
    - **metadata**: JSON string containing model metadata
    - **script_file**: Python script file (.py)
    
    Returns the generated model ID.
    """
    try:
        # Validate file type
        if not script_file.filename.endswith('.py'):
            raise HTTPException(status_code=400, detail="File must be a Python script (.py)")
        
        # Parse metadata
        try:
            metadata_dict = json.loads(metadata)
        except json.JSONDecodeError:
            raise HTTPException(status_code=400, detail="Invalid JSON in metadata")
        
        # Read script content
        script_content = await script_file.read()
        script_content = script_content.decode('utf-8')
        
        # Create temporary script file
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write(script_content)
            temp_script_path = f.name
        
        try:
            # Store in database
            model_id = store_simulation_script(
                model_name=model_name,
                metadata=metadata_dict,
                script_path=temp_script_path
            )
            
            return StoreModelResponse(
                model_id=model_id,
                status="success",
                message=f"Model {model_name} uploaded successfully"
            )
            
        finally:
            # Clean up temporary file
            Path(temp_script_path).unlink(missing_ok=True)
            
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to upload model: {str(e)}")


@router.get("/models", summary="List all models")
async def list_all_models(
    limit: int = 100,
    offset: int = 0,
    db = Depends(get_database)
):
    """
    Get a list of all simulation models.
    
    - **limit**: Maximum number of models to return (default: 100)
    - **offset**: Number of models to skip (default: 0)
    
    Returns paginated list of models.
    """
    try:
        models = db.simulation_repository.list()
        
        # Apply pagination
        total_count = len(models)
        models_page = models[offset:offset + limit]
        
        return {
            "status": "success",
            "total_count": total_count,
            "limit": limit,
            "offset": offset,
            "models": models_page
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to list models: {str(e)}")


@router.get("/models/{model_id}", summary="Get model details")
async def get_model_details(model_id: str, db = Depends(get_database)):
    """
    Get detailed information about a specific model.
    
    - **model_id**: ID of the simulation model
    
    Returns model metadata, script information, and statistics.
    """
    try:
        # Get model info
        models = db.simulation_repository.list({"id": model_id})
        if not models:
            raise HTTPException(status_code=404, detail=f"Model {model_id} not found")
        
        model_info = models[0]
        
        # Get result statistics
        with db.config.get_sqlite_connection() as conn:
            stats = conn.execute("""
                SELECT 
                    COUNT(*) as total_runs,
                    MIN(ts) as first_run,
                    MAX(ts) as last_run
                FROM results 
                WHERE model_id = ?
            """, (model_id,)).fetchone()
            
            reasoning_stats = conn.execute("""
                SELECT COUNT(*) as conversation_count
                FROM reasoning_agent 
                WHERE model_id = ?
            """, (model_id,)).fetchone()
        
        return {
            "status": "success",
            "model": model_info,
            "statistics": {
                "total_simulation_runs": stats["total_runs"] if stats else 0,
                "first_run": stats["first_run"] if stats else None,
                "last_run": stats["last_run"] if stats else None,
                "reasoning_conversations": reasoning_stats["conversation_count"] if reasoning_stats else 0
            }
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get model details: {str(e)}")


@router.delete("/models/{model_id}", summary="Delete model")
async def delete_model(model_id: str, db = Depends(get_database)):
    """
    Delete a simulation model and all associated data.
    
    - **model_id**: ID of the simulation model
    
    Returns confirmation of deletion.
    """
    try:
        with db.config.get_sqlite_connection() as conn:
            # Check if model exists
            model = conn.execute(
                "SELECT id FROM simulations WHERE id = ?",
                (model_id,)
            ).fetchone()
            
            if not model:
                raise HTTPException(status_code=404, detail=f"Model {model_id} not found")
            
            # Delete associated data
            results_deleted = conn.execute(
                "DELETE FROM results WHERE model_id = ?",
                (model_id,)
            ).rowcount
            
            reasoning_deleted = conn.execute(
                "DELETE FROM reasoning_agent WHERE model_id = ?",
                (model_id,)
            ).rowcount
            
            # Delete model
            conn.execute("DELETE FROM simulations WHERE id = ?", (model_id,))
        
        return {
            "status": "success",
            "message": f"Model {model_id} deleted successfully",
            "deleted_results": results_deleted,
            "deleted_conversations": reasoning_deleted
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to delete model: {str(e)}")


@router.get("/results", summary="Get all simulation results")
async def get_all_results(
    model_id: Optional[str] = None,
    limit: int = 100,
    offset: int = 0,
    db = Depends(get_database)
):
    """
    Get simulation results across all or specific models.
    
    - **model_id**: Optional filter by model ID
    - **limit**: Maximum number of results to return (default: 100)
    - **offset**: Number of results to skip (default: 0)
    
    Returns paginated simulation results.
    """
    try:
        if model_id:
            # Load results for specific model
            df = db.results_service.load_results(model_id=model_id)
        else:
            # Load all results
            df = db.results_service.load_results()
        
        # Apply pagination
        total_count = len(df)
        df_page = df.iloc[offset:offset + limit]
        
        # Clean NaN values for JSON serialization
        import numpy as np
        
        def clean_nan_values(obj):
            """Recursively replace NaN values with None for JSON serialization."""
            if isinstance(obj, dict):
                return {k: clean_nan_values(v) for k, v in obj.items()}
            elif isinstance(obj, list):
                return [clean_nan_values(item) for item in obj]
            elif isinstance(obj, (np.floating, float)) and np.isnan(obj):
                return None
            elif isinstance(obj, np.integer):
                return int(obj)
            elif isinstance(obj, np.floating):
                return float(obj)
            elif isinstance(obj, np.ndarray):
                return clean_nan_values(obj.tolist())
            else:
                return obj
        
        # Convert to records and clean NaN values
        results = df_page.to_dict('records')
        cleaned_results = clean_nan_values(results)
        
        # Log preview of results
        print(f"=== DATABASE RESULTS PREVIEW (First 5 rows) ===")
        print(f"Total count: {total_count}, Limit: {limit}, Offset: {offset}")
        for i, result in enumerate(cleaned_results[:5]):
            print(f"Row {i+1}: {list(result.keys())}")
            # Show key values for first few rows
            if i < 3:  # Show more details for first 3 rows
                for key, value in list(result.items())[:10]:  # First 10 keys
                    if isinstance(value, (list, tuple)) and len(value) > 5:
                        print(f"  {key}: {type(value).__name__} with {len(value)} items (first 3: {value[:3]})")
                    elif isinstance(value, dict):
                        print(f"  {key}: dict with {len(value)} keys")
                    else:
                        print(f"  {key}: {value}")
        print(f"=== END DATABASE RESULTS PREVIEW ===")
        
        return {
            "status": "success",
            "total_count": total_count,
            "limit": limit,
            "offset": offset,
            "model_id": model_id,
            "results": cleaned_results
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get results: {str(e)}")


@router.get("/stats", summary="Get database statistics")
async def get_database_stats(db = Depends(get_database)):
    """
    Get overall database statistics.
    
    Returns counts and statistics for all database entities.
    """
    try:
        with db.config.get_sqlite_connection() as conn:
            # Model statistics
            models_stats = conn.execute("""
                SELECT 
                    COUNT(*) as total_models,
                    MIN(created_at) as first_model,
                    MAX(created_at) as last_model
                FROM simulations
            """).fetchone()
            
            # Results statistics
            results_stats = conn.execute("""
                SELECT 
                    COUNT(*) as total_results,
                    COUNT(DISTINCT model_id) as models_with_results,
                    MIN(ts) as first_result,
                    MAX(ts) as last_result
                FROM results
            """).fetchone()
            
            # Reasoning statistics
            reasoning_stats = conn.execute("""
                SELECT 
                    COUNT(*) as total_conversations,
                    COUNT(DISTINCT model_id) as models_with_conversations,
                    MIN(ts) as first_conversation,
                    MAX(ts) as last_conversation
                FROM reasoning_agent
            """).fetchone()
            
            # Database size (SQLite specific)
            size_stats = conn.execute("PRAGMA page_count").fetchone()
            page_size = conn.execute("PRAGMA page_size").fetchone()
            
            db_size_bytes = (size_stats[0] if size_stats else 0) * (page_size[0] if page_size else 0)
            db_size_mb = round(db_size_bytes / (1024 * 1024), 2)
        
        return {
            "status": "success",
            "database": {
                "path": db.config.database_path,
                "size_mb": db_size_mb
            },
            "models": {
                "total": models_stats["total_models"] if models_stats else 0,
                "first_created": models_stats["first_model"] if models_stats else None,
                "last_created": models_stats["last_model"] if models_stats else None
            },
            "results": {
                "total": results_stats["total_results"] if results_stats else 0,
                "models_with_results": results_stats["models_with_results"] if results_stats else 0,
                "first_result": results_stats["first_result"] if results_stats else None,
                "last_result": results_stats["last_result"] if results_stats else None
            },
            "reasoning": {
                "total_conversations": reasoning_stats["total_conversations"] if reasoning_stats else 0,
                "models_with_conversations": reasoning_stats["models_with_conversations"] if reasoning_stats else 0,
                "first_conversation": reasoning_stats["first_conversation"] if reasoning_stats else None,
                "last_conversation": reasoning_stats["last_conversation"] if reasoning_stats else None
            }
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get database stats: {str(e)}")


@router.post("/backup", summary="Create database backup")
async def create_backup(db = Depends(get_database)):
    """
    Create a backup of the database.
    
    Returns the backup file path and statistics.
    """
    try:
        import shutil
        from datetime import datetime
        
        # Create backup filename with timestamp
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        backup_path = f"{db.config.database_path}.backup_{timestamp}"
        
        # Copy database file
        shutil.copy2(db.config.database_path, backup_path)
        
        # Get backup file size
        backup_size = Path(backup_path).stat().st_size
        backup_size_mb = round(backup_size / (1024 * 1024), 2)
        
        return {
            "status": "success",
            "message": "Database backup created successfully",
            "backup_path": backup_path,
            "backup_size_mb": backup_size_mb,
            "timestamp": timestamp
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to create backup: {str(e)}")
