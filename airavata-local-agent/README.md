# Airavata UI

*Ganning Xu*, GSoC 2024 Project, Developed with mentors from the [Apache Software Foundation](https://www.apache.org/)

## Running this project

### Setup
1. Clone this repository
2. Navigate to the the local agent directory
3. Install dependencies (`npm i`)

### Running the ElectronJS application
> **Make sure you have correct VNC server URL in `proxy/novnc_proxy`, line 53**

1. Run the ElectronJS application (`npm run dev`)
2. The application should open up automatically

## Current features

- [x] VNC client for connecting to a VNC server (auto-starts websockify server)