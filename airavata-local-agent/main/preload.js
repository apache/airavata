import { contextBridge, ipcRenderer } from 'electron';

const handler = {
  send(channel, value)
  {
    ipcRenderer.send(channel, value);
  },
  on(channel, callback)
  {
    const subscription = (_event, ...args) => callback(...args);
    ipcRenderer.on(channel, subscription);

    return () =>
    {
      ipcRenderer.removeListener(channel, subscription);
    };
  },
};

contextBridge.exposeInMainWorld('ipc', handler);

contextBridge.exposeInMainWorld('vnc', {
  startProxy: () => ipcRenderer.send('start-proxy'),

  proxyStarted: (callback) =>
  {
    ipcRenderer.once('proxy-started', (event, hostname, port) => callback(event, hostname, port));
  },

  stopProxy: (restart) => ipcRenderer.send('stop-proxy', restart),

  proxyStopped: (callback) =>
  {
    ipcRenderer.once('proxy-stopped', callback);
  }
});