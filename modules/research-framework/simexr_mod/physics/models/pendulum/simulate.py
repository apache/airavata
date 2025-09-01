REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Required keys
    required_keys = ["g", "L"]
    for key in required_keys:
        if key not in params:
            return {}
    # Cast and validate inputs
    try:
        g = params["g"]
        L = params["L"]
        # Cast g to np.float64 if scalar or string
        if isinstance(g, (str, int, float, np.generic)):
            g = np.float64(g)
        # Cast L to np.ndarray of np.float64, at least 1D
        if isinstance(L, list):
            L = np.atleast_1d(np.array(L, dtype=np.float64))
        else:
            # If scalar or string, cast to array
            L = np.atleast_1d(np.array([L], dtype=np.float64))
    except Exception:
        return {}

    # Initial conditions: theta(0) = pi/6, omega(0) = 0
    theta0 = np.float64(np.pi / 6)
    omega0 = np.float64(0.0)
    y0 = [theta0, omega0]

    # Time vector for simulation: 0 to 10 seconds, 1000 points
    t = np.linspace(0, 10, 1000)

    # Define the pendulum ODE system
    def pendulum_ode(y, t, g, L):
        theta, omega = y
        dtheta_dt = omega
        domega_dt = -(g / L) * np.sin(theta)
        return [dtheta_dt, domega_dt]

    # Prepare output containers
    results = {
        "g": float(g),
        "L": L.tolist(),
        "time": t.tolist(),
        "theta": [],
        "omega": [],
    }

    # Solve ODE for each L
    for length in L:
        sol = odeint(pendulum_ode, y0, t, args=(g, length), atol=1e-9, rtol=1e-9)
        theta_sol = sol[:, 0]
        omega_sol = sol[:, 1]
        results["theta"].append(theta_sol.tolist())
        results["omega"].append(omega_sol.tolist())

    assert isinstance(results, dict)
    return results


if __name__ == "__main__":
    import argparse, json

    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--params", required=True, help="JSON string with simulation parameters"
    )
    args = ap.parse_args()
    result = simulate(**json.loads(args.params))
    print(json.dumps(result))
