﻿-- 更新车辆的营运司机信息
update bs_car set driver=getDriverInfoByCarId(id);
select file_date,id,driver,getDriverInfoByCarId(id) from bs_car order by file_date desc;

-- 更新车辆的责任人信息
update bs_car set charger=getPrincipalInfoByCarId(id);
select file_date,id,charger,getPrincipalInfoByCarId(id) from bs_car order by file_date desc;

-- 更新司机的责任人信息
update bs_carman set charger=getPrincipalInfoByDriverId(id);
select file_date,id,charger,getPrincipalInfoByDriverId(id) from bs_carman order by file_date desc;
select file_date,id,charger,getPrincipalInfoByDriverId(id) from bs_carman where id=100840 order by file_date desc;