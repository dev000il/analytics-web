CREATE TABLE `ja_act_lotterylog` (
  `company` int(11) NOT NULL DEFAULT '0',
  `TIMESTAMP` datetime NOT NULL,
  `phone` varchar(20) NOT NULL DEFAULT '-',
  `openid` varchar(100) NOT NULL DEFAULT '-',
  `state` varchar(50) NOT NULL DEFAULT '-',
  `city` varchar(50) NOT NULL DEFAULT '-',
  `product` varchar(50) DEFAULT NULL,
  `reward` varchar(50) DEFAULT NULL,
  `points` decimal(10,2) NOT NULL DEFAULT '0.00',
  `hid` varchar(100) NOT NULL DEFAULT '-',
  `remark` varchar(100) NOT NULL DEFAULT '-'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT
/*!50100 PARTITION BY HASH (TO_DAYS(`TIMESTAMP`))
PARTITIONS 31 */;

ALTER table ja_act_lotterylog ADD INDEX index_lotterylog_cp_ts(company,`TIMESTAMP`);

CREATE TABLE `ja_act_claimlog` (
  `company` int(11) NOT NULL DEFAULT '0',
  `TIMESTAMP` datetime NOT NULL,
  `phone` varchar(20) NOT NULL DEFAULT '-',
  `openid` varchar(100) NOT NULL DEFAULT '-',
  `state` varchar(50) NOT NULL DEFAULT '-',
  `city` varchar(50) NOT NULL DEFAULT '-',
  `product` varchar(50) DEFAULT NULL,
  `reward` varchar(50) DEFAULT NULL,
  `rewardtype` varchar(20) DEFAULT NULL,
  `STATUS` varchar(20) DEFAULT NULL,
  `hid` varchar(100) NOT NULL DEFAULT '-',
  `remark` varchar(100) NOT NULL DEFAULT '-'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT
/*!50100 PARTITION BY HASH (TO_DAYS(`TIMESTAMP`))
PARTITIONS 31 */;

ALTER table ja_act_claimlog ADD INDEX index_claimlog_cp_ts(company,`TIMESTAMP`);

CREATE TABLE `ja_act_lottery_number` (
	`timestamp` datetime NOT NULL,
	`product` INT (11) NOT NULL DEFAULT '0',
	`state` VARCHAR (50) NOT NULL DEFAULT '-',
	`city` VARCHAR (50) NOT NULL DEFAULT '-',
	`openid` VARCHAR (100) NOT NULL DEFAULT '-',
	`count` INT (11) NOT NULL DEFAULT '1'
) ENGINE = INNODB DEFAULT CHARSET = utf8 ROW_FORMAT = COMPACT;

ALTER table ja_act_lottery_number ADD INDEX index_pro_st_ci_op(product, state, city, openid);

CREATE TABLE `ja_act_lottery_amount` (
	`product` INT (11) NOT NULL DEFAULT '0',
	`state` VARCHAR (50) NOT NULL DEFAULT '-',
	`city` VARCHAR (50) NOT NULL DEFAULT '-',
	`count` INT (11) NOT NULL DEFAULT '1'
) ENGINE = INNODB DEFAULT CHARSET = utf8 ROW_FORMAT = COMPACT;


//zb_act_lotterylog,zb_act_claimlog,city_name_mapping these three table should create in pipeline

CREATE TABLE `zb_act_lotterylog` (
  `company` int(11) NOT NULL DEFAULT '0',
  `TIMESTAMP` datetime NOT NULL,
  `phone` varchar(20) NOT NULL DEFAULT '-',
  `openid` varchar(100) NOT NULL DEFAULT '-',
  `state` varchar(50) NOT NULL DEFAULT '-',
  `city` varchar(50) NOT NULL DEFAULT '-',
  `product` varchar(50) DEFAULT NULL,
  `reward` varchar(50) DEFAULT NULL,
  `points` decimal(10,2) NOT NULL DEFAULT '0.00',
  `hid` varchar(100) NOT NULL DEFAULT '-',
  `remark` varchar(100) NOT NULL DEFAULT '-'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT
/*!50100 PARTITION BY HASH (TO_DAYS(`TIMESTAMP`))
PARTITIONS 31 */;

ALTER table zb_act_lotterylog ADD INDEX index_zb_lotterylog_cp_ts(company,`TIMESTAMP`);

CREATE TABLE `zb_act_claimlog` (
  `company` int(11) NOT NULL DEFAULT '0',
  `TIMESTAMP` datetime NOT NULL,
  `phone` varchar(20) NOT NULL DEFAULT '-',
  `openid` varchar(100) NOT NULL DEFAULT '-',
  `state` varchar(50) NOT NULL DEFAULT '-',
  `city` varchar(50) NOT NULL DEFAULT '-',
  `product` varchar(50) DEFAULT NULL,
  `reward` varchar(50) DEFAULT NULL,
  `rewardtype` varchar(20) DEFAULT NULL,
  `STATUS` varchar(20) DEFAULT NULL,
  `hid` varchar(100) NOT NULL DEFAULT '-',
  `remark` varchar(100) NOT NULL DEFAULT '-'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT
/*!50100 PARTITION BY HASH (TO_DAYS(`TIMESTAMP`))
PARTITIONS 31 */;

ALTER table zb_act_claimlog ADD INDEX index_zb_claimlog_cp_ts(company,`TIMESTAMP`);

CREATE TABLE `city_name_mapping` (
  `py_state` varchar(50) NOT NULL,
  `py_city` varchar(50) NOT NULL,
  `cn_state` varchar(100) NOT NULL,
  `cn_city` varchar(50) NOT NULL,
  `cn_addr` varchar(100) DEFAULT NULL,
  `remark` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`py_state`,`py_city`,`cn_state`,`cn_city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Maping Chinese to Pinyin';