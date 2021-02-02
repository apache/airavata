var ws, rmsd_plot, on_open, on_message, on_close, on_error;

var createStreamPanel = function() {
  var $panel, $logs, $rmsds, createLogs, updateRMSD;

  $panel = $("#simstream-content-panel-body");

  $logs = $('<ul id="simstream-openmm-logs"></ul>');

  $rmsd = $('<div id="simstream-opemm-rmsd"><h4>RMSD to Native Structure</h4><canvas id="simstream-opemm-rmsd-plot"></canvas></div>');

  $panel.append($logs).append($rmsd);

  var canvas = document.getElementById('simstream-opemm-rmsd-plot'),
    ctx = canvas.getContext('2d'),
    startingData = {
      labels: [],//[0,0,0,0,0,0,0,0,0,0,0],
      datasets: [
          {
              fillColor: "rgba(151,187,205,0.2)",
              strokeColor: "rgba(151,187,205,1)",
              pointColor: "rgba(151,187,205,1)",
              pointStrokeColor: "#fff",
              data: []//[0,0,0,0,0,0,0,0,0,0]
          }
      ]
    },
    latestLabel = 0,
    options = {
      animation: {animationSteps: 15},
      scales: {
        xAxes: [{
          display: true,
          scaleLabel: {
            display: true,
            labelString: "Step (ps)",
            fontSize: 20
          }
        }],
        yAxes: [{
            display: true,
            scaleLabel: {
              display: true,
              labelString: "RMSD (AA)",
              fontSize: 20
            },
            ticks: {
                min: 0,
                beginAtZero: true,  // minimum value will be 0.
                max: 1.5
            }
        }]
    }
    };

  Chart.defaults.global.legend.display = false;
  //Chart.defaults.global.tooltips.enabled = false;
  rmsd_plot = Chart.Line(ctx, {data: startingData, options: options});

  createLogs = function(logs) {
    for (var log in logs) {
      if (logs.hasOwnProperty(log)) {
        $logs.append($('<li class="simstream-openmm-log">' + logs[log] + '</li>'));
      }
    }
  };

  updateRMSD = function(rmsd) {
    if (rmsd.hasOwnProperty("rmsd")) {
      if (rmsd.hasOwnProperty("step")) {
        startingData.labels.push(rmsd.step);
        if (startingData.labels.length > 10)
          startingData.labels.splice(0, 1);
        startingData.datasets[0].data.push(rmsd.rmsd * 10);
        if (startingData.datasets[0].data.length > 10)
          startingData.datasets[0].data.splice(0, 1);
        rmsd_plot.data = startingData;
        rmsd_plot.update();
      }
    }
  };

  on_open = function() {

  };

  on_message = function(e) {
    var msg;
    console.log(e.data);

    try {
      msg = JSON.parse(e.data);
      console.log(msg);

      for (var i = 0; i < msg.length; i++) {
        if (msg[i].hasOwnProperty("logs")) {
          createLogs(msg[i]);
        }
        if (msg[i].hasOwnProperty("rmsd")) {
          updateRMSD(msg[i]);
        }
      }
    } catch(err) {
      console.log(e.data);
    }
  };

  on_close = function() {
    console.log("Closed connection");
  };

  on_error = function() {

  };
};

var createLogin = function(auth, redirect) {
  var $content, $form, $username, $password, $hidden, $submit;
  $content = $("#simstream-content");
  $content.empty();

  $form = $('<form id="simstream-content-form" action="' + auth + '" method="post"></form>');
  $username = $('<input type="text" name="username" id="username" />');
  $password = $('<input type="password" name="password" id="password" />');
  $hidden = $('<input type="hidden" name="redirect" id="redirect" value="" />').val(window.location.href);
  $submit = $('<input type="submit" value="Submit" />');

  $form.append('<p>Enter your Gateway Credentials</p>');
  $form.append('<label for="simstream-username">Username</label>');
  $form.append($username);
  $form.append('<br />');
  $form.append('<label for="simstream-password">Password</label>');
  $form.append($password);
  $form.append('<br />');
  $form.append($hidden);
  $form.append($submit);

  $content.append($form);
};

var createError = function() {

};

var checkAuth = function(auth_url, ws_url, experiment_url) {
  // $.ajax({
  //   url: auth_url,
  //   method: "get",
  //   crossDomain: true,
  //   success: function(data, textStatus, xhr) {
  //
  //   },
  //   error: function(xhr) {
  //
  //   },
  //   complete: function(xhr, textStatus) {
  //     if (xhr.status === 200) {
  //       createStreamPanel();
  //       ws = new WebSocket(ws_url);
  //       ws.onopen = on_open;
  //       ws.onclose = on_close;
  //       ws.onmessage = on_message;
  //       ws.onerror = on_error;
  //     }
  //     else if (xhr.status === 403) {
  //       createLogin(auth_url, experiment_url);
  //     }
  //     else {
  //       createError();
  //     }
  //   }
  // });

  createStreamPanel();
  ws = new WebSocket(ws_url);
  ws.onopen = on_open;
  ws.onmessage = on_message;
  ws.onerror = on_error;
  ws.onclose = on_close;
};
