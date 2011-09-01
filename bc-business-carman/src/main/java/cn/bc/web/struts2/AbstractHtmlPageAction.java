/**
 * 
 */
package cn.bc.web.struts2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import cn.bc.web.ui.Component;
import cn.bc.web.ui.html.page.HtmlPage;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.html.toolbar.Toolbar;
import cn.bc.web.ui.json.Json;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Html页面Action
 * 
 * @author dragon
 * 
 */
public abstract class AbstractHtmlPageAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	protected Log logger = LogFactory.getLog(this.getClass());

	/** 成功生成Html页面后所返回的视图名 */
	public static final String PAGE = "page";

	/** Ajax成功处理后返回Json信息对应的视图名 */
	public static final String JSON = "json";

	public Json json; // json页面
	public Component html; // html页面
	public boolean readonly; // 是否只读

	// == 子类必须复写的方法

	/** 页面的标题 */
	protected abstract String getHtmlPageTitle();

	/** 页面的工具条 */
	protected abstract Toolbar getHtmlPageToolbar();

	/** 访问页面的命名空间 */
	protected abstract String getHtmlPageNamespace();

	@Override
	public String execute() throws Exception {
		// 构建页面的html
		this.html = buildHtmlPage();

		// 返回
		return PAGE;
	}

	// == 默认实现的方法

	/** 构建页面的html代码 */
	protected HtmlPage buildHtmlPage() {
		HtmlPage htmlPage = new HtmlPage();

		// 设置页面参数
		htmlPage.setNamespace(getHtmlPageNamespace()).addJs(getHtmlPageJs())
				.setTitle(this.getHtmlPageTitle())
				.setInitMethod(getHtmlPageInitMethod())
				.setOption(getHtmlPageOption().toString()).setBeautiful(true)
				.addClazz("bc-page");

		// 附件工具条
		htmlPage.addChild(getHtmlPageToolbar());

		return htmlPage;
	}

	/** 页面配置参数 */
	protected PageOption getHtmlPageOption() {
		return new PageOption().setMinWidth(250).setMinHeight(120)
				.setModal(false);
	}

	/** 页面加载后调用的js初始化方法 */
	protected String getHtmlPageInitMethod() {
		return null;
	}

	/** 页面需要另外加载的js、css文件，逗号连接多个文件 */
	protected String getHtmlPageJs() {
		return null;
	}

	/** 获取访问该ation的上下文路径 */
	protected String getContextPath() {
		return ServletActionContext.getRequest().getContextPath();
	}
}
