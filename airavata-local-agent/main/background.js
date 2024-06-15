import path from 'path';
import { app, ipcMain, protocol, session, net } from 'electron';
const url = require('node:url');
import serve from 'electron-serve';
import { createWindow } from './helpers';
const { exec, spawn } = require('child_process');
const fs = require('fs');

const isProd = process.env.NODE_ENV === 'production';
const KILL_CMD = 'kill -9 $(lsof -ti:6080)';


if (isProd) {
  serve({ directory: 'app' });
} else {
  app.setPath('userData', `${app.getPath('userData')} (development)`);
}

; (async () => {
  await app.whenReady();

  const filter = {
    urls: ['https://md.cybershuttle.org/*']
  };

  // session.defaultSession.webRequest.onBeforeSendHeaders(
  //   filter,
  //   (details, callback) => {
  //     console.log(details);
  //     details.requestHeaders['Origin'] = 'https://md.cybershuttle.org';
  //     callback({ requestHeaders: details.requestHeaders });
  //   }
  // );

  // session.defaultSession.webRequest.onHeadersReceived(
  //   filter,
  //   (details, callback) => {
  //     console.log(details);
  //     details.responseHeaders['Access-Control-Allow-Origin'] = [
  //       '*'
  //     ];
  //     callback({ responseHeaders: details.responseHeaders });
  //   }
  // );


  // Construct the BrowserWindow if haven't done so yet...

  const mainWindow = createWindow('main', {
    width: 1500,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      webSecurity: false

    },
  });

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    require('electron').shell.openExternal(url);
    return { action: 'deny' };
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


ipcMain.on('message', async (event, arg) => {
  event.reply('message', `${arg} World!`);
});

function runit(cmd, timeout) {
  // https://stackoverflow.com/questions/31727684/node-js-exec-doesnt-callback-if-exec-an-exe
  return new Promise(function (resolve, reject) {
    var ch = exec(cmd, function (error, stdout, stderr) {
      if (error) {
        reject(error);
      } else {
        resolve("program exited without an error");
      }
    });
    setTimeout(function () {
      resolve("program still running");
    }, timeout);
  });
}

async function getToken(url) {
  console.log("getting token from url: ", url);

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

ipcMain.on('ci-logon-login', async (event) => {
  var authWindow = createWindow('authWindow', {
    width: 800,
    height: 600,
    show: false,
    'node-integration': false,
    'web-security': false
  });

  authWindow.loadURL('https://md.cybershuttle.org/auth/redirect_login/cilogon/');


  authWindow.show();
  authWindow.webContents.on('will-redirect', async (e, url) => {
    if (url.startsWith("https://md.cybershuttle.org/auth/callback/")) {
      const tokens = await getToken(url);

      if (tokens.length > 0) {
        const [accessToken, refreshToken] = tokens;
        event.sender.send('ci-logon-success', accessToken, refreshToken);
      }
      authWindow.close();
      authWindow.loadURL('https://md.cybershuttle.org/auth/redirect_login/cilogon/');

    }

  });
});

ipcMain.on('start-proxy', startIt);

async function startIt(event) {
  let cmd = spawn('./proxy/novnc_proxy', { shell: true });

  cmd.stdout.on('data', (data) => {
    data = data.toString().trim();

    if (data == "HANG_NOW") {
      fs.readFile('./proxy/config.txt', 'utf8', (err, data) => {
        const lines = data.split('\n');
        const hostname = lines[0];
        const port = lines[1];
        event.sender.send('proxy-started', hostname, port);
      });
    }
  });

  cmd.stderr.on('data', (data) => {
    console.error(`stderr: ${data}`);
  });

  cmd.on('close', async (code) => {
    console.log(`child process exited with code ${code}`);
    await stopIt(event, true);
  });
}

async function stopIt(event, restart) {
  exec(KILL_CMD,
    (error, stdout, stderr) => {
      event.sender.send('proxy-stopped', restart);
    });
}

ipcMain.on('stop-proxy', stopIt);

