#!/usr/bin/env python3
"""
SimExR Framework - Complete API Testing Script

This script demonstrates all the APIs and their functionality with real examples.
Run this script to test the complete workflow from GitHub import to AI analysis.
"""

import requests
import json
import time
import sys
from typing import Dict, Any, List

# Configuration
BASE_URL = "http://127.0.0.1:8000"
GITHUB_URL = "https://github.com/vash02/physics-systems-dataset/blob/main/vanderpol.py"
MODEL_NAME = "vanderpol_transform_test"

class SimExRAPITester:
    def __init__(self, base_url: str = BASE_URL):
        self.base_url = base_url
        self.model_id = None
        self.test_results = {}
        
    def print_header(self, title: str):
        """Print a formatted header."""
        print(f"\n{'='*60}")
        print(f"🧪 {title}")
        print(f"{'='*60}")
        
    def print_success(self, message: str):
        """Print a success message."""
        print(f"✅ {message}")
        
    def print_error(self, message: str):
        """Print an error message."""
        print(f"❌ {message}")
        
    def print_info(self, message: str):
        """Print an info message."""
        print(f"ℹ️  {message}")
        
    def make_request(self, method: str, endpoint: str, data: Dict = None) -> Dict:
        """Make an HTTP request and return the response."""
        url = f"{self.base_url}{endpoint}"
        headers = {"Content-Type": "application/json"}
        
        try:
            if method.upper() == "GET":
                response = requests.get(url, headers=headers)
            elif method.upper() == "POST":
                response = requests.post(url, headers=headers, json=data)
            elif method.upper() == "DELETE":
                response = requests.delete(url, headers=headers)
            else:
                raise ValueError(f"Unsupported method: {method}")
                
            response.raise_for_status()
            return response.json()
            
        except requests.exceptions.RequestException as e:
            self.print_error(f"Request failed: {e}")
            return {"error": str(e)}
            
    def test_health_api(self) -> bool:
        """Test the health check API."""
        self.print_header("Testing Health API")
        
        # Test health status
        result = self.make_request("GET", "/health/status")
        if "error" not in result:
            self.print_success("Health status API working")
            self.print_info(f"Status: {result.get('status', 'unknown')}")
            return True
        else:
            self.print_error("Health status API failed")
            return False
            
    def test_github_import(self) -> bool:
        """Test GitHub script import and transformation."""
        self.print_header("Testing GitHub Import & Transformation")
        
        data = {
            "github_url": GITHUB_URL,
            "model_name": MODEL_NAME,
            "max_smoke_iters": 3
        }
        
        self.print_info(f"Importing from: {GITHUB_URL}")
        result = self.make_request("POST", "/simulation/transform/github", data)
        
        if "error" not in result and "model_id" in result:
            self.model_id = result["model_id"]
            self.print_success(f"Successfully imported model: {self.model_id}")
            self.print_info(f"Model name: {result.get('model_name', 'unknown')}")
            self.print_info(f"Script path: {result.get('script_path', 'unknown')}")
            return True
        else:
            self.print_error("GitHub import failed")
            return False
            
    def test_single_simulation(self) -> bool:
        """Test single simulation execution."""
        self.print_header("Testing Single Simulation")
        
        if not self.model_id:
            self.print_error("No model ID available")
            return False
            
        data = {
            "model_id": self.model_id,
            "parameters": {
                "mu": 1.5,
                "z0": [1.5, 0.5],
                "eval_time": 25,
                "t_iteration": 250,
                "plot": False
            }
        }
        
        self.print_info("Running single simulation...")
        result = self.make_request("POST", "/simulation/run", data)
        
        if "error" not in result and "status" in result:
            self.print_success("Single simulation completed")
            self.print_info(f"Status: {result.get('status', 'unknown')}")
            self.print_info(f"Execution time: {result.get('execution_time', 'unknown')}s")
            return True
        else:
            self.print_error("Single simulation failed")
            return False
            
    def test_batch_simulation(self) -> bool:
        """Test batch simulation execution."""
        self.print_header("Testing Batch Simulation")
        
        if not self.model_id:
            self.print_error("No model ID available")
            return False
            
        data = {
            "model_id": self.model_id,
            "parameter_grid": [
                {
                    "mu": 1.0,
                    "z0": [2, 0],
                    "eval_time": 30,
                    "t_iteration": 300,
                    "plot": False
                },
                {
                    "mu": 1.5,
                    "z0": [1.5, 0.5],
                    "eval_time": 25,
                    "t_iteration": 250,
                    "plot": False
                }
            ]
        }
        
        self.print_info("Running batch simulation...")
        result = self.make_request("POST", "/simulation/batch", data)
        
        if "error" not in result and "results" in result:
            self.print_success("Batch simulation completed")
            self.print_info(f"Total simulations: {len(result.get('results', []))}")
            successful = sum(1 for r in result.get('results', []) if r.get('status') == 'completed')
            self.print_info(f"Successful: {successful}")
            return True
        else:
            self.print_error("Batch simulation failed")
            return False
            
    def test_model_management(self) -> bool:
        """Test model management APIs."""
        self.print_header("Testing Model Management APIs")
        
        # Test list models
        self.print_info("Testing list models...")
        result = self.make_request("GET", "/simulation/models")
        if "error" not in result and "models" in result:
            self.print_success(f"Listed {len(result['models'])} models")
        else:
            self.print_error("List models failed")
            return False
            
        # Test fuzzy search
        self.print_info("Testing fuzzy search...")
        result = self.make_request("GET", "/simulation/models/search?name=vanderpol&limit=3")
        if "error" not in result and "models" in result:
            self.print_success(f"Found {len(result['models'])} matching models")
        else:
            self.print_error("Fuzzy search failed")
            return False
            
        # Test get model info
        if self.model_id:
            self.print_info("Testing get model info...")
            result = self.make_request("GET", f"/simulation/models/{self.model_id}")
            if "error" not in result and "model" in result:
                self.print_success("Retrieved model information")
            else:
                self.print_error("Get model info failed")
                return False
                
        return True
        
    def test_results_apis(self) -> bool:
        """Test results retrieval APIs."""
        self.print_header("Testing Results APIs")
        
        if not self.model_id:
            self.print_error("No model ID available")
            return False
            
        # Test get model results
        self.print_info("Testing get model results...")
        result = self.make_request("GET", f"/simulation/models/{self.model_id}/results?limit=5")
        if "error" not in result and "results" in result:
            self.print_success(f"Retrieved {len(result['results'])} results")
            self.print_info(f"Total count: {result.get('total_count', 0)}")
        else:
            self.print_error("Get model results failed")
            return False
            
        # Test database results
        self.print_info("Testing database results...")
        result = self.make_request("GET", f"/database/results?model_id={self.model_id}&limit=3")
        if "error" not in result and "results" in result:
            self.print_success(f"Retrieved {len(result['results'])} database results")
        else:
            self.print_error("Database results failed")
            return False
            
        return True
        
    def test_reasoning_apis(self) -> bool:
        """Test AI reasoning APIs."""
        self.print_header("Testing AI Reasoning APIs")
        
        if not self.model_id:
            self.print_error("No model ID available")
            return False
            
        # Test ask reasoning question
        self.print_info("Testing AI reasoning...")
        data = {
            "model_id": self.model_id,
            "question": "What is the behavior of the van der Pol oscillator for mu=1.0 and mu=1.5? How do the trajectories differ?",
            "max_steps": 3
        }
        
        result = self.make_request("POST", "/reasoning/ask", data)
        if "error" not in result and "answer" in result:
            self.print_success("AI reasoning completed")
            self.print_info(f"Execution time: {result.get('execution_time', 'unknown')}s")
            self.print_info(f"Answer length: {len(result.get('answer', ''))} characters")
        else:
            self.print_error("AI reasoning failed")
            return False
            
        # Test get reasoning history
        self.print_info("Testing reasoning history...")
        result = self.make_request("GET", f"/reasoning/history/{self.model_id}?limit=3")
        if "error" not in result and "conversations" in result:
            self.print_success(f"Retrieved {len(result['conversations'])} conversations")
        else:
            self.print_error("Reasoning history failed")
            return False
            
        # Test get all conversations
        self.print_info("Testing all conversations...")
        result = self.make_request("GET", "/reasoning/conversations?limit=5")
        if "error" not in result and "conversations" in result:
            self.print_success(f"Retrieved {len(result['conversations'])} total conversations")
        else:
            self.print_error("All conversations failed")
            return False
            
        # Test reasoning statistics
        self.print_info("Testing reasoning statistics...")
        result = self.make_request("GET", "/reasoning/stats")
        if "error" not in result and "total_conversations" in result:
            self.print_success("Retrieved reasoning statistics")
            self.print_info(f"Total conversations: {result.get('total_conversations', 0)}")
            self.print_info(f"Unique models: {result.get('unique_models', 0)}")
        else:
            self.print_error("Reasoning statistics failed")
            return False
            
        return True
        
    def test_database_apis(self) -> bool:
        """Test database read-only APIs."""
        self.print_header("Testing Database APIs")
        
        # Test database stats
        self.print_info("Testing database stats...")
        result = self.make_request("GET", "/database/stats")
        if "error" not in result:
            self.print_success("Retrieved database statistics")
            self.print_info(f"Total models: {result.get('total_models', 0)}")
            self.print_info(f"Total results: {result.get('total_results', 0)}")
        else:
            self.print_error("Database stats failed")
            return False
            
        # Test database models
        self.print_info("Testing database models...")
        result = self.make_request("GET", "/database/models?limit=5")
        if "error" not in result and "models" in result:
            self.print_success(f"Retrieved {len(result['models'])} database models")
        else:
            self.print_error("Database models failed")
            return False
            
        return True
        
    def run_complete_test(self) -> Dict[str, Any]:
        """Run the complete API test suite."""
        self.print_header("SimExR Framework - Complete API Test Suite")
        
        tests = [
            ("Health API", self.test_health_api),
            ("GitHub Import", self.test_github_import),
            ("Single Simulation", self.test_single_simulation),
            ("Batch Simulation", self.test_batch_simulation),
            ("Model Management", self.test_model_management),
            ("Results APIs", self.test_results_apis),
            ("AI Reasoning", self.test_reasoning_apis),
            ("Database APIs", self.test_database_apis),
        ]
        
        results = {}
        total_tests = len(tests)
        passed_tests = 0
        
        for test_name, test_func in tests:
            try:
                success = test_func()
                results[test_name] = success
                if success:
                    passed_tests += 1
                time.sleep(1)  # Brief pause between tests
            except Exception as e:
                self.print_error(f"Test {test_name} failed with exception: {e}")
                results[test_name] = False
                
        # Print summary
        self.print_header("Test Results Summary")
        print(f"📊 Total Tests: {total_tests}")
        print(f"✅ Passed: {passed_tests}")
        print(f"❌ Failed: {total_tests - passed_tests}")
        print(f"📈 Success Rate: {(passed_tests/total_tests)*100:.1f}%")
        
        print("\n📋 Detailed Results:")
        for test_name, success in results.items():
            status = "✅ PASS" if success else "❌ FAIL"
            print(f"  {status} {test_name}")
            
        return {
            "total_tests": total_tests,
            "passed_tests": passed_tests,
            "failed_tests": total_tests - passed_tests,
            "success_rate": (passed_tests/total_tests)*100,
            "results": results,
            "model_id": self.model_id
        }

def main():
    """Main function to run the API tests."""
    print("🚀 SimExR Framework API Testing")
    print("=================================")
    
    # Check if server is running
    try:
        response = requests.get(f"{BASE_URL}/health/status", timeout=5)
        if response.status_code != 200:
            print(f"❌ Server is not responding properly. Status code: {response.status_code}")
            sys.exit(1)
    except requests.exceptions.RequestException:
        print(f"❌ Cannot connect to server at {BASE_URL}")
        print("💡 Make sure the server is running with: python start_api.py --host 127.0.0.1 --port 8000")
        sys.exit(1)
        
    # Run tests
    tester = SimExRAPITester()
    results = tester.run_complete_test()
    
    # Save results
    with open("test_results.json", "w") as f:
        json.dump(results, f, indent=2)
        
    print(f"\n📄 Test results saved to test_results.json")
    
    if results["passed_tests"] == results["total_tests"]:
        print("\n🎉 All tests passed! The SimExR framework is working perfectly.")
        sys.exit(0)
    else:
        print(f"\n⚠️  {results['failed_tests']} test(s) failed. Please check the logs above.")
        sys.exit(1)

if __name__ == "__main__":
    main()
