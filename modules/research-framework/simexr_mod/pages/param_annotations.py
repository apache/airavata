# pages/param_annotations.py
# SimExR: Parameter Annotations & Model Management

import streamlit as st
import json
import requests
import pandas as pd
from typing import Dict, List, Any
import time

# API Configuration
API_BASE_URL = "http://127.0.0.1:8001"

def make_api_request(method: str, endpoint: str, data: Dict = None, params: Dict = None) -> Dict:
    """Make an API request and return the response."""
    url = f"{API_BASE_URL}{endpoint}"
    headers = {"Content-Type": "application/json"}
    
    try:
        if method.upper() == "GET":
            response = requests.get(url, headers=headers, params=params)
        elif method.upper() == "POST":
            if params:
                response = requests.post(url, headers=headers, params=params)
            else:
                response = requests.post(url, headers=headers, json=data)
        elif method.upper() == "DELETE":
            response = requests.delete(url, headers=headers)
        else:
            raise ValueError(f"Unsupported method: {method}")
            
        response.raise_for_status()
        return response.json()
        
    except requests.exceptions.RequestException as e:
        st.error(f"API request failed: {e}")
        return {"error": str(e)}

def search_models(query: str, limit: int = 10) -> List[Dict]:
    """Search for models using the fuzzy search API with caching."""
    cache_key = f"{query}_{limit}"
    
    # Check cache first
    if cache_key in st.session_state.cached_search_results:
        return st.session_state.cached_search_results[cache_key]
    
    # Fetch from API
    result = make_api_request("GET", f"/simulation/models/search?name={query}&limit={limit}")
    models = result.get("models", []) if "error" not in result else []
    
    # Cache the results
    st.session_state.cached_search_results[cache_key] = models
    return models

def get_model_info(model_id: str) -> Dict:
    """Get detailed information about a model with caching."""
    # Check cache first
    if model_id in st.session_state.cached_model_info:
        return st.session_state.cached_model_info[model_id]
    
    # Fetch from API
    result = make_api_request("GET", f"/simulation/models/{model_id}")
    model_info = result.get("model", {}) if "error" not in result else {}
    
    # Cache the results
    if model_info:
        st.session_state.cached_model_info[model_id] = model_info
    
    return model_info

def get_model_script(model_id: str) -> str:
    """Get the refactored script for a model."""
    result = make_api_request("GET", f"/simulation/models/{model_id}/script")
    return result.get("script", "") if "error" not in result else ""

def save_model_script(model_id: str, script: str) -> Dict:
    """Save the modified script for a model."""
    data = {"script": script}
    result = make_api_request("POST", f"/simulation/models/{model_id}/script", data)
    return result

def extract_parameters_from_script(script_content: str) -> Dict:
    """Extract parameters from script content using simple AST analysis."""
    import ast
    import re
    
    params = {}
    
    try:
        # Parse the script
        tree = ast.parse(script_content)
        
        # Look for parameter definitions in the simulate function
        for node in ast.walk(tree):
            if isinstance(node, ast.FunctionDef) and node.name == 'simulate':
                # Look for parameter handling in the function
                for stmt in ast.walk(node):
                    if isinstance(stmt, ast.Assign):
                        for target in stmt.targets:
                            if isinstance(target, ast.Name):
                                param_name = target.id
                                # Skip common variable names
                                if param_name not in ['result', 'params', 'i', 'j', 'k', 'x', 'y', 't']:
                                    # Try to extract default value
                                    default_value = None
                                    if isinstance(stmt.value, ast.Constant):
                                        default_value = stmt.value.value
                                    elif isinstance(stmt.value, ast.Num):
                                        default_value = stmt.value.n
                                    elif isinstance(stmt.value, ast.Str):
                                        default_value = stmt.value.s
                                    elif isinstance(stmt.value, ast.List):
                                        default_value = [elt.value if isinstance(elt, ast.Constant) else str(elt) for elt in stmt.value.elts]
                                    
                                    # Determine parameter type
                                    param_type = 'string'
                                    if isinstance(default_value, (int, float)):
                                        param_type = 'number'
                                    elif isinstance(default_value, bool):
                                        param_type = 'boolean'
                                    elif isinstance(default_value, list):
                                        param_type = 'array'
                                    
                                    params[param_name] = {
                                        'type': param_type,
                                        'default': default_value,
                                        'description': f'Parameter {param_name} extracted from script'
                                    }
        
        # Also look for params.get() calls to find parameters
        param_pattern = r'params\.get\([\'"]([^\'"]+)[\'"]'
        matches = re.findall(param_pattern, script_content)
        
        for param_name in matches:
            if param_name not in params:
                params[param_name] = {
                    'type': 'string',
                    'default': '',
                    'description': f'Parameter {param_name} found in params.get() call'
                }
    
    except Exception as e:
        st.warning(f"Error parsing script: {e}")
    
    return params

