REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(m=1.0, c=0.5, k=1.0):
    # Define the system of equations
    def system(y, t):
        x, v = y
        dxdt = v
        dvdt = -(c / m) * v - (k / m) * x
        return [dxdt, dvdt]

    # Initial conditions
    y0 = [1.0, 0.0]

    # Time vector
    t = np.linspace(0, 10, 1000)

    # Solve the ODE
    sol = odeint(system, y0, t, atol=1e-9, rtol=1e-9)

    # Compute damping ratio
    wn = np.sqrt(k / m)  # natural frequency
    zeta = c / (2 * m * wn)  # damping ratio

    # Return results
    result = {
        "time": t.tolist(),
        "x": sol[:, 0].tolist(),
        "v": sol[:, 1].tolist(),
        "damping_ratio": zeta,
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
