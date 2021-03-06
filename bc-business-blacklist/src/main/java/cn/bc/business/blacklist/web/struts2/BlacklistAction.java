/**
 * 
 */
package cn.bc.business.blacklist.web.struts2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.BCConstants;
import cn.bc.business.OptionConstants;
import cn.bc.business.blacklist.domain.Blacklist;
import cn.bc.business.blacklist.service.BlacklistService;
import cn.bc.business.car.domain.Car;
import cn.bc.business.car.service.CarService;
import cn.bc.business.carman.domain.CarMan;
import cn.bc.business.carman.service.CarByDriverService;
import cn.bc.business.carman.service.CarManService;
import cn.bc.business.motorcade.domain.Motorcade;
import cn.bc.business.motorcade.service.MotorcadeService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.core.util.DateUtils;
import cn.bc.identity.domain.Actor;
import cn.bc.identity.service.ActorService;
import cn.bc.identity.web.SystemContext;
import cn.bc.option.domain.OptionItem;
import cn.bc.option.service.OptionService;
import cn.bc.web.ui.html.page.ButtonOption;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.json.Json;

/**
 * 黑名单Action
 * 
 * @author dragon
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class BlacklistAction extends FileEntityAction<Long, Blacklist> {
	// private static Log logger = LogFactory.getLog(ContractAction.class);
	private static final long serialVersionUID = 1L;
	public BlacklistService blacklistService;
	private MotorcadeService motorcadeService;
	public Long carManId;
	public OptionService optionService;
	public List<Map<String, String>> motorcadeList; // 可选车队列表
	public List<Map<String, String>> blackLevelList;// 黑名单等级列表
	public List<Map<String, String>> blackTypeList;// 黑名单限制项目
	public List<Map<String, String>> relatedDepartmenntsList;// 相关部门列表
	public CarByDriverService carByDriverService;
	public String carPlate;// 车牌号码
	public String unitName;// 车辆所属单位名
	public String motorcadeName;// 车辆所属车队名
	public CarManService carManService;
	private CarService carService;
	public Long carId;
	public Long unitId;
	public Long motorcadeId;
	public Map<String, String> statusesValue;
	public boolean isMoreCar;// 标识是否一个司机对应有多辆车
	public boolean isMoreCarMan;// 标识是否一辆车对应多个司机
	public boolean isNullCar;// 标识是否没有车和司机对应
	public boolean isNullCarMan;// 标识是否没有司机和车对应
	public List<Map<String, String>> driversInfoList; // 司机责任人Map
	private ActorService actorService;

	@Autowired
	public void setActorService(
			@Qualifier("actorService") ActorService actorService) {
		this.actorService = actorService;
	}

	public Long getCarManId() {
		return carManId;
	}

	public void setCarManId(Long carManId) {
		this.carManId = carManId;
	}

	public Long getCarId() {
		return carId;
	}

	public void setCarId(Long carId) {
		this.carId = carId;
	}

	@Autowired
	public void setMotorcadeService(MotorcadeService motorcadeService) {
		this.motorcadeService = motorcadeService;
	}

	@Autowired
	public void CarService(CarService carService) {
		this.carService = carService;
	}

	@Autowired
	public void setCarManService(CarManService carManService) {
		this.carManService = carManService;
	}

	@Autowired
	public void setCarByDriverService(CarByDriverService carByDriverService) {
		this.carByDriverService = carByDriverService;
	}

	@Autowired
	public void setOptionService(OptionService optionService) {
		this.optionService = optionService;
	}

	@Autowired
	public void setBlacklistService(BlacklistService blacklistService) {
		this.blacklistService = blacklistService;
		this.setCrudService(blacklistService);
	}

	@Override
	public boolean isReadonly() {
		// 黑名单管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.blacklist"),
				getText("key.role.bc.admin"));
		// }
	}

	@Override
	protected void afterCreate(Blacklist entity) {
		super.afterCreate(entity);
		SystemContext context = this.getSystyemContext();

		if (carManId != null) {
			// 如果司机Id不为空，加载司机信息
			// CarMan driver = this.carManService.load(carManId);
			List<Car> car = this.carService.selectAllCarByCarManId(carManId);
			if (car.size() == 1) {
				this.getE().setCar(car.get(0));
				this.getE().setCompany(car.get(0).getCompany());
				this.getE().setMotorcade(car.get(0).getMotorcade());
			} else if (car.size() > 1) {
				isMoreCar = true;
			} else {
				isNullCar = true;
			}
			String carMan = this.carManService
					.getDriverInfoByDriverId(carManId);
			this.getE().setDrivers(carMan);
			this.driversInfoList = formatDrivers(carMan);

		}

		if (carId != null) {
			// 如果车辆Id不为空，加载车辆信息
			Car car = this.carService.load(carId);
			this.getE().setCar(car);
			this.getE().setCompany(car.getCompany());
			this.getE().setMotorcade(car.getMotorcade());
			List<CarMan> carMan = this.carManService
					.selectAllCarManByCarId(carId);
			if (carMan.size() == 1) {
				String carManInfo = this.carManService
						.getDriverInfoByDriverId(carMan.get(0).getId());
				this.getE().setDrivers(carManInfo);
				this.driversInfoList = formatDrivers(carManInfo);

			} else if (carMan.size() > 1) {
				isMoreCarMan = true;
			} else {
				isNullCarMan = true;
			}
		}
		// 设置状态为锁定
		entity.setStatus(Blacklist.STATUS_LOCK);

		// 设置锁定人
		this.getE().setLocker(context.getUser());
		this.getE().setLockDate(Calendar.getInstance());
		this.getE().setCode(
				this.getIdGeneratorService().nextSN4Month(Blacklist.KEY_CODE));
		this.getE().setRelatedDepartmenntsId(context.getBelong().getId());

	}

	// 司机的Id
	public String blacklistDriverIds;

	@Override
	protected void beforeSave(Blacklist entity) {
		super.beforeSave(entity);
		SystemContext context = this.getSystyemContext();
		// 解决object references an unsaved transient instance 错误，（表单隐藏域以新建一个
		// 对象,但没有保存相关的数据而出现的错误）
		Blacklist e = this.getE();
		if (e.getUnlocker() != null && e.getUnlocker().getId() == null) {
			e.setUnlocker(null);
		}

		// 设置最后更新人的信息
		if (this.getE().getStatus() == Blacklist.STATUS_LOCK) {
			this.getE().setModifier(context.getUserHistory());
			this.getE().setModifiedDate(Calendar.getInstance());
			this.getE().setUnlocker(null);
			this.getE().setUnlockDate(null);
		} else {
			this.getE().setUnlocker(context.getUser());
			this.getE().setUnlockDate(Calendar.getInstance());
		}

		// // 司机与黑名单的关系[非新建]
		if (!this.getE().isNew()) {
			blacklistDriverIds = this.getDriversId(this.getE().getDrivers());

			Set<CarMan> carMans = null;
			if (this.blacklistDriverIds != null
					&& this.blacklistDriverIds.length() > 0) {
				carMans = new HashSet<CarMan>();
				String[] driverIds = this.blacklistDriverIds.split(",");
				CarMan carMan;
				for (String driverId : driverIds) {
					carMan = new CarMan();
					carMan.setId(new Long(driverId));
					carMans.add(carMan);
				}
			}
			if (this.getE().getCarMan() != null) {
				this.getE().getCarMan().clear();
				this.getE().getCarMan().addAll(carMans);
			} else {
				this.getE().setCarMan(carMans);
			}
		}

	}

	@Override
	protected void afterEdit(Blacklist entity) {
		super.afterEdit(entity);

		// 组装司机
		this.driversInfoList = this.formatDrivers(this.getE().getDrivers());
		// 加载可选车队列表
		this.motorcadeList = this.motorcadeService.findEnabled4Option();
		if (this.getE().getMotorcade() != null) {
			Long motorcadeId = this.getE().getMotorcade().getId();
			if (motorcadeId != null) {
				Motorcade m = this.motorcadeService.load(motorcadeId);
				if (m != null) {
					OptionItem.insertIfNotExist(this.motorcadeList, m.getId()
							.toString(), m.getName());
				}
			}
		} else {
			this.motorcadeList = this.motorcadeService.find4Option(null);
		}
		// 编辑时设置解锁人为登录用户
		SystemContext context = this.getSystyemContext();
		if (this.getE().getStatus() == Blacklist.STATUS_LOCK) {
			this.getE().setUnlockDate(Calendar.getInstance());
			this.getE().setUnlocker(context.getUser());
		}

	}

	@Override
	protected PageOption buildFormPageOption(boolean editable) {
		return super.buildFormPageOption(editable).setWidth(680)
				.setMinWidth(250).setMinHeight(300);
	}

	@Override
	protected void buildFormPageButtons(PageOption pageOption, boolean editable) {
		boolean readonly = this.isReadonly();

		if (this.useFormPrint()) {
			// 添加打印按钮
			pageOption.addButton(this.getDefaultPrintButtonOption());
		}

		if (editable && !readonly) {
			// 新建时为显示锁定按钮
			if (isReadonly() == false && this.getE().isNew()) {
				pageOption.addButton(new ButtonOption(
						getText("blacklist.locker"), null,
						"bc.business.blacklistForm.lcoker"));
				// 状态为锁定时，只显示解锁按钮
			} else if (isReadonly() == false
					&& this.getE().getStatus() == Blacklist.STATUS_LOCK) {
				pageOption.addButton(new ButtonOption(
						getText("blacklist.unlocker"), null,
						"bc.business.blacklistForm.unlcoker"));
				// 添加默认的保存按钮
				pageOption.addButton(this.getDefaultSaveButtonOption());
			}

		}
	}

	// 返回所选司机的信息
	public String carManMess() {
		Car car = this.carByDriverService
				.selectCarByCarManId(new Long(carManId));
		if (car != null) {
			carId = car.getId();
			motorcadeId = car.getMotorcade().getId();
			carPlate = car.getPlateType() + car.getPlateNo();
			unitName = car.getCompany();
			motorcadeName = car.getMotorcade().getName();
		}
		Json json = new Json();
		json.put("carId", carId);
		json.put("carPlate", carPlate);
		json.put("motorcadeId", motorcadeId);
		json.put("unitName", unitName);
		json.put("motorcadeName", motorcadeName);
		this.json = json.toString();
		return "json";

	}

	@Override
	protected void initForm(boolean editable) throws Exception {
		super.initForm(editable);
		Date startTime = new Date();
		// 状态列表
		statusesValue = this.getBLStatuses();

		// 加载可选车队列表
		this.motorcadeList = this.motorcadeService.findEnabled4Option();
		if (carId != null) {
			Motorcade m = this.carService.load(carId).getMotorcade();
			if (m != null) {
				OptionItem.insertIfNotExist(this.motorcadeList, m.getId()
						.toString(), m.getName());
			}

		}
		// 批量加载可选项列表
		Map<String, List<Map<String, String>>> optionItems = this.optionService
				.findOptionItemByGroupKeys(new String[] {
						OptionConstants.CARMAN_LEVEL,
						OptionConstants.BLACKLIST_TYPE, });

		// 加载黑名单等级列表
		this.blackLevelList = optionItems.get(OptionConstants.CARMAN_LEVEL);
		// 加载黑名单限制项目
		this.blackTypeList = optionItems.get(OptionConstants.BLACKLIST_TYPE);
		// 组装司机
		this.driversInfoList = this.formatDrivers(this.getE().getDrivers());
		// 相关部门列表
		relatedDepartmenntsList = this.actorService.find4option(new Integer[] {
				Actor.TYPE_UNIT, Actor.TYPE_DEPARTMENT },
				new Integer[] { BCConstants.STATUS_ENABLED });
		if (logger.isInfoEnabled())
			logger.info("findOptionItem耗时：" + DateUtils.getWasteTime(startTime));

	}

	/**
	 * 状态值转换列表：锁定|解锁|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getBLStatuses() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(Blacklist.STATUS_LOCK),
				getText("blacklist.locker"));
		statuses.put(String.valueOf(Blacklist.STATUS_UNLOCK),
				getText("blacklist.unlocker"));
		statuses.put("", getText("bs.status.all"));

		return statuses;
	}

	/**
	 * 组装司机姓名
	 * 
	 * @param drivers
	 * @return
	 */
	public List<Map<String, String>> formatDrivers(String driversStr) {
		String name = "";
		String classes = "";
		String id = "";
		List<Map<String, String>> driversInfo = new ArrayList<Map<String, String>>();
		Map<String, String> driver;
		if (null != driversStr && driversStr.trim().length() > 0) {
			String[] driverAry = driversStr.split(";");
			for (int i = 0; i < driverAry.length; i++) {
				driver = new HashMap<String, String>();
				name = driverAry[i].split(",")[0];
				classes = driverAry[i].split(",")[1];
				id = driverAry[i].split(",")[2];
				driver.put("id", id);
				driver.put("name", name);
				driver.put("classes", classes);
				driversInfo.add(driver);
			}

		}
		return driversInfo;
	}

	/**
	 * 获取司机的ID
	 * 
	 * @param drivers
	 * @return
	 */
	public String getDriversId(String driversStr) {
		String driversId = "";
		if (null != driversStr && driversStr.trim().length() > 0) {
			String[] driverAry = driversStr.split(";");
			for (int i = 0; i < driverAry.length; i++) {
				driversId += driverAry[i].split(",")[2];
				if ((i + 1) < driverAry.length)
					driversId += ",";
			}
		}
		return driversId;
	}

}