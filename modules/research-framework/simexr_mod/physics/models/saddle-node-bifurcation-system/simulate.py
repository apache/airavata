REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Read parameters from params
    r = params.get(
        "r", np.arange(-2, 2, 0.2)
    )  # default range from -2 to 2 with step 0.2

    # Define the system of equations
    def system(theta, t):
        omega = 0  # Assuming omega is 0 as it is not provided in the parameters
        p = 1  # Assuming p is 1 as it is not provided in the parameters
        dtheta_dt = omega + theta**2 * p
        return dtheta_dt

    # Initial conditions
    theta0 = 0.1

    # Time points
    t = np.linspace(0, 10, 100)

    # Store results
    results = {}

    # Solve ODE for each value of r
    for r_val in r:
        sol = odeint(system, theta0, t, atol=1e-9, rtol=1e-9)
        results[str(r_val)] = sol.tolist()

    # Ensure the return is a dict
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