def analyze_parameter_occurrences(script_content: str, parameters: Dict) -> Dict:
    """Analyze script to find parameter occurrences and their context."""
    import re
    
    occurrences = {}
    
    for param_name in parameters.keys():
        param_occurrences = []
        lines = script_content.split('\n')
        
        for line_num, line in enumerate(lines, 1):
            # Look for parameter usage in the line
            if param_name in line:
                # Get context (surrounding lines)
                start_line = max(0, line_num - 2)
                end_line = min(len(lines), line_num + 1)
                context_lines = lines[start_line:end_line]
                context = '\n'.join(context_lines)
                
                # Determine usage type
                usage_type = 'unknown'
                if f'params.get("{param_name}"' in line or f"params.get('{param_name}'" in line:
                    usage_type = 'parameter_access'
                elif f'{param_name} =' in line:
                    usage_type = 'assignment'
                elif param_name in line and any(op in line for op in ['+', '-', '*', '/', '=']):
                    usage_type = 'calculation'
                else:
                    usage_type = 'reference'
                
                param_occurrences.append({
                    'line': line_num,
                    'context': line.strip(),
                    'full_context': context,
                    'usage_type': usage_type
                })
        
        occurrences[param_name] = param_occurrences
    
    return occurrences

# Initialize session state for parameter tracking
if "parameter_changes" not in st.session_state:
    st.session_state.parameter_changes = {}
if "original_parameters" not in st.session_state:
    st.session_state.original_parameters = {}
if "current_script" not in st.session_state:
    st.session_state.current_script = ""

# Initialize session state for caching (needed for this page)
if "cached_model_info" not in st.session_state:
    st.session_state.cached_model_info = {}
if "cached_model_results" not in st.session_state:
    st.session_state.cached_model_results = {}
if "cached_model_code" not in st.session_state:
    st.session_state.cached_model_code = {}
if "cached_search_results" not in st.session_state:
    st.session_state.cached_search_results = {}
if "selected_model_id" not in st.session_state:
    st.session_state.selected_model_id = None

st.title("📝 Parameter Annotations & Script Management")

# Model Selection
st.header("1. Select Model")

search_query = st.text_input("Search models", placeholder="Enter model name...")

