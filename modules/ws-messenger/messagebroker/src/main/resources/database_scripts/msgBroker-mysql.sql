/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
 
CREATE DATABASE IF NOT EXISTS wsmg;
CREATE TABLE `subscription` (
                `SubscriptionId` varchar(200) NOT NULL default '',
                `Topics` varchar(255) default '',
                `XPath` varchar(200) default '',
                `ConsumerAddress` varchar(255) default '',
                `ReferenceProperties` blob,
                `content` blob,
                `wsrm` tinyint(1) NOT NULL default '0',
                `CreationTime` datetime NOT NULL default '0000-00-00 00:00:00'
              );
CREATE TABLE `specialSubscription` (
                       `SubscriptionId` varchar(200) NOT NULL default '',
                       `Topics` varchar(255) default '',
                       `XPath` varchar(200) default '',
                       `ConsumerAddress` varchar(255) default '',
                       `ReferenceProperties` blob,
                       `content` blob,
                       `wsrm` tinyint(1) NOT NULL default '0',
                       `CreationTime` datetime NOT NULL default '0000-00-00 00:00:00'
                     );


CREATE TABLE `disQ` (
          `id` bigint(11) NOT NULL auto_increment,
          `trackId` varchar(100) default NULL,
          `message` longblob,
          `status` int(11) default NULL,
          `topic` varchar(255) default '',
          PRIMARY KEY  (`id`)
        );

CREATE TABLE MaxIDTable(
       maxID integer
       );

CREATE TABLE MinIDTable(
       minID integer
       );

