"""
Simulation execution API endpoints.
"""

import time
from typing import List
from fastapi import APIRouter, HTTPException, Depends, Request
from pathlib import Path

from ..models import (
    SingleSimulationRequest, BatchSimulationRequest, SimulationResult,
    BatchSimulationResponse, StatusResponse, ErrorResponse
)
from ..dependencies import get_simulation_service, get_data_service, get_database
from core.interfaces import SimulationStatus


router = APIRouter()


@router.post("/import/github", summary="Import simulation from GitHub")
async def import_from_github(
    github_url: str,
    model_name: str,
    description: str = "",
    simulation_service = Depends(get_simulation_service)
):
    """
    Import a simulation model from a GitHub URL.
    
    - **github_url**: URL to the GitHub script (e.g., https://github.com/user/repo/blob/main/script.py)
    - **model_name**: Name for the imported model
    - **description**: Optional description of the model
    
    Returns the generated model ID.
    """
    try:
        # Extract parameters info from the script if possible
        parameters = {
            "github_url": "Source URL",
            "imported": "Imported from GitHub"
        }
        
        model_id = simulation_service.import_model_from_github(
            github_url=github_url,
            model_name=model_name,
            description=description,
            parameters=parameters
        )
        
        return {
            "status": "success",
            "model_id": model_id,
            "message": f"Successfully imported model from {github_url}",
            "github_url": github_url,
            "model_name": model_name
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to import from GitHub: {str(e)}")


@router.post("/transform/github", summary="Transform GitHub script using transform_code")
async def transform_github_script(
    github_url: str,
    model_name: str,
    max_smoke_iters: int = 3
):
    """
    Transform a GitHub script using the transform_code module.
    
    This endpoint uses ExternalScriptImporter to:
    1. Import the script from GitHub
    2. Refactor it to have a simulate(**params) function
    3. Refine it through smoke testing and fixes
    4. Return the model_id and metadata
    
    - **github_url**: URL to the GitHub script
    - **model_name**: Name for the imported model
    - **max_smoke_iters**: Maximum smoke test iterations (default: 3)
    
    Returns the generated model ID and processing details.
    """
    try:
        print(f"[TRANSFORM API] Starting transform process for {github_url}")
        from execute.loader.transform_code import ExternalScriptImporter
        import tempfile
        import os
        
        # Create importer
        print("[TRANSFORM API] Creating ExternalScriptImporter...")
        importer = ExternalScriptImporter()
        
        # Create temporary directory for processing
        print(f"[TRANSFORM API] Creating temporary directory...")
        with tempfile.TemporaryDirectory() as temp_dir:
            print(f"[TRANSFORM API] Temporary directory created: {temp_dir}")
            # Import and refactor using transform_code
            print(f"[TRANSFORM API] Calling import_and_refactor...")
            model_id, metadata = importer.import_and_refactor(
                source_url=github_url,
                model_name=model_name,
                dest_dir=temp_dir,
                max_smoke_iters=max_smoke_iters
            )
            
            print(f"[TRANSFORM API] Import and refactor completed. Model ID: {model_id}")
            # Get the final script path from the database
            from db import get_simulation_path
            try:
                script_path = get_simulation_path(model_id)
                print(f"[TRANSFORM API] Script path from database: {script_path}")
            except:
                # Fallback to expected path
                script_path = f"external_models/{model_name}.py"
                print(f"[TRANSFORM API] Using fallback script path: {script_path}")
            
            # Read the final script content
            print(f"[TRANSFORM API] Reading script content...")
            with open(script_path, 'r') as f:
                script_content = f.read()
            print(f"[TRANSFORM API] Script content length: {len(script_content)}")
            
            return {
                "status": "success",
                "model_id": model_id,
                "message": f"Successfully transformed script from {github_url}",
                "github_url": github_url,
                "model_name": model_name,
                "script_path": script_path,
                "script_content": script_content,
                "metadata": metadata,
                "processing_details": {
                    "max_smoke_iters": max_smoke_iters,
                    "script_size": len(script_content),
                    "temp_directory": temp_dir
                }
            }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to transform GitHub script: {str(e)}")


@router.post("/run", response_model=SimulationResult, summary="Run single simulation")
async def run_single_simulation(
    request: SingleSimulationRequest,
    simulation_service = Depends(get_simulation_service)
):
    """
    Execute a single simulation with given parameters.
    
    - **model_id**: ID of the simulation model
    - **parameters**: Dictionary of simulation parameters
    
    Returns the simulation result with outputs and execution metadata.
    """
    try:
        # Use the service layer
        result = simulation_service.run_single_simulation(
            model_id=request.model_id,
            parameters=request.parameters.model_dump()
        )
        
        # Convert to API response format
        return SimulationResult(
            success=result.status == SimulationStatus.COMPLETED,
            parameters=result.parameters,
            results=result.outputs,
            execution_time=result.execution_time,
            stdout=result.stdout,
            stderr=result.stderr,
            error_message=result.error_message
        )
        
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"Model {request.model_id} not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Simulation failed: {str(e)}")


