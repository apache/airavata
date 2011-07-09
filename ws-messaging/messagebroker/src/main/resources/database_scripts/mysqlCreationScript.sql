##Used for mySQL database
CREATE TABLE `subscription` (                                     
                `SubscriptionId` varchar(200) NOT NULL default '',          
                `Topics` varchar(255) default '',                               
                `XPath` varchar(200) default '',                       
                `ConsumerAddress` varchar(100) default '',                      
                `ReferenceProperties` blob,
                `xml` blob,                                                     
                `wsrm` tinyint(1) NOT NULL default '0',                                  
                `CreationTime` datetime NOT NULL default '0000-00-00 00:00:00'  
              );
CREATE TABLE `specialSubscription` (                              
                       `SubscriptionId` varchar(200) NOT NULL default '',              
                       `Topics` varchar(255) default '',                               
                       `XPath` varchar(200) default '',                                
                       `ConsumerAddress` varchar(100) default '',                      
                       `ReferenceProperties` blob,                                     
                       `xml` blob,                                                     
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
	