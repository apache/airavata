def simulate(**params):
    import math
    try:
        import numpy as np
    except Exception as e:
        return {"success": False, "message": f"Missing dependency numpy: {e}"}

    # --- Parameter defaults and robust casting ---
    # Helper: robust cast inline (no function defs)
    def _robust_to_float(val, fallback):
        try:
            if isinstance(val, (list, tuple, np.ndarray)):
                val = val[0]
            return float(val)
        except Exception:
            try:
                return float(fallback)
            except Exception:
                return fallback

    # Read scalar parameters with reasonable defaults
    alpha = _robust_to_float(params.get("alpha", 1.0), 1.0)
    beta = _robust_to_float(params.get("beta", 0.1), 0.1)
    delta = _robust_to_float(params.get("delta", 0.075), 0.075)
    gamma = _robust_to_float(params.get("gamma", 1.5), 1.5)

    # Time span
    t_span_param = params.get("t_span", (0.0, 60.0))
    try:
        if isinstance(t_span_param, (list, tuple, np.ndarray)) and len(t_span_param) >= 2:
            t0 = float(t_span_param[0])
            tf = float(t_span_param[1])
        elif isinstance(t_span_param, (int, float)):
            t0 = 0.0
            tf = float(t_span_param)
        else:
            raise ValueError("Invalid t_span")
    except Exception:
        t0, tf = 0.0, 60.0
    if tf <= t0:
        tf = t0 + abs(tf - t0) if tf != t0 else t0 + 60.0

    # t_eval: accept int (npoints) or array-like
    t_eval_param = params.get("t_eval", None)
    if t_eval_param is None:
        n_points = int(params.get("n_points", 2000))
        if n_points < 2:
            n_points = 2000
        t_eval = np.linspace(t0, tf, n_points)
    else:
        try:
            if isinstance(t_eval_param, (int, np.integer)):
                n_points = int(t_eval_param)
                if n_points < 2:
                    n_points = 2000
                t_eval = np.linspace(t0, tf, n_points)
            else:
                t_eval = np.array(t_eval_param, dtype=float)
                # Ensure within t0..tf and sorted
                if t_eval.size < 2 or t_eval[0] > t_eval[-1]:
                    t_eval = np.linspace(t0, tf, max(2, t_eval.size))
        except Exception:
            t_eval = np.linspace(t0, tf, 2000)

    # Initial conditions (x0 prey, y0 predator)
    x0_param = params.get("x0", None)
    y0_param = params.get("y0", None)
    try:
        if x0_param is None and y0_param is None:
            # intelligent guess based on equilibria: use (alpha/beta * 0.8, gamma/delta * 0.8)
            x0 = 0.8 * (alpha / beta if beta != 0 else 10.0)
            y0 = 0.8 * (gamma / delta if delta != 0 else 5.0)
        else:
            x0 = _robust_to_float(x0_param if x0_param is not None else 10.0, 10.0)
            y0 = _robust_to_float(y0_param if y0_param is not None else 5.0, 5.0)
    except Exception:
        x0, y0 = 10.0, 5.0

    # Grid resolution for vector field (if user provided)
    try:
        nx = int(params.get("nx", 20))
    except Exception:
        nx = 20
    try:
        ny = int(params.get("ny", 20))
    except Exception:
        ny = 20
    if nx <= 0:
        nx = 20
    if ny <= 0:
        ny = 20

    # Solver tolerances
    try:
        rtol = float(params.get("rtol", 1e-8))
    except Exception:
        rtol = 1e-8
    try:
        atol = float(params.get("atol", 1e-10))
    except Exception:
        atol = 1e-10

    # Prepare outputs dictionary
    result = {
        "success": False,
        "message": "",
        "params_used": {
            "alpha": float(alpha),
            "beta": float(beta),
            "delta": float(delta),
            "gamma": float(gamma),
            "t0": float(t0),
            "tf": float(tf),
            "x0": float(x0),
            "y0": float(y0),
        },
    }

    # --- Define dynamics inline (no extra defs) ---
    # dx/dt = alpha*x - beta*x*y
    # dy/dt = delta*x*y - gamma*y

    # Attempt to use scipy.integrate.solve_ivp; fallback to RK4 if not available
    use_scipy = True
    sol_t = None
    sol_y = None
    try:
        from scipy.integrate import solve_ivp  # type: ignore
    except Exception:
        use_scipy = False

    try:
        if use_scipy:
            # use lambda to avoid defining a new function
            sol = solve_ivp(
                fun=lambda t, z: [alpha * z[0] - beta * z[0] * z[1], delta * z[0] * z[1] - gamma * z[1]],
                t_span=(t0, tf),
                y0=[x0, y0],
                method=params.get("method", "RK45"),
                t_eval=t_eval,
                rtol=rtol,
                atol=atol,
            )
            if not getattr(sol, "success", False):
                # capture message but still try fallback RK4
                fallback_msg = f"scipy solve_ivp failed: {getattr(sol, 'message', 'unknown')}"
                # fallback to RK4
                use_scipy = False
                sol_t = None
                sol_y = None
            else:
                sol_t = np.array(sol.t, dtype=float)
                sol_y = np.array(sol.y, dtype=float)
                result["message"] = "Integration successful (scipy.solve_ivp)."
                result["success"] = True
        if not use_scipy:
            # Simple fixed-step RK4 over t_eval
            te = np.array(t_eval, dtype=float)
            n_steps = te.size
            y_out = np.zeros((2, n_steps), dtype=float)
            y = np.array([float(x0), float(y0)], dtype=float)
            y_out[:, 0] = y
            for i in range(1, n_steps):
                dt = te[i] - te[i - 1]
                if dt <= 0:
                    dt = (tf - t0) / max(1, n_steps - 1)
                # k1
                k1x = alpha * y[0] - beta * y[0] * y[1]
                k1y = delta * y[0] * y[1] - gamma * y[1]
                k1 = np.array([k1x, k1y], dtype=float)
                # k2
                yk = y + 0.5 * dt * k1
                k2x = alpha * yk[0] - beta * yk[0] * yk[1]
                k2y = delta * yk[0] * yk[1] - gamma * yk[1]
                k2 = np.array([k2x, k2y], dtype=float)
                # k3
                yk = y + 0.5 * dt * k2
                k3x = alpha * yk[0] - beta * yk[0] * yk[1]
                k3y = delta * yk[0] * yk[1] - gamma * yk[1]
                k3 = np.array([k3x, k3y], dtype=float)
                # k4
                yk = y + dt * k3
                k4x = alpha * yk[0] - beta * yk[0] * yk[1]
                k4y = delta * yk[0] * yk[1] - gamma * yk[1]
                k4 = np.array([k4x, k4y], dtype=float)
                y = y + (dt / 6.0) * (k1 + 2.0 * k2 + 2.0 * k3 + k4)
                # Prevent negative populations (clip)
                y = np.maximum(y, 0.0)
                y_out[:, i] = y
            sol_t = te
            sol_y = y_out
            result["message"] = "Integration successful (fallback RK4)."
            result["success"] = True

    except Exception as e:
        result["success"] = False
        result["message"] = f"Integration error: {e}"
        # Return partial info
        return result

    # Ensure sol_t and sol_y are numpy arrays
    try:
        sol_t = np.array(sol_t, dtype=float)
        sol_y = np.array(sol_y, dtype=float)
    except Exception:
        result["success"] = False
        result["message"] = "Solver produced invalid numerical arrays."
        return result

    # Extract x and y
    try:
        if sol_y.shape[0] >= 2:
            x = np.array(sol_y[0, :], dtype=float)
            y = np.array(sol_y[1, :], dtype=float)
        elif sol_y.shape[1] >= 2:
            x = np.array(sol_y[:, 0], dtype=float)
            y = np.array(sol_y[:, 1], dtype=float)
        else:
            raise ValueError("Unexpected solver output shape")
    except Exception as e:
        result["success"] = False
        result["message"] = f"Error extracting solution arrays: {e}"
        return result

    # Equilibria
    try:
        E0 = (0.0, 0.0)
        E1 = (gamma / delta if delta != 0 else float("nan"), alpha / beta if beta != 0 else float("nan"))
    except Exception:
        E0 = (0.0, 0.0)
        E1 = (float("nan"), float("nan"))

    # Jacobians
    try:
        # J = [[alpha - beta*y, -beta*x],
        #      [delta*y, delta*x - gamma]]
        J_e0 = np.array([[alpha - beta * E0[1], -beta * E0[0]], [delta * E0[1], delta * E0[0] - gamma]], dtype=float)
        J_e1 = np.array([[alpha - beta * E1[1], -beta * E1[0]], [delta * E1[1], delta * E1[0] - gamma]], dtype=float)
    except Exception:
        J_e0 = np.array([[float("nan"), float("nan")], [float("nan"), float("nan")]], dtype=float)
        J_e1 = np.array([[float("nan"), float("nan")], [float("nan"), float("nan")]], dtype=float)

    # Eigenvalues
    try:
        eig_e0 = np.linalg.eigvals(J_e0).tolist()
    except Exception:
        eig_e0 = [complex(float("nan")), complex(float("nan"))]
    try:
        eig_e1 = np.linalg.eigvals(J_e1).tolist()
    except Exception:
        eig_e1 = [complex(float("nan")), complex(float("nan"))]

    # Nullclines
    try:
        y_nc = alpha / beta if beta != 0 else float("nan")
        x_nc = gamma / delta if delta != 0 else float("nan")
    except Exception:
        y_nc = float("nan")
        x_nc = float("nan")

    # Vector field grid (small arrays converted to lists)
    try:
        xmax = max(float(np.max(x)), float(x_nc) if not math.isnan(x_nc) else 0.0) * 1.2
        ymax = max(float(np.max(y)), float(y_nc) if not math.isnan(y_nc) else 0.0) * 1.2
        if not np.isfinite(xmax) or xmax <= 0:
            xmax = max(10.0, float(np.max(x)) * 1.2 if np.isfinite(np.max(x)) else 10.0)
        if not np.isfinite(ymax) or ymax <= 0:
            ymax = max(5.0, float(np.max(y)) * 1.2 if np.isfinite(np.max(y)) else 5.0)
        Xg = np.linspace(0.0, xmax, nx)
        Yg = np.linspace(0.0, ymax, ny)
        X_mesh, Y_mesh = np.meshgrid(Xg, Yg)
        U = alpha * X_mesh - beta * X_mesh * Y_mesh
        V = delta * X_mesh * Y_mesh - gamma * Y_mesh
        N = np.hypot(U, V)
        N[N == 0] = 1.0
        Un = (U / N).tolist()
        Vn = (V / N).tolist()
        X_list = X_mesh.tolist()
        Y_list = Y_mesh.tolist()
    except Exception:
        X_list = []
        Y_list = []
        Un = []
        Vn = []

    # Peak detection for prey (x) to estimate periods
    periods = []
    mean_period = None
    try:
        # Prefer scipy.signal.find_peaks if available
        use_find_peaks = False
        try:
            from scipy.signal import find_peaks  # type: ignore
            use_find_peaks = True
        except Exception:
            use_find_peaks = False

        if use_find_peaks:
            peaks, _ = find_peaks(x, distance=int(params.get("peak_distance", 10)))
            if peaks.size > 1:
                periods = np.diff(sol_t[peaks]).tolist()
        else:
            # Simple local maxima detection
            min_dist = int(params.get("peak_distance", 10))
            idxs = []
            last_idx = -min_dist - 1
            for i in range(1, len(x) - 1):
                if x[i] > x[i - 1] and x[i] > x[i + 1]:
                    if i - last_idx >= min_dist:
                        idxs.append(i)
                        last_idx = i
            if len(idxs) > 1:
                periods = np.diff(sol_t[np.array(idxs, dtype=int)]).tolist()
        if len(periods) > 0:
            mean_period = float(np.mean(periods))
    except Exception:
        periods = []
        mean_period = None

    # Prepare final result with built-in types (lists, floats)
    try:
        result.update(
            {
                "t": sol_t.tolist(),
                "x": x.tolist(),
                "y": y.tolist(),
                "equilibria": {"E0": (float(E0[0]), float(E0[1])), "E1": (float(E1[0]) if E1[0] == E1[0] else None, float(E1[1]) if E1[1] == E1[1] else None)},
                "jacobians": {"J_e0": J_e0.tolist(), "J_e1": J_e1.tolist()},
                "eigenvalues": {"eig_e0": [complex(v).real if complex(v).imag == 0 else complex(v) for v in eig_e0],
                                "eig_e1": [complex(v).real if complex(v).imag == 0 else complex(v) for v in eig_e1]},
                "nullclines": {"y_nc": float(y_nc) if not math.isnan(y_nc) else None, "x_nc": float(x_nc) if not math.isnan(x_nc) else None},
                "vector_field": {"X": X_list, "Y": Y_list, "U_normalized": Un if 'Un' in locals() else Un, "V_normalized": Vn if 'Vn' in locals() else Vn},
                "periods": [float(p) for p in periods] if periods is not None else [],
                "mean_period": float(mean_period) if mean_period is not None else None,
            }
        )
    except Exception as e:
        # Fallback if conversion fails
        result["message"] = f"Post-processing error: {e}"
        result["success"] = False

    return result