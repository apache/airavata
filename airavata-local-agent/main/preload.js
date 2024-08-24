/*****************************************************************
*
*  Licensed to the Apache Software Foundation (ASF) under one  
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information       
*  regarding copyright ownership.  The ASF licenses this file  
*  to you under the Apache License, Version 2.0 (the           
*  "License"); you may not use this file except in compliance  
*  with the License.  You may obtain a copy of the License at  
*                                                              
*    http://www.apache.org/licenses/LICENSE-2.0                
*                                                              
*  Unless required by applicable law or agreed to in writing,  
*  software distributed under the License is distributed on an 
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY      
*  KIND, either express or implied.  See the License for the   
*  specific language governing permissions and limitations     
*  under the License.                                          
*                                                              
*
*****************************************************************/

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