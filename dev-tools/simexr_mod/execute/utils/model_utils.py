import re
import hashlib
from pathlib import Path

# Import database function - adjust path as needed
from db import get_simulation_path

def _sha256(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest()

def make_variant_name(model_id: str, new_script: str, hash_len: int = 12) -> tuple[str, str, str]:
    """
    Create (variant_name, variant_path, variant_model_id) using a '<prefix>_<hash>' model id.

    Examples:
      model_id='lorenz_attractor_ea73a2d691d3'
        -> prefix='lorenz_attractor'
      model_id='lorenz_attractor_ea73a2d691d3::anything'
        -> prefix='lorenz_attractor'

    We compute new_hash = sha256(new_script)[:hash_len] and return:
      variant_model_id = f'{prefix}_{new_hash}'
      variant_name     = f'{variant_model_id}.py'
      variant_path     = Path(get_simulation_path(prefix)).with_name(variant_name)
    """
    # Strip any '::suffix' if present
    base = model_id.split("::", 1)[0]

    # Extract prefix by removing a trailing _<hexhash> (6..64 hex chars) if present
    m = re.match(r"^(?P<prefix>.+?)_(?P<hash>[0-9a-fA-F]{6,64})$", base)
    prefix = m.group("prefix") if m else base

    # Compute new short hash from script content
    new_hash = _sha256(new_script)[:hash_len]

    # Compose ids/paths
    variant_model_id = f"{prefix}_{new_hash}"
    variant_name = f"{variant_model_id}.py"
    # Save alongside the base model's script
    variant_path = Path(get_simulation_path(model_id)).with_name(variant_name)

    return variant_name, str(variant_path), variant_model_id
