import hashlib
from pathlib import Path
from typing import Union

HASH_LENGTH = 12

def generate_model_id(model_name: str, model_script_path: Union[str, Path]) -> str:
    """
    Generate a unique model identifier by combining the model name with a content hash.

    Args:
        model_name: Name of the machine learning model
        model_script_path: Path to the model's script file

    Returns:
        str: Combined identifier in format 'model_name_contenthash'
    """
    def calculate_content_hash(file_content: str) -> str:
        return hashlib.sha1(file_content.encode()).hexdigest()[:HASH_LENGTH]

    script_content = Path(model_script_path).read_text()
    content_hash = calculate_content_hash(script_content)
    
    return f"{model_name}_{content_hash}"