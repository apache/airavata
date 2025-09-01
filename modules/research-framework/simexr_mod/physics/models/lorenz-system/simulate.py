REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Required keys
    required_keys = ["sigma", "rho", "beta", "x0", "y0", "z0", "t"]
    for key in required_keys:
        if key not in params:
            return {}

    # Cast and sanitize inputs
    def cast_val(v):
        if isinstance(v, (list, tuple)):
            arr = np.atleast_1d(np.array(v, dtype=np.float64))
            return arr
        else:
            # Cast scalars or strings to np.float64
            return np.float64(v)

    sigma = cast_val(params["sigma"])
    rho = cast_val(params["rho"])
    beta = cast_val(params["beta"])
    x0 = cast_val(params["x0"])
    y0 = cast_val(params["y0"])
    z0 = cast_val(params["z0"])
    t = cast_val(params["t"])

    # Initial state vector
    init_state = np.array([x0, y0, z0], dtype=np.float64)

    # Lorenz system ODEs
    def lorenz(state, time, sigma, rho, beta):
        x, y, z = state
        dxdt = sigma * (y - x)
        dydt = x * (rho - z) - y
        dzdt = x * y - beta * z
        return [dxdt, dydt, dzdt]

    # Integrate ODE with tight tolerances for determinism
    sol = odeint(lorenz, init_state, t, args=(sigma, rho, beta), atol=1e-9, rtol=1e-9)

    # Extract solution components
    x = sol[:, 0]
    y = sol[:, 1]
    z = sol[:, 2]

    # Convert numpy arrays and scalars to python lists and floats
    def to_py(obj):
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        elif isinstance(obj, (np.float64, np.float32)):
            return float(obj)
        elif isinstance(obj, (np.int64, np.int32)):
            return int(obj)
        else:
            return obj

    result = {
        "sigma": to_py(sigma),
        "rho": to_py(rho),
        "beta": to_py(beta),
        "x0": to_py(x0),
        "y0": to_py(y0),
        "z0": to_py(z0),
        "t": to_py(t),
        "x": to_py(x),
        "y": to_py(y),
        "z": to_py(z),
    }

    assert isinstance(result, dict)
    return result


if __name__ == "__main__":
    import argparse, json

    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--params", required=True, help="JSON string with simulation parameters"
    )
    args = ap.parse_args()
    result = simulate(**json.loads(args.params))
    print(json.dumps(result))
