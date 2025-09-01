REQUIREMENTS = ["numpy", "scipy", "matplotlib"]

import json, sys
import numpy as np
from scipy.integrate import odeint
import matplotlib.pyplot as plt

# ─── Reproducibility ───────────────────────────────
np.random.seed(0)


def simulate(**params):
    # Cast parameters to np.float64 or np.ndarray as needed
    def cast_param(p):
        if isinstance(p, (list, tuple)):
            return np.atleast_1d(np.array(p, dtype=np.float64))
        elif isinstance(p, (str, int, float, np.generic)):
            try:
                return np.float64(p)
            except Exception:
                # fallback if cannot cast directly
                return p
        else:
            return p

    # Default parameters
    sigma = cast_param(params.get("sigma", 10.0))
    rho = cast_param(params.get("rho", 28.0))
    beta = cast_param(params.get("beta", 8.0 / 3.0))

    # Time span
    t_range = params.get("t", [0, 50])
    t_range = np.atleast_1d(t_range)
    if t_range.size == 2:
        t = np.linspace(t_range[0], t_range[1], 10000)
    else:
        t = np.atleast_1d(t_range).astype(np.float64)

    # Initial conditions
    x0 = cast_param(params.get("x(0)", 1.0))
    y0 = cast_param(params.get("y(0)", 1.0))
    z0 = cast_param(params.get("z(0)", 1.0))
    y_init = np.array([x0, y0, z0], dtype=np.float64)

    # Lorenz system ODEs
    def lorenz(y, t, sigma, rho, beta):
        x, y_, z = y
        dxdt = sigma * (y_ - x)
        dydt = x * (rho - z) - y_
        dzdt = x * y_ - beta * z
        return [dxdt, dydt, dzdt]

    # Integrate ODE
    sol = odeint(lorenz, y_init, t, args=(sigma, rho, beta), atol=1e-9, rtol=1e-9)

    # Prepare output converting numpy arrays/scalars to python lists/scalars
    out = {
        "t": t.tolist(),
        "x": sol[:, 0].tolist(),
        "y": sol[:, 1].tolist(),
        "z": sol[:, 2].tolist(),
        "sigma": float(sigma),
        "rho": float(rho),
        "beta": float(beta),
        "x(0)": float(x0),
        "y(0)": float(y0),
        "z(0)": float(z0),
    }

    assert isinstance(out, dict)
    return out


if __name__ == "__main__":
    import argparse, json

    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--params", required=True, help="JSON string with simulation parameters"
    )
    args = ap.parse_args()
    result = simulate(**json.loads(args.params))
    print(json.dumps(result))