if search_query:
    models = search_models(search_query, limit=10)
    if models:
        model_options = {f"{m['name']} ({m['id']})": m['id'] for m in models}
        selected_model = st.selectbox("Choose a model", list(model_options.keys()))
        
        if selected_model:
            model_id = model_options[selected_model]
            st.session_state.selected_model_id = model_id
            
            # Get model info
            model_info = get_model_info(model_id)
            
            if model_info:
                st.success(f"✅ Selected model: {model_info.get('name', model_id)}")
                
                # Display model metadata
                with st.expander("📋 Model Information"):
                    st.json(model_info)
                
                # Extract parameters from model info and script
                extracted_params = model_info.get('parameters', {})
                
                # Get script content for parameter extraction
                script_result = make_api_request("GET", f"/simulation/models/{model_id}/script")
                script_content = ""
                if "error" not in script_result:
                    script_content = script_result.get("script", "")
                
                # Extract parameters from script if not available in model info
                if not extracted_params and script_content:
                    extracted_params = extract_parameters_from_script(script_content)
                
                if extracted_params:
                    st.header("2. Parameter Management")
                    
                    # Store original parameters if not already stored
                    if model_id not in st.session_state.original_parameters:
                        st.session_state.original_parameters[model_id] = extracted_params.copy()
                        st.session_state.parameter_changes[model_id] = {}
                    
                    # Analyze script for parameter occurrences
                    param_occurrences = analyze_parameter_occurrences(script_content, extracted_params)
                    
                    # Two-column layout: Parameters on left, Statistics on right
                    col1, col2 = st.columns([2, 1])
                    
                    with col1:
                        st.markdown("### 📊 Parameters & Values")
                        
                        # Parameter editing form
                        with st.form("parameter_form"):
                            updated_params = {}
                            
                            for param_name, param_info in extracted_params.items():
                                param_type = param_info.get('type', 'string')
                                param_default = param_info.get('default', '')
                                occurrence_count = len(param_occurrences.get(param_name, []))
                                
                                # Determine status and change indicator
                                is_changed = param_name in st.session_state.parameter_changes.get(model_id, {})
                                change_tag = " 🔄" if is_changed else ""
                                
                                # Status indicator
                                if occurrence_count == 0:
                                    status = "🔴"
                                elif occurrence_count == 1:
                                    status = "🟡"
                                else:
                                    status = "🟢"
                                
                                st.markdown(f"**{param_name}** ({param_type}) {status}{change_tag}")
                                
                                # Create appropriate input based on type
                                if param_type == 'number':
                                    value = st.number_input(
                                        f"Value for {param_name}",
                                        value=float(param_default) if param_default else 0.0,
                                        key=f"param_{model_id}_{param_name}"
                                    )
                                elif param_type == 'boolean':
                                    value = st.checkbox(
                                        f"Value for {param_name}",
                                        value=bool(param_default) if param_default else False,
                                        key=f"param_{model_id}_{param_name}"
                                    )
                                elif param_type == 'array':
                                    value_str = st.text_input(
                                        f"Value for {param_name} (JSON array)",
                                        value=json.dumps(param_default) if param_default else "[]",
                                        key=f"param_{model_id}_{param_name}"
                                    )
                                    try:
                                        value = json.loads(value_str)
                                    except json.JSONDecodeError:
                                        value = param_default
                                else:
                                    value = st.text_input(
                                        f"Value for {param_name}",
                                        value=str(param_default) if param_default else "",
                                        key=f"param_{model_id}_{param_name}"
                                    )
                                
                                updated_params[param_name] = value
                                
                                # Track changes
                                original_value = st.session_state.original_parameters[model_id].get(param_name, {}).get('default', '')
                                if value != original_value:
                                    st.session_state.parameter_changes[model_id][param_name] = {
                                        'original': original_value,
                                        'current': value,
                                        'changed': True,
                                        'occurrences': occurrence_count
                                    }
                                else:
                                    if param_name in st.session_state.parameter_changes[model_id]:
                                        del st.session_state.parameter_changes[model_id][param_name]
                            
                            submit_params = st.form_submit_button("💾 Save Changes")
                    
                    with col2:
                        st.markdown("### 📈 Statistics & Actions")
                        
                        # Parameter statistics
                        total_params = len(extracted_params)
                        active_params = sum(1 for param in extracted_params.keys() if param_occurrences.get(param))
                        unused_params = total_params - active_params
                        changed_params = len(st.session_state.parameter_changes.get(model_id, {}))
                        
                        st.metric("Total Parameters", total_params)
                        st.metric("Active Parameters", active_params)
                        st.metric("Changed Parameters", changed_params)
                        
                        # Quick actions
                        st.markdown("### ⚡ Quick Actions")
                        
                        if st.button("🔄 Reset All"):
                            st.session_state.parameter_changes[model_id] = {}
                            st.rerun()
                        
                        if st.button("📊 Export"):
                            param_data = {
                                "model_id": model_id,
                                "parameters": updated_params,
                                "changes": st.session_state.parameter_changes.get(model_id, {}),
                                "occurrences": param_occurrences
                            }
                            st.download_button(
                                label="📄 Download",
                                data=json.dumps(param_data, indent=2),
                                file_name=f"{model_id}_parameters.json",
                                mime="application/json"
                            )
                        
                        # Show change summary
                        if changed_params > 0:
                            st.success(f"🔄 {changed_params} parameters modified")
                            with st.expander("📝 View Changes"):
                                for param_name, change_info in st.session_state.parameter_changes[model_id].items():
                                    st.write(f"**{param_name}:** {change_info['original']} → {change_info['current']}")
                        else:
                            st.info("✅ No changes detected")
                
                # Script Management
                st.header("3. Script Management")
                
                # Get current script
                current_script = get_model_script(model_id)
                
                if current_script:
                    st.subheader("📝 Refactored Script")
                    
                    # Script editing
                    edited_script = st.text_area(
                        "Edit the refactored script:",
                        value=current_script,
                        height=400,
                        key=f"script_{model_id}"
                    )
                    
                    col1, col2 = st.columns(2)
                    
                    with col1:
                        if st.button("💾 Save Script Changes"):
                            result = save_model_script(model_id, edited_script)
                            if "error" not in result:
                                st.success("✅ Script saved successfully!")
                            else:
                                st.error(f"❌ Failed to save script: {result.get('error')}")
                    
                    with col2:
                        if st.button("🔄 Reset Script"):
                            st.rerun()
                    
                    # Script preview
                    with st.expander("👀 Script Preview"):
                        st.code(edited_script, language="python")
                
                # Simulation with updated parameters
                st.header("4. Quick Simulation")
                
                if st.button("🚀 Run Simulation with Current Parameters"):
                    # Get the updated parameters from the form
                    updated_params = {}
                    for param_name in extracted_params.keys():
                        param_key = f"param_{model_id}_{param_name}"
                        if param_key in st.session_state:
                            updated_params[param_name] = st.session_state[param_key]
                    
                    if updated_params:
                        with st.spinner("Running simulation with updated parameters..."):
                            data = {
                                "model_id": model_id,
                                "parameters": updated_params
                            }
                            
                            result = make_api_request("POST", "/simulation/run", data)
                            
                            if "error" not in result:
                                st.success("✅ Simulation completed successfully!")
                                
                                with st.expander("📊 Simulation Results"):
                                    st.json(result)
                                
                                # Store results for other pages
                                st.session_state.simulation_results = result
                            else:
                                st.error(f"❌ Simulation failed: {result.get('error')}")
                    else:
                        st.warning("⚠️ No parameters available for simulation")
                
            else:
                st.error("❌ Failed to load model information")
        else:
            st.info("Please select a model to continue")
    else:
        st.warning("No models found matching your search.")
else:
    st.info("🔍 Enter a model name to search and get started with parameter annotations")
