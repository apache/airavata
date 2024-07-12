import { useEffect, useState } from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import { docco } from 'react-syntax-highlighter/dist/cjs/styles/hljs';

export const DockerInspectModal = ({ containerId }) => {
  const [inspectContent, setInspectContent] = useState({});

  useEffect(() => {
    window.ipc.send("inspect-container", containerId);

    window.ipc.on("container-inspected", (data) => {
      setInspectContent(data);
    });

    return () => {
      window.ipc.removeAllListeners("container-inspected");
    };
  }, []);

  return (
    <>
      <SyntaxHighlighter language="javascript" style={docco}>
        {JSON.stringify(inspectContent, null, 2)}
      </SyntaxHighlighter>
    </>
  );
};