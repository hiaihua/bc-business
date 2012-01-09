-- ##Ӫ����ϵͳ�� oracle �Զ��庯���ʹ洢����##

-- ��ȡָ������ʵʱ��Ӫ��˾����Ϣ,ֻ�����ڶԵ�ǰ�ڰ������Ĵ���
-- ����ֵ�ĸ�ʽΪ������(����),����(����),С��(����)
-- ����ֵ���Ȱ�Ӫ�˰�����������ٰ�˾������ְʱ������������кϲ���
-- ������cid - ������id
CREATE OR REPLACE FUNCTION getDriverInfoByCarId(cid IN integer) RETURNS varchar2 AS
driverInfo varchar2(4000);
BEGIN
    select wmsys.wm_concat(name || '(' || 
        (case when classes=1 then '����' when classes=2 then '����' when classes=3 then '����' else '��' end)
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

-- ��ȡָ������ʵʱ�ľ��ú�ͬ��������Ϣ,ֻ�����ڶԵ�ǰ�ڰ������Ĵ���
-- ����ֵ�ĸ�ʽΪ������,����
-- ����ֵ�ǰ������˵���ְʱ�����������
-- ������cid - ������id
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

-- ��ȡָ��˾����Ӫ�˳����ľ��ú�ͬ��������Ϣ,ֻ�����ڶԵ�ǰ�ڰ�˾���Ĵ���
-- ����ֵ�ĸ�ʽΪ������,����
-- ����ֵ�ǰ������˵Ĵ���ʱ�����������
-- ������did - ˾����id
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
			-- ������Ӫ�˰����Ϣ+��ǰ���ú�ͬ ����
			where cd.status_=0 and c.main=0 and c.type_=2 and cd.driver_id=did
			order by p.file_date asc) as t;
	return chargerInfo;
END;
/
