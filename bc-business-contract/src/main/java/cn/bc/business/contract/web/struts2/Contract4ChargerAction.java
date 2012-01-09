/**
 * 
 */
package cn.bc.business.contract.web.struts2;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.business.OptionConstants;
import cn.bc.business.contract.domain.Contract;
import cn.bc.business.contract.domain.Contract4Charger;
import cn.bc.business.contract.service.Contract4ChargerService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.docs.service.AttachService;
import cn.bc.docs.web.ui.html.AttachWidget;
import cn.bc.identity.web.SystemContext;
import cn.bc.option.domain.OptionItem;
import cn.bc.option.service.OptionService;
import cn.bc.web.ui.html.page.ButtonOption;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.json.Json;

/**
 * 经济合同Action
 * 
 * @author wis.ho
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class Contract4ChargerAction extends FileEntityAction<Long, Contract4Charger> {
	// private static Log logger = LogFactory.getLog(ContractAction.class);
	private static final long 			serialVersionUID 			= 1L;
	private Contract4ChargerService 	contract4ChargerService;
//	private Contract4LabourService   	contract4LabourService;
	private AttachService 				attachService;
	private OptionService				optionService;
	private	Long						carId; 
	public	Long						carManId;								
	public 	AttachWidget 				attachsUI;
	
	public 	Map<String,String>			statusesValue;
	public	Map<String,String>			chargerInfoMap;							//责任人Map
	
	public  List<Map<String, String>>	signTypeList;							//可选签约类型
	public  List<Map<String, String>>	businessTypeList;						//可选营运性质列表
	public	String						assignChargerIds;						//多个责任人Id
	public  String						assignChargerNames;						//多个责任人name
	public  String []					chargerNameAry;							
	public  Map<String,Object>	 		carInfoMap;								//车辆Map
	public 	boolean 					isExistContract;						// 是否存在合同
	public 	Json 						json;
//	public	Long 					carManId;
//	public  String 					certCode;
//	public 	ContractService 		contractService;

//	@Autowired
//	public void setContractService(ContractService contractService) {
//		this.contractService = contractService;
//	}
	
	@Autowired
	public void setContract4ChargerService(Contract4ChargerService contract4ChargerService) {
		this.contract4ChargerService = contract4ChargerService;
		this.setCrudService(contract4ChargerService);
	}

//	@Autowired
//	public void setContract4LabourService(Contract4LabourService contract4LabourService) {
//		this.contract4LabourService = contract4LabourService;
//	}
	
	
	@Autowired
	public void setAttachService(AttachService attachService) {
		this.attachService = attachService;
	}

	@Autowired
	public void setOptionService(OptionService optionService) {
		this.optionService = optionService;
	}
	
	public Long getCarId() {
		return carId;
	}

	public void setCarId(Long carId) {
		this.carId = carId;
	}
	
	@Override
	public boolean isReadonly() {
		// 经济合同管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.contract4charger"),
				getText("key.role.bc.admin"));
	}
	
	@Override
	protected void afterCreate(Contract4Charger entity){
		
		super.afterCreate(entity);
		if(carId != null){
			//查找此车辆是否存在经济合同,若存在前台提示
			isExistContract = this.contract4ChargerService.isExistContract(carId);
			if(isExistContract == false){
				//根据carId查找车辆的车牌号码
				carInfoMap = this.contract4ChargerService.findCarByCarId(carId);
				entity.setExt_str1(isNullObject(carInfoMap.get("plate_type")+"."+carInfoMap.get("plate_no")));
			}
		}
		
		if(carManId != null){
			//根据carManId查找车辆的车牌号码
			carInfoMap = this.contract4ChargerService.findCarByCarManId(carManId);
			entity.setExt_str1(isNullObject(carInfoMap.get("plate_type")+"."+carInfoMap.get("plate_no")));
			carId = Long.valueOf(isNullObject(carInfoMap.get("id")));
		}
		
		
		entity.setPatchNo(this.getIdGeneratorService().next(Contract4Charger.KEY_UID));
		entity.setOpType(Contract.OPTYPE_CREATE);
		entity.setMain(Contract.MAIN_NOW);
		entity.setVerMajor(Contract.MAJOR_DEFALUT);
		entity.setVerMinor(Contract.MINOR_DEFALUT);
		entity.setUid(this.getIdGeneratorService().next(Contract4Charger.KEY_UID));
		entity.setCode(this.getIdGeneratorService().nextSN4Month(Contract4Charger.KEY_CODE));
		entity.setType(Contract.TYPE_CHARGER);
		entity.setStatus(Contract.STATUS_NORMAL);
	}
	
	@Override
	public String save() throws Exception{
		SystemContext context = this.getSystyemContext();
		Contract4Charger e = this.getE();
		this.beforeSave(e);
		//设置最后更新人的信息
		e.setFileDate(Calendar.getInstance());
		e.setModifier(context.getUserHistory());
		e.setModifiedDate(Calendar.getInstance());
		//设置责任人姓名
		e.setExt_str2(assignChargerNames);
		
		this.contract4ChargerService.save(e,this.getCarId(),
				assignChargerIds,assignChargerNames);

		this.afterSave(e);
//		//保存证件与车辆的关联表信息
//		this.contract4ChargerService.carNContract4Save(carId,getE().getId());
//		
//		//保存证件与责任人的关联表信息
//		this.contract4ChargerService.carMansNContract4Save(assignChargerIds, getE().getId());
//		//更新车辆的chager列显示责任人姓名
//		this.contract4ChargerService.updateCar4dirverName(assignChargerNames,carId);
//		//更新司机的chager列显示责任人姓名
//		this.contract4ChargerService.updateCarMan4dirverName(assignChargerNames,carId);
		
		
		return "saveSuccess";
		
	}
	
	@Override
	protected void afterEdit(Contract4Charger e) {
		// == 合同维护处理
		setChargerList(e);
		// 构建附件控件
		attachsUI = buildAttachsUI(false, false);

		// 将次版本号加1
		if(e.getVerMinor() != null){
			e.setVerMinor(e.getVerMinor() + 1);
		}

		// 操作类型设置为维护
		e.setOpType(Contract.OPTYPE_MAINTENANCE);
	}
	
	@Override
	protected void afterOpen(Contract4Charger e) {
		// == 合同维护处理
		setChargerList(e);
		// 构建附件控件
		attachsUI = buildAttachsUI(false, true);
	}

	@Override
	protected void initForm(boolean editable) {
		super.initForm(editable);
		
		// 状态列表
		statusesValue		=	this.getEntityStatuses();
		// 表单可选项的加载
		initSelects();
	}
	
	private void setChargerList(Contract4Charger e){
		//根据contractId查找所属的carId
		carId = this.contract4ChargerService.findCarIdByContractId(e.getId());
		//根据contractId查找所属的carManId列表
		List<String> chargerIdList = this.contract4ChargerService.findChargerIdByContractId(e.getId());
		if((chargerIdList != null && chargerIdList.size() > 0) && (e.getExt_str2() != null && e.getExt_str2().length() > 0)){
			chargerNameAry = this.getE().getExt_str2().split(",");
			chargerInfoMap = new HashMap<String, String>();
			for(int i=0; i<chargerIdList.size(); i++){
				chargerInfoMap.put(chargerIdList.get(i), chargerNameAry[i]);
			}
		}
	}
	
	/** 判断指定的车辆是否已经存在经济合同 */
	public String isExistContract() {
		json = new Json();
		json.put("isExistContract",
				this.contract4ChargerService.isExistContract(carId));
		return "json";
	}
	
	
	
