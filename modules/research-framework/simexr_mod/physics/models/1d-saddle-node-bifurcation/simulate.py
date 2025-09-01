REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Required parameter: r
    if "r" not in params:
        return {}
    r = params["r"]

    # Cast r to np.float64 if needed
    if isinstance(r, (list, tuple, np.ndarray)):
        r = np.atleast_1d(np.array(r, dtype=np.float64))
    else:
        try:
            r = np.float64(r)
        except Exception:
            return {}

    # Initial condition x0 = 0.1 (given)
    x0 = np.float64(0.1)

    # Time span for integration
    t = np.linspace(0, 10, 500)

    # Define the ODE function
    def ode(x, t, r):
        return r + x**2

    # If r is array-like, simulate for each r and collect final states
    if isinstance(r, np.ndarray):
        xs = []
        for r_val in r:
            sol = odeint(ode, x0, t, args=(r_val,), atol=1e-9, rtol=1e-9)
            xs.append(sol[:, 0].tolist())
        # Convert xs to list of lists
        xs = [list(map(float, xlist)) for xlist in xs]
        r_out = r.tolist()
        result = {"r": r_out, "x_t": xs, "t": t.tolist()}
    else:
        # Single r value
        sol = odeint(ode, x0, t, args=(r,), atol=1e-9, rtol=1e-9)
        x_out = sol[:, 0].tolist()
        result = {"r": float(r), "x_t": x_out, "t": t.tolist()}

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
