import VNCViewer from "../components/VNCViewer";
import JupyterLab from "./JupyterLab";

const PanelBody = ({ type, applicationId, reqPort, experimentId, headers }) => {
    if (type === "VMD") {
        return <VNCViewer applicationId={applicationId} reqPort={reqPort} experimentId={experimentId} headers={headers} />;
    } else if (type === "JUPYTER_LAB") {
        return <JupyterLab applicationId={applicationId} reqPort={reqPort} experimentId={experimentId} headers={headers} />;
    } else if (type === "JUPYTER_DOCKER") {
        // return <JupyterDocker
    }
    else {
        return <div>Unknown type</div>;
    }
};

export default PanelBody;