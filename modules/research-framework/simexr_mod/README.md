# SimExR: Simulation Execution and Reasoning Framework

A comprehensive framework for importing, executing, and analyzing scientific simulations with AI-powered reasoning capabilities.

## Overview

SimExR is a FastAPI-based framework that provides a complete pipeline for:
- **Importing** external simulation scripts from GitHub
- **Transforming** scripts into standardized `simulate(**params)` functions
- **Executing** single and batch simulations with automatic result storage
- **Analyzing** results using AI-powered reasoning agents
- **Managing** models, results, and conversations through REST APIs

## Architecture
<img width="3840" height="1004" alt="arch" src="https://github.com/user-attachments/assets/cd26cc8e-2b12-40a8-be8b-5213b767d422" />


### Core Components

```
simexr_mod/
├── api/                    # FastAPI application and routers
│   ├── main.py            # Main API application
│   ├── dependencies.py    # Dependency injection
│   └── routers/           # API endpoint definitions
│       ├── simulation.py  # Simulation execution APIs
│       ├── reasoning.py   # AI reasoning APIs
│       ├── database.py    # Database read-only APIs
│       └── health.py      # Health check APIs
├── core/                   # Core business logic
│   ├── interfaces.py      # Abstract base classes
│   ├── patterns.py        # Design patterns implementation
│   └── services.py        # Main service layer
├── execute/               # Simulation execution engine
│   ├── loader/           # Script loading and transformation
│   ├── run/              # Simulation execution
│   └── test/             # Code testing and refinement
├── reasoning/             # AI reasoning engine
│   ├── agent/            # Reasoning agent implementation
│   ├── messages/         # LLM client implementations
│   └── base.py           # Base reasoning classes
├── db/                    # Database layer
│   ├── repositories/     # Data access layer
│   ├── services/         # Database services
│   └── utils/            # Database utilities
├── code/                  # Code processing utilities
│   ├── refactor/         # Code refactoring
│   ├── extract/          # Metadata extraction
│   └── utils/            # Code utilities
└── utils/                 # Configuration and utilities
```

## Installation & Setup

### Prerequisites

- Python 3.8+
- Git
- OpenAI API key

### 1. Clone and Setup Environment

```bash
# Clone the repository
git clone <repository-url>
cd simexr_mod

# Create virtual environment
python -m venv simexr_venv
source simexr_venv/bin/activate  # On Windows: simexr_venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
```

### 2. Configuration

Create `utils/config.yaml` with your OpenAI API key:

```yaml
openai:
  api_key: "your-openai-api-key-here"
```

### 3. Database Setup

The framework uses SQLite by default. The database will be automatically created at `mcp.db` on first run.

## Quick Start

### Option 1: Web UI (Recommended)

Start the complete application with the user-friendly Streamlit interface:

```bash
source simexr_venv/bin/activate
python start_streamlit.py
```

This will automatically:
- Start the API server
- Launch the Streamlit web interface
- Open your browser to http://localhost:8501

### Option 2: API Only

Start just the API server for programmatic access:

```bash
source simexr_venv/bin/activate
python start_api.py --host 127.0.0.1 --port 8001
```

The server will be available at:
- **API**: http://127.0.0.1:8001
- **Documentation**: http://127.0.0.1:8001/docs

### 2. Using the Web Interface

Once the Streamlit app is running, you can:

1. **📥 Import Models**: Use the "Import Models" page to import scripts from GitHub
2. **⚙️ Run Simulations**: Use the "Run Simulations" page to execute simulations
3. **📊 View Results**: Use the "View Results" page to explore simulation data
4. **🤖 AI Analysis**: Use the "AI Analysis" page to ask questions about your results
5. **🔍 Search Models**: Use the "Model Search" page to find existing models

### 3. Using the API Directly

If you prefer to use the API directly:

```bash
# Import and transform a simulation
curl -X POST "http://127.0.0.1:8001/simulation/transform/github" \
  -H "Content-Type: application/json" \
  -d '{
    "github_url": "https://github.com/vash02/physics-systems-dataset/blob/main/vanderpol.py",
    "model_name": "vanderpol_transform",
    "max_smoke_iters": 3
  }'

# Run simulations
curl -X POST "http://127.0.0.1:8001/simulation/run" \
  -H "Content-Type: application/json" \
  -d '{
    "model_id": "vanderpol_transform_eac8429aea8f",
    "parameters": {
      "mu": 1.5,
      "z0": [1.5, 0.5],
      "eval_time": 25,
      "t_iteration": 250,
      "plot": false
    }
  }'

# Analyze results with AI
curl -X POST "http://127.0.0.1:8001/reasoning/ask" \
  -H "Content-Type: application/json" \
  -d '{
    "model_id": "vanderpol_transform_eac8429aea8f",
    "question": "What is the behavior of the van der Pol oscillator for mu=1.0 and mu=1.5? How do the trajectories differ?",
    "max_steps": 5
  }'
```