@router.post("/batch", response_model=BatchSimulationResponse, summary="Run batch simulations")
async def run_batch_simulation(
    request: BatchSimulationRequest,
    simulation_service = Depends(get_simulation_service)
):
    """
    Execute multiple simulations in batch with different parameter sets.
    
    - **model_id**: ID of the simulation model
    - **parameter_grid**: List of parameter dictionaries
    
    Returns batch execution results with statistics.
    """
    try:
        start_time = time.time()
        
        # Convert parameter grid
        param_grid = [params.model_dump() for params in request.parameter_grid]
        
        # Use the service layer
        results = simulation_service.run_batch_simulations(
            model_id=request.model_id,
            parameter_grid=param_grid
        )
        
        # Convert to API response format
        api_results = []
        for result in results:
            api_result = SimulationResult(
                success=result.status == SimulationStatus.COMPLETED,
                parameters=result.parameters,
                results=result.outputs,
                execution_time=result.execution_time,
                stdout=result.stdout,
                stderr=result.stderr,
                error_message=result.error_message
            )
            api_results.append(api_result)
        
        execution_time = time.time() - start_time
        successful_runs = sum(1 for r in api_results if r.success)
        failed_runs = len(api_results) - successful_runs
        
        return BatchSimulationResponse(
            status="completed",
            total_runs=len(api_results),
            successful_runs=successful_runs,
            failed_runs=failed_runs,
            results=api_results,
            execution_time=execution_time
        )
        
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"Model {request.model_id} not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Batch simulation failed: {str(e)}")


