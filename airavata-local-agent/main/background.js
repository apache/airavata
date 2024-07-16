import path from 'path';
import { app, ipcMain, dialog, session, Menu, shell } from 'electron';
const url = require('node:url');
import serve from 'electron-serve';
import { createWindow } from './helpers';
const { exec, spawn } = require('child_process');
const fs = require('fs');
import log from 'electron-log/main';

const isProd = process.env.NODE_ENV === 'production';
const KILL_CMD = 'pkill -f websockify';
const server = 'https://airavata-28o5suo4t-ganning127s-projects.vercel.app';
const updateUrl = `${server}/update/${process.platform}/${app.getVersion()}`;

if (isProd) {
  serve({ directory: 'app' });
} else {
  app.setPath('userData', `${app.getPath('userData')} (development)`);
}
let mainWindow;

if (process.defaultApp) {
  if (process.argv.length >= 2) {
    app.setAsDefaultProtocolClient('csagent', process.execPath, [path.resolve(process.argv[1])]);
  }
} else {
  app.setAsDefaultProtocolClient('csagent');
}

const gotTheLock = app.requestSingleInstanceLock();

if (!gotTheLock) {
  app.quit();
} else {
  app.on('second-instance', (event, commandLine, workingDirectory) => {
    // Someone tried to run a second instance, we should focus our window.
    if (mainWindow) {
      if (mainWindow.isMinimized()) mainWindow.restore();
      mainWindow.focus();
    }

    dialog.showErrorBox('Welcome Back', `You arrived from: ${commandLine.pop().slice(0, -1)}`);
  });

  // Create mainWindow, load the rest of the app, etc...
  app.whenReady().then(async () => {
    mainWindow = createWindow('main', {
      width: 1700,
      height: 1000,
      autoHideMenuBar: true,
      webPreferences: {
        preload: path.join(__dirname, 'preload.js'),
        webSecurity: false,
      },
      'node-integration': true,

    });

    log.warn("App is now ready");

    app.commandLine.appendSwitch('ignore-certificate-errors');

    session.defaultSession.clearStorageData([], (data) => {
      log.info("Cleared storage data", data);
    });



    mainWindow.webContents.setWindowOpenHandler(({ url }) => {
      require('electron').shell.openExternal(url);
      return { action: 'deny' };
    });

    mainWindow.on('close', function (e) {
      var choice = dialog.showMessageBoxSync(this,
        {
          type: 'question',
          buttons: ['Yes', 'No'],
          title: 'Confirm',
          message: 'Are you sure you want to close the local agent?'
        });
      if (choice == 1) {
        e.preventDefault();
      }
    });

    if (isProd) {
      await mainWindow.loadURL('app://./docker-page');
      // globalShortcut.register("CommandOrControl+R", () => {
      //   log.info("CommandOrControl+R is pressed: Shortcut Disabled");
      // });
      // globalShortcut.register("F5", () => {
      //   log.info("F5 is pressed: Shortcut Disabled");
      // });

      // mainWindow.removeMenu();
      // Menu.setApplicationMenu(Menu.buildFromTemplate([]));

    } else {
      const port = process.argv[2];
      await mainWindow.loadURL(`http://localhost:${port}/home`);
      mainWindow.webContents.openDevTools();
    }

  });

  app.on('open-url', async (event, url) => {
    // this is the handler we need to target when they came back
    const rawCode = /code=([^&]*)/.exec(url) || null;
    const code = (rawCode && rawCode.length > 1) ? rawCode[1] : null;

    if (isProd) {
      await mainWindow.loadURL(`app://./login-callback?code=${code}`);
    } else {
      const port = process.argv[2];
      await mainWindow.loadURL(`http://localhost:${port}/login-callback?code=${code}`);
    }
  });
}

app.on('window-all-closed', () => {
  app.quit();
});

app.on("before-quit", (event) => {
  // stop the proxy so it's not constantly running in the bkg
  exec(KILL_CMD,
    (error, stdout, stderr) => {
      event.sender.send('proxy-stopped', restart);
    });
  process.exit(); // really let the app exit now
});

