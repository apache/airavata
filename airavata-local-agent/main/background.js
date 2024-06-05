import path from 'path';
import { app, ipcMain, protocol, session, net } from 'electron';
const url = require('node:url');
import serve from 'electron-serve';
import { createWindow } from './helpers';
const { exec, spawn } = require('child_process');
const fs = require('fs');
// import electronisdev


const ProtocolRegistry = require("protocol-registry");

const isProd = process.env.NODE_ENV === 'production';
const KILL_CMD = 'kill -9 $(lsof -ti:6080)';


if (isProd) {
  serve({ directory: 'app' });
} else {
  app.setPath('userData', `${app.getPath('userData')} (development)`);
}

; (async () => {
  await app.whenReady();

  const mainWindow = createWindow('main', {
    width: 1500,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
    },
  });

  const AIRAVATA_PROTOCOL = 'airavata';
  if (!isProd) {
    ProtocolRegistry
      .register({
        protocol: AIRAVATA_PROTOCOL,
        command: `"${process.execPath}" "${path.resolve(
          process.argv[1]
        )}" $_URL_`,
        override: true,
        script: true,
        terminal: !isProd,
      })
      .then(() => console.log("Successfully registered"))
      .catch(console.error);
  } else {
    // TODO: uncomment when in prod
    // if (!app.isDefaultProtocolClient(AIRAVATA_PROTOCOL)) {
    //   app.setAsDefaultProtocolClient(AIRAVATA_PROTOCOL);
    // }
  }

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

  return "token";
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
    console.log(url);

    if (url.startsWith("https://md.cybershuttle.org/auth/callback/")) {
      const token = await getToken(url);
      console.log("token: ", token);
      // event.sender.send('ci-logon-code', code);
    }

    // const keywords = [
    //   'code', 'iam.scigap.org', 'molecular-dynamics'
    // ];

    // for (let keyword of keywords) {
    //   if (!url.includes(keyword)) {
    //     return;
    //   }
    // }
    // // get the code parameter from the url
    // const rawCode = /code=([^&]*)/.exec(url) || null;
    // const code = (rawCode && rawCode.length > 1) ? rawCode[1] : null;
    // if (code) {
    //   // console.log("WE HAVE THE CODE:", code);
    //   // event.sender.send('ci-logon-code', code);
    //   // authWindow.close();
    //   console.log("WE HAVE THE CODE:", code);

    //   // the cilogon code isnt what we need, we need the iam sci one
    // }
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
  // runit('./proxy/novnc_proxy &', 20000).then(function (data)
  // {
  //   console.log("success: ", data);
  //   // fs.readFile('./proxy/config.txt', 'utf8', (err, data) =>
  //   // {
  //   //   // the file is formatted like:
  //   //   // hostname
  //   //   // port
  //   //   // read both 
  //   //   const lines = data.split('\n');
  //   //   const hostname = lines[0];
  //   //   const port = lines[1];
  //   //   console.log(hostname, port);
  //   // });

  // }, function (err)
  // {
  //   console.log("fail: ", err);
  // });


  // event.sender.send('proxy-started', 'localhost', 5900);
}

async function stopIt(event, restart) {
  exec(KILL_CMD,
    (error, stdout, stderr) => {
      event.sender.send('proxy-stopped', restart);
    });
}

ipcMain.on('stop-proxy', stopIt);

