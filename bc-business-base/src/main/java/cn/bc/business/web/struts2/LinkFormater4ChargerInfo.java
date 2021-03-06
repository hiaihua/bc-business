/**
 * 
 */
package cn.bc.business.web.struts2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import cn.bc.business.BSConstants;
import cn.bc.web.formater.CalendarFormater;
import cn.bc.web.formater.LinkFormater;

/**
 * 责任人沉余信息字段的超链接格式化
 * <p>
 * 生成类似&lt;a ...&gt;张三&lt;/a&gt;,&lt;a ...&gt;李四&lt;/a&gt;的格式
 * </p>
 * 
 * @author dragon
 * 
 */
public class LinkFormater4ChargerInfo extends LinkFormater {
	protected String urlPattern = BSConstants.NAMESPACE + "/carMan/edit?id=";
	protected String moduleKey = "carMan";
	private boolean showTip = true;
	private String contextPath;// 链接的上下文路径

	public LinkFormater4ChargerInfo(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * @param contextPath
	 *            链接的上下文路径
	 * @param showTip
	 *            是否显示鼠标悬停信息
	 */
	public LinkFormater4ChargerInfo(String contextPath, boolean showTip) {
		this.contextPath = contextPath;
		this.showTip = showTip;
	}

	public String format(Object context, Object value) {
		@SuppressWarnings("rawtypes")
		Map contract = (Map) context;
		// 提前终止方
		String qutter = "";
		// 如果提前终止方不为空
		if (contract.get("qutter") != null) {
			qutter = (String) contract.get("qutter");
		}

		String _value = (String) value;
		if (value == null || _value.trim().length() == 0) {
			return "&nbsp;";
		}
		_value = _value.trim();

		// 分隔出每个责任人的配置：[责任人1姓名],[责任人1id];[责任人2姓名],[责任人2id];...
		String[] vvs = _value.split(";");

		// 循环每个责任人执行格式化处理
		String[] vs;
		StringBuffer tpl = new StringBuffer();
		List<String> labels = new ArrayList<String>();
		int i = 0;
		// 提前终止方是否为责任人标识
		boolean isChanger = false;
		for (String vv : vvs) {
			if (i > 0)
				tpl.append(",");

			vs = vv.split(",");// [0]-责任人姓名,[1]-责任人id

			if (vs.length == 2) {
				// 链接地址、模块类型、样式控制
				tpl.append("<a class=\"bc-link\" data-mtype=\""
						+ this.moduleKey + "\" href=\"" + this.contextPath
						+ this.urlPattern + vs[1] + "\"");

				// 任务栏显示的标题：责任人张三
				tpl.append(" data-title=\"责任人" + vs[0] + "\"");

				// 对话框的id
				tpl.append(" data-mid=\"" + this.moduleKey + vs[1] + "\"");

				// 链接显示的文字：张三
				// 经济合同视图:责任人列特殊处理，如"张三,李四(~[补充协议的结束日期])"
				if (qutter.length() > 0) {
					if (qutter.endsWith(vs[0])) {
						isChanger = true;
						Date endDate = (Date) contract
								.get("agreement_start_date");
						CalendarFormater cf = new CalendarFormater();
						String date = cf.format(context, endDate);
						if (date != null) {
							labels.add(vs[0] + "(~" + date + ")");
							tpl.append(">" + vs[0] + "(" + date + "终止)"
									+ "</a>");
						} else {
							labels.add(vs[0]);
							tpl.append(">" + vs[0] + "</a>");
						}
					} else {
						labels.add(vs[0]);
						tpl.append(">" + vs[0] + "</a>");
					}
				} else {
					labels.add(vs[0]);
					tpl.append(">" + vs[0] + "</a>");

				}
			} else {
				tpl.append(vv);
				labels.add(vv);
			}

			i++;
		}
		// 如果存在不是责任为提前终止方
		if (isChanger == false && qutter.length() > 0) {
			// 提前终止方
			String qutterId = "";
			// 如果提前终止方不为空
			if (contract.get("qutter") != null) {
				qutterId = String.valueOf(contract.get("quitter_id"));
			}

			// 链接地址、模块类型、样式控制
			tpl.append("<a class=\"bc-link\" data-mtype=\"" + this.moduleKey
					+ "\" href=\"" + this.contextPath + this.urlPattern
					+ qutterId + "\"");

			// 任务栏显示的标题：责任人张三
			tpl.append(" data-title=\"司机" + qutter + "\"");

			// 对话框的id
			tpl.append(" data-mid=\"" + this.moduleKey + qutterId + "\"");

			Date endDate = (Date) contract.get("agreement_start_date");
			CalendarFormater cf = new CalendarFormater();
			String date = cf.format(context, endDate);
			if (date != null) {
				labels.add(",司机" + qutter + "(~" + date + ")");
				tpl.append(">" + ",司机" + qutter + "(" + date + "终止)" + "</a>");
			} else {
				labels.add(",司机" + qutter);
				tpl.append(">" + ",司机" + qutter + "</a>");
			}
		}

		if (this.showTip) {
			return "<div title=\""
					+ StringUtils.collectionToCommaDelimitedString(labels)
					+ "\">" + tpl.toString() + "</div>";
		} else {
			return tpl.toString();
		}
	}

	@SuppressWarnings("null")
	@Override
	public String getLinkText(Object context, Object value) {
		@SuppressWarnings("rawtypes")
		Map contract = (Map) context;
		// 提前终止方
		String qutter = "";
		// 如果提前终止方不为空
		if (contract.get("qutter") != null) {
			qutter = (String) contract.get("qutter");
		}

		String _value = (String) value;
		if (value == null || _value.trim().length() == 0) {
			return "";
		}
		_value = _value.trim();

		// 分隔出每个责任人的配置：[责任人1姓名],[责任人1id];[责任人2姓名],[责任人2id];...
		String[] vvs = _value.split(";");

		// 循环每个责任人执行格式化处理
		String[] vs;
		String labels = "";
		int i = 0;
		// 提前终止方是否为责任人标识
		boolean isChanger = false;
		for (String vv : vvs) {
			if (i > 0)
				labels += ",";

			vs = vv.split(",");// [0]-责任人姓名,[1]-责任人id
			if (vs.length == 2) {
				// 经济合同视图:责任人列特殊处理，如"张三,李四([补充协议的开始日期]终止)"
				if (qutter != null || qutter.length() > 0) {
					if (qutter.endsWith(vs[0])) {
						isChanger = true;
						Date endDate = (Date) contract
								.get("agreement_start_date");
						CalendarFormater cf = new CalendarFormater();
						String date = cf.format(context, endDate);
						if (date != null) {
							labels += vs[0] + "(" + date + "终止)";
						} else {
							labels += vs[0];
						}
					} else {
						labels += vs[0];
					}
				} else {
					labels += vs[0];
				}

			} else {
				labels += vv;
			}

			i++;
		}
		// 如果存在不是责任为提前终止方
		if (isChanger == false && qutter.length() > 0) {
			Date endDate = (Date) contract.get("agreement_start_date");
			CalendarFormater cf = new CalendarFormater();
			String date = cf.format(context, endDate);
			if (date != null) {
				labels += ",司机" + qutter + "(" + date + "终止)";
			} else {
				labels += ",司机" + qutter;
			}

		}
		return labels;
	}

	@Override
	public Object[] getParams(Object context, Object value) {
		return null;
	}
}