ipcMain.on('get-version-number', (event) => {

  log.info(`Cybershuttle Local Agent version: ${app.getVersion()}`);

  event.sender.send('version-number', app.getVersion());
});


ipcMain.on('message', async (event, arg) => {
  event.reply('message', `${arg} World!`);
});

async function getToken(url) {
  const rawCode = /code=([^&]*)/.exec(url) || null;
  const code = (rawCode && rawCode.length > 1) ? rawCode[1] : null;

  if (code) {
    const resp = await fetch(`https://md.cybershuttle.org/auth/get-token-from-code/?code=${code}`);

    const data = await resp.json();
    return data;

  } else {
    return null;
  }
}

ipcMain.on('ci-logon-logout', (event) => {
  log.warn('logging out');
  // session.defaultSession.clearStorageData([], (data) => {
  //   log.info("Cleared storage data", data);
  // });
});

ipcMain.on('ci-logon-login', async (event) => {
  log.warn("Logging in with CI logon");
  var authWindow = createWindow('authWindow', {
    width: 1200,
    height: 800,
    show: false,
    'node-integration': false,
    'web-security': false
  });

  // authWindow.loadURL('https://md.cybershuttle.org/auth/redirect_login/cilogon/');
  authWindow.loadURL("https://iam.scigap.org/auth/realms/molecular-dynamics/protocol/openid-connect/auth?response_type=code&client_id=pga&redirect_uri=https%3A%2F%2Fmd.cybershuttle.org%2Fauth%2Fcallback%2F%3Fidp_alias%3Dcilogon&scope=openid&state=asdfasdfasdf&kc_idp_hint=cilogon");
  authWindow.show();

  authWindow.webContents.on('will-redirect', async (e, url) => {
    if (url.startsWith("https://md.cybershuttle.org/auth/callback/")) {
      // hitUrl = true
      setTimeout(async () => {
        const data = await getToken(url);
        log.info("Got the token: ", data);
        event.sender.send('ci-logon-success', data);
        authWindow.close();
      }, 2000);

      authWindow.hide();
    }
  });
});

let associatedIDToWindow = {};
let counter = 0;

function printKeys(obj) {
  // only show the keys
  log.info(Object.keys(obj));
}

const removeExpWindow = (event, associatedId) => {
  log.info("Removing the window with id: ", associatedId);
  try {
    associatedIDToWindow[associatedId].removeAllListeners('close');
    associatedIDToWindow[associatedId].close();
    delete associatedIDToWindow[associatedId];
  } catch (e) {
    log.error("Window doesn't exist with id: ", associatedId);
  }
};

const createExpWindow = (event, url, associatedId) => {
  log.info("Showing the window with url: ", url, " and associatedId: ", associatedId);

  if (associatedIDToWindow[associatedId]) {
    log.info("Window already exists with id: ", associatedId, " not creating a new one.");
    return;
  }

  counter++;
  let window = createWindow(`jnWindow-${counter}`, {
    width: 1200,
    height: 800,
    show: false,
    webPreferences: {
      allowDisplayingInsecureContent: true,
      allowRunningInsecureContent: true
    }
  });

  log.info("Window created with id: ", associatedId, " and counter: ", counter);

  associatedIDToWindow[associatedId] = window;
  window.loadURL(url);
  window.show();

  printKeys(associatedIDToWindow);

  window.on('close', () => {
    log.info("Window has been closed: ", associatedId);


    associatedIDToWindow[associatedId].removeAllListeners('close');
    delete associatedIDToWindow[associatedId];

    printKeys(associatedIDToWindow);
    event.sender.send('window-has-been-closed', associatedId);
  });
};

ipcMain.on('show-window', createExpWindow);

ipcMain.on('close-window', (event, associatedId) => {
  try {
    log.info("Closing the window with id: ", associatedId);
    associatedIDToWindow[associatedId].removeAllListeners('close');
    associatedIDToWindow[associatedId].close();
    delete associatedIDToWindow[associatedId];
  } catch (e) {
    log.error("Window doesn't exist with id: ", associatedId);
  }
});

