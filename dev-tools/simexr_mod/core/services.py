"""
Service layer implementations for SimExR.

This module provides high-level service classes that orchestrate
various components and implement business logic.
"""

from typing import Dict, Any, List, Optional, Union
from pathlib import Path
from dataclasses import dataclass
import time
import json
import logging

from .interfaces import (
    ISimulationRunner, ISimulationLoader, IResultStore, IReasoningAgent,
    SimulationRequest, SimulationResult, SimulationStatus
)
from .patterns import (
    SimulationFacade, SimulationFactory, ExecutionStrategyManager,
    SimulationSubject, CommandInvoker, ResourceManager, DIContainer,
    LocalExecutionStrategy, LoggingObserver, GitHubScriptAdapter
)


@dataclass
class ServiceConfiguration:
    """Configuration for services."""
    database_path: str = "mcp.db"
    models_directory: str = "systems/models"
    results_directory: str = "results_media"
    default_timeout: float = 30.0
    max_batch_size: int = 1000
    enable_logging: bool = True
    log_file: Optional[str] = None


class SimulationService:
    """High-level simulation service."""
    
    def __init__(self, config: ServiceConfiguration = None):
        self.config = config or ServiceConfiguration()
        self.facade = SimulationFacade()
        self.logger = self._setup_logging()
        
        # Initialize components
        self._initialize_components()
    
    def _setup_logging(self) -> logging.Logger:
        """Set up logging for the service."""
        logger = logging.getLogger("SimulationService")
        logger.setLevel(logging.INFO if self.config.enable_logging else logging.WARNING)
        
        if not logger.handlers:
            handler = logging.StreamHandler()
            formatter = logging.Formatter(
                '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
            )
            handler.setFormatter(formatter)
            logger.addHandler(handler)
        
        return logger
    
    def _initialize_components(self):
        """Initialize service components."""
        if self.config.enable_logging:
            log_file = Path(self.config.log_file) if self.config.log_file else None
            observer = LoggingObserver(log_file)
            self.facade.add_observer(observer)
    
    def run_single_simulation(
        self, 
        model_id: str, 
        parameters: Dict[str, Any],
        timeout: Optional[float] = None
    ) -> SimulationResult:
        """Run a single simulation."""
        self.logger.info(f"Running simulation for model {model_id}")
        
        try:
            result = self.facade.run_simulation(
                model_id=model_id,
                parameters=parameters,
                timeout=timeout or self.config.default_timeout
            )
            
            self.logger.info(f"Simulation completed with status: {result.status.value}")
            
            # Save results to database if successful
            if result.status == SimulationStatus.COMPLETED:
                try:
                    from db import store_simulation_results
                    
                    # Convert SimulationResult to the format expected by store_simulation_results
                    result_row = {
                        **result.parameters,  # Include all parameters
                        **result.outputs,     # Include all outputs
                        '_ok': True,          # Mark as successful
                        '_execution_time': result.execution_time
                    }
                    
                    # Extract parameter keys from the parameters dict
                    param_keys = list(result.parameters.keys())
                    
                    # Store the result
                    store_simulation_results(model_id, [result_row], param_keys)
                    self.logger.info(f"Results saved to database for model {model_id}")
                    
                except Exception as save_error:
                    self.logger.warning(f"Failed to save results to database: {save_error}")
            
            return result
            
        except Exception as e:
            self.logger.error(f"Simulation failed: {str(e)}")
            raise
    
    def run_batch_simulations(
        self,
        model_id: str,
        parameter_grid: List[Dict[str, Any]],
        max_workers: int = 4
    ) -> List[SimulationResult]:
        """Run multiple simulations in batch."""
        if len(parameter_grid) > self.config.max_batch_size:
            raise ValueError(f"Batch size {len(parameter_grid)} exceeds maximum {self.config.max_batch_size}")
        
        self.logger.info(f"Running batch of {len(parameter_grid)} simulations for model {model_id}")
        
        # Import tqdm for progress bar
        try:
            from tqdm import tqdm
            use_tqdm = True
            self.logger.info("Using tqdm for progress tracking")
        except ImportError:
            use_tqdm = False
            self.logger.warning("tqdm not available, running without progress bar")
        
        results = []
        iterator = tqdm(parameter_grid, desc=f"Running {model_id} simulations") if use_tqdm else enumerate(parameter_grid)
        
        for i, parameters in (enumerate(iterator) if use_tqdm else iterator):
            if use_tqdm:
                # tqdm already provides the index
                pass
            else:
                # Manual enumeration
                i = i
            
            self.logger.info(f"Running simulation {i+1}/{len(parameter_grid)}")
            self.logger.debug(f"Parameters: {parameters}")
            
            try:
                result = self.run_single_simulation(model_id, parameters)
                results.append(result)
                if use_tqdm:
                    iterator.set_postfix({"status": "success"})
            except Exception as e:
                self.logger.error(f"Simulation {i+1} failed: {str(e)}")
                # Create failed result
                failed_result = SimulationResult(
                    status=SimulationStatus.FAILED,
                    parameters=parameters,
                    outputs={},
                    execution_time=0.0,
                    error_message=str(e)
                )
                results.append(failed_result)
                if use_tqdm:
                    iterator.set_postfix({"status": "failed"})
        
        successful = sum(1 for r in results if r.status == SimulationStatus.COMPLETED)
        self.logger.info(f"Batch completed: {successful}/{len(results)} successful")
        
        # Save successful results to database
        if successful > 0:
            try:
                from db import store_simulation_results
                
                # Convert successful SimulationResults to the format expected by store_simulation_results
                successful_rows = []
                param_keys = None
                
                for result in results:
                    if result.status == SimulationStatus.COMPLETED:
                        result_row = {
                            **result.parameters,  # Include all parameters
                            **result.outputs,     # Include all outputs
                            '_ok': True,          # Mark as successful
                            '_execution_time': result.execution_time
                        }
                        successful_rows.append(result_row)
                        
                        # Use parameter keys from the first successful result
                        if param_keys is None:
                            param_keys = list(result.parameters.keys())
                
                if successful_rows and param_keys:
                    store_simulation_results(model_id, successful_rows, param_keys)
                    self.logger.info(f"Saved {len(successful_rows)} results to database for model {model_id}")
                
            except Exception as save_error:
                self.logger.warning(f"Failed to save batch results to database: {save_error}")
        
        return results
    
    def import_model_from_github(
        self,
        github_url: str,
        model_name: str,
        description: str = "",
        parameters: Dict[str, str] = None
    ) -> str:
        """Import a simulation model from GitHub."""
        self.logger.info(f"Importing model from GitHub: {github_url}")
        
        metadata = {
            "description": description,
            "source": "github",
            "source_url": github_url,
            "parameters": parameters or {},
            "imported_at": time.time()
        }
        
        try:
            model_id = self.facade.import_from_github(github_url, model_name, metadata)
            self.logger.info(f"Successfully imported model with ID: {model_id}")
            return model_id
        except Exception as e:
            self.logger.error(f"Failed to import model: {str(e)}")
            raise
    
    def get_model_info(self, model_id: str) -> Dict[str, Any]:
        """Get information about a simulation model."""
        try:
            from db import get_simulation_path
            script_path = get_simulation_path(model_id)
            
            # Get metadata from database
            from db import Database, DatabaseConfig
            db_config = DatabaseConfig(database_path=self.config.database_path)
            db = Database(db_config)
            
            models = db.simulation_repository.list({"id": model_id})
            if not models:
                raise ValueError(f"Model {model_id} not found")
            
            model_info = models[0]
            model_info["script_path"] = script_path
            
            return model_info
            
        except Exception as e:
            self.logger.error(f"Failed to get model info: {str(e)}")
            raise
    
    def list_models(self) -> List[Dict[str, Any]]:
        """List all available models."""
        try:
            from db import Database, DatabaseConfig
            db_config = DatabaseConfig(database_path=self.config.database_path)
            db = Database(db_config)
            
            return db.simulation_repository.list()
            
        except Exception as e:
            self.logger.error(f"Failed to list models: {str(e)}")
            raise
    
    def cleanup(self):
        """Clean up service resources."""
        self.facade.cleanup()


