import BaseModel from './BaseModel'
import UserStatus from './UserStatus'

const FIELDS = [
     'userModelVersion',
     'airavataInternalUserId',
     'userId',
     'gatewayId',
     'emails',
     'firstName',
     'lastName',
     'middleName',
     'namePrefix',
     'nameSuffix',
     'orcidId',
     'phones',
     'country',
     'nationality',
     'homeOrganization',
     'orginationAffiliation',
     {
         name: 'creationTime',
         type: 'date',
     },
     {
         name: 'lastAccessTime',
         type: 'date',
     },
     'validUntil',
     {
         name: 'State',
         type: UserStatus,
     },
     'comments',
     'labeledURI',
     'gpgKey',
     'timeZone',
     'nsfDemographics',
     'customDashboard',
];

export default class UserProfile extends BaseModel {
    constructor(data = {}) {
        super(FIELDS, data);
    }

    get email() {
        return (emails != null && emails.length > 0) ? emails[0] : null;
    }
}
