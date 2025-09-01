REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Cast and validate parameters
    def cast_param(x):
        if isinstance(x, (list, tuple)):
            return np.atleast_1d(np.array(x, dtype=np.float64))
        else:
            return np.float64(x)

    # Parameters with defaults or guesses
    # g default 9.81 if missing or empty string
    g = params.get("g", 9.81)
    if isinstance(g, str):
        g = g.strip()
        g = 9.81 if g == "" else float(g)
    g = np.float64(g)

    # L must be provided or guessed
    L = params.get("L", None)
    if L is None or (isinstance(L, str) and L.strip() == ""):
        # Guess L = 1.0 m if missing
        L = np.float64(1.0)
    else:
        L = cast_param(L)

    # Initial conditions: theta(0) = pi/6, omega(0) = 0
    theta0 = params.get("theta0", np.pi / 6)
    if isinstance(theta0, str):
        theta0 = float(theta0)
    theta0 = np.float64(theta0)

    omega0 = params.get("omega0", 0.0)
    if isinstance(omega0, str):
        omega0 = float(omega0)
    omega0 = np.float64(omega0)

    # Time span for simulation: 0 to 10 seconds, 1000 points
    t = params.get("t", np.linspace(0, 10, 1000))
    t = cast_param(t)

    # ODE system: y = [theta, omega]
    def pendulum_ode(y, t, g, L):
        theta, omega = y
        dtheta_dt = omega
        domega_dt = -(g / L) * np.sin(theta)
        return [dtheta_dt, domega_dt]

    # If L is array-like, simulate for each L and return results
    if np.ndim(L) > 0:
        results = {"L": L.tolist(), "theta": [], "omega": [], "t": t.tolist()}
        for L_i in L:
            y0 = [theta0, omega0]
            sol = odeint(pendulum_ode, y0, t, args=(g, L_i), atol=1e-9, rtol=1e-9)
            theta_sol = sol[:, 0]
            omega_sol = sol[:, 1]
            results["theta"].append(theta_sol.tolist())
            results["omega"].append(omega_sol.tolist())
    else:
        y0 = [theta0, omega0]
        sol = odeint(pendulum_ode, y0, t, args=(g, L), atol=1e-9, rtol=1e-9)
        theta_sol = sol[:, 0]
        omega_sol = sol[:, 1]
        results = {
            "L": float(L),
            "g": float(g),
            "theta0": float(theta0),
            "omega0": float(omega0),
            "t": t.tolist(),
            "theta": theta_sol.tolist(),
            "omega": omega_sol.tolist(),
        }

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
