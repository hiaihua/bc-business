-- ##bc营运管理子系统的 oracle 删表脚本##
-- ##运行此脚本之后再运行平台的删表脚本framework.db.oracle.drop.sql##

-- 用于生成数据转换 id的序列
CALL DROP_USER_SEQUENCE('CORE_SEQUENCE');
CALL DROP_USER_SEQUENCE('DC_SEQUENCE');

-- 数据转换记录
CALL DROP_USER_TABLE('DC_RECORD');

-- 同步记录
CALL DROP_USER_TABLE('BS_SYNC_JIAOWEI_JTWF');
CALL DROP_USER_TABLE('BS_SYNC_JINDUN_JTWF');

-- 营运事件
CALL DROP_USER_TABLE('BS_CASE_ADVICE');
CALL DROP_USER_TABLE('BS_CASE_PRAISE');
CALL DROP_USER_TABLE('BS_CASE_ACCIDENT');
CALL DROP_USER_TABLE('BS_CASE_INFRACT_TRAFFIC');
CALL DROP_USER_TABLE('BS_CASE_INFRACT_BUSINESS');
CALL DROP_USER_TABLE('BS_CASE_BASE');

-- 黑名单
CALL DROP_USER_TABLE('BS_BLACKLIST');

-- 合同
CALL DROP_USER_TABLE('BS_CAR_CONTRACT');
CALL DROP_USER_TABLE('BS_CARMAN_CONTRACT');
CALL DROP_USER_TABLE('BS_INDUSTRIAL_INJURY');
CALL DROP_USER_TABLE('BS_CONTRACT_LABOUR');
CALL DROP_USER_TABLE('BS_CONTRACT_CHARGER');
CALL DROP_USER_TABLE('BS_CONTRACT');

-- 司机迁移历史
CALL DROP_USER_TABLE('BS_CARMAN_HISTORY');
CALL DROP_USER_TABLE('BS_CAR_DRIVER_HISTORY');

--承保险种
CALL DROP_USER_TABLE('BS_INSURANCE_TYPE');
-- 车辆保单
CALL DROP_USER_TABLE('BS_CAR_POLICY');


-- 司机营运车辆
CALL DROP_USER_TABLE('BS_CAR_DRIVER');

-- 车辆与证件的关联
CALL DROP_USER_TABLE('BS_CAR_CERT');

-- 车辆
CALL DROP_USER_TABLE('BS_CAR');

-- 车队历史车辆数
CALL DROP_USER_TABLE('BS_MOTORCADE_CARQUANTITY');

-- 车队
CALL DROP_USER_TABLE('BS_MOTORCADE');

-- 司机责任人与证件的关联
CALL DROP_USER_TABLE('BS_CARMAN_CERT');

-- 证件
CALL DROP_USER_TABLE('BS_CERT_IDENTITY');
CALL DROP_USER_TABLE('BS_CERT_DRIVING');
CALL DROP_USER_TABLE('BS_CERT_CYZG');
CALL DROP_USER_TABLE('BS_CERT_FWZG');
CALL DROP_USER_TABLE('BS_CERT_JSPX');
CALL DROP_USER_TABLE('BS_CERT_ROADTRANSPORT');
CALL DROP_USER_TABLE('BS_CERT_VEHICELICENSE');
CALL DROP_USER_TABLE('BS_CERT');

-- 司机责任人
CALL DROP_USER_TABLE('BS_CARMAN');
