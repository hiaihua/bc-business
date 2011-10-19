/**
 * 
 */
package cn.bc.business.carman.service;

import java.util.List;

import cn.bc.business.carman.domain.CarMan;
import cn.bc.business.cert.domain.Cert;
import cn.bc.core.service.CrudService;

/**
 * 司机责任人Service
 * 
 * @author dragon
 */
public interface CarManService extends CrudService<CarMan> {
	/**
	 * 为司机责任人添加一个证件
	 * 
	 * @param carManId
	 * @param cert
	 * @return
	 */

	CarMan saveCert4CarMan(Long carManId, Cert cert);

	/**
	 * 为司机责任人添加一个证件关联
	 * 
	 * @param carManId
	 * @param certId
	 * @return
	 */
	CarMan saveCertRelationship(Long carManId, Long certId);

	/** 根据司机ID查找返回状态为启用中相关辆信息 */
	List<CarMan> selectAllCarManByCarId(Long id);
}