## Web Interface

The SimExR framework includes a modern, user-friendly web interface built with Streamlit:

### Interface Pages

- **Dashboard**: Overview of system status, recent activity, and quick actions
- **Import Models**: Import and transform scripts from GitHub URLs
- **⚙Run Simulations**: Execute single or batch simulations with custom parameters
- **View Results**: Explore simulation results with interactive data tables
- **AI Analysis**: Ask AI-powered questions about your simulation results
- **Model Search**: Search and browse all available models

### Key Features

- **Fuzzy Search**: Intelligent model search with relevance scoring
- **Interactive Results**: View and download simulation results as CSV
- **AI Chat**: Natural language analysis of simulation data
- **⚙Parameter Management**: Edit and manage simulation parameters
- **Script Editor**: View and edit simulation scripts
- **Templates**: Pre-built parameter templates for common systems

## API Endpoints

### Health Check APIs
- `GET /health/status` - System health status
- `POST /health/test` - Run system tests

### Simulation APIs
- `POST /simulation/transform/github` - Import and transform GitHub scripts
- `POST /simulation/run` - Run single simulation
- `POST /simulation/batch` - Run batch simulations
- `GET /simulation/models` - List all models
- `GET /simulation/models/search` - Fuzzy search models by name
- `GET /simulation/models/{model_id}` - Get model information
- `GET /simulation/models/{model_id}/results` - Get simulation results
- `DELETE /simulation/models/{model_id}/results` - Clear model results

### Reasoning APIs
- `POST /reasoning/ask` - Ask AI reasoning questions
- `GET /reasoning/history/{model_id}` - Get reasoning history
- `GET /reasoning/conversations` - Get all conversations
- `GET /reasoning/stats` - Get reasoning statistics

### Database APIs (Read-only)
- `GET /database/results` - Get simulation results
- `GET /database/models` - Get database models

## 🧪 Testing Results

### Complete Workflow Test

We successfully tested the complete workflow from GitHub import to AI analysis:

#### 1. GitHub Script Import & Transformation
```bash
# Test URL: https://github.com/vash02/physics-systems-dataset/blob/main/vanderpol.py
# Result: Successfully imported and transformed into simulate(**params) function
# Model ID: vanderpol_transform_eac8429aea8f
```

#### 2. Single Simulation Execution
```bash
# Parameters: mu=1.5, z0=[1.5, 0.5], eval_time=25, t_iteration=250
# Result: Successfully executed with detailed logging
# Execution time: ~0.06 seconds
# Data points: 250 time steps, 15x15 grid
```

#### 3. Batch Simulation Execution
```bash
# Parameter grid: 2 different configurations
# Result: Successfully executed with tqdm progress bars
# Automatic result saving to database
# Execution time: ~0.5 seconds total
```

#### 4. AI Reasoning Analysis
```bash
# Question: "What is the behavior of the van der Pol oscillator for mu=1.0 and mu=1.5?"
# Result: Comprehensive scientific analysis with:
# - Common behavior identification
# - Parameter-specific differences
# - Technical details and insights
# Execution time: ~83 seconds
```

### API Performance Metrics

| API Endpoint | Status | Response Time | Features |
|--------------|--------|---------------|----------|
| `GET /health/status` | ✅ | <100ms | System health |
| `POST /simulation/transform/github` | ✅ | ~5s | Import + transform + refine |
| `POST /simulation/run` | ✅ | ~0.1s | Single simulation + auto-save |
| `POST /simulation/batch` | ✅ | ~0.5s | Batch simulation + tqdm + auto-save |
| `GET /simulation/models` | ✅ | <100ms | 50 models listed |
| `GET /simulation/models/search` | ✅ | <100ms | Fuzzy search with relevance scoring |
| `GET /simulation/models/{id}/results` | ✅ | <200ms | Results with NaN handling |
| `POST /reasoning/ask` | ✅ | ~83s | AI analysis with 5 reasoning steps |
| `GET /reasoning/history/{id}` | ✅ | <100ms | Conversation history |
| `GET /reasoning/stats` | ✅ | <100ms | 173 conversations, 18 models |

