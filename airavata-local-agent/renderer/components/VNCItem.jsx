import { VncScreen } from "react-vnc";

export const VNCItem = ({ vncRef, url, username, password, handleOnDisconnect }) => {
  let interval;
  console.log("VMD connecting to...", url);

  const onMessageListener = (event) => {
    console.log("Received message", event.data);
    const data = JSON.parse(event.data);
    if (data.type === "pong") {
      console.log("Received pong from WebSockify");
    }
  };

  return (
    <VncScreen
      url={url}
      // scaleViewport
      background="#000000"
      style={{
        width: '100%',
        height: '100%',
      }}
      rfbOptions={{
        credentials: {
          username,
          password
        }
      }}
      ref={vncRef}
      onDisconnect={(rfb) => {
        clearInterval(interval);

        // remove listener

        handleOnDisconnect();
      }}
      onConnect={(rfb) => {
        console.log(rfb);
        // rfb._sock._websocket.send(JSON.stringify({
        //   type: "ping"
        // }))

        // interval = setInterval(() => {
        //   rfb._sock._websocket.send(JSON.stringify({
        //     type: "ping"
        //   }));
        // }, 1000);

        // ws.onmessage = (event) => {
        //   const data = JSON.parse(event.data);
        //   if (data.type === "pong") {
        //   console.log("Received pong from WebSockify");
        //      } 
        //   };

        // rfb._sock._websocket.onmessage = (e) => {
        //   console.log("Received message", e);
        //   // const data = JSON.parse(event.data);
        // };'

        // send a pong every 10 seconds

        const websocket = rfb._sock._websocket;

        console.log(websocket);
        console.log(typeof websocket);

        //   setInterval(() => {
        //     console.log("Sending ping");
        //     rfb._sock._websocket.send(JSON.stringify({
        //       type: "ping"
        //     }));
        //   }, 2000);
      }}
      autoConnect={true}
    />
  );
};