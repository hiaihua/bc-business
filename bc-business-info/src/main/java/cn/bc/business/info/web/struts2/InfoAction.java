/**
 * 
 */
package cn.bc.business.info.web.struts2;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.business.info.domain.Info;
import cn.bc.business.info.service.InfoService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.docs.service.AttachService;
import cn.bc.docs.web.ui.html.AttachWidget;
import cn.bc.identity.service.IdGeneratorService;
import cn.bc.identity.web.SystemContext;
import cn.bc.web.ui.html.page.ButtonOption;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.json.Json;

/**
 * ��ϢAction
 * 
 * @author dragon
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class InfoAction extends FileEntityAction<Long, Info> implements
		SessionAware {
	// private static Log logger = LogFactory.getLog(InfoAction.class);
	private static final long serialVersionUID = 1L;
	protected IdGeneratorService idGeneratorService;
	protected AttachService attachService;
	protected InfoService infoService;
	public String statusDesc;
	public String typeDesc;

	// ���±���ͨ��Struts��xml�����ļ���������
	public int customType = Info.TYPE_COMPANYGILE;// Ĭ�ϵ���Ϣ����
	public String customRole;// �����ߵĽ�ɫ���룬����ö�������

	public InfoAction() {
		super();
		this.setEntityClass(Info.class);
	}

	@Autowired
	public void setInfoService(InfoService infoService) {
		this.infoService = infoService;
		this.setCrudService(infoService);
	}

	@Autowired
	public void setAttachService(AttachService attachService) {
		this.attachService = attachService;
	}

	@Autowired
	public void setIdGeneratorService(IdGeneratorService idGeneratorService) {
		this.idGeneratorService = idGeneratorService;
	}

	@Override
	public boolean isReadonly() {
		SystemContext context = (SystemContext) this.getContext();
		String roles;
		if (customRole == null || customRole.length() == 0)
			roles = "BC_ADMIN";
		else
			roles = customRole + ",BC_ADMIN";
		return !context.hasAnyOneRole(roles);
	}

	@Override
	protected void afterCreate(Info entity) {
		SystemContext context = (SystemContext) this.getContext();
		Info e = this.getE();
		e.setFileDate(Calendar.getInstance());
		e.setSendDate(e.getFileDate());// ��������Ĭ�ϵ��ڴ�������
		e.setSource(context.getBelong().getName());// ��ԴĬ�ϵ��ڵ�ǰ�û���������
		e.setAuthor(context.getUserHistory());
		e.setStatus(Info.STATUS_DRAFT);
		e.setUid(this.idGeneratorService.next("info.uid"));
		e.setType(this.customType);

		// ���������ؼ�
		attachsUI = buildAttachsUI(true, false);

		// ״̬����
		typeDesc = this.getTypes().get(String.valueOf(this.getE().getType()));

		// ״̬����
		statusDesc = this.getStatuses().get(
				String.valueOf(this.getE().getStatus()));
	}

	@Override
	protected PageOption buildFormPageOption(boolean editable) {
		if (editable)
			return super.buildFormPageOption(editable).setWidth(680)
					.setHeight(450);
		else
			return super.buildFormPageOption(editable).setWidth(650)
					.setHeight(400);
	}

	@Override
	protected void buildFormPageButtons(PageOption option, boolean editable) {
		// �Ǳ༭״̬û���κβ�����ť
		if (!editable)
			return;

		// ����Ա
		if (!this.isReadonly()) {
			option.addButton(new ButtonOption(getText("label.preview"), null,
					"bs.infoForm.preview"));
			option.addButton(this.getDefaultSaveButtonOption());
		}
	}

	public AttachWidget attachsUI;

	@Override
	protected void afterEdit(Info entity) {
		// ���������ؼ�
		attachsUI = buildAttachsUI(false, false);

		// ״̬����
		typeDesc = this.getTypes().get(String.valueOf(this.getE().getType()));
	}

	@Override
	protected void afterOpen(Info e) {
		this.customType = e.getType();
		
		// ���������ؼ�
		attachsUI = buildAttachsUI(false, true);

		// ״̬����
		typeDesc = this.getTypes().get(String.valueOf(e.getType()));

		// ״̬����
		statusDesc = this.getStatuses().get(
				String.valueOf(this.getE().getStatus()));
	}

	protected AttachWidget buildAttachsUI(boolean isNew, boolean forceReadonly) {
		// ���������ؼ�
		String ptype = "info.main";
		String puid = this.getE().getUid();
		boolean readonly = forceReadonly ? true : this.isReadonly();
		AttachWidget attachsUI = AttachWidget.defaultAttachWidget(isNew,
				readonly, isFlashUpload(), this.attachService, ptype, puid);

		// �ϴ�����������
		attachsUI.addExtension(getText("app.attachs.extensions"))
				.setMaxCount(Integer.parseInt(getText("app.attachs.maxCount")))
				.setMaxSize(Integer.parseInt(getText("app.attachs.maxSize")));

		return attachsUI;
	}

	private Map<String, String> getTypes() {
		Map<String, String> types = new LinkedHashMap<String, String>();
		types.put(String.valueOf(Info.TYPE_COMPANYGILE), getText("companyFile"));
		types.put(String.valueOf(Info.TYPE_REGULATION), getText("regulation"));
		types.put(String.valueOf(Info.TYPE_NOTICE), getText("notice"));
		return types;
	}

	/**
	 * ��ȡ״ֵ̬ת���б�
	 * 
	 * @return
	 */
	public Map<String, String> getStatuses() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(Info.STATUS_DRAFT),
				getText("info.status.draft"));
		statuses.put(String.valueOf(Info.STATUS_ISSUED),
				getText("info.status.issued"));
		statuses.put(String.valueOf(Info.STATUS_DISABLED),
				getText("info.status.disadled"));
		return statuses;
	}

	// ����
	public String doIssue() throws Exception {
		Json json = new Json();
		json.put("id", getId());
		try {
			this.infoService.doIssue(this.getId());
			json.put("success", true);
			json.put("msg", getText("info.issueSuccess"));
		} catch (Exception e) {
			json.put("success", false);
			json.put("msg", e.getMessage());
		}
		this.json = json.toString();
		return "json";
	}

	// ����
	public String doDisabled() throws Exception {
		Json json = new Json();
		json.put("id", getId());
		try {
			this.infoService.doDisabled(this.getId());
			json.put("success", true);
			json.put("msg", getText("info.disabledSuccess"));
		} catch (Exception e) {
			json.put("success", false);
			json.put("msg", e.getMessage());
		}
		this.json = json.toString();
		return "json";
	}
}