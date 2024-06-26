import path from 'path';
import { app, ipcMain, dialog, session, autoUpdater } from 'electron';
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
    width: 1900,
    height: 1000,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      webSecurity: false,
    },
    'node-integration': true,

  });

  log.warn("App is now ready");

  session.defaultSession.clearStorageData([], (data) => {
    console.log("Cleared storage data", data);
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

  try {
    autoUpdater.setFeedURL({ url: updateUrl });
    setInterval(() => {
      autoUpdater.checkForUpdates();
    }, 5000); // check every minute
  } catch (e) {
    console.error("Error setting up auto updater", e);
    log.error("Error setting up auto updater", e);
  }

  autoUpdater.on('update-downloaded', (event, releaseNotes, releaseName) => {
    const dialogOpts = {
      type: 'info',
      buttons: ['Restart', 'Later'],
      title: 'Application Update',
      message: process.platform === 'win32' ? releaseNotes : releaseName,
      detail:
        'A new version has been downloaded. Restart the application to apply the updates.'
    };

    dialog.showMessageBox(dialogOpts).then((returnValue) => {
      if (returnValue.response === 0) autoUpdater.quitAndInstall();
    });
  });

  autoUpdater.on('error', (message) => {
    console.error('There was a problem updating the application');
    console.error(message);
  });




  if (isProd) {
    await mainWindow.loadURL('app://./home');
  } else {
    const port = process.argv[2];
    await mainWindow.loadURL(`http://localhost:${port}/home`);
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
  console.log("Getting version number");
  console.log(app.getVersion());

  event.sender.send('version-number', app.getVersion());
});


ipcMain.on('message', async (event, arg) => {
  event.reply('message', `${arg} World!`);
});

async function getToken(url) {
  console.log(url);
  const rawCode = /code=([^&]*)/.exec(url) || null;
  const code = (rawCode && rawCode.length > 1) ? rawCode[1] : null;

  if (code) {
    const resp = await fetch(`https://md.cybershuttle.org/auth/get-token-from-code/?code=${code}`);

    const data = await resp.json();

    const accessToken = data.access_token;
    const refreshToken = data.refresh_token;

    return [accessToken, refreshToken];

  } else {
    return [];
  }
}

ipcMain.on('ci-logon-logout', (event) => {
  log.warn('logging out');
  console.log("Logging out with CI logon");
  session.defaultSession.clearStorageData([], (data) => {
    console.log("Cleared storage data", data);
  });
});

ipcMain.on('ci-logon-login', async (event) => {

  console.log("Logging in with CI logon");
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
        const tokens = await getToken(url);

        if (tokens.length > 0) {
          const [accessToken, refreshToken] = tokens;
          console.log("Tokens", accessToken, refreshToken);
          event.sender.send('ci-logon-success', accessToken, refreshToken);
          authWindow.close();
        }
      }, 5000);

      authWindow.hide();
    }
  });
});

let associatedIDToWindow = {};
ipcMain.on('show-window', (event, url, associatedId) => {
  console.log("Showing the window with " + url);
  let window = createWindow(url, {
    width: 1200,
    height: 800,
    'node-integration': true,
    'web-security': false
  });

  associatedIDToWindow[associatedId] = window;
  window.loadURL(url);
  window.show();

  log.info("associatedIDToWindow", associatedIDToWindow);

  window.on('close', () => {
    delete associatedIDToWindow[associatedId];

    log.info("deleted tab:", associatedIDToWindow);

    event.sender.send('close-tab', associatedId);
  });
});

ipcMain.on('close-window', (event, associatedId) => {
  try {
    console.log("Closing window with associatedId", associatedId);
    associatedIDToWindow[associatedId].removeAllListeners('close');
    associatedIDToWindow[associatedId].close();
  } catch (e) {
    console.error("Error closing window", e);
  }

});