import sys
sys.path.append('/')

c = get_config()

c.InteractiveShellApp.exec_lines = [
    "import sys"
    "sys.path.append('/')"
    "import airavata_magics",
    "airavata_magics.load_ipython_extension(get_ipython())"
]

# Set the IP address Jupyter Lab will listen on
c.ServerApp.ip = '0.0.0.0'

# Set the port Jupyter Lab will listen on
c.ServerApp.port = 8888

# Don't open the browser by default
c.ServerApp.open_browser = False

c.FileContentsManager.use_atomic_writing = False

# Allow root access
c.ServerApp.allow_root = True