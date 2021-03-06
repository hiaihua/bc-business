/**
 * 
 */
package cn.bc.business.runcase.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import cn.bc.business.runcase.domain.Case4InfractBusiness;
import cn.bc.core.service.CrudService;
import cn.bc.sync.domain.SyncBase;


/**
 * 营运事件营运违章Service
 * 
 * @author dragon
 */
public interface CaseBusinessService extends CrudService<Case4InfractBusiness> {

	/**
	 * 保存并更新Sycn对象的状态
	 * @param e
	 * @param sb
	 * @return
	 */
	Case4InfractBusiness save(Case4InfractBusiness e, SyncBase sb);

	
	/**
	 * 结案操作
	 * @param fromBusinessId
	 * @param closeDate
	 * @return
	 */
	Case4InfractBusiness doCloseFile(Long fromBusinessId, Calendar closeDate);
	

	/**
	 * 发起流程
	 * @param key 流程key值
	 * @param ids 客管投诉信息的ID
	 * @throws Exception
	 * 
	 * return List<Map<String,String>> :{("moduleId":"模块id","procInstId":"流程实例id")
	 * 									,(.......)
	 * 									,.....}
	 */
	List<Map<String,String>> doStartFlow(String key, Long[] ids) throws Exception;
	
	/**
	 * 更新的信息
	 * 
	 * @param id
	 *            
	 * @param attributes
	 *            更新的信息
	 */
	void updateCaseBusinessInfo4Flow(Long id, Map<String, Object> attributes);

}