REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Helper to cast inputs to np.float64 or np.ndarray of np.float64
    def cast_param(val):
        if isinstance(val, (list, tuple)):
            arr = np.atleast_1d(np.array(val, dtype=np.float64))
            return arr
        else:
            # scalar or string representing a number
            return np.float64(val)

    # Cast parameters r, a, t
    # Defaults:
    # r: 0.0 (midpoint of -1 to 1.5)
    # a: 0.2 (given)
    # t: np.linspace(0,30,300)
    r = params.get("r", 0.0)
    a = params.get("a", 0.2)
    t = params.get("t", np.linspace(0, 30, 300))

    r = cast_param(r)
    a = cast_param(a)
    t = cast_param(t)

    # Initial condition x(0)=0.0 if not provided
    x0 = params.get("x0", 0.0)
    x0 = cast_param(x0)
    # Ensure x0 is scalar float64
    if isinstance(x0, np.ndarray):
        x0 = float(x0.flat[0])

    # Define ODE function: dx/dt = r - x^2 + a*x^3
    def dxdt(x, t):
        return r - x**2 + a * x**3

    # Integrate ODE
    sol = odeint(dxdt, x0, t, atol=1e-9, rtol=1e-9)
    x = sol.flatten()

    # Convert outputs to python native types
    def to_py(val):
        if isinstance(val, np.ndarray):
            return val.tolist()
        elif isinstance(val, (np.generic, np.float64, np.float32, np.int64, np.int32)):
            return val.item()
        else:
            return val

    r_out = to_py(r)
    a_out = to_py(a)
    t_out = to_py(t)
    x_out = to_py(x)

    result = {
        "r": r_out,
        "a": a_out,
        "t": t_out,
        "x": x_out,
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
