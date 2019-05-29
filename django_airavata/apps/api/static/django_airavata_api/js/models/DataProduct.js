
import BaseModel from './BaseModel'
import DataReplicaLocation from './DataReplicaLocation'

import URL from 'url-parse'

const FIELDS = [
    'productUri',
    'gatewayId',
    'parentProductUri',
    'productName',
    'productDescription',
    'ownerName',
    'dataProductType',
    'productSize',
    {
        name: 'creationTime',
        type: 'date',
    },
    {
        name: 'lastModifiedTime',
        type: 'date',
    },
    'productMetadata',
    {
        name: 'replicaLocations',
        type: DataReplicaLocation,
        list: true
    },
    'downloadURL',
    'isInputFileUpload'
];

const FILENAME_REGEX = /[^/]+$/;

export default class DataProduct extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }

    get filename() {
        if (this.replicaLocations && this.replicaLocations.length > 0) {
            const firstReplicaLocation = this.replicaLocations[0];
            const fileURL = new URL(firstReplicaLocation.filePath);
            const filenameMatch = FILENAME_REGEX.exec(fileURL.pathname);
            if (filenameMatch) {
                return filenameMatch[0];
            }
        }
        return null;
    }
}
