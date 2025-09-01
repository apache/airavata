REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Cast and validate parameters
    # r parameter
    if "r" not in params:
        r = np.float64(0.0)  # default guess
    else:
        r = params["r"]
        if isinstance(r, (str, int, float, np.generic)):
            r = np.float64(r)
        elif isinstance(r, (list, np.ndarray)):
            r = np.atleast_1d(np.array(r, dtype=np.float64))
        else:
            raise TypeError("Parameter 'r' must be numeric or list/array of numerics")

    # Initial condition x0
    if "x0" not in params:
        x0 = np.float64(0.1)  # given initial condition guess
    else:
        x0 = params["x0"]
        if isinstance(x0, (str, int, float, np.generic)):
            x0 = np.float64(x0)
        elif isinstance(x0, (list, np.ndarray)):
            x0 = np.atleast_1d(np.array(x0, dtype=np.float64))
        else:
            raise TypeError(
                "Initial condition 'x0' must be numeric or list/array of numerics"
            )

    # Time vector t
    if "t" not in params:
        t = np.linspace(0, 20, 201)  # default time span 0 to 20 with 201 points
    else:
        t = params["t"]
        if isinstance(t, (str, int, float, np.generic)):
            t = np.linspace(0, float(t), 201)
        elif isinstance(t, (list, np.ndarray)):
            t = np.atleast_1d(np.array(t, dtype=np.float64))
        else:
            raise TypeError("Parameter 't' must be numeric or list/array of numerics")

    # Define the ODE function dx/dt = r + x^2
    def ode_func(x, time, r_val):
        return r_val + x**2

    # If r is array-like with multiple values, simulate for each r and store results
    if np.ndim(r) > 0 and r.size > 1:
        xs = []
        for r_val in r:
            sol = odeint(ode_func, x0, t, args=(r_val,), atol=1e-9, rtol=1e-9)
            xs.append(sol.flatten())
        xs = np.array(xs)  # shape (len(r), len(t))
        # Convert to lists for output
        r_out = r.tolist()
        t_out = t.tolist()
        xs_out = xs.tolist()
        result = {"r": r_out, "t": t_out, "x": xs_out}
    else:
        # Single r value
        r_val = np.float64(r) if np.ndim(r) == 0 else r.item()
        sol = odeint(ode_func, x0, t, args=(r_val,), atol=1e-9, rtol=1e-9)
        x_out = sol.flatten().tolist()
        t_out = t.tolist()
        r_out = float(r_val)
        result = {"r": r_out, "t": t_out, "x": x_out}

    # Convert any numpy scalars to python built-ins
    def convert_types(obj):
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        elif isinstance(obj, np.generic):
            return obj.item()
        elif isinstance(obj, list):
            return [convert_types(i) for i in obj]
        else:
            return obj

    result = {k: convert_types(v) for k, v in result.items()}

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
