/**
 * 
 */
package cn.bc.business.info.service;

import cn.bc.business.info.domain.Info;
import cn.bc.core.service.CrudService;

/**
 * ��Ϣ����Service
 * 
 * @author dragon
 */
public interface InfoService extends CrudService<Info> {
	/**
	 * ����ָ������Ϣ
	 * 
	 * @param id
	 */
	void doIssue(Long id);

	/**
	 * ����ָ������Ϣ
	 * 
	 * @param id
	 */
	void doDisabled(Long id);
}