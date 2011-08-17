/**
 * 
 */
package cn.bc.business.blacklist.web.struts2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.business.blacklist.domain.Blacklist;
import cn.bc.business.blacklist.service.BlacklistService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.web.ui.html.grid.Column;
import cn.bc.web.ui.html.grid.GridData;
import cn.bc.web.ui.html.grid.TextColumn;
import cn.bc.web.ui.html.page.ButtonOption;
import cn.bc.web.ui.html.page.PageOption;

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
	public BlacklistService contractService;

	@Autowired
	public void setContractService(BlacklistService contractService) {
		this.contractService = contractService;
		this.setCrudService(contractService);
	}

	@Override
	public String create() throws Exception {
		this.readonly = false;
		Blacklist e = this.contractService.create();
		this.setE(e);

		this.formPageOption = buildFormPageOption();

		return "form";
	}

	@Override
	protected PageOption buildFormPageOption() {
		PageOption option = new PageOption().setWidth(680).setMinWidth(250)
				.setMinHeight(200).setModal(false);
		option.addButton(new ButtonOption(getText("label.save"), "save"));
		return option;
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
	protected PageOption buildListPageOption() {
		return super.buildListPageOption().setWidth(800).setMinWidth(300)
				.setHeight(400).setMinHeight(300);
	}

	@Override
	protected String[] getSearchFields() {
		return new String[] { "name", "description" };
	}

	@Override
	protected List<Column> buildGridColumns() {
		List<Column> columns = super.buildGridColumns();
		columns.add(new TextColumn("name", getText("label.subject"))
				.setSortable(true).setUseTitleFromLabel(true));
		return columns;
	}
}