//	private void dealCharger4Save() {
//		Set<CarMan> chargers = null;
//		if(this.assignChargerIds != null && this.assignChargerIds.length() > 0){
//			chargers = new HashSet<CarMan>();
//			String[] chargerIds = this.assignChargerIds.split(",");
//			CarMan carMan;
//			for(String cid : chargerIds){
//				carMan = new CarMan();
//				carMan.setId(new Long(cid));
//				chargers.add(carMan);
//			}
//		}
//		if(this.getE().getChargers() != null){
//			this.getE().getChargers().clear();
//			this.getE().getChargers().addAll(chargers);
//		}else{
//			this.getE().setChargers(chargers);
//		}
//	}

	private AttachWidget buildAttachsUI(boolean isNew, boolean forceReadonly) {
		// 构建附件控件
		String ptype = "contract4Charger.main";
		AttachWidget attachsUI = new AttachWidget();
		attachsUI.setFlashUpload(isFlashUpload());
		attachsUI.addClazz("formAttachs");
		if (!isNew)
			attachsUI.addAttach(this.attachService.findByPtype(ptype, this
					.getE().getUid()));
		attachsUI.setPuid(this.getE().getUid()).setPtype(ptype);

		// 上传附件的限制
		attachsUI.addExtension(getText("app.attachs.extensions"))
				.setMaxCount(Integer.parseInt(getText("app.attachs.maxCount")))
				.setMaxSize(Integer.parseInt(getText("app.attachs.maxSize")));

		// 只读控制
		attachsUI.setReadOnly(forceReadonly ? true : this.isReadonly());
		return attachsUI;
	}

	@Override
	protected PageOption buildFormPageOption(boolean editable) {
		return super.buildFormPageOption(editable).setWidth(748).setMinWidth(250)
				.setMinHeight(160).setHeight(450);
		//option.addButton(new ButtonOption(getText("label.save"), "save"));
//		if (!this.isReadonly()) {
//			option.addButton(new ButtonOption(getText("label.save"), null, "bc.contractChargerForm.save"));
//		}
//		return option;
	}
	
	@Override
	protected void buildFormPageButtons(PageOption pageOption, boolean editable) {
		// 特殊处理的部分
		if (!this.isReadonly()) {// 有权限
			if (editable) {// 编辑状态显示保存按钮
				pageOption.addButton(new ButtonOption(getText("label.save"),
						null, "bc.contract4ChargerForm.save"));
			} else {// 只读状态显示操作按钮
				if(this.getE().getMain() == Contract.MAIN_NOW){
					pageOption.addButton(new ButtonOption(
							getText("contract4Labour.optype.maintenance"), null,
							"bc.contract4ChargerForm.doMaintenance"));
					pageOption.addButton(new ButtonOption(
							getText("contract4Labour.optype.renew"), null,
							"bc.contract4ChargerForm.doRenew"));
				}
			}
		}
	}


	
	// 表单可选项的加载
	public void initSelects(){
		
		// 批量加载可选项列表
		Map<String, List<Map<String, String>>> optionItems = this.optionService
				.findOptionItemByGroupKeys(new String[] {
						OptionConstants.CONTRACT_SIGNTYPE,
						OptionConstants.CAR_BUSINESS_NATURE,
			});
		
		// 加载可选签约类型
		this.signTypeList		=	 optionItems.get(OptionConstants.CONTRACT_SIGNTYPE);
		if(!this.getE().isNew() && this.getE().getSignType().equals(getText("contract4Labour.optype.renew"))){
			OptionItem.insertIfNotExist(signTypeList, getText("contract4Labour.optype.renew"), getText("contract4Labour.optype.renew"));
		}
		// 加载可选营运性质列表
		this.businessTypeList	=	 optionItems.get(OptionConstants.CAR_BUSINESS_NATURE);

	}
	
	
	/**
	 * 获取Contract的合同类型列表
	 * 
	 * @return
	 */
	protected Map<String, String> getEntityTypes() {
		Map<String, String> types = new HashMap<String, String>();
		types.put(String.valueOf(Contract.TYPE_LABOUR),
				getText("contract.select.labour"));
		types.put(String.valueOf(Contract.TYPE_CHARGER),
				getText("contract.select.charger"));
		return types;
	}

    public String isNullObject(Object obj){
    	if(null != obj){
    		return obj.toString();
    	}else{
    		return "";
    	}
    }
}