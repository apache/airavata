import path from 'path';
import { app, ipcMain, dialog, session, Menu, globalShortcut } from 'electron';
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

ipcMain.on('show-window', (event, url, associatedId) => {
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
    // 'web-security': false,
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
});

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

ipcMain.on('start-notebook', (event, imageName) => {
  log.info("Starting the notebook with imageName: ", imageName);

  let createOptions = {
    'Tty': false,
    'ExposedPorts': {
      '8888/tcp': {}
    },
    'HostConfig': {
      'PortBindings': {
        '8888/tcp': [
          {
            'HostPort': '6080'
          }
        ]
      }
    }
  };

  console.log("Create options: ", createOptions);

  try {
    docker.run(imageName, [], null, createOptions, function (err, data, container) {
      container.remove();
    })
      .on('container', function (container) {
        event.sender.send('notebook-started', container.id);
      });

  } catch (e) {
    console.log(e);
  }
});

const getRunningContainers = (event) => {
  log.info("Getting running containers");
  let runningContainers = [];
  docker.listContainers(function (err, containers) {
    containers.forEach(function (containerInfo) {
      runningContainers.push({
        id: containerInfo.Id,
        name: containerInfo.Names[0]
      });
    });

    event.sender.send('got-running-containers', runningContainers);
  });
};

ipcMain.on("get-running-containers", getRunningContainers);


ipcMain.on('stop-notebook', (event, containerId) => {
  log.info("Stopping the notebook with containerId: ", containerId);

  let container = docker.getContainer(containerId);
  container.stop(function (err, data) {
    console.log("Container stopped: ", containerId);
    event.sender.send('notebook-stopped');
  });
});