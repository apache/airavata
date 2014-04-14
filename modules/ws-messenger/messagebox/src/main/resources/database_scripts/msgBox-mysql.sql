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

DROP TABLE IF EXISTS msgBoxes;

CREATE TABLE `msgBoxes` (
  `msgboxid` varchar(100) NOT NULL default '',
  PRIMARY KEY  (`msgboxid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS msgbox;

CREATE TABLE `msgbox` (
  `id` int(11) NOT NULL auto_increment,
  `content` longblob NOT NULL,
  `msgboxid` varchar(100) NOT NULL default '""',
  `messageid` varchar(100) default '""',
  `soapaction` varchar(100) default '""',
  `time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`id`),
  KEY `MSGBOXID` (`msgboxid`)
) ENGINE=MyISAM AUTO_INCREMENT=7665 DEFAULT CHARSET=latin1;
