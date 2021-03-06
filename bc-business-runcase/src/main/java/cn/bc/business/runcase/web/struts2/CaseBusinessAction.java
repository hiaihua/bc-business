/**
 * 
 */
package cn.bc.business.runcase.web.struts2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.business.OptionConstants;
import cn.bc.business.car.domain.Car;
import cn.bc.business.car.service.CarService;
import cn.bc.business.carman.domain.CarMan;
import cn.bc.business.carman.service.CarManService;
import cn.bc.business.motorcade.service.MotorcadeService;
import cn.bc.business.runcase.domain.Case4InfractBusiness;
import cn.bc.business.runcase.domain.CaseBase;
import cn.bc.business.runcase.service.CaseBaseService;
import cn.bc.business.runcase.service.CaseBusinessService;
import cn.bc.business.sync.domain.JiaoWeiYYWZ;
import cn.bc.business.sync.service.JiaoWeiYYWZService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.core.query.condition.Direction;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.core.util.StringUtils;
import cn.bc.docs.web.ui.html.AttachWidget;
import cn.bc.identity.web.SystemContext;
import cn.bc.option.domain.OptionItem;
import cn.bc.option.service.OptionService;
import cn.bc.sync.domain.SyncBase;
import cn.bc.sync.service.SyncBaseService;
import cn.bc.web.ui.html.page.ButtonOption;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.json.Json;
import cn.bc.web.ui.json.JsonArray;
import cn.bc.workflow.service.WorkflowModuleRelationService;

