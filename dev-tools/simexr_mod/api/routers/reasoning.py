"""
Reasoning agent API endpoints.
"""

import time
from typing import List, Optional
from fastapi import APIRouter, HTTPException, Depends

from reasoning import ReasoningAgent
from db.config.database import DatabaseConfig
from ..models import ReasoningRequest, ReasoningResponse, StatusResponse
from ..dependencies import get_database


router = APIRouter()


@router.post("/ask", response_model=ReasoningResponse, summary="Ask reasoning agent")
async def ask_reasoning_agent(
    request: ReasoningRequest,
    db = Depends(get_database)
):
    """
    Ask the reasoning agent a question about a simulation model.
    
    - **model_id**: ID of the simulation model to analyze
    - **question**: Question to ask the reasoning agent
    - **max_steps**: Maximum number of reasoning steps (default: 20)
    
    Returns the agent's analysis and answer.
    """
    try:
        start_time = time.time()
        
        # Verify model exists
        from db import get_simulation_path
        try:
            get_simulation_path(request.model_id)
        except KeyError:
            raise HTTPException(status_code=404, detail=f"Model {request.model_id} not found")
        
        # Create reasoning agent
        agent = ReasoningAgent(
            model_id=request.model_id,
            db_config=db.config,
            max_steps=request.max_steps or 20
        )
        
        # Ask question
        result = agent.ask(request.question)
        
        execution_time = time.time() - start_time
        
        return ReasoningResponse(
            answer=result.answer,
            model_id=request.model_id,
            question=request.question,
            history=result.history,
            code_map=result.code_map,
            images=result.images,
            execution_time=execution_time
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Reasoning failed: {str(e)}")


@router.get("/history/{model_id}", summary="Get reasoning history")
async def get_reasoning_history(
    model_id: str,
    limit: int = 50,
    offset: int = 0,
    db = Depends(get_database)
):
    """
    Get reasoning conversation history for a model.
    
    - **model_id**: ID of the simulation model
    - **limit**: Maximum number of conversations to return (default: 50)
    - **offset**: Number of conversations to skip (default: 0)
    
    Returns paginated reasoning history.
    """
    try:
        with db.config.get_sqlite_connection() as conn:
            # Get total count
            count_result = conn.execute(
                "SELECT COUNT(*) as count FROM reasoning_agent WHERE model_id = ?",
                (model_id,)
            ).fetchone()
            total_count = count_result["count"] if count_result else 0
            
            # Get paginated results
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
        
        return {
            "status": "success",
            "total_count": total_count,
            "limit": limit,
            "offset": offset,
            "history": history
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get history: {str(e)}")


@router.delete("/history/{model_id}", summary="Clear reasoning history")
async def clear_reasoning_history(model_id: str, db = Depends(get_database)):
    """
    Clear all reasoning history for a specific model.
    
    - **model_id**: ID of the simulation model
    
    Returns confirmation of deletion.
    """
    try:
        with db.config.get_sqlite_connection() as conn:
            cursor = conn.execute(
                "DELETE FROM reasoning_agent WHERE model_id = ?",
                (model_id,)
            )
            deleted_count = cursor.rowcount
        
        return {
            "status": "success",
            "message": f"Deleted {deleted_count} reasoning conversations for model {model_id}",
            "deleted_count": deleted_count
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to clear history: {str(e)}")


@router.get("/conversations", summary="Get all reasoning conversations")
async def get_all_conversations(
    limit: int = 100,
    offset: int = 0,
    model_id: Optional[str] = None,
    db = Depends(get_database)
):
    """
    Get all reasoning conversations across all models.
    
    - **limit**: Maximum number of conversations to return (default: 100)
    - **offset**: Number of conversations to skip (default: 0)
    - **model_id**: Optional filter by model ID
    
    Returns paginated conversation list.
    """
    try:
        with db.config.get_sqlite_connection() as conn:
            # Build query
            base_query = "FROM reasoning_agent"
            params = []
            
            if model_id:
                base_query += " WHERE model_id = ?"
                params.append(model_id)
            
            # Get total count
            count_result = conn.execute(
                f"SELECT COUNT(*) as count {base_query}",
                params
            ).fetchone()
            total_count = count_result["count"] if count_result else 0
            
            # Get paginated results
            rows = conn.execute(f"""
                SELECT id, model_id, question, answer, images, ts
                {base_query}
                ORDER BY ts DESC
                LIMIT ? OFFSET ?
            """, params + [limit, offset]).fetchall()
            
            conversations = []
            for row in rows:
                conversations.append({
                    "id": row["id"],
                    "model_id": row["model_id"],
                    "question": row["question"],
                    "answer": row["answer"],
                    "images": row["images"],
                    "timestamp": row["ts"]
                })
        
        return {
            "status": "success",
            "total_count": total_count,
            "limit": limit,
            "offset": offset,
            "conversations": conversations
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get conversations: {str(e)}")


@router.get("/stats", summary="Get reasoning statistics")
async def get_reasoning_stats(db = Depends(get_database)):
    """
    Get statistics about reasoning agent usage.
    
    Returns overall statistics and per-model breakdown.
    """
    try:
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
            
            model_stats = []
            for row in per_model:
                model_stats.append({
                    "model_id": row["model_id"],
                    "conversation_count": row["conversation_count"],
                    "first_conversation": row["first_conversation"],
                    "last_conversation": row["last_conversation"]
                })
        
        return {
            "status": "success",
            "overall": {
                "total_conversations": overall["total_conversations"] if overall else 0,
                "unique_models": overall["unique_models"] if overall else 0,
                "first_conversation": overall["first_conversation"] if overall else None,
                "last_conversation": overall["last_conversation"] if overall else None
            },
            "per_model": model_stats
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to get stats: {str(e)}")
