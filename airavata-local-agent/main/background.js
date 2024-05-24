import path from 'path';
import { app, ipcMain } from 'electron';
import serve from 'electron-serve';
import { createWindow } from './helpers';
const { exec, spawn } = require('child_process');
const fs = require('fs');


const isProd = process.env.NODE_ENV === 'production';
const KILL_CMD = 'kill -9 $(lsof -ti:6080)';

if (isProd)
{
  serve({ directory: 'app' });
} else
{
  app.setPath('userData', `${app.getPath('userData')} (development)`);
}

; (async () =>
{
  await app.whenReady();

  const mainWindow = createWindow('main', {
    width: 1500,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
    },
  });

  if (isProd)
  {
    await mainWindow.loadURL('app://./home');
  } else
  {
    const port = process.argv[2];
    await mainWindow.loadURL(`http://localhost:${port}/home`);
    mainWindow.webContents.openDevTools();
  }
})();

app.on('window-all-closed', () =>
{
  app.quit();
});

app.on("before-quit", (event) =>
{
  // stop the proxy so it's not constantly running in the bkg
  exec(KILL_CMD,
    (error, stdout, stderr) =>
    {
      event.sender.send('proxy-stopped', restart);
    });
  process.exit(); // really let the app exit now
});


ipcMain.on('message', async (event, arg) =>
{
  event.reply('message', `${arg} World!`);
});

function runit(cmd, timeout)
{
  // https://stackoverflow.com/questions/31727684/node-js-exec-doesnt-callback-if-exec-an-exe
  return new Promise(function (resolve, reject)
  {
    var ch = exec(cmd, function (error, stdout, stderr)
    {
      if (error)
      {
        reject(error);
      } else
      {
        resolve("program exited without an error");
      }
    });
    setTimeout(function ()
    {
      resolve("program still running");
    }, timeout);
  });
}


ipcMain.on('start-proxy', startIt);

async function startIt(event)
{

  let cmd = spawn('./proxy/novnc_proxy', { shell: true });

  cmd.stdout.on('data', (data) =>
  {
    data = data.toString().trim();

    if (data == "HANG_NOW")
    {
      fs.readFile('./proxy/config.txt', 'utf8', (err, data) =>
      {
        const lines = data.split('\n');
        const hostname = lines[0];
        const port = lines[1];
        event.sender.send('proxy-started', hostname, port);
      });
    }
  });

  cmd.stderr.on('data', (data) =>
  {
    console.error(`stderr: ${data}`);
  });

  cmd.on('close', async (code) =>
  {
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

async function stopIt(event, restart)
{
  exec(KILL_CMD,
    (error, stdout, stderr) =>
    {
      event.sender.send('proxy-stopped', restart);
    });
}
ipcMain.on('stop-proxy', stopIt);