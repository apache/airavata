import { VncScreen } from "react-vnc";

export const VNCItem = ({ vncRef, url, username, password, handleOnDisconnect }) => {
  console.log("VMD CONNECTING TO", url);
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
      onDisconnect={handleOnDisconnect}
      autoConnect={true}
    />
  );
};