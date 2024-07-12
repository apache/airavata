import path from 'path';
import { app, ipcMain, dialog, session, Menu, globalShortcut } from 'electron';
const url = require('node:url');
import serve from 'electron-serve';
import { createWindow } from './helpers';
const { exec, spawn } = require('child_process');
const fs = require('fs');
import log from 'electron-log/main';
import { frame } from 'framer-motion';

const isProd = process.env.NODE_ENV === 'production';
const KILL_CMD = 'pkill -f websockify';
const server = 'https://airavata-28o5suo4t-ganning127s-projects.vercel.app';
const updateUrl = `${server}/update/${process.platform}/${app.getVersion()}`;



if (isProd) {
  serve({ directory: 'app' });
} else {
  app.setPath('userData', `${app.getPath('userData')} (development)`);
}

; (async () => {
  await app.whenReady();
  const mainWindow = createWindow('main', {
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
    await mainWindow.loadURL('app://./home');
    globalShortcut.register("CommandOrControl+R", () => {
      log.info("CommandOrControl+R is pressed: Shortcut Disabled");
    });
    globalShortcut.register("F5", () => {
      log.info("F5 is pressed: Shortcut Disabled");
    });

    mainWindow.removeMenu();
    Menu.setApplicationMenu(Menu.buildFromTemplate([]));

  } else {
    const port = process.argv[2];
    await mainWindow.loadURL(`http://localhost:${port}/docker-page`);
    mainWindow.webContents.openDevTools();
  }
})();

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

  log.info(`Cybershuttle MD Local Agent version: ${app.getVersion()}`);

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

  authWindow.loadURL('https://md.cybershuttle.org/auth/redirect_login/cilogon/');
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

const showWindowWhenReady = (event, id, port) => {
  let url = `http://localhost:${port}/lab`;

  let interval = setInterval(() => {
    fetch(url)
      .then((response) => {
        if (response.status === 200) {
          log.info("Got a 200 response from the notebook, showing the window");
          createExpWindow(event, url, id);
          clearInterval(interval);
        }
      })
      .catch((error) => {
        log.error("Error: ", error);
      });
  }, 5000);
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

ipcMain.on('start-notebook', (event, imageName, createOptions) => {
  log.info("Starting the notebook with imageName: ", imageName);
  console.log("Create options: ", createOptions);

  try {
    docker.run(imageName, [], null, createOptions, function (err, data, container) { })
      .on('container', function (container) {
        log.info("Container created: ", container.id);
        let err = "";

        try {
          showWindowWhenReady(event, container.id, createOptions.HostConfig.PortBindings['8888/tcp'][0].HostPort);
        } catch (e) {
          console.log(e);
          err = e;
        }

        event.sender.send('container-started', container.id, err);

      });
  } catch (e) {
    console.log(e);
  }
});

const getRunningContainers = (event) => {
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

    event.sender.send('got-running-containers', containers);
  });
};

ipcMain.on("get-running-containers", getRunningContainers);


ipcMain.on('stop-notebook', (event, containerId) => {
  log.info("Stopping the notebook with containerId: ", containerId);

  let container = docker.getContainer(containerId);
  container.stop(function (err, data) {
    console.log("Container stopped: ", containerId);
    removeExpWindow(event, containerId);
    event.sender.send('container-stopped', containerId);
  });
});

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

ipcMain.on("choose-filepath", async (event) => {
  const result = await dialog.showOpenDialog({
    properties: ['openDirectory']
  });

  if (!result.canceled) {
    event.sender.send('filepath-chosen', result.filePaths[0]);
  }
});