class ReasoningService:
    """High-level reasoning service."""
    
    def __init__(self, config: ServiceConfiguration = None):
        self.config = config or ServiceConfiguration()
        self.logger = self._setup_logging()
    
    def _setup_logging(self) -> logging.Logger:
        """Set up logging for the service."""
        logger = logging.getLogger("ReasoningService")
        logger.setLevel(logging.INFO if self.config.enable_logging else logging.WARNING)
        
        if not logger.handlers:
            handler = logging.StreamHandler()
            formatter = logging.Formatter(
                '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
            )
            handler.setFormatter(formatter)
            logger.addHandler(handler)
        
        return logger
    
    def ask_question(
        self,
        model_id: str,
        question: str,
        max_steps: int = 20
    ) -> Dict[str, Any]:
        """Ask a question about a simulation model."""
        self.logger.info(f"Processing reasoning request for model {model_id}")
        
        try:
            from reasoning import ReasoningAgent
            from db.config.database import DatabaseConfig
            
            db_config = DatabaseConfig(database_path=self.config.database_path)
            agent = ReasoningAgent(
                model_id=model_id,
                db_config=db_config,
                max_steps=max_steps
            )
            
            result = agent.ask(question)
            
            self.logger.info("Reasoning request completed successfully")
            return {
                "answer": result.answer,
                "model_id": model_id,
                "question": question,
                "history": result.history,
                "code_map": result.code_map,
                "images": result.images
            }
            
        except Exception as e:
            self.logger.error(f"Reasoning request failed: {str(e)}")
            raise
    
    def get_conversation_history(
        self,
        model_id: str,
        limit: int = 50,
        offset: int = 0
    ) -> List[Dict[str, Any]]:
        """Get conversation history for a model."""
        try:
            from db import Database, DatabaseConfig
            db_config = DatabaseConfig(database_path=self.config.database_path)
            db = Database(db_config)
            
            with db.config.get_sqlite_connection() as conn:
                rows = conn.execute("""
                    SELECT id, model_id, question, answer, images, ts
                    FROM reasoning_agent 
                    WHERE model_id = ?
                    ORDER BY ts DESC
                    LIMIT ? OFFSET ?
                """, (model_id, limit, offset)).fetchall()
                
                history = []
                for row in rows:
                    history.append({
                        "id": row["id"],
                        "model_id": row["model_id"],
                        "question": row["question"],
                        "answer": row["answer"],
                        "images": row["images"],
                        "timestamp": row["ts"]
                    })
                
                return history
                
        except Exception as e:
            self.logger.error(f"Failed to get conversation history: {str(e)}")
            raise
    
    def get_reasoning_statistics(self) -> Dict[str, Any]:
        """Get statistics about reasoning usage."""
        try:
            from db import Database, DatabaseConfig
            db_config = DatabaseConfig(database_path=self.config.database_path)
            db = Database(db_config)
            
            with db.config.get_sqlite_connection() as conn:
                # Overall stats
                overall = conn.execute("""
                    SELECT 
                        COUNT(*) as total_conversations,
                        COUNT(DISTINCT model_id) as unique_models,
                        MIN(ts) as first_conversation,
                        MAX(ts) as last_conversation
                    FROM reasoning_agent
                """).fetchone()
                
                # Per-model stats
                per_model = conn.execute("""
                    SELECT 
                        model_id,
                        COUNT(*) as conversation_count,
                        MIN(ts) as first_conversation,
                        MAX(ts) as last_conversation
                    FROM reasoning_agent
                    GROUP BY model_id
                    ORDER BY conversation_count DESC
                """).fetchall()
                
                return {
                    "overall": {
                        "total_conversations": overall["total_conversations"] if overall else 0,
                        "unique_models": overall["unique_models"] if overall else 0,
                        "first_conversation": overall["first_conversation"] if overall else None,
                        "last_conversation": overall["last_conversation"] if overall else None
                    },
                    "per_model": [
                        {
                            "model_id": row["model_id"],
                            "conversation_count": row["conversation_count"],
                            "first_conversation": row["first_conversation"],
                            "last_conversation": row["last_conversation"]
                        }
                        for row in per_model
                    ]
                }
                
        except Exception as e:
            self.logger.error(f"Failed to get reasoning statistics: {str(e)}")
            raise


