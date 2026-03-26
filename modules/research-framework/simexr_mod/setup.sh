#!/bin/bash

# SimExR Framework Setup Script
# This script automates the installation and configuration of the SimExR framework

set -e  # Exit on any error

echo "🚀 SimExR Framework Setup"
echo "=========================="

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.8+ first."
    exit 1
fi

# Check Python version
PYTHON_VERSION=$(python3 -c 'import sys; print(".".join(map(str, sys.version_info[:2])))')
echo "✅ Python version: $PYTHON_VERSION"

# Create virtual environment
echo "📦 Creating virtual environment..."
if [ -d "simexr_venv" ]; then
    echo "⚠️  Virtual environment already exists. Removing..."
    rm -rf simexr_venv
fi

python3 -m venv simexr_venv
echo "✅ Virtual environment created"

# Activate virtual environment
echo "🔧 Activating virtual environment..."
source simexr_venv/bin/activate

# Upgrade pip
echo "⬆️  Upgrading pip..."
pip install --upgrade pip

# Install dependencies
echo "📚 Installing dependencies..."
if [ -f "requirements.txt" ]; then
    pip install -r requirements.txt
else
    echo "⚠️  requirements.txt not found. Installing common dependencies..."
    pip install fastapi uvicorn openai pandas numpy scipy matplotlib tqdm sqlalchemy
fi

# Create config directory if it doesn't exist
echo "⚙️  Setting up configuration..."
mkdir -p utils

# Create config file if it doesn't exist
if [ ! -f "utils/config.yaml" ]; then
    echo "📝 Creating config.yaml template..."
    cat > utils/config.yaml << EOF
# SimExR Configuration
openai:
  api_key: "your-openai-api-key-here"
  
# Database configuration
database:
  path: "mcp.db"
  
# Logging configuration
logging:
  level: "INFO"
  format: "%(asctime)s | %(levelname)-8s | %(name)-20s | %(message)s"
EOF
    echo "✅ Configuration file created at utils/config.yaml"
    echo "⚠️  Please update utils/config.yaml with your OpenAI API key"
else
    echo "✅ Configuration file already exists"
fi

# Create external_models directory
echo "📁 Creating directories..."
mkdir -p external_models
mkdir -p systems/models
mkdir -p logs

# Set up database
echo "🗄️  Setting up database..."
if [ ! -f "mcp.db" ]; then
    echo "✅ Database will be created on first run"
else
    echo "✅ Database already exists"
fi

# Test the installation
echo "🧪 Testing installation..."
python3 -c "
import sys
print('✅ Python path:', sys.executable)
try:
    import fastapi
    print('✅ FastAPI installed')
except ImportError:
    print('❌ FastAPI not installed')
    sys.exit(1)
try:
    import openai
    print('✅ OpenAI installed')
except ImportError:
    print('❌ OpenAI not installed')
    sys.exit(1)
try:
    import pandas
    print('✅ Pandas installed')
except ImportError:
    print('❌ Pandas not installed')
    sys.exit(1)
print('✅ All core dependencies installed successfully')
"

echo ""
echo "🎉 Setup completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Update utils/config.yaml with your OpenAI API key"
echo "2. Activate the virtual environment: source simexr_venv/bin/activate"
echo "3. Start the API server: python start_api.py --host airavata.localhost --port 8001"
echo "4. Visit http://airavata.localhost:8001/docs for API documentation"
echo ""
echo "🔗 Quick start commands:"
echo "source simexr_venv/bin/activate"
echo "python start_api.py --host airavata.localhost --port 8001"
echo ""
echo "📖 For more information, see README.md"
