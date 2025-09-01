def simulate(**params):
    import math
    # imports with fallbacks
    try:
        import numpy as _np
    except Exception:
        _np = None
    try:
        from scipy.integrate import solve_ivp as _solve_ivp
    except Exception:
        _solve_ivp = None
    try:
        import matplotlib.pyplot as _plt
    except Exception:
        _plt = None

    # Helper converters (kept local)
    def _to_float(x, default=0.0):
        if x is None:
            return float(default)
        if isinstance(x, float):
            return x
        if isinstance(x, int):
            return float(x)
        if isinstance(x, (list, tuple)):
            # try convert first element
            try:
                return float(x[0])
            except Exception:
                return float(default)
        try:
            return float(x)
        except Exception:
            # try string cleanup
            try:
                return float(str(x).strip())
            except Exception:
                return float(default)

    def _to_int(x, default=0):
        if x is None:
            return int(default)
        if isinstance(x, int):
            return x
        if isinstance(x, float):
            return int(x)
        try:
            return int(float(x))
        except Exception:
            try:
                return int(str(x).strip())
            except Exception:
                return int(default)

    def _to_list_of_floats(x, length=None, default=None):
        # If x is a scalar, convert to list of length=length if provided
        if default is None:
            default = 0.0
        if x is None:
            if length is None:
                return []
            return [float(default)] * length
        if isinstance(x, (list, tuple)):
            out = []
            for elem in x:
                try:
                    out.append(float(elem))
                except Exception:
                    try:
                        out.append(float(str(elem)))
                    except Exception:
                        out.append(float(default))
            if length is not None and len(out) < length:
                out.extend([float(default)] * (length - len(out)))
            return out
        # scalar
        try:
            val = float(x)
            if length is None:
                return [val]
            return [val] * length
        except Exception:
            try:
                val = float(str(x))
                if length is None:
                    return [val]
                return [val] * length
            except Exception:
                if length is None:
                    return [float(default)]
                return [float(default)] * length

    def _ensure_iterable(x):
        if x is None:
            return []
        if isinstance(x, (list, tuple)):
            return list(x)
        return [x]

    # Defaults (these were the "globals" in the original script)
    defaults = {
        'eval_time': 100.0,
        't_iteration': 1000,
        't_span': None,  # will be built from eval_time if not provided
        'z0': [2.0, 0.0],
        'mu': 1.0,
        'mgrid_size': 8.0,
        'mesh_points': 15,
        'nullcline_step': 0.001,
        'plot': False,
        'plot_save_path': None
    }

    # Override defaults with params if present
    used = {}
    for k, v in defaults.items():
        used[k] = params.get(k, v)

    # Also accept direct keys that match names in original code
    # Convert and validate types
    eval_time = _to_float(used.get('eval_time', 100.0), default=100.0)
    t_iteration = _to_int(used.get('t_iteration', 1000), default=1000)
    if t_iteration < 2:
        t_iteration = 1000
    mu = _to_float(used.get('mu', 1.0), default=1.0)
    mgrid_size = _to_float(used.get('mgrid_size', 8.0), default=8.0)
    mesh_points = _to_int(used.get('mesh_points', 15), default=15)
    nullcline_step = _to_float(used.get('nullcline_step', 0.001), default=0.001)
    plot_flag = bool(used.get('plot', False))
    plot_save_path = used.get('plot_save_path', None)

    # initial conditions
    z0_input = params.get('z0', used.get('z0', [2.0, 0.0]))
    z0_list = _to_list_of_floats(z0_input, length=2, default=0.0)
    z0 = [z0_list[0], z0_list[1]]

    # t_span handling: allow t_span to be provided as [t0, t1] or single end time
    t_span_param = params.get('t_span', None)
    if t_span_param is None:
        t_span = [0.0, eval_time]
    else:
        if isinstance(t_span_param, (list, tuple)) and len(t_span_param) >= 2:
            t0 = _to_float(t_span_param[0], default=0.0)
            t1 = _to_float(t_span_param[1], default=eval_time)
            t_span = [t0, t1]
        else:
            # single numeric -> interpreted as end time
            t1 = _to_float(t_span_param, default=eval_time)
            t_span = [0.0, t1]

    # t_eval handling: allow user-specified array or generate
    t_eval_param = params.get('t_eval', None)
    if t_eval_param is None:
        try:
            if _np is not None:
                t_eval = _np.linspace(t_span[0], t_span[1], t_iteration)
            else:
                # fallback pure python linspace
                t_eval = [t_span[0] + (t_span[1] - t_span[0]) * i / (t_iteration - 1) for i in range(t_iteration)]
        except Exception:
            t_eval = [t_span[0], t_span[1]]
    else:
        # try to coerce to list/np array of floats
        if isinstance(t_eval_param, (list, tuple)):
            t_eval = [_to_float(x, default=0.0) for x in t_eval_param]
        else:
            # if numpy array-like
            try:
                if _np is not None and hasattr(t_eval_param, 'tolist'):
                    t_eval = list(map(float, t_eval_param.tolist()))
                else:
                    # single number
                    t_eval = [_to_float(t_eval_param, default=0.0)]
            except Exception:
                t_eval = [_to_float(t_eval_param, default=0.0)]

    # ensure t_eval sorted and within t_span
    try:
        if _np is not None:
            t_eval = _np.array(t_eval, dtype=float)
            t_eval = t_eval[(t_eval >= min(t_span)) & (t_eval <= max(t_span))]
            if t_eval.size == 0:
                t_eval = _np.linspace(t_span[0], t_span[1], t_iteration)
        else:
            # pure python
            t_eval = [float(x) for x in t_eval if float(x) >= min(t_span) and float(x) <= max(t_span)]
            if len(t_eval) == 0:
                t_eval = [t_span[0] + (t_span[1] - t_span[0]) * i / (t_iteration - 1) for i in range(t_iteration)]
    except Exception:
        # fallback
        if _np is not None:
            t_eval = _np.linspace(t_span[0], t_span[1], t_iteration)
        else:
            t_eval = [t_span[0] + (t_span[1] - t_span[0]) * i / (t_iteration - 1) for i in range(t_iteration)]

    # Define model functions inline
    def _van_der_pol(t, z, mu_val):
        # z expected sequence length 2
        try:
            x = float(z[0])
            y = float(z[1])
        except Exception:
            # if scalars or wrong shape, coerce
            x = _to_float(z, default=0.0) if not hasattr(z, '__len__') else _to_float(z[0], default=0.0)
            y = _to_float(z[1] if hasattr(z, '__len__') and len(z) > 1 else 0.0, default=0.0)
        dxdt = y
        dydt = mu_val * (1.0 - x * x) * y - x
        return [dxdt, dydt]

    def _y_nullcline(x_val, mu_val):
        # safe division
        try:
            denom = mu_val * (1.0 - x_val * x_val)
            if denom == 0:
                return None
            return x_val / denom
        except Exception:
            return None

    def _x_nullcline(y_val, mu_val):
        # always zero line in original code
        try:
            # preserve shape: if y_val is iterable, return zeros of same length
            if hasattr(y_val, '__len__'):
                return [0.0 for _ in range(len(y_val))]
            return 0.0
        except Exception:
            return 0.0

    # Solve ODE: try scipy first, fallback to RK4 fixed-step using t_eval points
    solver_used = None
    sol_t = None
    sol_y = None
    try:
        if _solve_ivp is not None:
            solver_used = 'scipy.solve_ivp'
            # scipy expects numpy arrays
            if _np is not None:
                z0_np = _np.array(z0, dtype=float)
                # wrap function to signature (t, y)
                def _f(t, y, mu_val=mu):
                    return _van_der_pol(t, y, mu_val)
                # solve
                sol = _solve_ivp(lambda t, y: _f(t, y, mu), t_span, z0_np, t_eval=(t_eval if not hasattr(t_eval, 'tolist') else t_eval), args=(), dense_output=False)
                if hasattr(sol, 't') and hasattr(sol, 'y'):
                    sol_t = sol.t
                    sol_y = sol.y
                else:
                    # unexpected result, fallback
                    raise RuntimeError("Unexpected solve_ivp return structure")
            else:
                # no numpy but scipy exists (unlikely), try direct
                sol = _solve_ivp(lambda t, y: _van_der_pol(t, y, mu), t_span, z0, t_eval=t_eval)
                sol_t = list(sol.t)
                sol_y = list(sol.y)
    except Exception:
        solver_used = None
        sol_t = None
        sol_y = None

    if sol_y is None:
        # fallback RK4 fixed-step between provided t_eval points
        solver_used = 'rk4_fixed'
        # ensure t_eval as sorted list
        if _np is not None:
            t_list = list(map(float, sorted(list(t_eval))))
        else:
            t_list = sorted([float(x) for x in t_eval])
        n_points = len(t_list)
        y0 = [float(z0[0]), float(z0[1])]
        ys = [y0]
        ts = [t_list[0]]
        current_y = y0[:]
        current_t = t_list[0]
        # integrate stepwise to each next t in t_list using RK4 with substeps if needed
        for j in range(1, n_points):
            t_next = t_list[j]
            dt_total = t_next - current_t
            # choose internal steps to improve accuracy: at most 100 substeps
            n_sub = max(1, min(100, int(math.ceil(abs(dt_total) / ((t_span[1] - t_span[0]) / max(1, t_iteration))))))
            h = dt_total / n_sub
            y_temp = current_y[:]
            t_temp = current_t
            for _ in range(n_sub):
                k1 = _van_der_pol(t_temp, y_temp, mu)
                yk1 = [h * k1[0], h * k1[1]]
                y_mid1 = [y_temp[0] + 0.5 * yk1[0], y_temp[1] + 0.5 * yk1[1]]

                k2 = _van_der_pol(t_temp + 0.5 * h, y_mid1, mu)
                yk2 = [h * k2[0], h * k2[1]]
                y_mid2 = [y_temp[0] + 0.5 * yk2[0], y_temp[1] + 0.5 * yk2[1]]

                k3 = _van_der_pol(t_temp + 0.5 * h, y_mid2, mu)
                yk3 = [h * k3[0], h * k3[1]]
                y_end = [y_temp[0] + yk3[0], y_temp[1] + yk3[1]]

                k4 = _van_der_pol(t_temp + h, y_end, mu)
                yk4 = [h * k4[0], h * k4[1]]

                # RK4 update
                y_temp = [
                    y_temp[0] + (yk1[0] + 2.0 * yk2[0] + 2.0 * yk3[0] + yk4[0]) / 6.0,
                    y_temp[1] + (yk1[1] + 2.0 * yk2[1] + 2.0 * yk3[1] + yk4[1]) / 6.0
                ]
                t_temp += h
            current_y = y_temp
            current_t = t_next
            ys.append([current_y[0], current_y[1]])
            ts.append(current_t)
        # convert to array-like structures
        sol_t = ts
        # transpose ys to shape (2, n)
        x_vals = [row[0] for row in ys]
        y_vals = [row[1] for row in ys]
        if _np is not None:
            import numpy as _np_local
            sol_y = _np_local.vstack([_np_local.array(x_vals, dtype=float), _np_local.array(y_vals, dtype=float)])
        else:
            sol_y = [x_vals, y_vals]

    # Ensure sol_t and sol_y are standard python lists for output
    try:
        if _np is not None:
            sol_t_list = list(map(float, sol_t.tolist() if hasattr(sol_t, 'tolist') else sol_t))
            if hasattr(sol_y, 'tolist'):
                sy = sol_y.tolist()
                # If shape (2, N) -> sy is list of lists
                if isinstance(sy, list) and len(sy) == 2:
                    sol_y_list = [list(map(float, sy[0])), list(map(float, sy[1]))]
                else:
                    # fallback flatten
                    sol_y_list = [list(map(float, row)) for row in sy]
            else:
                # assume list of lists
                sol_y_list = [list(map(float, sol_y[0])), list(map(float, sol_y[1]))]
        else:
            sol_t_list = [float(x) for x in sol_t]
            if isinstance(sol_y, list) and len(sol_y) == 2 and all(hasattr(sol_y[i], '__len__') for i in (0, 1)):
                sol_y_list = [list(map(float, sol_y[0])), list(map(float, sol_y[1]))]
            else:
                # if shape is [[x,y],...], convert
                sol_y_list = [[float(r[0]) for r in sol_y], [float(r[1]) for r in sol_y]]
    except Exception:
        # absolute fallback
        sol_t_list = [float(t) for t in (list(sol_t) if hasattr(sol_t, '__iter__') else [sol_t])]
        try:
            sol_y_list = [[float(v) for v in sol_y[0]], [float(v) for v in sol_y[1]]]
        except Exception:
            sol_y_list = [[0.0], [0.0]]

    # Compute vector field on meshgrid
    vector_field = {'x': None, 'y': None, 'u': None, 'v': None}
    try:
        if _np is not None:
            xs = _np.linspace(-mgrid_size, mgrid_size, mesh_points)
            ys = _np.linspace(-mgrid_size, mgrid_size, mesh_points)
            X, Y = _np.meshgrid(xs, ys)
            U = Y
            V = mu * (1.0 - X * X) * Y - X
            vector_field['x'] = X.tolist()
            vector_field['y'] = Y.tolist()
            vector_field['u'] = U.tolist()
            vector_field['v'] = V.tolist()
        else:
            # pure python meshgrid
            xs = [_to_float(-mgrid_size + 2.0 * mgrid_size * i / (mesh_points - 1)) for i in range(mesh_points)]
            ys = [_to_float(-mgrid_size + 2.0 * mgrid_size * j / (mesh_points - 1)) for j in range(mesh_points)]
            X = []
            Y = []
            U = []
            V = []
            for y_val in ys:
                row_x = []
                row_y = []
                row_u = []
                row_v = []
                for x_val in xs:
                    row_x.append(x_val)
                    row_y.append(y_val)
                    row_u.append(y_val)
                    row_v.append(mu * (1.0 - x_val * x_val) * y_val - x_val)
                X.append(row_x)
                Y.append(row_y)
                U.append(row_u)
                V.append(row_v)
            vector_field['x'] = X
            vector_field['y'] = Y
            vector_field['u'] = U
            vector_field['v'] = V
    except Exception:
        vector_field = {'x': [], 'y': [], 'u': [], 'v': []}

    # Nullclines: x_null range and y_nullcline, x_nullcline
    nullcline = {'x_null': None, 'y_null': None, 'x_nullcline': None}
    try:
        # Build x_null array from -mgrid_size to mgrid_size step nullcline_step
        if nullcline_step <= 0 or nullcline_step is None:
            nullcline_step = 0.001
        if _np is not None:
            try:
                x_null_arr = _np.arange(-mgrid_size, mgrid_size, nullcline_step)
                # limit size to avoid enormous arrays (safeguard)
                max_len = 20000
                if x_null_arr.size > max_len:
                    # reduce resolution
                    x_null_arr = _np.linspace(-mgrid_size, mgrid_size, max_len)
                y_null_list = []
                x_nullcline_list = []
                for xv in x_null_arr:
                    yv = _y_nullcline(float(xv), mu)
                    # convert None to None, else float
                    y_null_list.append(None if yv is None else float(yv))
                    # x nullcline is 0 for all
                    x_nullcline_list.append(0.0)
                nullcline['x_null'] = x_null_arr.tolist()
                nullcline['y_null'] = y_null_list
                nullcline['x_nullcline'] = x_nullcline_list
            except Exception:
                # fallback pure python
                raise
        else:
            # pure python
            x_list = []
            cur = -mgrid_size
            max_len = 20000
            count = 0
            while cur < mgrid_size and count < max_len:
                x_list.append(float(cur))
                cur += nullcline_step
                count += 1
            y_list = [_y_nullcline(xx, mu) for xx in x_list]
            x_nullcline_list = _x_nullcline(y_list, mu)
            nullcline['x_null'] = x_list
            nullcline['y_null'] = [None if vv is None else float(vv) for vv in y_list]
            nullcline['x_nullcline'] = x_nullcline_list
    except Exception:
        nullcline = {'x_null': [], 'y_null': [], 'x_nullcline': []}

    # Optional plotting (does not display unless plot flag set and backend supports it)
    plot_output_path = None
    if plot_flag and _plt is not None:
        try:
            fig = _plt.figure(figsize=(6, 6))
            _plt.clf()
            # attempt to plot streamplot if vector_field available
            try:
                X = vector_field.get('x', [])
                Y = vector_field.get('y', [])
                U = vector_field.get('u', [])
                V = vector_field.get('v', [])
                # convert to numpy for plotting if needed
                if _np is not None and isinstance(X, list):
                    X = _np.array(X)
                    Y = _np.array(Y)
                    U = _np.array(U)
                    V = _np.array(V)
                speed = None
                try:
                    speed = _np.sqrt(_np.array(U) ** 2 + _np.array(V) ** 2) if _np is not None else None
                except Exception:
                    speed = None
                if hasattr(_plt, 'streamplot') and X is not None and Y is not None:
                    if speed is not None:
                        _plt.streamplot(X, Y, U, V, color=speed, cmap='cool', density=2.0)
                    else:
                        try:
                            _plt.streamplot(X, Y, U, V, density=2.0)
                        except Exception:
                            pass
            except Exception:
                pass
            # plot nullclines
            try:
                x_null_plot = nullcline.get('x_null', [])
                y_null_plot = nullcline.get('y_null', [])
                x_nullcline_plot = nullcline.get('x_nullcline', [])
                # filter None values for plotting
                if x_null_plot and y_null_plot:
                    x_plot = [float(xi) for xi, yi in zip(x_null_plot, y_null_plot) if yi is not None]
                    y_plot = [float(yi) for yi in y_null_plot if yi is not None]
                    if x_plot and y_plot:
                        _plt.plot(x_plot, y_plot, '.', c="darkturquoise", markersize=2)
                if x_null_plot and x_nullcline_plot:
                    _plt.plot(x_null_plot, x_nullcline_plot, '.', c="darkturquoise", markersize=2)
            except Exception:
                pass
            # plot trajectory
            try:
                _plt.plot(sol_y_list[0], sol_y_list[1], 'r-', lw=3, label=f'Trajectory for mu={mu} and z0={z0}')
                # start and end markers
                if len(sol_y_list[0]) >= 1:
                    _plt.plot(sol_y_list[0][0], sol_y_list[1][0], 'bo', label='start point', alpha=0.75, markersize=7)
                    _plt.plot(sol_y_list[0][-1], sol_y_list[1][-1], 'o', c="yellow", label='end point', alpha=0.75, markersize=7)
            except Exception:
                pass
            _plt.title('phase plane plot: Van der Pol oscillator')
            _plt.xlabel('x')
            _plt.ylabel('y')
            try:
                _plt.legend(loc='lower right')
            except Exception:
                pass
            ax = _plt.gca()
            try:
                ax.spines['top'].set_visible(False)
                ax.spines['right'].set_visible(False)
                ax.spines['bottom'].set_visible(False)
                ax.spines['left'].set_visible(False)
            except Exception:
                pass
            try:
                ax.set_ylim(-mgrid_size, mgrid_size)
            except Exception:
                pass
            _plt.tight_layout()
            # save or close
            if plot_save_path:
                try:
                    _plt.savefig(plot_save_path)
                    plot_output_path = plot_save_path
                except Exception:
                    plot_output_path = None
            # close figure to not block
            try:
                _plt.close(fig)
            except Exception:
                pass
        except Exception:
            plot_output_path = None

    # Build params_used to return
    params_used = {
        'eval_time': eval_time,
        't_iteration': t_iteration,
        't_span': t_span,
        't_eval_length': len(sol_t_list) if hasattr(sol_t_list, '__len__') else 1,
        'z0': [float(z0[0]), float(z0[1])],
        'mu': float(mu),
        'mgrid_size': float(mgrid_size),
        'mesh_points': int(mesh_points),
        'nullcline_step': float(nullcline_step),
        'plot': bool(plot_flag),
        'plot_save_path': plot_save_path
    }

    result = {
        't': list(map(float, sol_t_list)),
        'sol_y': [list(map(float, sol_y_list[0])), list(map(float, sol_y_list[1]))],
        'params_used': params_used,
        'vector_field': vector_field,
        'nullcline': nullcline,
        'solver': solver_used,
        'status': 'success',
        'plot_path': plot_output_path
    }

    return result