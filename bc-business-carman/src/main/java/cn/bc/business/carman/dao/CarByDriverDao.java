/**
 * 
 */
package cn.bc.business.carman.dao;

import cn.bc.business.car.domain.Car;
import cn.bc.business.carman.domain.CarByDriver;
import cn.bc.core.dao.CrudDao;

/**
 * 司机营运车辆Dao
 * 
 * @author dragon
 */
public interface CarByDriverDao extends CrudDao<CarByDriver> {

	// 根据司机ID查询营运车辆信息
	Car findBycarManId(Long id);

	/**
	 * 更新车辆模块的司机信息
	 * 
	 * @param id
	 */
	void updateCar4Driver(Long id);

	/**
	 * 更新司机模块的车辆信息
	 * 
	 * @param id
	 */
	void updateDriver4Car(Long id);

}