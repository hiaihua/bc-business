-- bc营运管理子系统的删表脚本
-- 运行此脚本之后再运行平台的删表脚本framework.db.mysql.drop.sql

-- 车辆
drop table if exists BS_CAR;

-- 车队信息
drop table if exists BS_MOTORCADE;

--查看历史车辆数
drop table if exists BS_HISTORY_CAR_QUANTITY;
