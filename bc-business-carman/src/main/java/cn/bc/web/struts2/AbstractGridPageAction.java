/**
 * 
 */
package cn.bc.web.struts2;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.bc.core.CrudOperations;
import cn.bc.core.Page;
import cn.bc.core.query.condition.Condition;
import cn.bc.core.query.condition.Direction;
import cn.bc.core.query.condition.impl.AndCondition;
import cn.bc.core.query.condition.impl.LikeCondition;
import cn.bc.core.query.condition.impl.OrCondition;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.web.ui.html.grid.Column;
import cn.bc.web.ui.html.grid.Grid;
import cn.bc.web.ui.html.grid.GridData;
import cn.bc.web.ui.html.grid.GridFooter;
import cn.bc.web.ui.html.grid.GridHeader;
import cn.bc.web.ui.html.grid.IdColumn;
import cn.bc.web.ui.html.grid.PageSizeGroupButton;
import cn.bc.web.ui.html.grid.SeekGroupButton;
import cn.bc.web.ui.html.page.HtmlPage;

/**
 * 带grid的Action
 * 
 * @author dragon
 * 
 */
public abstract class AbstractGridPageAction extends AbstractHtmlPageAction {
	private static final long serialVersionUID = 1L;
	protected Log logger = LogFactory.getLog(this.getClass());
	public boolean multiple;// 是否允许多选
	public List<? extends Object> es; // grid的数据
	public Page<? extends Object> page; // 分页对象
	public String search; // 搜索框输入的文本
	public String sort; // grid的排序配置，格式为"filed1 asc,filed2 desc,..."

	// == 子类必须复写的方法

	/** grid数据的查询服务 */
	protected abstract CrudOperations<? extends Object> getGridDataService();

	/** 计算grid数据行标签信息的表达式 */
	protected abstract String getGridRowLabelExpression();

	/** 查询条件中要匹配的域，通常用于子类复写 */
	protected abstract String[] getGridSearchFields();

	// == Action方法

	@Override
	public String execute() throws Exception {
		return this.list();
	}

	// Action：无分页Grid
	public String list() throws Exception {
		// 根据请求的条件查找信息
		this.es = this.findList();

		// 构建页面的html
		this.html = buildHtmlPage();

		// 返回全局的global-results：在cn/bc/web/struts2/struts.xml中定义的
		return "page";
	}

	// Action：分页
	public String paging() throws Exception {
		// 首次请求时page对象为空，需要初始化一下
		if (this.page == null)
			this.page = new Page<Object>();
		if (this.page.getPageSize() < 1)
			this.page.setPageSize(Integer
					.parseInt(getText("app.grid.pageSize")));

		// 根据请求的条件查找分页信息
		this.page = this.findPage();
		this.es = page.getData();

		// 构建页面的html
		this.html = buildHtmlPage();

		// 返回全局的global-results：在cn/bc/web/struts2/struts.xml中定义的
		return "page";
	}

	// Action：仅获取表格的数据信息部分
	public String data() throws Exception {
		if (this.page != null) {// 分页的处理
			// 根据请求的条件查找分页信息
			this.page = this.findPage();
			this.es = this.page.getData();

			// 构建页面的html
			this.html = getGridData(this.getGridColumns());
		} else {// 非分页的处理
			// 根据请求的条件查找分页信息
			this.es = this.findList();

			// 构建页面的html
			this.html = getGridData(this.getGridColumns());
		}

		// 返回全局的global-results：在cn/bc/web/struts2/struts.xml中定义的
		return "page";
	}

	// == 默认实现的方法

	/**
	 * 根据请求的条件查找列表对象
	 * 
	 * @return
	 */
	protected List<? extends Object> findList() {
		return this.getGridDataService().createQuery()
				.condition(this.getGridCondition()).list();
	}

	/**
	 * 根据请求的条件查找分页信息对象
	 * 
	 * @return
	 */
	protected Page<? extends Object> findPage() {
		return this.getGridDataService().createQuery()
				.condition(this.getGridCondition())
				.page(page.getPageNo(), page.getPageSize());
	}

	@Override
	protected HtmlPage buildHtmlPage() {
		HtmlPage htmlPage = super.buildHtmlPage();

		// 附加Grid
		htmlPage.addChild(getHtmlPageGrid());

		return htmlPage;
	}

	protected Grid getHtmlPageGrid() {
		Grid grid = new Grid();

		// grid的列配置
		List<Column> columns = this.getGridColumns();
		grid.setColumns(columns);

		// grid列头部分
		grid.setGridHeader(this.getGridHeader(columns));

		// grid数据部分
		grid.setGridData(this.getGridData(columns));

		// grid远程排序控制
		grid.setRemoteSort("true"
				.equalsIgnoreCase(getText("app.grid.remoteSort")));

		// 多选及双击行编辑
		grid.setSingleSelect(!this.multiple).setDblClickRow(
				this.getGridDblRowMethod());

		// 分页条
		grid.setFooter(getGridFooter(grid));

		return grid;
	}

