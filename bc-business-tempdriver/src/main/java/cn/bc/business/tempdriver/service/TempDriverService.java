package cn.bc.business.tempdriver.service;

import java.util.List;

import cn.bc.business.tempdriver.domain.TempDriver;
import cn.bc.core.service.CrudService;
import cn.bc.template.service.AddAttachFromTemplateService;

/**
 * 司机招聘Service
 * 
 * @author lbj
 * 
 */
public interface TempDriverService extends CrudService<TempDriver>,AddAttachFromTemplateService {
	
	/**
	 * 身份证号唯一性检测
	 * 
	 * @param id 招聘司机Id
	 * @param certIdentity 身份证号码
	 * @return
	 */
	boolean isUniqueCertIdentity(Long id,String certIdentity);
	
	/**
	 * 身份证号查对象
	 * 
	 * @param certIdentity 身份证号码
	 * @return
	 */
	TempDriver loadByCertIdentity(String certIdentity);
	
	/**
	 * 发起流程
	 * @param key 流程key值
	 * @param ids 招聘司机的ID
	 * @return
	 */
	String doStartFlow(String key,Long[] ids);
	
	/**
	 * 批量保存方法
	 * @param lists
	 */
	void doSaveList(List<TempDriver> lists);
	
	/**
	 * 查招聘司机表是否已保存此身份证号的心
	 * 
	 * @param certIdentity 身份证号码
	 * @return true 是，false 否
	 */
	boolean isExistCertIdentity(String certIdentity);
}
