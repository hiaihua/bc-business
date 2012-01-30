/**
 * 
 */
package cn.bc.business.car.web.struts2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.business.OptionConstants;
import cn.bc.business.car.domain.Car;
import cn.bc.business.car.service.CarService;
import cn.bc.business.carmodel.domain.CarModel;
import cn.bc.business.carmodel.service.CarModelService;
import cn.bc.business.motorcade.domain.Motorcade;
import cn.bc.business.motorcade.service.MotorcadeService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.identity.domain.Actor;
import cn.bc.identity.service.ActorService;
import cn.bc.identity.web.SystemContext;
import cn.bc.option.domain.OptionItem;
import cn.bc.option.service.OptionService;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.json.Json;

/**
 * 车辆Action
 * 
 * @author dragon
 * 
 */
/**
 * @author wis
 *
 */
/**
 * @author wis
 *
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class CarAction extends FileEntityAction<Long, Car> {
	// private static Log logger = LogFactory.getLog(CarAction.class);
	private static final long serialVersionUID = 1L;
	private MotorcadeService motorcadeService;
	private CarModelService carModelService;
	private ActorService actorService;
	private OptionService optionService;

	public List<Map<String, String>> motorcadeList; // 可选车队列表
	public List<Map<String, String>> businessTypeList; // 可选营运性质列表
	public List<Map<String, String>> levelTypeList; // 可选车辆定级列表
	public List<Map<String, String>> factoryTypeList; // 可选厂牌类型列表
	public List<Map<String, String>> fuelTypeList; // 可选燃料类型列表
	public List<Map<String, String>> colorTypeList; // 可选颜色类型列表
	public List<Map<String, String>> taximeterFactoryTypeList; // 可选计价器制造厂列表
	public List<Map<String, String>> oldUnitList; // 所属单位列表
	public List<Map<String, String>> logoutReasonList; // 注销原因列表
	public List<Map<String, String>> carModelList; // 车型配置列表
	public Map<String, String> statusesValue;
	public Json json;

	@Autowired
	public void setActorService(
			@Qualifier("actorService") ActorService actorService) {
		this.actorService = actorService;
	}

	@Autowired
	public void setCarService(CarService carService) {
		this.setCrudService(carService);
	}

	@Autowired
	public void setMotorcadeService(MotorcadeService motorcadeService) {
		this.motorcadeService = motorcadeService;
	}
	
	@Autowired
	public void setCarModelService(CarModelService carModelService) {
		this.carModelService = carModelService;
	}

	@Autowired
	public void setOptionService(OptionService optionService) {
		this.optionService = optionService;
	}

	@Override
	public boolean isReadonly() {
		// 车辆管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.car"),
				getText("key.role.bc.admin"));
	}

	@Override
	protected PageOption buildFormPageOption(boolean editable) {
		return super.buildFormPageOption(editable).setWidth(790)
				.setMinWidth(250).setHeight(500).setMinHeight(200);
	}

	@Override
	protected void afterCreate(Car entity) {
		super.afterCreate(entity);
		// 自动生成uid
		this.getE().setUid(this.getIdGeneratorService().next(Car.KEY_UID));

		// 初始化车辆的状态
		this.getE().setStatus(Car.CAR_STAUTS_NORMAL);
		// 车辆表单初始化信息
		this.getE().setLevel("一级");// 车辆定级
		this.getE().setColor("绿灰");// 车辆颜色
		this.getE().setFuelType("汽油"); // 燃料类型 
		// // 设置默认的原归属单位信息
		// this.getE().setOldUnitName(getText("app.oldUnitName"));

		// // 自动生成自编号
		// this.getE().setCode(
		// this.getIdGeneratorService().nextSN4Month(Car.KEY_CODE));

	}

	@Override
	protected void afterOpen(Car entity) {
		if (isReadonly()) {
			this.getE().setCertNo2("******");
		}
	}

	@Override
	protected void beforeSave(Car entity) {
		if (entity.isLogout()) {
			entity.setStatus(Car.CAR_STAUTS_LOGOUT);
			entity.setScrapDate(entity.getReturnDate());
		} else {
			entity.setStatus(Car.CAR_STAUTS_NORMAL);
		}
	}

	@Override
	protected void initForm(boolean editable) {
		super.initForm(editable);

		// 状态列表
		statusesValue = this.getCarStatuses();

		// 加载可选车队列表
		this.motorcadeList = this.motorcadeService.findEnabled4Option();
		Motorcade m = this.getE().getMotorcade();
		if (m != null) {
			OptionItem.insertIfNotExist(this.motorcadeList, m.getId()
					.toString(), m.getName());
		}
		
		// 加载可选车型配置列表
		this.carModelList = this.carModelService.findEnabled4Option();

		// 批量加载可选项列表
		Map<String, List<Map<String, String>>> optionItems = this.optionService
				.findOptionItemByGroupKeys(new String[] {
						OptionConstants.CAR_BUSINESS_NATURE,
						OptionConstants.CAR_RANK, OptionConstants.CAR_BRAND,
						OptionConstants.CAR_FUEL_TYPE,
						OptionConstants.CAR_COLOR,
						OptionConstants.CAR_TAXIMETERFACTORY,
						OptionConstants.CAR_OLD_UNIT_NAME,
						OptionConstants.CAR_LOGOUT_REASON });

		// 加载可选营运性质列表
		this.businessTypeList = optionItems
				.get(OptionConstants.CAR_BUSINESS_NATURE);

		// 加载可选营运性质列表
		this.levelTypeList = optionItems.get(OptionConstants.CAR_RANK);

		// 加载可选厂牌类型列表
		this.factoryTypeList = optionItems.get(OptionConstants.CAR_BRAND);

		// 加载可选燃料类型列表
		this.fuelTypeList = optionItems.get(OptionConstants.CAR_FUEL_TYPE);

		// 加载可选颜色类型列表
		this.colorTypeList = optionItems.get(OptionConstants.CAR_COLOR);

		// 加载可选 计价器制造厂列表
		this.taximeterFactoryTypeList = optionItems
				.get(OptionConstants.CAR_TAXIMETERFACTORY);

		// 所属单位列表
		this.oldUnitList = optionItems.get(OptionConstants.CAR_OLD_UNIT_NAME);
		OptionItem.insertIfNotExist(oldUnitList, null, getE().getOldUnitName());

		// 注销原因列表
		this.logoutReasonList = optionItems
				.get(OptionConstants.CAR_LOGOUT_REASON);
	}

	/**
	 * 状态值转换列表：在案|注销|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getCarStatuses() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(Car.CAR_STAUTS_NORMAL),
				getText("bs.status.active"));
		statuses.put(String.valueOf(Car.CAR_STAUTS_LOGOUT),
				getText("bs.status.logout"));
		statuses.put(" ", getText("bs.status.all"));
		return statuses;
	}

	public JSONArray motorcades;// 车队的下拉列表信息
	public JSONArray units;// 分公司的下拉列表信息

	/**
	 * 高级搜索条件窗口
	 * 
	 * @return
	 * @throws Exception
	 */
	public String conditions() throws Exception {
		// 可选车队列表
		motorcades = new JSONArray();
		JSONObject json;
		for (Map<String, String> map : this.motorcadeService.find4Option(null)) {
			json = new JSONObject();
			json.put("label", map.get("value"));
			json.put("value", map.get("key"));
			motorcades.put(json);
		}

		// 可选分公司列表
		units = new JSONArray();
		for (Map<String, String> map : this.actorService.find4option(
				new Integer[] { Actor.TYPE_UNIT }, (Integer[]) null)) {
			json = new JSONObject();
			json.put("label", map.get("name"));
			json.put("value", map.get("id"));
			units.put(json);
		}
		return SUCCESS;
	}
	
	// ======== 通过factoryModel查找车型配置的相关信息开始 ========
	
	private String factoryModel;
	
	public String getFactoryModel() {
		return factoryModel;
	}

	public void setFactoryModel(String factoryModel) {
		this.factoryModel = factoryModel;
	}

	public String carModelInfo(){
		json = new Json();
		Car car = this.getE();
		CarModel obj = this.carModelService.findcarModelByFactoryModel(factoryModel);
		car.setFactoryType(obj.getFactoryType()); // 厂牌类型
		car.setEngineType(obj.getEngineType());// 发动机类型
		car.setFuelType(obj.getFuelType());
		return "json";
	}
	
	// ======== 通过factoryModel查找车型配置的相关信息结束 ========
}