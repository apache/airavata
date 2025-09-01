REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Required parameters: g, L
    required_keys = ["g", "L"]
    for key in required_keys:
        if key not in params:
            return {}
    # Cast inputs to np.float64 or np.ndarray as needed
    g = params["g"]
    L = params["L"]

    # Cast numeric inputs to np.float64 if needed
    if isinstance(g, (str, int, float, np.generic)):
        g = np.float64(g)
    elif isinstance(g, (list, np.ndarray)):
        g = np.atleast_1d(np.array(g, dtype=np.float64))
    else:
        return {}

    if isinstance(L, (str, int, float, np.generic)):
        L = np.float64(L)
    elif isinstance(L, (list, np.ndarray)):
        L = np.atleast_1d(np.array(L, dtype=np.float64))
    else:
        return {}

    # Initial conditions: theta(0) = pi/6, theta'(0) = 0
    theta0 = np.float64(np.pi / 6)
    omega0 = np.float64(0.0)
    y0 = [theta0, omega0]

    # Time span for simulation
    t = np.linspace(0, 10, 500)  # simulate for 10 seconds

    # Define the ODE system
    def pendulum_ode(y, t, g, L):
        theta, omega = y
        dtheta_dt = omega
        domega_dt = -(g / L) * np.sin(theta)
        return [dtheta_dt, domega_dt]

    # If L is array-like with multiple values, simulate each and return results
    # Otherwise simulate once
    results = {}
    if np.ndim(L) > 0 and L.size > 1:
        # Multiple L values: simulate each and store results
        theta_t_all = []
        for L_val in L:
            sol = odeint(pendulum_ode, y0, t, args=(g, L_val), atol=1e-9, rtol=1e-9)
            theta_t_all.append(sol[:, 0])
        # Convert to list of lists
        theta_t_all = [list(map(float, arr)) for arr in theta_t_all]
        results["L"] = list(map(float, L))
        results["t"] = list(map(float, t))
        results["theta_t"] = theta_t_all
    else:
        # Single L value
        L_val = float(L) if np.ndim(L) == 0 else float(L[0])
        sol = odeint(pendulum_ode, y0, t, args=(float(g), L_val), atol=1e-9, rtol=1e-9)
        theta_t = sol[:, 0]
        results["g"] = float(g)
        results["L"] = L_val
        results["t"] = list(map(float, t))
        results["theta_t"] = list(map(float, theta_t))

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
