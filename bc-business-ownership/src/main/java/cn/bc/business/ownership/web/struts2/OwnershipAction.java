package cn.bc.business.ownership.web.struts2;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.business.OptionConstants;
import cn.bc.business.car.domain.Car;
import cn.bc.business.car.service.CarService;
import cn.bc.business.ownership.domain.Ownership;
import cn.bc.business.ownership.service.OwnershipService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.identity.web.SystemContext;
import cn.bc.option.service.OptionService;
import cn.bc.web.ui.html.page.PageOption;

/**
 * 车辆经营权Action
 * 
 * @author zxr
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class OwnershipAction extends FileEntityAction<Long, Ownership> {
	private static final long serialVersionUID = 1L;
	public OwnershipService ownershipService;
	public CarService carService;

	private OptionService optionService;
	public List<Map<String, String>> natures; // 经营权性质
	public List<Map<String, String>> situations; // 经营权情况
	public List<Map<String, String>> owners; // 车辆产权
	Ownership e = new Ownership();

	public Ownership getE() {
		return e;
	}

	public void setE(Ownership e) {
		this.e = e;
	}

	@Autowired
	public void setOptionService(OptionService optionService) {
		this.optionService = optionService;
	}

	@Autowired
	public void setCarService(CarService carService) {
		this.carService = carService;
	}

	@Autowired
	public void setOwnershipService(OwnershipService ownershipService) {
		this.ownershipService = ownershipService;
		this.setCrudService(ownershipService);
	}
	@Override
	public boolean isReadonly() {
		// 车辆经营权管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.ownership"),
				getText("key.role.bc.admin"));
	}

	@Override
	protected void initForm(boolean editable) throws Exception {
		super.initForm(editable);
		// 批量加载可选项列表
		Map<String, List<Map<String, String>>> optionItems = this.optionService
				.findOptionItemByGroupKeys(new String[] {
						OptionConstants.OWNERSHIP_NATURE,
						OptionConstants.OWNERSHIP_OWNER,
						OptionConstants.OWNERSHIP_SITUATION });
		// 经营权性质
		natures = optionItems.get(OptionConstants.OWNERSHIP_NATURE);
		// 经营权情况
		situations = optionItems.get(OptionConstants.OWNERSHIP_SITUATION);
		// 车辆产权
		owners = optionItems.get(OptionConstants.OWNERSHIP_OWNER);

	}

	@Override
	protected PageOption buildFormPageOption(boolean editable) {
		return super.buildFormPageOption(editable).setWidth(400)
				.setMinWidth(250);
	}

	// 编辑表单
	public String edit() throws Exception {
		e = this.ownershipService.getEByCarId(this.getId());
		// 如果e为不空就执行编辑方法
		if (e != null) {

			this.formPageOption = buildFormPageOption(true);

			// 初始化表单的其他配置
			this.initForm(true);
			this.afterEdit(e);
			return "form";
		} else {
			// 如果e为空，则新建一个表单
			// 初始化E
			this.setE(this.getCrudService().create());
			//设置车辆的信息
			Car car = this.carService.load(this.getId());
			this.getE().setCar(car);
			// 初始化表单的配置信息
			this.formPageOption = buildFormPageOption(true);

			// 初始化表单的其他配置
			this.initForm(true);

			this.afterCreate(this.getE());

			return "form";
		}
	}

	// 保存
	public String save() throws Exception {
		this.beforeSave(e);
		this.getCrudService().save(e);
		this.afterSave(e);
		return "saveSuccess";
	}

}