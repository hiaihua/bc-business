package cn.bc.business.insuranceType.web.struts2;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.BCConstants;
import cn.bc.business.insuranceType.domain.InsuranceType;
import cn.bc.business.insuranceType.service.InsuranceTypeService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.core.exception.CoreException;
import cn.bc.identity.web.SystemContext;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.json.Json;

/**
 * 车辆保单险种Action
 * 
 * @author dragon
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class InsuranceTypeAction extends FileEntityAction<Long, InsuranceType> {
	// private static Log logger = LogFactory.getLog(MotorcadeAction.class);
	private static final long serialVersionUID = 1L;
	public InsuranceTypeService insuranceTypeService;
	
	public List<Map<String,String>> templateList;//可选模板列表

	@Autowired
	public void setInsuranceTypeService(
			InsuranceTypeService insuranceTypeService) {
		this.insuranceTypeService = insuranceTypeService;
		this.setCrudService(insuranceTypeService);
	}

	@Override
	public boolean isReadonly() {
		// 车辆保单险种管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.insuranceType"),
				getText("key.role.bc.admin"));
	}

	@Override
	protected void afterCreate(InsuranceType entity) {
		super.afterCreate(entity);
		InsuranceType e = this.getE();
		// 初始状态
		e.setStatus(BCConstants.STATUS_ENABLED);


	}

	@Override
	protected PageOption buildFormPageOption(boolean editable) {
		return super.buildFormPageOption(editable).setWidth(420)
				.setMinWidth(300).setHeight(340).setMinHeight(200);
	}

	@Override
	public String delete() throws Exception {
		SystemContext context = this.getSystyemContext();
		// 将状态设置为禁用而不是物理删除,更新最后修改人和修改时间
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("status", new Integer(BCConstants.STATUS_DISABLED));
		attributes.put("modifier", context.getUserHistory());
		attributes.put("modifiedDate", Calendar.getInstance());

		if (this.getId() != null) {// 处理一条
			this.insuranceTypeService.update(this.getId(), attributes);
		} else {// 处理一批
			if (this.getIds() != null && this.getIds().length() > 0) {
				Long[] ids = cn.bc.core.util.StringUtils
						.stringArray2LongArray(this.getIds().split(","));
				this.insuranceTypeService.update(ids, attributes);
			} else {
				throw new CoreException("must set property id or ids");
			}
		}
		Json json = new Json();
		json.put("msg", getText("form.disabled.success"));
		this.json = json.toString();
		return "json";
	}

	@Override
	protected void initForm(boolean editable) throws Exception {
		super.initForm(editable);
		templateList=this.insuranceTypeService.findEnabled4Option();
	}

	@Override
	protected void beforeSave(InsuranceType entity) {
		//但模板修改类型险种时，下拉列还有当前模板可选择，保存时需要清空此模板
		if(entity.getId()==entity.getPid()){
			entity.setPid(null);
		}
		//模板保存时清空
		if(entity.getType()==InsuranceType.TYPE_TEMPLATE){
			entity.setCoverage(null);
			entity.setPid(null);
		}
		super.beforeSave(entity);
	}

	
	
}