// ----------------- DOCKER -----------------
var Docker = require('dockerode');
var docker = new Docker(); //defaults to above if env variables are not used

let portsCache = {};

const showWindowWhenReady = (event, id, port) => {
  let url = `http://localhost:${port}/lab`;

  let interval = setInterval(async () => {

    // check if the container is still running
    let container = await docker.getContainer(id);
    let inspected = await container.inspect();
    if (inspected.State.Status !== "running") {
      log.info("Container is not running, removing the window with id: ", id, " and clearing the interval");
      removeExpWindow(event, id);
      clearInterval(interval);
      return;
    } else {
      fetch(url)
        .then((response) => {
          if (response.status === 200) {
            log.info("Got a 200 response from the popup, showing the window");
            createExpWindow(event, url, id);
            clearInterval(interval);
          }
        })
        .catch((error) => {
          log.error("Error: ", error);
        });
    }


  }, 5000);
};

const getContainers = (event) => {
  log.info("Getting running containers");
  docker.listContainers({
    all: true
  }, function (err, containers) {
    // make sure everything in associatedIDToWindow is a container that is running. if not, remove it

    for (let key in associatedIDToWindow) {
      for (let i = 0; i < containers.length; i++) {
        if (containers[i].Id === key) {
          if (containers[i].State !== "running") {
            log.info("Container is not running, removing the window with id: ", key);
            removeExpWindow(event, key);
            break;
          }
        }
      }
    }

    event.sender.send('got-containers', containers);
  });
};

const pullDockerImage = (event, imageName, callback) => {
  log.info("Pulling docker image: ", imageName);

  const onProgress = function (obj) {
    event.sender.send('docker-pull-progress', obj);
  };

  const onFinished = function (err, output) {
    log.info("Finished: ", output);
    event.sender.send('docker-pull-finished', output);

    if (callback) {
      callback();
    }
  };

  docker.pull(imageName, function (err, stream) {
    docker.modem.followProgress(stream, onFinished, onProgress);
  });
};

const doesImageExist = async (imageName) => {
  const image = docker.getImage(imageName);
  console.log(image);

  try {
    const data = await image.inspect(); // will throw an error if the image doesn't exist
    return true;
  } catch (e) {
    console.log(e);
    return false;
  }
};

ipcMain.on('start-container', (event, containerId) => {
  log.info("Starting the container with containerId: ", containerId);

  let container = docker.getContainer(containerId);


  container.start(async function (err, data) {
    log.info("Starting container: ", containerId);

    if (err) {
      event.sender.send('container-started', containerId, err.message);
    } else {
      let error = "";

      try {
        let cont = await container.inspect();
        let port = cont.NetworkSettings.Ports['8888/tcp'][0].HostPort;
        showWindowWhenReady(event, containerId, port);


      } catch (e) {
        log.error("Error: ", e);
        err = e.message;
      }

      event.sender.send('container-started', containerId, error);
    }
  });


});


ipcMain.on('stop-container', (event, containerId) => {
  log.info("Stopping the container with containerId: ", containerId);

  let container = docker.getContainer(containerId);
  container.stop(function (err, data) {
    console.log("Container stopped: ", containerId);
    event.sender.send('container-stopped', containerId);
  });
});


ipcMain.on('start-notebook', async (event, createOptions) => {
  const imageName = "jupyter/datascience-notebook";
  log.info("Starting the notebook with imageName: ", imageName);
  console.log("Create options: ", createOptions);

  // idk if we need to add "--LabApp.default_url=\"/lab/work\"" in the list of commands (rn we open up the jupyter and they need to manually open work)
  const startNotebook = () => {
    docker.run(imageName, ["jupyter", "lab", "--NotebookApp.token=''"], null, createOptions, function (err, data, container) { })
      .on('container', function (container) {
        log.info("Container created: ", container.id);
        let err = "";

        try {
          showWindowWhenReady(event, container.id, createOptions.HostConfig.PortBindings['8888/tcp'][0].HostPort);
        } catch (e) {
          console.log(e);
          err = e;
        }

        event.sender.send('notebook-started', container.id, err);

      });

  };

  try {
    const existImage = await doesImageExist(imageName);
    if (existImage) {
      startNotebook();
    } else {
      pullDockerImage(event, imageName, startNotebook);
    }
  } catch (e) {
    console.log(e);
  }
});


