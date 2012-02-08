/**
 * 
 */
package cn.bc.business.car.dao;

import java.util.List;
import java.util.Map;

import cn.bc.business.car.domain.Car;
import cn.bc.core.Page;
import cn.bc.core.dao.CrudDao;
import cn.bc.core.query.condition.Condition;

/**
 * 车辆Dao
 * 
 * @author dragon
 */
public interface CarDao extends CrudDao<Car> {

	/**
	 * 查找汽车列表
	 * 
	 * @parma condition
	 * @return
	 */
	List<Map<String, Object>> list(Condition condition);

	/**
	 * 查找汽车分页
	 * 
	 * @parma condition
	 * @parma Page
	 * @return
	 */
	Page<Map<String, Object>> page(Condition condition, int pageNo, int pageSize);

	/** 通过carManId找车辆 */
	List<Car> findAllcarBycarManId(Long id);

	/**
	 * 根据车牌号查找车辆id
	 * @parma carPlateNo 
	 * @return Long
	 */
	Long findcarIdByCarPlateNo(String carPlateNo);

	/**
	 * 通过自编号生成原车号
	 * @param code
	 * @return
	 */
	Car findcarOriginNoByCode(String code);

	/**
	 * 通过车牌号查找此车辆所属的分公司与车队
	 * @parma carPlateNo 
	 * @return Map<String, Object>
	 */
	Map<String, Object> findcarInfoByCarPlateNo2(String carPlateNo);
}