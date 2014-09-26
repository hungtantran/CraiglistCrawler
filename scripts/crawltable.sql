USE powdb;

CREATE TABLE IF NOT EXISTS `domain_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `domain` char(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `domain` (`domain`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
 
CREATE TABLE IF NOT EXISTS `link_crawled_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `link` char(255) NOT NULL,
  `priority` int(10) unsigned DEFAULT NULL,
  `domain_table_id_1` int(10) unsigned NOT NULL,
  `time_crawled` char(128) NOT NULL,
  `date_crawled` char(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `link` (`link`),
  KEY `domain_table_id_1` (`domain_table_id_1`),
  CONSTRAINT `link_crawled_table_ibfk_1` FOREIGN KEY (`domain_table_id_1`) REFERENCES `domain_table` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
 
CREATE TABLE IF NOT EXISTS `link_queue_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `link` char(255) NOT NULL,
  `domain_table_id_1` int(10) unsigned NOT NULL,
  `priority` int(10) unsigned DEFAULT NULL,
  `persistent` int(10) unsigned DEFAULT NULL,
  `time_crawled` char(128) NOT NULL,
  `date_crawled` char(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `link` (`link`),
  KEY `domain_table_id_1` (`domain_table_id_1`),
  CONSTRAINT `link_queue_table_ibfk_1` FOREIGN KEY (`domain_table_id_1`) REFERENCES `domain_table` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
 
CREATE TABLE IF NOT EXISTS `topic_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `type_table_id` int(10) unsigned NOT NULL,
  `topic` char(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `topic` (`topic`),
  KEY `type_table_id` (`type_table_id`),
  CONSTRAINT `topic_table_ibfk_1` FOREIGN KEY (`type_table_id`) REFERENCES `type_table` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
 
CREATE TABLE IF NOT EXISTS `type_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `type` char(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1