/*
  1. share binaries w/Eroma to test
  - if docker is not running, show that message on the home page

  - do authentication before showing docker page
    - make this auth in default browser, need create cs:// url for login? with token, parse token
    - https://www.electronjs.org/docs/latest/tutorial/launch-app-from-url-in-another-app
*/

ipcMain.on("get-containers", getContainers);

ipcMain.on('inspect-container', (event, containerId) => {
  log.info("Inspecting the container with containerId: ", containerId);

  let container = docker.getContainer(containerId);
  container.inspect(function (err, data) {
    event.sender.send('container-inspected', data);
  });
});

ipcMain.on('pause-container', (event, containerId) => {
  log.info("Pausing the container with containerId: ", containerId);

  let container = docker.getContainer(containerId);
  container.pause(function (err, data) {
    console.log("Container paused: ", containerId);
    event.sender.send('container-paused', containerId);
  });
});

ipcMain.on('unpause-container', (event, containerId) => {
  log.info("Unpausing the container with containerId: ", containerId);

  let container = docker.getContainer(containerId);
  container.unpause(function (err, data) {
    console.log("Container unpaused: ", containerId);
    event.sender.send('container-unpaused', containerId);
  });
});

ipcMain.on('remove-container', (event, containerId) => {
  log.info("Removing the container with containerId: ", containerId);

  let container = docker.getContainer(containerId);
  container.remove(function (err, data) {
    console.log("Container removed: ", containerId);
    event.sender.send('container-removed', containerId);
  });
});

ipcMain.on('rename-container', (event, containerId, newName) => {
  log.info("Renaming the container with containerId: ", containerId, " to ", newName);

  let container = docker.getContainer(containerId);
  container.rename({ name: newName }, function (err, data) {
    console.log("Container renamed: ", containerId);
    event.sender.send('container-renamed', containerId, newName);
  });
});

ipcMain.on("choose-filepath", async (event) => {
  const result = await dialog.showOpenDialog({
    properties: ['openDirectory']
  });

  if (!result.canceled) {
    event.sender.send('filepath-chosen', result.filePaths[0]);
  }
});

ipcMain.on('get-container-ports', async (event, containers) => {
  log.info("Getting container ports");

  let ports = {}; // array of objects with containerId and port

  for (let i = 0; i < containers.length; i++) {
    if (portsCache[containers[i].Id]) {
      ports[containers[i].Id] = portsCache[containers[i].Id];
    } else {
      log.warn("Not in cache: ", containers[i].Id, " getting from docker");
      const container = docker.getContainer(containers[i].Id);
      const data = await container.inspect();

      let containerPorts = Object.keys(data.HostConfig.PortBindings);
      let tempMappings = [];
      for (let j = 0; j < containerPorts.length; j++) {
        let tempMapping = {};
        let hostPort = data.HostConfig.PortBindings[containerPorts[j]][0].HostPort;
        tempMapping.containerPort = containerPorts[j];
        tempMapping.hostPort = hostPort;

        tempMappings.push(tempMapping);
      }

      ports[containers[i].Id] = tempMappings;
      portsCache[containers[i].Id] = tempMappings;
    }
  }
  event.sender.send('got-container-ports', ports);

  /*
    Ports looks like:
    {
      "containerId": [
        {
          "containerPort": "8888/tcp",
          "hostPort": "6080"
        }
      ]
    }
  */
});

ipcMain.on('docker-ping', (event) => {
  log.info("Pinging docker");

  docker.ping(function (err, data) {
    log.info("Docker pinged: ", data);
    event.sender.send('docker-pinged', data);
  });
});


// ----------------- IMAGES -----------------
ipcMain.on('get-all-images', (event) => {
  log.info("Getting all images");

  docker.listImages(function (err, images) {
    event.sender.send('got-all-images', images);
  });
});

