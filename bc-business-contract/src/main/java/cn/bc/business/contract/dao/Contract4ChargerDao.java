/**
 * 
 */
package cn.bc.business.contract.dao;

import java.util.List;
import java.util.Map;

import cn.bc.business.contract.domain.Contract4Charger;
import cn.bc.core.Page;
import cn.bc.core.dao.CrudDao;
import cn.bc.core.query.condition.Condition;


/**
 * 责任人合同Dao
 * 
 * @author dragon
 */
public interface Contract4ChargerDao extends CrudDao<Contract4Charger> {

	/**
	 * 删除单个CarNContract
	 * @parma contractId 
	 * @return
	 */
	void deleteCarNContract(Long contractId);

	/**
	 * 删除批量CarNContract
	 * @parma contractId 
	 * @return
	 */
	void deleteCarNContract(Long[] contractIds);

	/**
	 * 保存合同与车辆的关联表信息
	 * @parma carId 
	 * @parma contractId 
	 * @return
	 */
	void carNContract4Save(Long carId, Long contractId);

	/**
	 * 查找车辆合同列表
	 * @parma condition 
	 * @parma carId 
	 * @return
	 */
	List<Map<String, Object>> list4car(Condition condition, Long carId);

	/**
	 * 查找车辆合同分页
	 * @parma condition 
	 * @parma carId 
	 * @return
	 */
	Page<Map<String,Object>> page4car(Condition condition, int pageNo,
			int pageSize);

	/**
	 * 根据contractId查找car信息
	 * @parma contractId 
	 * @return
	 */
	Map<String, Object> findCarInfoByContractId(Long contractId);

	/**
	 * 根据contractId查找car信息
	 * @parma contractId 
	 * @return
	 */
	List<String> findChargerIdByContractId(Long contractId);

	/**
	 * 根据责任人ID和合同ID.保存到人员与合同中间表,不存在插入新纪录,存在删除.重新插入
	 * @param assignChargerIds
	 * @param contractId
	 */
	void carMansNContract4Save(String assignChargerIds, Long contractId);

	/**
	 * 根据合同ID查找关联责任人
	 * @param contractId
	 * @return
	 */
	public Long findCarIdByContractId(Long contractId);

	/**
	 * 更新车辆表的负责人信息
	 * @param assignChargerNames
	 * @param carId
	 */
	void updateCar4dirverName(String assignChargerNames, Long carId);

	/**
	 * JDBC
	 * 更新司机表的负责人信息
	 * @param assignChargerNames
	 * @param carId
	 */
	void updateCarMan4dirverName(String assignChargerNames, Long carId);

	/**
	 * JDBC
	 * 根据车辆ID查找车辆信息
	 * @param carId
	 * @return
	 */
	Map<String, Object> findCarByCarId(Long carId);

	/**
	 * 根据司机ID查找车辆信息
	 * @param carManId
	 * @return
	 */
	Map<String, Object> findCarByCarManId(Long carManId);

	/**
	 * 更新司机表的负责人信息
	 * @param assignChargerNames
	 * @param carId
	 */
	void updateCar4ChargerName(String assignChargerNames, Long carId);

	/**
	 * 更新司机表的负责人信息
	 * @param assignChargerNames
	 * @param carId
	 */
	void updateCarMan4ChargerName(String assignChargerNames, Long carId);

	/** 判断指定的车辆是否已经存在经济合同*/
	boolean isExistContract(Long carId);

	/**
	 * 更新车辆表的负责人信息(调用存储过程)
	 * @param carId
	 */
	void updateCar4ChargerName(Long carId);

	/**
	 * 更新司机表的负责人信息(调用存储过程)
	 * @param carId
	 */
	void updateCarMan4ChargerName(Long carId);


}