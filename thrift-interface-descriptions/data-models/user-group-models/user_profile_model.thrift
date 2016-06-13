/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

 include "../../airavata-apis/airavata_commons.thrift"

 namespace java org.apache.airavata.model.user
 namespace php Airavata.Model.User
 namespace cpp apache.airavata.model.user
 namespace py apache.airavata.model.user

const string USER_PROFILE_VERSION = "1.0"

enum Status {
    ACTIVE,
    CONFIRMED,
    APPROVED,
    DELETED,
    DUPLICATE,
    GRACE_PERIOD,
    INVITED,
    DENIED
    PENDING,
    PENDING_APPROVAL,
    PENDING_CONFIRMATION
    SUSPENDED
    DECLINED
    EXPIRED
}

/**
 * U.S. Citizen (see: http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)
 *
*/
enum USCitizenship {
    US_CITIZEN,
    US_PERMANENT_RESIDENT,
    OTHER_NON_US_CITIZEN
}

/**
 * Hispanic or Latino - a person of Mexican, Puerto Rican, Cuban, South or
 *  Central American, or other Spanish culture or origin, regardless of race.
 *
*/
enum ethnicity {
    HISPANIC_LATINO,
    NOT_HISPANIC_LATINO
}

/**
 * Asian - a person having origins in any of the original peoples of the Far East,
 *      Southeast Asia, or the Indian subcontinent including, for example, Cambodia,
  *      China, India, Japan, Korea, Malaysia, Pakistan, the Philippine Islands,
  *      Thailand, and Vietnam.
 * American Indian or Alaskan Native - a person having origins in any of the original
  *     peoples of North and South America (including Central America), and who maintains
   *     tribal affiliation or community attachment.
 * Black or African American - a person having origins in any of the black racial groups
 *      of Africa.
 * Native Hawaiian or Pacific Islander - a person having origins in any of the original
 *      peoples of Hawaii, Guan, Samoa, or other Pacific Islands.
 * White - a person having origins in any of the original peoples of Europe, the Middle East, or North Africa.
 *
*/
enum race {
    ASIAN,
    AMERICAN_INDIAN_OR_ALASKAN_NATIVE,
    BLACK_OR_AFRICAN_AMERICAN,
    NATIVE_HAWAIIAN_OR_PACIFIC_ISLANDER,
    WHITE
}

enum disability {
    HEARING_IMAPAIRED,
    VISUAL_IMPAIRED,
    MOBILITY_OR_ORTHOPEDIC_IMPAIRMENT,
    OTHER_IMPAIRMENT
}

/**
 * A structure holding the NSF Demographic information.
 *
 *
*/
struct NSFDemographics {
    1: optional string gender,
    2: optional USCitizenship usCitizenship,
    3: optional list<ethnicity> ethnicities,
    4: optional list<race> races,
    5: optional list<disability> disabilities
}

/**
 * A structure holding the user profile and its child models.
 *
 * Notes:
 *  The model does not include passwords as it is assumed an external identity provider is used to authenticate user.
 *  References:
 *     NSF Demographic Information - http://www.nsf.gov/pubs/2000/00form1225/00form1225.doc
 *     LDAP Schema - https://tools.ietf.org/html/rfc4519
 *     SCIM 2.0 - https://tools.ietf.org/html/rfc7643
 *
 * userModelVersion:
 *  Version number of profile
 *
 * airavataInternalUserId:
 *  internal to Airavata, not intended to be used outside of the Airavata platform or possibly by gateways
 *  (that is, never shown to users), never reassigned, REQUIRED
 *
 * userId:
 *  Externally assertable unique identifier. SAML (primarly in higher education, academic) tends to keep
 *   user name less opaque. OpenID Connect maintains them to be opaque.
 *
 * emails:
 *   Email identifier are Verified, REQUIRED and MULTIVALUED
 *
 * userName:
 *  Name-based identifiers can be multivalues. To keep it simple, Airavata will make it a string.
 *   In the future these can be enumerated as:
     *   Official name (as asserted possibly by some external identity provider)
     *   Prefered name (as asserted or suggested by user directly)
     *   Components:
     *      givenName
     *      surname (familyName)
     *      displayName (often asserted by user to handle things like middle names, suffix, prefix, and the like)
 *
 * orcidId: ORCID ID - http://orcid.org/about/what-is-orcid)
 *
 * phones: Telephone MULTIVALUED
 *
 * country: Country of Residance
 *
 * nationality Countries of citizenship
 *
 * comments:
 *   Free-form information (treated as opaque by Airavata and simply passed to resource).
 *
 * labeledURI:
   * Google Scholar, Web of Science, ACS, e.t.c
 *
 * timeZone:
 *  User’s preferred timezone - IANA Timezone Databases - http://www.iana.org/time-zones.
 *
*/

struct UserProfile {
    1: required string userModelVersion = USER_PROFILE_VERSION,
    2: required string airavataInternalUserId = airavata_commons.DEFAULT_ID,
    3: required string userId,
    4: required list<string> emails,
    5: optional string userName,
    6: optional string orcidId,
    7: optional list<string> phones,
    8: optional string country,
    9: optional list<string> nationality,
    10: optional string homeOrganization,
    11: optional string orginationAffiliation,
    12: required string creationTime,
    13: required string lastAccessTime,
    14: required string validUntil,
    15: required Status State,
    16: optional string comments,
    17: optional list<string> labeledURI,
    18: optional string gpgKey,
    19: optional string timeZone,
    20: optional NSFDemographics nsfDemographics
}