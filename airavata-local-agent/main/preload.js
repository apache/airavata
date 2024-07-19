import { contextBridge, ipcRenderer } from 'electron';

const handler = {
  send(channel, ...args) {
    ipcRenderer.send(channel, ...args);
  },
  on(channel, callback) {
    const subscription = (_event, ...args) => callback(...args);
    ipcRenderer.on(channel, subscription);

    return () => {
      ipcRenderer.removeListener(channel, subscription);
    };
  },
  removeAllListeners(channel) {
    ipcRenderer.removeAllListeners(channel);
  }
};

// window.myPrompt = function (title, val) {
//   return ipcRenderer.sendSync('prompt', { title, val });
// };

contextBridge.exposeInMainWorld('userActions', {
  myPrompt: (title, val) => ipcRenderer.sendSync('prompt', { title, val }),
});

contextBridge.exposeInMainWorld('ipc', handler);


contextBridge.exposeInMainWorld('config', {
  getVersionNumber: () => ipcRenderer.send('get-version-number'),
  versionNumber: (callback) => {
    ipcRenderer.once('version-number', callback);
  },
});

contextBridge.exposeInMainWorld('auth', {
  ciLogonLogin: () => ipcRenderer.send('ci-logon-login'),
  ciLogonSuccess: (callback) => {
    ipcRenderer.once('ci-logon-success', callback);
  },

  ciLogonLogout: () => ipcRenderer.send('ci-logon-logout'),
});

contextBridge.exposeInMainWorld('jn', {
  showWindow: (url, associatedId) => ipcRenderer.send('show-window', url, associatedId),
  closeTab: (callback) => {
    ipcRenderer.once('close-tab', callback);
  },
  closeWindow: (associatedId) => ipcRenderer.send('close-window', associatedId),
});

contextBridge.exposeInMainWorld('vnc', {
  startProxy: (experimentId, reqHost, reqPort, websocketPort) => ipcRenderer.send('start-proxy', experimentId, reqHost, reqPort, websocketPort),

  proxyStarted: (callback) => {
    ipcRenderer.once('proxy-started', (event, hostname, port, experimentId) => callback(event, hostname, port, experimentId));
  },

  stopProxy: (restart, experimentId) => ipcRenderer.send('stop-proxy', restart, experimentId),

  proxyStopped: (callback) => {
    ipcRenderer.once('proxy-stopped', callback);
  },


  killedAllWebsockify: (callback) => {
    ipcRenderer.once('killed-all-websockify', callback);
  },


  killAllWebsockify: () => ipcRenderer.send('kill-all-websockify'),
});