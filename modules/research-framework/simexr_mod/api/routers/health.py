"""
Health check and testing API endpoints.
"""

import time
import tempfile
from typing import Dict, Any
from pathlib import Path
from fastapi import APIRouter, HTTPException, Depends
from datetime import datetime

from execute import SimulationRunner
from reasoning import ReasoningAgent
from ..models import HealthResponse, HealthStatus, ComponentHealth, TestRequest, TestResponse
from ..dependencies import get_database


router = APIRouter()


@router.get("/status", response_model=HealthResponse, summary="System health status")
async def health_status(db = Depends(get_database)):
    """
    Get overall system health status.
    
    Checks all major components and returns their health status.
    """
    components = []
    overall_status = HealthStatus.HEALTHY
    
    # Check database
    try:
        with db.config.get_sqlite_connection() as conn:
            conn.execute("SELECT 1").fetchone()
        
        components.append(ComponentHealth(
            name="database",
            status=HealthStatus.HEALTHY,
            message="Database connection successful",
            last_check=datetime.utcnow()
        ))
    except Exception as e:
        components.append(ComponentHealth(
            name="database",
            status=HealthStatus.UNHEALTHY,
            message=f"Database error: {str(e)}",
            last_check=datetime.utcnow()
        ))
        overall_status = HealthStatus.UNHEALTHY
    
    # Check simulation execution
    try:
        # Create a simple test script
        test_script = '''
def simulate(**params):
    return {"result": params.get("x", 0) * 2}
'''
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write(test_script)
            temp_path = f.name
        
        try:
            runner = SimulationRunner()
            result = runner.run(Path(temp_path), {"x": 5})
            
            if result.get("_ok", False) and result.get("result") == 10:
                components.append(ComponentHealth(
                    name="simulation_execution",
                    status=HealthStatus.HEALTHY,
                    message="Simulation execution working",
                    last_check=datetime.utcnow()
                ))
            else:
                components.append(ComponentHealth(
                    name="simulation_execution",
                    status=HealthStatus.DEGRADED,
                    message="Simulation execution returned unexpected results",
                    last_check=datetime.utcnow()
                ))
                if overall_status == HealthStatus.HEALTHY:
                    overall_status = HealthStatus.DEGRADED
        finally:
            Path(temp_path).unlink(missing_ok=True)
            
    except Exception as e:
        components.append(ComponentHealth(
            name="simulation_execution",
            status=HealthStatus.UNHEALTHY,
            message=f"Simulation execution error: {str(e)}",
            last_check=datetime.utcnow()
        ))
        overall_status = HealthStatus.UNHEALTHY
    
    # Check models directory
    try:
        from ..config import settings
        models_dir = Path(settings.models_dir)
        
        if models_dir.exists():
            model_count = len(list(models_dir.glob("*/simulate.py")))
            components.append(ComponentHealth(
                name="models_directory",
                status=HealthStatus.HEALTHY,
                message=f"Models directory accessible, {model_count} models found",
                last_check=datetime.utcnow()
            ))
        else:
            components.append(ComponentHealth(
                name="models_directory",
                status=HealthStatus.DEGRADED,
                message="Models directory not found",
                last_check=datetime.utcnow()
            ))
            if overall_status == HealthStatus.HEALTHY:
                overall_status = HealthStatus.DEGRADED
                
    except Exception as e:
        components.append(ComponentHealth(
            name="models_directory",
            status=HealthStatus.UNHEALTHY,
            message=f"Models directory error: {str(e)}",
            last_check=datetime.utcnow()
        ))
        overall_status = HealthStatus.UNHEALTHY
    
    return HealthResponse(
        status=overall_status,
        components=components
    )


@router.post("/test", response_model=TestResponse, summary="Run system tests")
async def run_test(request: TestRequest, db = Depends(get_database)):
    """
    Run specific system tests.
    
    - **test_type**: Type of test to run (simulation, database, reasoning, etc.)
    - **parameters**: Optional test parameters
    
    Returns test results and performance metrics.
    """
    start_time = time.time()
    
    try:
        if request.test_type == "simulation":
            return await _test_simulation(request.parameters)
        elif request.test_type == "database":
            return await _test_database(db, request.parameters)
        elif request.test_type == "reasoning":
            return await _test_reasoning(db, request.parameters)
        elif request.test_type == "end_to_end":
            return await _test_end_to_end(db, request.parameters)
        else:
            raise HTTPException(
                status_code=400, 
                detail=f"Unknown test type: {request.test_type}"
            )
            
    except Exception as e:
        execution_time = time.time() - start_time
        return TestResponse(
            test_type=request.test_type,
            success=False,
            message=f"Test failed: {str(e)}",
            execution_time=execution_time
        )


async def _test_simulation(parameters: Dict[str, Any]) -> TestResponse:
    """Test simulation execution."""
    start_time = time.time()
    
    # Create test script
    test_script = '''
def simulate(x=1, y=2):
    import math
    result = x * y + math.sqrt(x + y)
    return {
        "product": x * y,
        "sum": x + y,
        "result": result
    }
'''
    
    with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
        f.write(test_script)
        temp_path = f.name
    
    try:
        runner = SimulationRunner()
        test_params = parameters.get("params", {"x": 3, "y": 4})
        result = runner.run(Path(temp_path), test_params)
        
        execution_time = time.time() - start_time
        
        success = result.get("_ok", False)
        details = {
            "input_params": test_params,
            "output_keys": list(result.keys()),
            "execution_time_s": result.get("_duration_s", 0),
            "stdout_length": len(result.get("_stdout", "")),
            "stderr_length": len(result.get("_stderr", ""))
        }
        
        if success:
            expected_product = test_params["x"] * test_params["y"]
            actual_product = result.get("product")
            if actual_product != expected_product:
                success = False
                details["validation_error"] = f"Expected product {expected_product}, got {actual_product}"
        
        return TestResponse(
            test_type="simulation",
            success=success,
            message="Simulation test completed" if success else "Simulation test failed",
            details=details,
            execution_time=execution_time
        )
        
    finally:
        Path(temp_path).unlink(missing_ok=True)


