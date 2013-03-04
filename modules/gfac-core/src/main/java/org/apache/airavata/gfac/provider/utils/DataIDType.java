package org.apache.airavata.gfac.provider.utils;

import org.apache.airavata.core.gfac.exception.GfacException;
import org.xmlpull.v1.builder.XmlElement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Represents a DataID (A schema with real names), currently it only sends a one
 * location value.
 */
public class DataIDType {
    public static final String LOCATION_ATTRIBUTE = "location";

    private URI dataID;

    private ArrayList<URI> dataLocations = new ArrayList<URI>();

    public URI getRealLocation() {
        if (dataLocations.size() > 0) {
            return dataLocations.get(0);
        } else {
            return null;
        }
    }

    public DataIDType(XmlElement ele) throws GfacException {
        try {
            String value = ele.requiredTextContent();
            if (value != null) {
                this.dataID = new URI(value);
            } else {
                throw new GfacException(
                        "Illegal InputMessage, No value content found for the parameter "
                                + ele.getName() + "/value. Invalid Local Argument");
            }
            String location = ele.getAttributeValue(null, DataIDType.LOCATION_ATTRIBUTE);
            if (location != null) {
                addDataLocation(new URI(location));
            }
        } catch (URISyntaxException e) {
            throw new GfacException("Invalid Local Argument", e);
        }
    }

    public DataIDType(URI dataID) {
        super();
        this.dataID = dataID;
    }

    public void addDataLocation(URI dataLocation) {
        dataLocations.add(dataLocation);
    }

    public ArrayList<URI> getDataLocations() {
        return dataLocations;
    }

    public URI getDataID() {
        return dataID;
    }

    public void fillData(XmlElement ele) {
        ele.addChild(dataID.toString());
        URI location = getRealLocation();
        if (location != null) {
            ele.addAttribute(DataIDType.LOCATION_ATTRIBUTE, location.toString());
        }
    }

}

