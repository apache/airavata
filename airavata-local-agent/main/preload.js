import { contextBridge, ipcRenderer } from 'electron';

const handler = {
  send(channel, value) {
    ipcRenderer.send(channel, value);
  },
  on(channel, callback) {
    const subscription = (_event, ...args) => callback(...args);
    ipcRenderer.on(channel, subscription);

    return () => {
      ipcRenderer.removeListener(channel, subscription);
    };
  },
};

contextBridge.exposeInMainWorld('ipc', handler);

contextBridge.exposeInMainWorld('auth', {
  ciLogonLogin: () => ipcRenderer.send('ci-logon-login'),
  ciLogonSuccess: (callback) => {
    ipcRenderer.once('ci-logon-success', callback);
  },

  ciLogonLogout: () => ipcRenderer.send('ci-logon-logout'),
});

contextBridge.exposeInMainWorld('jn', {
  showWindow: (url) => ipcRenderer.send('show-window', url),
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