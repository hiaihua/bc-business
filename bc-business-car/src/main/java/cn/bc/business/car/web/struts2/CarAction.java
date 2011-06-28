/**
 * 
 */
package cn.bc.business.car.web.struts2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.business.car.domain.Car;
import cn.bc.business.web.struts2.CrudAction;
import cn.bc.core.query.condition.Condition;
import cn.bc.core.query.condition.Direction;
import cn.bc.core.query.condition.impl.EqualsCondition;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.core.service.CrudService;
import cn.bc.docs.web.ui.html.AttachWidget;
import cn.bc.identity.web.SystemContext;
import cn.bc.web.formater.CalendarFormater;
import cn.bc.web.ui.html.grid.Column;
import cn.bc.web.ui.html.grid.GridData;
import cn.bc.web.ui.html.grid.TextColumn;
import cn.bc.web.ui.html.page.ButtonOption;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.html.toolbar.Toolbar;

/**
 * ����Action
 * 
 * @author dragon
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class CarAction extends CrudAction<Long, Car> {
	// private static Log logger = LogFactory.getLog(BulletinAction.class);
	private static final long serialVersionUID = 1L;
	private String MANAGER_KEY = "R_MANAGER_BUSINESS";// �����ɫ�ı���
	public boolean isManager;

	@Autowired
	public void setCarService(
			@Qualifier(value = "carService") CrudService<Car> crudService) {
		this.setCrudService(crudService);
	}

	@Override
	public String create() throws Exception {
		this.readonly = false;
		Car e = this.getCrudService().create();
		this.setE(e);

		// ���������ؼ�
		attachsUI = buildAttachsUI(true);

		// �����Ի������
		this.formPageOption = buildFormPageOption();

		return "form";
	}

	@Override
	protected PageOption buildFormPageOption() {
		PageOption option = new PageOption().setWidth(680).setMinWidth(250)
				.setMinHeight(200).setModal(false);
		if (isManager()) {
			option.addButton(new ButtonOption(getText("label.save"), "save"));
		}
		return option;
	}

	// �ύ����
	@Override
	public String save() throws Exception {
		Car e = this.getE();
		this.getCrudService().save(e);
		return "saveSuccess";
	}

	public AttachWidget attachsUI;

	@Override
	public String edit() throws Exception {
		this.readonly = false;
		this.setE(this.getCrudService().load(this.getId()));
		this.formPageOption = buildFormPageOption();

		// ���������ؼ�
		attachsUI = buildAttachsUI(false);

		// TODO ��ȡ�ظ���Ϣ�б�

		return "form";
	}

	private AttachWidget buildAttachsUI(boolean isNew) {
		isManager = isManager();
		// ���������ؼ�
//		String ptype = "car.main";
//		AttachWidget attachsUI = new AttachWidget();
//		attachsUI.setFlashUpload(this.isFlashUpload());
//		attachsUI.addClazz("formAttachs");
//		if (!isNew)
//			attachsUI.addAttach(this.attachService.findByPtype(ptype, this
//					.getE().getUid()));
//		attachsUI.setPuid(this.getE().getUid()).setPtype(ptype);
//
//		// �ϴ�����������
//		attachsUI.addExtension(getText("app.attachs.extensions"))
//				.setMaxCount(Integer.parseInt(getText("app.attachs.maxCount")))
//				.setMaxSize(Integer.parseInt(getText("app.attachs.maxSize")));
//		attachsUI.setReadOnly(!this.getE().isNew());
		return attachsUI;
	}

	@Override
	protected GridData buildGridData(List<Column> columns) {
		return super.buildGridData(columns).setRowLabelExpression("name");
	}

	@Override
	protected OrderCondition getDefaultOrderCondition() {
		return null;// new OrderCondition("fileDate", Direction.Desc);
	}

	@Override
	protected Condition getSpecalCondition() {
		return null;
	}

	// ����ҳ��ĳߴ�
	@Override
	protected PageOption buildListPageOption() {
		return super.buildListPageOption().setWidth(800).setMinWidth(300)
				.setHeight(400).setMinHeight(300);
	}

	@Override
	protected Toolbar buildToolbar() {
		isManager = isManager();
		Toolbar tb = new Toolbar();
		
		if (isManager) {
			// �½���ť
			tb.addButton(getDefaultCreateToolbarButton());
			
			// �༭��ť
			tb.addButton(getDefaultEditToolbarButton());
			
			// ɾ����ť
			tb.addButton(getDefaultDeleteToolbarButton());
		} else {// ��ͨ�û�
			// �鿴��ť
			tb.addButton(getDefaultOpenToolbarButton());
		}

		// ������ť
		tb.addButton(getDefaultSearchToolbarButton());

		return tb;
	}

	@Override
	protected String[] getSearchFields() {
		return new String[] { "name", "description" };
	}

	@Override
	protected List<Column> buildGridColumns() {
		// �Ƿ�ģ�����Ա
		isManager = isManager();

		List<Column> columns = super.buildGridColumns();
		columns.add(new TextColumn("name", getText("label.subject"))
				.setSortable(true).setUseTitleFromLabel(true));
		return columns;
	}

	// �жϵ�ǰ�û��Ƿ��Ǳ�ģ�����Ա
	private boolean isManager() {
		return ((SystemContext) this.getContext()).hasAnyRole(MANAGER_KEY);
	}

	//
	// @Override
	// protected String getJs() {
	// return contextPath + "/bc-business/car/list.js";
	// }
}