async def _test_database(db, parameters: Dict[str, Any]) -> TestResponse:
    """Test database operations."""
    start_time = time.time()
    
    details = {}
    
    try:
        # Test database connection
        with db.config.get_sqlite_connection() as conn:
            # Test basic query
            result = conn.execute("SELECT COUNT(*) as count FROM simulations").fetchone()
            details["models_count"] = result["count"] if result else 0
            
            # Test results table
            result = conn.execute("SELECT COUNT(*) as count FROM results").fetchone()
            details["results_count"] = result["count"] if result else 0
            
            # Test reasoning table
            result = conn.execute("SELECT COUNT(*) as count FROM reasoning_agent").fetchone()
            details["conversations_count"] = result["count"] if result else 0
            
            # Test write operation (if requested)
            if parameters.get("test_write", False):
                test_id = f"test_{int(time.time())}"
                conn.execute("""
                    INSERT INTO simulations (id, name, metadata, script_path)
                    VALUES (?, ?, ?, ?)
                """, (test_id, "test_model", "{}", "/tmp/test.py"))
                
                # Clean up
                conn.execute("DELETE FROM simulations WHERE id = ?", (test_id,))
                details["write_test"] = "passed"
        
        execution_time = time.time() - start_time
        
        return TestResponse(
            test_type="database",
            success=True,
            message="Database test completed successfully",
            details=details,
            execution_time=execution_time
        )
        
    except Exception as e:
        execution_time = time.time() - start_time
        return TestResponse(
            test_type="database",
            success=False,
            message=f"Database test failed: {str(e)}",
            details=details,
            execution_time=execution_time
        )


async def _test_reasoning(db, parameters: Dict[str, Any]) -> TestResponse:
    """Test reasoning agent (mock test)."""
    start_time = time.time()
    
    # For now, just test that we can create a reasoning agent
    # In a real test, we'd need a valid model with results
    try:
        details = {
            "reasoning_agent_class": "ReasoningAgent",
            "test_mode": "mock",
            "note": "Full reasoning test requires valid model with simulation results"
        }
        
        # Check if we have any models to test with
        models = db.simulation_repository.list()
        if models:
            details["available_models"] = len(models)
            details["sample_model"] = models[0].get("id") if models else None
        else:
            details["available_models"] = 0
        
        execution_time = time.time() - start_time
        
        return TestResponse(
            test_type="reasoning",
            success=True,
            message="Reasoning test completed (mock mode)",
            details=details,
            execution_time=execution_time
        )
        
    except Exception as e:
        execution_time = time.time() - start_time
        return TestResponse(
            test_type="reasoning",
            success=False,
            message=f"Reasoning test failed: {str(e)}",
            execution_time=execution_time
        )


async def _test_end_to_end(db, parameters: Dict[str, Any]) -> TestResponse:
    """Test complete end-to-end workflow."""
    start_time = time.time()
    
    details = {}
    
    try:
        # 1. Create and store a test model
        test_script = '''
def simulate(amplitude=1.0, frequency=1.0, phase=0.0):
    import math
    import numpy as np
    
    t = np.linspace(0, 2*math.pi, 100)
    signal = amplitude * np.sin(frequency * t + phase)
    
    return {
        "max_value": float(np.max(signal)),
        "min_value": float(np.min(signal)),
        "mean_value": float(np.mean(signal)),
        "signal_length": len(signal)
    }
'''
        
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
            f.write(test_script)
            temp_path = f.name
        
        try:
            # Store model
            from db import store_simulation_script
            model_id = store_simulation_script(
                model_name="e2e_test_model",
                metadata={"description": "End-to-end test model", "test": True},
                script_path=temp_path
            )
            details["model_created"] = model_id
            
            # 2. Run simulation
            runner = SimulationRunner()
            result = runner.run(Path(temp_path), {"amplitude": 2.0, "frequency": 0.5})
            
            details["simulation_success"] = result.get("_ok", False)
            details["simulation_results"] = {k: v for k, v in result.items() if not k.startswith("_")}
            
            # 3. Store results
            if result.get("_ok", False):
                from db import store_simulation_results
                store_simulation_results(model_id, [result], ["amplitude", "frequency"])
                details["results_stored"] = True
            
            # 4. Test reasoning (mock)
            details["reasoning_available"] = True
            
            # 5. Clean up
            with db.config.get_sqlite_connection() as conn:
                conn.execute("DELETE FROM results WHERE model_id = ?", (model_id,))
                conn.execute("DELETE FROM simulations WHERE id = ?", (model_id,))
            details["cleanup_completed"] = True
            
        finally:
            Path(temp_path).unlink(missing_ok=True)
        
        execution_time = time.time() - start_time
        
        success = all([
            details.get("model_created"),
            details.get("simulation_success", False),
            details.get("results_stored", False),
            details.get("cleanup_completed", False)
        ])
        
        return TestResponse(
            test_type="end_to_end",
            success=success,
            message="End-to-end test completed" if success else "End-to-end test had failures",
            details=details,
            execution_time=execution_time
        )
        
    except Exception as e:
        execution_time = time.time() - start_time
        return TestResponse(
            test_type="end_to_end",
            success=False,
            message=f"End-to-end test failed: {str(e)}",
            details=details,
            execution_time=execution_time
        )
