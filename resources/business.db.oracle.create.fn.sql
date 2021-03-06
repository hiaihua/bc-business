-- ##营运子系统的 oracle 自定义函数和存储过程##

-- 获取指定车辆实时的营运司机信息,只适用于对当前在案车辆的处理
-- 返回值的格式为：张三(正班),李四(副班),小明(顶班)
-- 返回值是先按营运班次正序排序再按司机的入职时间正序排序进行合并的
-- 参数：cid - 车辆的id
CREATE OR REPLACE FUNCTION getDriverInfoByCarId(cid IN integer) RETURNS varchar2 AS
driverInfo varchar2(4000);
BEGIN
    select wmsys.wm_concat(name || '(' || 
        (case when classes=1 then '正班' when classes=2 then '副班' when classes=3 then '顶班' else '无' end)
        || ')'))
        into driverInfo
		    from (select m.name as name,cm.classes as classes 
            from BS_CAR_DRIVER cm
            inner join BS_CARMAN m on m.id=cm.driver_id
            where cm.status_=0 and cm.car_id=cid
            order by cm.classes asc,m.work_date asc) as t;
    return driverInfo;
END;
/

-- 获取指定车辆实时的经济合同责任人信息,只适用于对当前在案车辆的处理
-- 返回值的格式为：张三,李四
-- 返回值是按责任人的入职时间正序排序的
-- 参数：cid - 车辆的id
CREATE OR REPLACE FUNCTION getChargerInfoByCarId(cid IN integer) RETURNS varchar2 AS
chargerInfo varchar2(4000);
BEGIN
	select wmsys.wm_concat(name) into chargerInfo
		from (SELECT m.name as name
			FROM bs_car_contract cc
			inner join bs_carman_contract cm on cm.contract_id=cc.contract_id
			inner join bs_carman m on m.id=cm.man_id
			where cc.car_id=cid
			order by m.work_date asc) as t;
	return chargerInfo;
END;
/

-- 获取指定司机所营运车辆的经济合同责任人信息,只适用于对当前在案司机的处理
-- 返回值的格式为：张三,李四
-- 返回值是按责任人的创建时间正序排序的
-- 参数：did - 司机的id
CREATE OR REPLACE FUNCTION getChargerInfoByDriverId(did IN integer) RETURNS varchar2 AS
chargerInfo varchar2(4000);
BEGIN
	select wmsys.wm_concat(name) into chargerInfo
		from (SELECT distinct p.name as name,p.file_date
			FROM bs_car_driver cd
			inner join bs_car_contract cc on cc.car_id=cd.car_id
			inner join bs_contract c on c.id=cc.contract_id
			inner join bs_carman_contract pm on pm.contract_id=cc.contract_id
			inner join bs_carman p on p.id=pm.man_id
			-- 正常的营运班次信息+当前经济合同 条件
			where cd.status_=0 and c.main=0 and c.type_=2 and cd.driver_id=did
			order by p.file_date asc) as t;
	return chargerInfo;
END;
/