### Key Features Validated

✅ **GitHub Integration**: Successfully imports and transforms external scripts  
✅ **Code Refactoring**: Converts scripts to standardized `simulate(**params)` format  
✅ **Automatic Result Saving**: All simulations automatically saved to database  
✅ **Enhanced Logging**: Detailed execution logs with result previews  
✅ **tqdm Progress Bars**: Visual progress for batch operations  
✅ **NaN Handling**: Proper JSON serialization of scientific data  
✅ **Fuzzy Search**: Intelligent model search with relevance scoring  
✅ **AI Reasoning**: Comprehensive analysis of simulation results  
✅ **Error Handling**: Graceful handling of various error conditions  

## 🔧 Advanced Usage

### Custom Simulation Parameters

The framework supports dynamic parameter extraction and validation:

```python
# Example parameter structure for van der Pol oscillator
parameters = {
    "mu": 1.5,                    # Damping parameter
    "z0": [1.5, 0.5],            # Initial conditions [x0, y0]
    "eval_time": 25,              # Simulation time
    "t_iteration": 250,           # Number of time steps
    "plot": False                 # Plotting flag
}
```

### Batch Simulation with Parameter Grids

```bash
curl -X POST "http://127.0.0.1:8001/simulation/batch" \
  -H "Content-Type: application/json" \
  -d '{
    "model_id": "your_model_id",
    "parameter_grid": [
      {"param1": "value1", "param2": "value2"},
      {"param1": "value3", "param2": "value4"}
    ]
  }'
```

### Fuzzy Model Search

```bash
# Search by partial name
curl "http://127.0.0.1:8001/simulation/models/search?name=vanderpol&limit=5"

# Search by model type
curl "http://127.0.0.1:8001/simulation/models/search?name=lorenz&limit=3"
```

### AI Reasoning with Custom Questions

```bash
curl -X POST "http://127.0.0.1:8001/reasoning/ask" \
  -H "Content-Type: application/json" \
  -d '{
    "model_id": "your_model_id",
    "question": "Analyze the stability of the system and identify bifurcation points",
    "max_steps": 10
  }'
```

## Troubleshooting

### Common Issues

1. **OpenAI API Key Error**
   ```bash
   # Ensure API key is set in utils/config.yaml
   # Or set environment variable
   export OPENAI_API_KEY="your-key-here"
   ```

2. **Import Errors**
   ```bash
   # Ensure virtual environment is activated
   source simexr_venv/bin/activate
   
   # Install missing dependencies
   pip install -r requirements.txt
   ```

3. **Database Connection Issues**
   ```bash
   # Check database file permissions
   ls -la mcp.db
   
   # Recreate database if corrupted
   rm mcp.db
   # Restart server to recreate
   ```

4. **Simulation Execution Errors**
   ```bash
   # Check script syntax
   python -m py_compile your_script.py
   
   # Verify simulate function exists
   grep -n "def simulate" your_script.py
   ```

### Debug Mode

Enable detailed logging by setting environment variables:

```bash
export LOG_LEVEL=DEBUG
export SIMEXR_DEBUG=true
python start_api.py --host 127.0.0.1 --port 8001
```

## Performance Optimization

### Database Optimization

- Use appropriate indexes for large datasets
- Implement result pagination for large result sets

### Simulation Optimization

- Use vectorized operations in simulation scripts
- Implement parallel processing for batch simulations
- Cache frequently used simulation results

### AI Reasoning Optimization

- Implement conversation caching
- Use streaming responses for long analyses
- Optimize prompt engineering for faster responses

## Future Enhancements

### Planned Features

- MCP Server Integration: Standard protocol or MCP server for agentic control of scientific experimentation workflow
- Advanced Exploration: Using better techniques for domain specific param space exploration
- Cybershuttle Integration: Imntegration with research jupyter notebooks on cybershuttle
- Plugin System: Extensible architecture for custom features
- Integration APIs: Connect with other scientific tools and platforms

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## Support

For questions and support:
- Create an issue on GitHub
- Check the documentation at `/docs`
- Review the API documentation at `/docs`

---

**SimExR Framework** - Empowering scientific simulation with AI reasoning capabilities.
