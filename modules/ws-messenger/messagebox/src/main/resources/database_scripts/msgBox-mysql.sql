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