/**
 * 营运违章Action
 * 
 * @author dragon
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class CaseBusinessAction extends
		FileEntityAction<Long, Case4InfractBusiness> {
	// private static Log logger = LogFactory.getLog(CarAction.class);
	private static final long serialVersionUID = 1L;
	public Long carId;
	public Long carManId;
	private Long syncId; // 同步ID

	public boolean isMoreCar;
	public boolean isMoreCarMan;
	public boolean isNullCar;
	public boolean isNullCarMan;

	private CaseBusinessService caseBusinessService;
	private CaseBaseService caseBaseService;
	private MotorcadeService motorcadeService;
	private OptionService optionService;
	private CarManService carManService;
	private CarService carService;
	private SyncBaseService syncBaseService; // 平台同步基类Serivce
	private JiaoWeiYYWZService jiaoWeiYYWZService; // 平台同步交通违章Serivce
	private String sourceStr;

	public List<Map<String, String>> motorcadeList; // 可选车队列表
	public List<Map<String, String>> dutyList; // 可选责任列表
	public List<Map<String, String>> properitesList; // 可选性质列表
	public List<Map<String, String>> degreeList; // 可选程度列表
	public List<Map<String, String>> certList; // 可选没收证件列表
	public List<Map<String, String>> departmentList; // 可选执法机关列表

	public Map<String, String> statusesValue;
	public Map<String, String> sourcesValue;
	public Map<String, String> categoryValue;
	private Map<String, List<Map<String, String>>> allList;

	private WorkflowModuleRelationService workflowModuleRelationService;
	public List<Map<String, Object>> list_WorkflowModuleRelation; // 工作流程集合

	public JSONArray illegalActivityList; // 违法行为集合列
	public AttachWidget attachsUI;

	public Long getCarId() {
		return carId;
	}

	public void setCarId(Long carId) {
		this.carId = carId;
	}

	public Long getCarManId() {
		return carManId;
	}

	public void setCarManId(Long carManId) {
		this.carManId = carManId;
	}

	public Long getSyncId() {
		return syncId;
	}

	public void setSyncId(Long syncId) {
		this.syncId = syncId;
	}

	public String getSourceStr() {
		return sourceStr;
	}

	public void setSourceStr(String sourceStr) {
		this.sourceStr = sourceStr;
	}

	@Autowired
	public void setCaseBusinessService(CaseBusinessService caseBusinessService) {
		this.caseBusinessService = caseBusinessService;
		this.setCrudService(caseBusinessService);
	}

	@Autowired
	public void setMotorcadeService(MotorcadeService motorcadeService) {
		this.motorcadeService = motorcadeService;
	}

	@Autowired
	public void setOptionService(OptionService optionService) {
		this.optionService = optionService;
	}

	@Autowired
	public void setCarManService(CarManService carManService) {
		this.carManService = carManService;
	}

	@Autowired
	public void setCarService(CarService carService) {
		this.carService = carService;
	}

	@Autowired
	public void setSyncBaseService(SyncBaseService syncBaseService) {
		this.syncBaseService = syncBaseService;
	}

	@Autowired
	public void setJiaoWeiYYWZService(JiaoWeiYYWZService jiaoWeiYYWZService) {
		this.jiaoWeiYYWZService = jiaoWeiYYWZService;
	}

	@Autowired
	public void setCaseBaseService(CaseBaseService caseBaseService) {
		this.caseBaseService = caseBaseService;
	}

	@Autowired
	public void setWorkflowModuleRelationService(
			WorkflowModuleRelationService workflowModuleRelationService) {
		this.workflowModuleRelationService = workflowModuleRelationService;
	}

	@Override
	protected OrderCondition getDefaultOrderCondition() {
		return new OrderCondition("status", Direction.Asc).add("fileDate",
				Direction.Desc);
	}

	// 复写搜索URL方法
	protected String getEntityConfigName() {
		return "caseBusiness";
	}

	@Override
	public boolean isReadonly() {
		// 营运违章管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.infractBusiness"),
				getText("key.role.bc.admin"));
	}

	@Override
	protected PageOption buildFormPageOption(boolean editable) {
		return super.buildFormPageOption(editable).setWidth(745)
				.setMinWidth(250).setHeight(450).setMinHeight(200);
	}

	// 显示车辆,司机相关信息临时变量 //
	private String chargers;
	private Calendar birthdate;
	private String origin;
	private Calendar workDate;
	private String businessType;
	private Calendar registerDate;
	private Calendar scrapDate;

	public String getChargers() {
		return chargers;
	}

	public void setChargers(String chargers) {
		this.chargers = chargers;
	}

	public Calendar getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Calendar birthdate) {
		this.birthdate = birthdate;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Calendar getWorkDate() {
		return workDate;
	}

	public void setWorkDate(Calendar workDate) {
		this.workDate = workDate;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public Calendar getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(Calendar registerDate) {
		this.registerDate = registerDate;
	}

	public Calendar getScrapDate() {
		return scrapDate;
	}

	public void setScrapDate(Calendar scrapDate) {
		this.scrapDate = scrapDate;
	}

	@Override
	protected void buildFormPageButtons(PageOption pageOption, boolean editable) {
		boolean readonly = this.isReadonly();

		if (this.useFormPrint()) {
			// 添加打印按钮
			pageOption.addButton(this.getDefaultPrintButtonOption());
		}

		if (!readonly) {
			if (editable) {
				// 添加默认的保存按钮
				pageOption.addButton(this.getDefaultSaveButtonOption());
			} else {
				if (!getE().isNew()) {
					// 维护按钮
					pageOption.addButton(new ButtonOption(getText("维护"), null,
							"bc.caseBusinessForm.doMaintenance"));
				}
			}
			// 特殊处理结案按钮
			if (CaseBase.STATUS_ACTIVE == getE().getStatus() && !getE().isNew()) {
				ButtonOption buttonOption = new ButtonOption(
						getText("label.closefile"), null,
						"bc.caseBusinessForm.doCloseFile");
				buttonOption.put("id", "bcSaveDlgButton");
				pageOption.addButton(buttonOption);
			}
		}
	}

	@SuppressWarnings("static-access")
	@Override
	protected void afterCreate(Case4InfractBusiness entity) {
		super.afterCreate(entity);

		if (syncId != null) { // 判断同步id是否为空
			JiaoWeiYYWZ jiaoweiYYWZ = this.jiaoWeiYYWZService.load(syncId);
			String carPlateNo = "";
			carPlateNo = jiaoweiYYWZ.getCarPlate()
					.replaceAll("粤A.", carPlateNo);
			if (carPlateNo.length() > 0) {
				// 根据车牌号码查找carId
				findCarId(carPlateNo);
			}
			// 设置从交委同步的信息
			this.getE().setCaseNo(jiaoweiYYWZ.getSyncCode());
			this.getE().setHappenDate(jiaoweiYYWZ.getHappenDate());
			this.getE().setCloseDate(jiaoweiYYWZ.getCloseDate());
			this.getE().setDriverCert(jiaoweiYYWZ.getDriverCert());
			this.getE().setOperator(jiaoweiYYWZ.getOperator());
			this.getE().setOperateUnit(jiaoweiYYWZ.getOperateUnit());
			this.getE().setConfiscateCertNo(jiaoweiYYWZ.getConfiscateCertNo());
			this.getE().setPullUnit(jiaoweiYYWZ.getPullUnit());
			this.getE().setArea(jiaoweiYYWZ.getArea());
			if (null != jiaoweiYYWZ.getPenalty()) {
				this.getE().setPenalty(jiaoweiYYWZ.getPenalty());
			}
			this.getE().setBusinessCertNo(jiaoweiYYWZ.getBusinessCertNo());
			this.getE().setAddress(jiaoweiYYWZ.getAddress());
			this.getE().setSubject(jiaoweiYYWZ.getContent());
			this.getE().setReceipt(jiaoweiYYWZ.getReceipt());
			this.getE().setDetain(jiaoweiYYWZ.getDetain());
			String str = jiaoweiYYWZ.getDetain();
			if (null != str && str.length() > 0 && str.lastIndexOf("分") > 0) { // 检查字符串是否存在一个"分"字,如出现多个"分"字取最后一个
				str = str.substring(str.lastIndexOf("分") - 2,
						str.lastIndexOf("分")).trim();
				this.getE().setJeom(Float.parseFloat(str));
			}
			// this.getE().setFrom(getText("runcase.jiaowei"));

			// 设置来源
			this.getE().setSource(CaseBase.SOURCE_GENERATION);
			// 设置syncId
			this.getE().setSyncId(syncId);
		}

		if (carManId != null) {
			CarMan driver = this.carManService.load(carManId);
			List<Car> car = this.carService.selectAllCarByCarManId(carManId);
			if (car.size() == 1) {
				this.getE().setCarId(car.get(0).getId());
				this.getE().setCarPlate(
						car.get(0).getPlateType() + "."
								+ car.get(0).getPlateNo());
				this.getE().setMotorcadeId(car.get(0).getMotorcade().getId());
				this.getE().setMotorcadeName(
						car.get(0).getMotorcade().getName());
				this.getE().setCompany(car.get(0).getCompany());
				this.getE().setCharger(car.get(0).getCharger());

				this.chargers = formatChargers(car.get(0).getCharger());
				this.businessType = car.get(0).getBusinessType();
				this.registerDate = car.get(0).getRegisterDate();
				this.scrapDate = car.get(0).getScrapDate();

			} else if (car.size() > 1) {
				isMoreCar = true;
			} else {
				isNullCar = true;
			}
			this.getE().setDriverId(carManId);
			this.getE().setDriverName(driver.getName());
			this.getE().setDriverCert(driver.getCert4FWZG());

			this.birthdate = driver.getBirthdate();
			this.origin = driver.getOrigin();
			this.workDate = driver.getWorkDate();
		}
		if (carId != null) {
			Car car = this.carService.load(carId);
			this.getE()
					.setCarPlate(car.getPlateType() + "." + car.getPlateNo());
			this.getE().setCarId(carId);
			this.getE().setMotorcadeId(car.getMotorcade().getId());
			this.getE().setMotorcadeName(car.getMotorcade().getName());
			this.getE().setCompany(car.getCompany());
			this.getE().setCharger(car.getCharger());

			this.chargers = formatChargers(car.getCharger());
			this.businessType = car.getBusinessType();
			this.registerDate = car.getRegisterDate();
			this.scrapDate = car.getScrapDate();

			List<CarMan> carMan = this.carManService
					.selectAllCarManByCarId(carId);
			if (carMan.size() == 1) {
				this.getE().setDriverName(carMan.get(0).getName());
				this.getE().setDriverId(carMan.get(0).getId());
				this.getE().setDriverCert(carMan.get(0).getCert4FWZG());

				this.birthdate = carMan.get(0).getBirthdate();
				this.origin = carMan.get(0).getOrigin();
				this.workDate = carMan.get(0).getWorkDate();
			} else if (carMan.size() > 1) {
				isMoreCarMan = true;
			} else {
				isNullCarMan = true;
			}
		}

		// 初始化信息
		this.getE().setUid(
				this.getIdGeneratorService().next(this.getE().ATTACH_TYPE));
		// 自动生成自编号
		this.getE().setCode(
				this.getIdGeneratorService().nextSN4Month(
						Case4InfractBusiness.KEY_CODE));

		SystemContext context = this.getSystyemContext();
		this.getE().setType(CaseBase.TYPE_INFRACT_BUSINESS);
		this.getE().setStatus(CaseBase.STATUS_ACTIVE);
		this.getE().setReceiverId(context.getUserHistory().getId());
		this.getE().setReceiverName(context.getUserHistory().getName());
		this.getE().setCategory(Case4InfractBusiness.CATEGORY_BUSINESS);

		// 来源
		if (syncId == null) { // 不是同步过来的信息设为自建
			this.getE().setSource(CaseBase.SOURCE_SYS);
		}

		sourceStr = getSourceStatuses().get(this.getE().getSource() + "");
	}

	/** 根据车牌号查找carId */
	public void findCarId(String carPlateNo) {
		if (carPlateNo.length() > 0) { // 判断车牌号是否为空
			Long tempCarId = this.carService.findcarIdByCarPlateNo(carPlateNo);
			this.carId = tempCarId;
		}
	}

	@Override
	public String edit() throws Exception {
		if (syncId != null) {// 根据syncId查找已存在CaseBase的记录
			CaseBase cb = this.caseBaseService.findCaseBaseBysyncId(syncId);
			this.setE(this.getCrudService().load(cb.getId()));
		} else {
			this.setE(this.getCrudService().load(this.getId()));
		}

		this.formPageOption = buildFormPageOption(true);

		// 表单可选项的加载
		statusesValue = this.getCaseStatuses();
		sourcesValue = this.getSourceStatuses();
		categoryValue = this.getCategory();
		sourceStr = getSourceStatuses().get(this.getE().getSource() + "");
		initForm(true);

		this.carId = this.getE().getCarId();
		// 设置显示车辆,司机相关信息
		if (carId != null) {
			Car car = this.carService.load(carId);

			this.chargers = formatChargers(this.getE().getCharger());
			this.businessType = car.getBusinessType();
			this.registerDate = car.getRegisterDate();
			this.scrapDate = car.getScrapDate();

			List<CarMan> carMan = this.carManService
					.selectAllCarManByCarId(carId);
			if (carMan.get(0).getBirthdate() != null) {
				this.birthdate = carMan.get(0).getBirthdate();
			}
			this.origin = carMan.get(0).getOrigin();
			this.workDate = carMan.get(0).getWorkDate();
		}

		return "form";
	}

	@Override
	protected void afterOpen(Case4InfractBusiness entity) {
		super.afterOpen(entity);
		sourceStr = getSourceStatuses().get(this.getE().getSource() + "");
	}

	@Override
	public String save() throws Exception {

		SystemContext context = this.getSystyemContext();
		Case4InfractBusiness e = this.getE();
		// 设置最后更新人的信息
		e.setModifier(context.getUserHistory());
		e.setModifiedDate(Calendar.getInstance());

		SyncBase sb = null;
		if (syncId != null) { // 处理相应的来源信息的状态
			sb = this.syncBaseService.load(syncId);
			sb.setStatus(SyncBase.STATUS_GEN);
			this.beforeSave(e);
			// 保存并更新Sycn对象的状态
			this.caseBusinessService.save(e, sb);
			this.afterSave(e);
		} else {
			this.getCrudService().save(e);
		}
		return "saveSuccess";

	}

	@Override
	protected void initForm(boolean editable) throws Exception {
		super.initForm(editable);

		statusesValue = this.getCaseStatuses();
		sourcesValue = this.getSourceStatuses();
		categoryValue = this.getCategory();
		// 表单可选项的加载
		initSelects();

		if (!this.getE().isNew()) {
			list_WorkflowModuleRelation = this.workflowModuleRelationService
					.findList(this.getE().getId(),
							Case4InfractBusiness.class.getSimpleName(),
							new String[] { "subject" });
		}

		// 加载可选的违法行为
		if (editable) {
			List<Map<String, String>> _illegalList = new ArrayList<Map<String, String>>();
			_illegalList.addAll(this.allList
					.get(OptionConstants.CA_BS_ILLEGALACTIVITY));
			_illegalList.addAll(this.allList
					.get(OptionConstants.CA_SV_ILLEGALACTIVITY));
			this.illegalActivityList = OptionItem.toLabelValues(_illegalList);
		}

		// 构建附件
		this.attachsUI = this.buildAttachsUI(this.getE().isNew(), (!editable)||this
				.isReadonly(), Case4InfractBusiness.ATTACH_TYPE, this.getE()
				.getUid());
	}

	public String selectCarMansInfo() {
		List<CarMan> drivers = this.carManService.selectAllCarManByCarId(carId);
		JsonArray jsons = new JsonArray();
		Json o;
		for (CarMan driver : drivers) {
			o = new Json();
			o.put("name", driver.getName());
			o.put("id", driver.getId());
			o.put("cert4FWZG", driver.getCert4FWZG());
			o.put("origin", driver.getOrigin());
			o.put("birthDate", calendarToString(driver.getBirthdate()));
			o.put("workDate", calendarToString(driver.getWorkDate()));
			// o.put("region", driver.getRegion());
			// o.put("drivingStatus", driver.getDrivingStatus());
			jsons.add(o);
		}
		json = jsons.toString();
		return "json";
	}

	// 表单可选项的加载
	public void initSelects() throws Exception {
		// 加载可选车队列表
		this.motorcadeList = this.motorcadeService.findEnabled4Option();
		if (this.getE().getMotorcadeId() != null)
			OptionItem.insertIfNotExist(this.motorcadeList, this.getE()
					.getMotorcadeId().toString(), this.getE()
					.getMotorcadeName());

		this.allList = this.optionService
				.findOptionItemByGroupKeys(new String[] {
						OptionConstants.IT_DUTY, OptionConstants.IT_PROPERITES,
						OptionConstants.IT_DEGREE, OptionConstants.BS_CERT,
						OptionConstants.CA_DEPARTMENT,
						OptionConstants.CA_BS_ILLEGALACTIVITY,
						OptionConstants.CA_SV_ILLEGALACTIVITY });
		// 加载可选责任列表
		this.dutyList = this.allList.get(OptionConstants.IT_DUTY);
		// 加载可选性质列表
		this.properitesList = this.allList.get(OptionConstants.IT_PROPERITES);
		// 加载可选程度列表
		this.degreeList = this.allList.get(OptionConstants.IT_DEGREE);
		// 加载可选没收证件列表
		this.certList = this.allList.get(OptionConstants.BS_CERT);
		// 加载可选执法机关列表
		this.departmentList = this.allList.get(OptionConstants.CA_DEPARTMENT);

	}

	/**
	 * 获取Entity的状态值转换列表
	 * 
	 * @return
	 */
	protected Map<String, String> getCaseStatuses() {
		Map<String, String> statuses = new HashMap<String, String>();
		statuses.put(String.valueOf(CaseBase.STATUS_ACTIVE),
				getText("runcase.select.status.active"));
		statuses.put(String.valueOf(CaseBase.STATUS_CLOSED),
				getText("runcase.select.status.closed"));
		return statuses;
	}

	/**
	 * 获取Entity的来源转换列表
	 * 
	 * @return
	 */
	protected Map<String, String> getSourceStatuses() {
		Map<String, String> statuses = new HashMap<String, String>();
		statuses.put(String.valueOf(CaseBase.SOURCE_SYS),
				getText("runcase.select.source.sys"));
		statuses.put(String.valueOf(CaseBase.SOURCE_SYNC),
				getText("runcase.select.source.sync.auto"));
		statuses.put(String.valueOf(CaseBase.SOURCE_GENERATION),
				getText("runcase.select.source.sync.gen"));
		return statuses;
	}

	/**
	 * 获取Entity的违章类别转换列表
	 * 
	 * @return
	 */
	protected Map<String, String> getCategory() {
		Map<String, String> statuses = new HashMap<String, String>();
		statuses.put(String.valueOf(Case4InfractBusiness.CATEGORY_BUSINESS),
				getText("runcase.category.business"));
		statuses.put(String.valueOf(Case4InfractBusiness.CATEGORY_STATION),
				getText("runcase.category.station"));
		statuses.put(String.valueOf(Case4InfractBusiness.CATEGORY_SERVICE),
				getText("runcase.category.service"));
		return statuses;
	}

	/**
	 * 格式化日期
	 * 
	 * @return
	 */
	public String calendarToString(Calendar object) {
		if (null != object && object.toString().length() > 0) {
			Calendar calendar = object;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String dateStr = df.format(calendar.getTime());
			return dateStr;
		} else {
			return "";
		}
	}

	/**
	 * 组装责任人姓名
	 * 
	 * @param chargers
	 * @return
	 */
	public String formatChargers(String chargersStr) {
		String chargers = "";
		if (null != chargersStr && chargersStr.trim().length() > 0) {
			String[] chargerAry = chargersStr.split(";");
			for (int i = 0; i < chargerAry.length; i++) {
				chargers += chargerAry[i].split(",")[0];
				if ((i + 1) < chargerAry.length)
					chargers += ",";
			}
		}
		return chargers;
	}

	// ---发起流程---开始---
	public String tdIds;

	public String startFlow() throws Exception {
		Json json = new Json();
		// 去掉最后一个逗号
		String[] _ids = tdIds.substring(0, tdIds.lastIndexOf(",")).split(",");
		List<Map<String, String>> processValue = this.caseBusinessService
				.doStartFlow(
						getText("runcase.startFlow.key4InfractBusinessHandle"),
						StringUtils.stringArray2LongArray(_ids));
		json.put("success", true);
		json.put("msg", getText("runcase.startFlow.success.true"));

		JSONArray _processValue = OptionItem.toLabelValues(processValue);

		json.put("processInfo", _processValue);

		this.json = json.toString();
		return "json";
	}

	// --流程结束---

}
