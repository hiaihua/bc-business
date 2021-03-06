/**
 * 
 */
package cn.bc.business.cert.web.struts2;

import java.util.Calendar;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.BCConstants;
import cn.bc.business.cert.domain.Cert;
import cn.bc.business.cert.domain.Cert4Driving;
import cn.bc.business.cert.service.CertDrivingService;
import cn.bc.business.cert.service.CertService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.docs.service.AttachService;
import cn.bc.docs.web.ui.html.AttachWidget;
import cn.bc.identity.web.SystemContext;
import cn.bc.web.ui.html.page.PageOption;

/**
 * 机动车驾驶证Action
 * 
 * @author wis.ho
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class CertDrivingAction extends FileEntityAction<Long, Cert4Driving> {
	// private static Log logger = LogFactory.getLog(CertIdentityAction.class);
	private static final long 		    serialVersionUID		= 1L;                       
	public 	CertDrivingService			certDrivingService;    
	public 	CertService					certService;
	private AttachService 			    attachService;                                          
	public 	AttachWidget 			    attachsUI;                                              
	public 	Map<String,String>		    statusesValue;                                          
	public	Long					    carManId;                                               
	public  Map<String,Object>	 		carManMap;
	public  Map<String,Object>	 		carManMessMap;
	
	@Autowired
	public void setCertDrivingService(CertDrivingService certDrivingService) {
		this.certDrivingService = certDrivingService;
		this.setCrudService(certDrivingService);
	}
	
	@Override
	public boolean isReadonly() {
		// 司机证件管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.cert4driver"),
				getText("key.role.bc.admin"));
	}
	
	@Autowired
	public void setCertService(CertService certService) {
		this.certService = certService;
	}


	@Autowired
	public void setAttachService(AttachService attachService) {
		this.attachService = attachService;
	}
	
	public Long getCarManId() {
		return carManId;
	}

	public void setCarManId(Long carManId) {
		this.carManId = carManId;
	}
	

	@SuppressWarnings("static-access")
	public String create() throws Exception {
		String r = super.create();

		if(carManId != null){
			carManMap = this.certService.findCarManByCarManId(carManId);
			this.getE().setName(isNullObject(carManMap.get("name")));
			this.getE().setCertCode(isNullObject(carManMap.get("cert_fwzg")));
		}
		
		this.getE().setUid(this.getIdGeneratorService().next(this.getE().ATTACH_TYPE));
		// 自动生成自编号
		this.getE().setCertCode(
				this.getIdGeneratorService().nextSN4Month(Cert4Driving.KEY_CODE));
		this.getE().setType(Cert.TYPE_DRIVING);
		this.getE().setStatus(BCConstants.STATUS_ENABLED);
		statusesValue		=	this.getEntityStatuses();
		
		attachsUI = buildAttachsUI(true,false);
		return r;
	}
	
	@Override
	public String edit() throws Exception {
		this.setE(this.getCrudService().load(this.getId()));
		
		this.formPageOption = 	buildFormPageOption(false);
		statusesValue		=	this.getEntityStatuses();
		
		//根据certId查找carMan信息
		carManMessMap = this.certService.findCarManMessByCertId(this.getId());
		carManId = Long.valueOf(carManMessMap.get("id")+"");
		this.getE().setName(carManMessMap.get("name")+"");
		
		// 构建附件控件
		attachsUI = buildAttachsUI(false,false);
		return "form";
	}
	

	@Override
	public String save() throws Exception{
		SystemContext context = this.getSystyemContext();
		
		//设置最后更新人的信息
		Cert4Driving e = this.getE();
		
		//计算有效期
//		Date date = e.getStartDate().getTime();
//		Calendar resultDate = Calendar.getInstance();
//		date = DateUtils.addYears(date, Integer.valueOf(e.getValidFor()));
//		resultDate.setTime(date);
//		e.setEndDate(resultDate);
		
		if(null != e && (e.getStartDate() != null && e.getValidFor() != null)){
			Calendar result = Calendar.getInstance();
			result.setTime(e.getStartDate().getTime());
			result.add(Calendar.YEAR, Integer.valueOf(e.getValidFor()));
			
			e.setEndDate(result);
		}
		
		e.setModifier(context.getUserHistory());
		e.setModifiedDate(Calendar.getInstance());
		
		this.getCrudService().save(e);
		
		//保存证件与司机的关联表信息
		if(carManId != null){
			this.certService.carManNCert4Save(carManId,getE().getId());
		}
		
		return "saveSuccess";
	}
	
	
	private AttachWidget buildAttachsUI(boolean isNew, boolean forceReadonly) {
		// 构建附件控件
		String ptype = "certDriving.main";
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
		return super.buildFormPageOption(editable).setWidth(740).setHeight(400);
	}

	@Override
	protected OrderCondition getDefaultOrderCondition() {
		return null;// new OrderCondition("fileDate", Direction.Desc);
	}


    public String isNullObject(Object obj){
    	if(null != obj){
    		return obj.toString();
    	}else{
    		return "";
    	}
    }
}