class DataService:
    """High-level data management service."""
    
    def __init__(self, config: ServiceConfiguration = None):
        self.config = config or ServiceConfiguration()
        self.logger = self._setup_logging()
    
    def _setup_logging(self) -> logging.Logger:
        """Set up logging for the service."""
        logger = logging.getLogger("DataService")
        logger.setLevel(logging.INFO if self.config.enable_logging else logging.WARNING)
        
        if not logger.handlers:
            handler = logging.StreamHandler()
            formatter = logging.Formatter(
                '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
            )
            handler.setFormatter(formatter)
            logger.addHandler(handler)
        
        return logger
    
    def get_simulation_results(
        self,
        model_id: Optional[str] = None,
        limit: int = 100,
        offset: int = 0
    ) -> Dict[str, Any]:
        """Get simulation results with pagination."""
        try:
            from db import Database, DatabaseConfig
            import numpy as np
            import pandas as pd
            
            db_config = DatabaseConfig(database_path=self.config.database_path)
            db = Database(db_config)
            
            if model_id:
                df = db.results_service.load_results(model_id=model_id)
            else:
                df = db.results_service.load_results()
            
            # Apply pagination
            total_count = len(df)
            df_page = df.iloc[offset:offset + limit]
            
            # Clean NaN values for JSON serialization
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
            self.logger.info(f"=== RESULTS PREVIEW (First 5 rows) ===")
            self.logger.info(f"Total count: {total_count}, Limit: {limit}, Offset: {offset}")
            for i, result in enumerate(cleaned_results[:5]):
                self.logger.info(f"Row {i+1}: {list(result.keys())}")
                # Show key values for first few rows
                if i < 3:  # Show more details for first 3 rows
                    for key, value in list(result.items())[:10]:  # First 10 keys
                        if isinstance(value, (list, tuple)) and len(value) > 5:
                            self.logger.info(f"  {key}: {type(value).__name__} with {len(value)} items (first 3: {value[:3]})")
                        elif isinstance(value, dict):
                            self.logger.info(f"  {key}: dict with {len(value)} keys")
                        else:
                            self.logger.info(f"  {key}: {value}")
            self.logger.info(f"=== END RESULTS PREVIEW ===")
            
            return {
                "total_count": total_count,
                "limit": limit,
                "offset": offset,
                "results": cleaned_results
            }
            
        except Exception as e:
            self.logger.error(f"Failed to get simulation results: {str(e)}")
            raise
    
    def store_model(
        self,
        model_name: str,
        script_content: str,
        metadata: Dict[str, Any] = None
    ) -> str:
        """Store a new simulation model."""
        try:
            from db import store_simulation_script
            import tempfile
            
            # Create temporary script file
            with tempfile.NamedTemporaryFile(mode='w', suffix='.py', delete=False) as f:
                f.write(script_content)
                temp_path = f.name
            
            try:
                model_id = store_simulation_script(
                    model_name=model_name,
                    metadata=metadata or {},
                    script_path=temp_path
                )
                
                self.logger.info(f"Stored model {model_name} with ID: {model_id}")
                return model_id
                
            finally:
                Path(temp_path).unlink(missing_ok=True)
                
        except Exception as e:
            self.logger.error(f"Failed to store model: {str(e)}")
            raise
    
    def delete_model(self, model_id: str) -> Dict[str, int]:
        """Delete a model and all associated data."""
        try:
            from db import Database, DatabaseConfig
            db_config = DatabaseConfig(database_path=self.config.database_path)
            db = Database(db_config)
            
            with db.config.get_sqlite_connection() as conn:
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
                model_deleted = conn.execute(
                    "DELETE FROM simulations WHERE id = ?",
                    (model_id,)
                ).rowcount
                
                if model_deleted == 0:
                    raise ValueError(f"Model {model_id} not found")
                
                self.logger.info(f"Deleted model {model_id} and associated data")
                
                return {
                    "models_deleted": model_deleted,
                    "results_deleted": results_deleted,
                    "conversations_deleted": reasoning_deleted
                }
                
        except Exception as e:
            self.logger.error(f"Failed to delete model: {str(e)}")
            raise
    
    def get_database_statistics(self) -> Dict[str, Any]:
        """Get comprehensive database statistics."""
        try:
            from db import Database, DatabaseConfig
            db_config = DatabaseConfig(database_path=self.config.database_path)
            db = Database(db_config)
            
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
                
                # Database size
                size_stats = conn.execute("PRAGMA page_count").fetchone()
                page_size = conn.execute("PRAGMA page_size").fetchone()
                
                db_size_bytes = (size_stats[0] if size_stats else 0) * (page_size[0] if page_size else 0)
                db_size_mb = round(db_size_bytes / (1024 * 1024), 2)
                
                return {
                    "database": {
                        "path": self.config.database_path,
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
            self.logger.error(f"Failed to get database statistics: {str(e)}")
            raise
    
    def create_backup(self) -> Dict[str, Any]:
        """Create a database backup."""
        try:
            import shutil
            from datetime import datetime
            
            # Create backup filename with timestamp
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            backup_path = f"{self.config.database_path}.backup_{timestamp}"
            
            # Copy database file
            shutil.copy2(self.config.database_path, backup_path)
            
            # Get backup file size
            backup_size = Path(backup_path).stat().st_size
            backup_size_mb = round(backup_size / (1024 * 1024), 2)
            
            self.logger.info(f"Created database backup: {backup_path}")
            
            return {
                "backup_path": backup_path,
                "backup_size_mb": backup_size_mb,
                "timestamp": timestamp
            }
            
        except Exception as e:
            self.logger.error(f"Failed to create backup: {str(e)}")
            raise
