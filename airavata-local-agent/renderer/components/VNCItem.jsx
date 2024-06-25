import { VncScreen } from "react-vnc";

export const VNCItem = ({ vncRef, url, username, password, handleOnDisconnect }) => {
  let interval;
  console.log("VMD connecting to...", url);
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
      onDisconnect={() => {
        clearInterval(interval);
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


        // rfb._sock._websocket.onmessage = (e) => {
        //   console.log("Received message", e.data);
        //   if (e.data === "ping") {
        //     rfb._sock._websocket.send(JSON.stringify({
        //       type: "ping"
        //     }));
        //   }
        // };
      }}
      autoConnect={true}
    />
  );
};