	/** 获取表格双击行的js处理函数名 */
	protected String getGridDblRowMethod() {
		return readonly ? "bc.page.open" : "bc.page.edit";
	}

	protected List<Column> getGridColumns() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(IdColumn.DEFAULT());
		return columns;
	}

	/**
	 * 构建表格列头
	 * 
	 * @return
	 */
	protected GridHeader getGridHeader(List<Column> columns) {
		GridHeader header = new GridHeader();
		header.setColumns(columns);
		header.setToggleSelectTitle(getText("title.toggleSelect"));
		return header;
	}

	/**
	 * 构建数据部分的html
	 * 
	 * @return
	 */
	protected GridData getGridData(List<Column> columns) {
		GridData data = new GridData();
		if (this.page != null) {
			data.setPageNo(page.getPageNo());
			data.setPageCount(page.getPageCount());
		}
		data.setData(this.es);
		data.setColumns(columns);
		data.setRowLabelExpression(getGridRowLabelExpression());
		// data.setName(getText(StringUtils.uncapitalize(getEntityConfigName())));
		return data;
	}

	/** 构建视图页面表格底部的工具条 */
	protected GridFooter getGridFooter(Grid grid) {
		GridFooter footer = new GridFooter();

		// 刷新按钮
		footer.addButton(GridFooter
				.getDefaultRefreshButton(getText("label.refresh")));

		// 本地或远程排序方式切换按钮
		footer.addButton(GridFooter.getDefaultSortButton(grid.isRemoteSort(),
				getText("title.click2remoteSort"),
				getText("title.click2localSort")));

		// 分页按钮
		if (this.page != null) {
			footer.addButton(new SeekGroupButton().setPageNo(page.getPageNo())
					.setPageCount(page.getPageCount()));
			footer.addButton(new PageSizeGroupButton().setActiveValue(25)
					.setValues(new int[] { 25, 50, 100 })
					.setTitle(getText("label.pageSize")));
		}

		// 导出按钮
		footer.addButton(GridFooter
				.getDefaultExportButton(getText("label.export")));

		// 打印按钮
		// footer.addButton(GridFooter.getDefaultPrintButton(getText("label.print")));

		return footer;
	}

	/** grid数据的查询条件 */
	protected Condition getGridCondition(){
		return new AndCondition().add(getGridSpecalCondition())
				.add(getGridSearchCondition()).add(getGridOrderCondition());
	}

	/**
	 * 构建排序条件
	 * 
	 * @return
	 */
	protected OrderCondition getGridOrderCondition() {
		if (this.sort == null || this.sort.length() == 0)
			return getGridDefaultOrderCondition();

		// sort为grid的排序配置，格式为"filed1 asc,filed2 desc,..."
		String[] cfgs = this.sort.split(",");
		String[] cfg = cfgs[0].split(" ");

		OrderCondition oc = new OrderCondition(cfg[0],
				cfg.length > 1 ? (Direction.Desc.toSymbol().equalsIgnoreCase(
						cfg[1]) ? Direction.Desc : Direction.Asc)
						: Direction.Asc);

		for (int i = 1; i < cfgs.length; i++) {
			cfg = cfgs[i].split(" ");
			oc.add(cfg[0], cfg.length > 1 ? (Direction.Desc.toSymbol()
					.equalsIgnoreCase(cfg[1]) ? Direction.Desc : Direction.Asc)
					: Direction.Asc);
		}

		return oc;
	}

	/**
	 * 构建默认的排序条件，通常用于子类复写
	 * 
	 * @return
	 */
	protected OrderCondition getGridDefaultOrderCondition() {
		return null;
	}

	/**
	 * 构建特殊的条件，通常用于子类复写
	 * 
	 * @return
	 */
	protected Condition getGridSpecalCondition() {
		return null;
	}

	/**
	 * 构建查询条件
	 * 
	 * @return
	 */
	protected OrCondition getGridSearchCondition() {
		if (this.search == null || this.search.length() == 0)
			return null;

		// 用空格分隔多个查询条件的值的处理
		String[] values = this.search.split(" ");

		// 用空格分隔多个查询条件的值的处理
		String[] likeFields = this.getGridSearchFields();
		if (likeFields == null || likeFields.length == 0)
			return null;

		// 添加模糊查询条件
		// TODO 添加更复杂的查询处理，参考google的搜索格式
		OrCondition or = new OrCondition();
		for (String field : likeFields) {
			for (String value : values) {
				or.add(new LikeCondition(field, value));
			}
		}

		// 用括号将多个or条件括住
		or.setAddBracket(true);

		return or;
	}
}
