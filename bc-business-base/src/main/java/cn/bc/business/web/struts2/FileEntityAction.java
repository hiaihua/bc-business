/**
 * 
 */
package cn.bc.business.web.struts2;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import cn.bc.BCConstants;
import cn.bc.docs.service.AttachService;
import cn.bc.docs.web.ui.html.AttachWidget;
import cn.bc.identity.domain.FileEntity;

/**
 * bc-business子系统FileEntity的CRUD通用Action
 * 
 * @author dragon
 * 
 */
public class FileEntityAction<K extends Serializable, E extends FileEntity<K>>
		extends cn.bc.identity.web.struts2.FileEntityAction<K, E> {
	private static final long serialVersionUID = 1L;
	private AttachService attachService;

	public AttachService getAttachService() {
		return attachService;
	}

	@Autowired
	public void setAttachService(AttachService attachService) {
		this.attachService = attachService;
	}

	@Override
	protected String getActionPathPrefix() {
		// 与配置文件对应：src/main/resources/cn/bc/business/web/struts2/struts.xml
		return EntityAction.PATH_PREFIX;
	}

	/**
	 * 状态值转换列表：在案|注销|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getBSStatuses1() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(BCConstants.STATUS_ENABLED),
				getText("bs.status.active"));
		statuses.put(String.valueOf(BCConstants.STATUS_DISABLED),
				getText("bs.status.logout"));
		statuses.put(" ", getText("bs.status.all"));
		return statuses;
	}

	/**
	 * 状态值转换列表：在案|结案|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getBSStatuses2() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(BCConstants.STATUS_ENABLED),
				getText("bs.status.active"));
		statuses.put(String.valueOf(BCConstants.STATUS_DISABLED),
				getText("bs.status.closed"));
		statuses.put(" ", getText("bs.status.all"));
		return statuses;
	}

	/**
	 * 状态值转换列表：在案|注销|草稿|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getBSStatuses3() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(BCConstants.STATUS_ENABLED),
				getText("bs.status.active"));
		statuses.put(String.valueOf(BCConstants.STATUS_DISABLED),
				getText("bs.status.logout"));
		statuses.put(String.valueOf(BCConstants.STATUS_DRAFT),
				getText("bc.status.draft"));
		statuses.put("", getText("bs.status.all"));
		return statuses;
	}

	/**
	 * 构建一个默认的附件控件
	 * 
	 * @param isNew
	 *            所属文档是否处于新建状态
	 * @param readonly
	 *            控件的只读状态
	 * @param ptype
	 *            所属文档类型
	 * @param puid
	 *            所属文档的uid
	 * @return 已经根据全局配置初始化好的附件控件
	 */
	protected AttachWidget buildAttachsUI(boolean isNew, boolean readonly,
			String ptype, String puid) {
		// 构建附件控件
		AttachWidget attachsUI = AttachWidget.defaultAttachWidget(isNew,
				readonly, isFlashUpload(), this.attachService, ptype, puid);

		// 上传附件的限制
		attachsUI.addExtension(getText("app.attachs.extensions"))
				.setMaxCount(Integer.parseInt(getText("app.attachs.maxCount")))
				.setMaxSize(Integer.parseInt(getText("app.attachs.maxSize")));

		return attachsUI;
	}
}