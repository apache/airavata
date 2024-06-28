import { VncScreen } from "react-vnc";

export const VNCItem = ({ vncRef, url, username, password, handleOnDisconnect }) => {
  console.log("VMD connecting to...", url);

  return (
    <VncScreen
      url={url}
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
        handleOnDisconnect();
      }}
      autoConnect={true}
    />
  );
};