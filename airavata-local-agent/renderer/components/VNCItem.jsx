import { VncScreen } from "react-vnc";

export const VNCItem = ({ vncRef, url, username, password, handleOnDisconnect }) => {
    return (
        <VncScreen
            url={url}
            scaleViewport
            background="#000000"
            style={{
                width: '100%',
                height: '75vh',
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