@router.get("/models", summary="List available simulation models")
async def list_models(simulation_service = Depends(get_simulation_service)):
    """
    Get a list of all available simulation models.
    
    Returns list of models with basic information.
    """
    try:
        models = simulation_service.list_models()
        return {
            "status": "success",
            "count": len(models),
            "models": models
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to list models: {str(e)}")


@router.get("/models/search", summary="Search models by name (fuzzy search)")
async def search_models_by_name(
    name: str,
    limit: int = 20,
    simulation_service = Depends(get_simulation_service)
):
    """
    Search for simulation models by name using fuzzy matching.
    
    - **name**: Partial name to search for (case-insensitive)
    - **limit**: Maximum number of results to return (default: 20)
    
    Returns models that match the search criteria.
    """
    try:
        import re
        
        # Get all models
        all_models = simulation_service.list_models()
        
        # Convert search term to lowercase for case-insensitive matching
        search_term = name.lower()
        
        # Filter models using fuzzy matching
        matching_models = []
        for model in all_models:
            model_name = model.get('name', '').lower()
            model_id = model.get('id', '').lower()
            
            # Check if search term appears in model name or ID
            if (search_term in model_name or 
                search_term in model_id or
                any(word in model_name for word in search_term.split()) or
                any(word in model_id for word in search_term.split())):
                matching_models.append(model)
        
        # Sort by relevance (exact matches first, then partial matches)
        def relevance_score(model):
            model_name = model.get('name', '').lower()
            model_id = model.get('id', '').lower()
            
            # Exact match gets highest score
            if search_term == model_name or search_term == model_id:
                return 100
            # Starts with search term
            elif model_name.startswith(search_term) or model_id.startswith(search_term):
                return 90
            # Contains search term
            elif search_term in model_name or search_term in model_id:
                return 80
            # Word boundary matches
            elif any(word in model_name for word in search_term.split()):
                return 70
            else:
                return 50
        
        # Sort by relevance and limit results
        matching_models.sort(key=relevance_score, reverse=True)
        limited_models = matching_models[:limit]
        
        return {
            "status": "success",
            "search_term": name,
            "total_matches": len(matching_models),
            "returned_count": len(limited_models),
            "limit": limit,
            "models": limited_models
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to search models: {str(e)}")


@router.get("/models/{model_id}", summary="Get model information")
async def get_model_info(model_id: str, simulation_service = Depends(get_simulation_service)):
    """
    Get detailed information about a specific simulation model.
    
    - **model_id**: ID of the simulation model
    
    Returns model metadata and script information.
    """
    try:
        model_info = simulation_service.get_model_info(model_id)
        
        return {
            "status": "success",
            "model": model_info
        }
        
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get model info: {str(e)}")


@router.get("/models/{model_id}/results", summary="Get simulation results")
async def get_model_results(
    model_id: str, 
    limit: int = 100,
    offset: int = 0,
    data_service = Depends(get_data_service)
):
    """
    Get simulation results for a specific model.
    
    - **model_id**: ID of the simulation model
    - **limit**: Maximum number of results to return (default: 100)
    - **offset**: Number of results to skip (default: 0)
    
    Returns paginated simulation results.
    """
    try:
        results_data = data_service.get_simulation_results(
            model_id=model_id, 
            limit=limit, 
            offset=offset
        )
        
        return {
            "status": "success",
            **results_data
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get results: {str(e)}")


@router.delete("/models/{model_id}/results", summary="Clear model results")
async def clear_model_results(model_id: str, db = Depends(get_database)):
    """
    Clear all simulation results for a specific model.
    
    - **model_id**: ID of the simulation model
    
    Returns confirmation of deletion.
    """
    try:
        # Delete results from database
        with db.config.get_sqlite_connection() as conn:
            cursor = conn.execute(
                "DELETE FROM results WHERE model_id = ?", 
                (model_id,)
            )
            deleted_count = cursor.rowcount
        
        return {
            "status": "success",
            "message": f"Deleted {deleted_count} results for model {model_id}",
            "deleted_count": deleted_count
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to clear results: {str(e)}")


@router.get("/models/{model_id}/script", summary="Get model script")
async def get_model_script(model_id: str, simulation_service = Depends(get_simulation_service)):
    """
    Get the refactored script for a specific model.
    
    - **model_id**: ID of the simulation model
    
    Returns the script content.
    """
    try:
        model_info = simulation_service.get_model_info(model_id)
        script_path = model_info.get("script_path")
        
        # Try to find the script in common locations if script_path is not available
        if not script_path:
            # Look for script in external_models directory
            possible_paths = [
                f"external_models/{model_id}.py",
                f"external_models/{model_info.get('name', model_id)}.py",
                f"systems/models/{model_id}.py",
                f"systems/models/{model_info.get('name', model_id)}.py"
            ]
            
            for path in possible_paths:
                if Path(path).exists():
                    script_path = path
                    break
        
        if not script_path or not Path(script_path).exists():
            raise HTTPException(status_code=404, detail=f"Script not found for model {model_id}")
        
        # Read the script file
        with open(script_path, 'r') as f:
            script_content = f.read()
        
        return {
            "status": "success",
            "model_id": model_id,
            "script": script_content,
            "script_path": script_path,
            "is_placeholder": False
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get script: {str(e)}")


@router.post("/models/{model_id}/script", summary="Save model script")
async def save_model_script(
    model_id: str, 
    script_data: dict,
    simulation_service = Depends(get_simulation_service)
):
    """
    Save the modified script for a specific model.
    
    - **model_id**: ID of the simulation model
    - **script_data**: Dictionary containing the script content
    
    Returns confirmation of save.
    """
    try:
        model_info = simulation_service.get_model_info(model_id)
        script_path = model_info.get("script_path")
        
        # If no script path exists, create one in external_models directory
        if not script_path:
            script_path = f"external_models/{model_id}.py"
            # Ensure the directory exists
            Path("external_models").mkdir(exist_ok=True)
        
        script_content = script_data.get("script")
        if not script_content:
            raise HTTPException(status_code=400, detail="Script content is required")
        
        # Write the script to file
        with open(script_path, 'w') as f:
            f.write(script_content)
        
        # Update the model info with the new script path if it wasn't set before
        if not model_info.get("script_path"):
            # This would require updating the database, but for now we'll just return success
            # In a full implementation, you'd update the model metadata in the database
            pass
        
        return {
            "status": "success",
            "model_id": model_id,
            "message": f"Script saved successfully for model {model_id}",
            "script_path": script_path
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to save script: {str(e)}")
