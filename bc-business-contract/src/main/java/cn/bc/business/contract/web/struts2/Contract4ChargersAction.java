/**
 * 
 */
package cn.bc.business.contract.web.struts2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.business.OptionConstants;
import cn.bc.business.contract.domain.Contract;
import cn.bc.business.motorcade.service.MotorcadeService;
import cn.bc.business.web.struts2.LinkFormater4ChargerInfo;
import cn.bc.business.web.struts2.ViewAction;
import cn.bc.core.query.condition.Condition;
import cn.bc.core.query.condition.ConditionUtils;
import cn.bc.core.query.condition.Direction;
import cn.bc.core.query.condition.impl.EqualsCondition;
import cn.bc.core.query.condition.impl.LikeCondition;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.core.util.StringUtils;
import cn.bc.db.jdbc.RowMapper;
import cn.bc.db.jdbc.SqlObject;
import cn.bc.identity.domain.Actor;
import cn.bc.identity.service.ActorService;
import cn.bc.identity.web.SystemContext;
import cn.bc.option.domain.OptionItem;
import cn.bc.option.service.OptionService;
import cn.bc.web.formater.AbstractFormater;
import cn.bc.web.formater.CalendarFormater;
import cn.bc.web.formater.EntityStatusFormater;
import cn.bc.web.formater.LinkFormater4Id;
import cn.bc.web.ui.html.grid.Column;
import cn.bc.web.ui.html.grid.IdColumn4MapKey;
import cn.bc.web.ui.html.grid.TextColumn4MapKey;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.html.toolbar.Toolbar;
import cn.bc.web.ui.json.Json;

/**
 * 经济合同视图Action
 * 
 * @author dragon
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class Contract4ChargersAction extends ViewAction<Map<String, Object>> {
	private static final long serialVersionUID = 1L;
	public String status = String.valueOf(Contract.STATUS_NORMAL);
	public String mains = String.valueOf(Contract.MAIN_NOW); // 现实当前版本
	public int type = Contract.TYPE_CHARGER;

	public Long contractId;
	public String patchNo;
	public Long carId;
	public Long driverId;

	@Override
	public boolean isReadonly() {
		// 经济合同管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.contract4charger"),
				getText("key.role.bc.admin"));
	}

	/**
	 * @param useDisabledReplaceDelete
	 *            控制是使用删除按钮还是禁用按钮
	 * @return
	 */
	@Override
	protected Toolbar getHtmlPageToolbar(boolean useDisabledReplaceDelete) {
		Toolbar tb = new Toolbar();

		if (this.isReadonly()) {
			// 查看按钮
			tb.addButton(this.getDefaultOpenToolbarButton());
		} else {

			if (contractId == null) {
				// 新建按钮
				tb.addButton(this.getDefaultCreateToolbarButton());
			}
			// 查看按钮
			tb.addButton(this.getDefaultOpenToolbarButton());
			if (contractId == null) {

				if (useDisabledReplaceDelete) {
					// 禁用按钮
					tb.addButton(this.getDefaultDisabledToolbarButton());
				} else {
					// 删除按钮
					tb.addButton(this.getDefaultDeleteToolbarButton());
				}
			}
		}

		// 搜索按钮
		tb.addButton(this.getDefaultSearchToolbarButton());
		return tb;
	}

	@Override
	protected OrderCondition getGridDefaultOrderCondition() {
		// 默认排序方向：状态|登记日期
		if (contractId == null) {// 当前版本
			return new OrderCondition("c.file_date", Direction.Desc).add(
					"c.status_", Direction.Asc);
		} else { // 历史版本
			return new OrderCondition("c.file_date", Direction.Desc);
		}
	}

	@Override
	protected LikeCondition getGridSearchCondition4OneField(String field,
			String value) {
		if (field.indexOf("ext_str1") != -1) {// 车牌，忽略大小写
			return new LikeCondition(field, value != null ? value.toUpperCase()
					: value);
		} else {
			return super.getGridSearchCondition4OneField(field, value);
		}
	}

	@Override
	protected SqlObject<Map<String, Object>> getSqlObject() {
		SqlObject<Map<String, Object>> sqlObject = new SqlObject<Map<String, Object>>();

		// 构建查询语句,where和order by不要包含在sql中(要统一放到condition中)
		StringBuffer sql = new StringBuffer();
		sql.append("select cc.id,cc.sign_type,cc.bs_type,cc.contract_version_no,c.word_no,c.status_,c.ext_str1,c.ext_str2");
		sql.append(",c.transactor_name,c.sign_date,c.start_date,c.end_date,c.code,c.logout_id,iah.actor_name,c.logout_date");
		sql.append(",cc.payment_date,car.id carId");
		// sql.append(",man.id manId");
		sql.append(",c.ver_major,c.ver_minor,c.op_type,c.file_date");
		sql.append(",car.company company");
		sql.append(",bia.id batch_company_id,bia.name batch_company");
		sql.append(",m.id motorcade_id,m.name motorcade_name");
		sql.append(" from BS_CONTRACT_CHARGER cc");
		sql.append(" inner join BS_CONTRACT c on cc.id = c.id");
		sql.append(" inner join BS_CAR_CONTRACT carc on c.id = carc.contract_id");
		sql.append(" inner join BS_Car car on carc.car_id = car.id");
		if (driverId != null) {
			sql.append(" left join BS_CARMAN_CONTRACT manc on c.id = manc.contract_id");
			sql.append(" left join BS_CARMAN man on manc.man_id = man.id");
		}
		sql.append(" left join BC_IDENTITY_ACTOR_HISTORY iah on c.author_id = iah.id");
		sql.append(" left join bs_motorcade m on m.id=car.motorcade_id");
		sql.append(" left join bc_identity_actor bia on bia.id=m.unit_id");

		sqlObject.setSql(sql.toString());

		// 注入参数
		sqlObject.setArgs(null);

		// 数据映射器
		sqlObject.setRowMapper(new RowMapper<Map<String, Object>>() {
			public Map<String, Object> mapRow(Object[] rs, int rowNum) {
				Map<String, Object> map = new HashMap<String, Object>();
				int i = 0;
				map.put("id", rs[i++]);
				map.put("sign_type", rs[i++]);
				map.put("bs_type", rs[i++]);
				map.put("contract_version_no", rs[i++]);
				map.put("word_no", rs[i++]);
				map.put("status_", rs[i++]);
				map.put("ext_str1", rs[i++]);
				map.put("ext_str2", rs[i++]);
				map.put("transactor_name", rs[i++]);
				map.put("sign_date", rs[i++]);
				map.put("start_date", rs[i++]);
				map.put("end_date", rs[i++]);
				map.put("code", rs[i++]);
				map.put("logout_id", rs[i++]);
				map.put("actor_name", rs[i++]);
				map.put("logout_date", rs[i++]);
				map.put("payment_date", rs[i++]);
				map.put("carId", rs[i++]);
				// map.put("manId", rs[i++]);
				map.put("ver_major", rs[i++]);
				map.put("ver_minor", rs[i++]);
				map.put("op_type", rs[i++]);
				map.put("fileDate", rs[i++]);
				map.put("company", rs[i++]);
				map.put("batch_company_id", rs[i++]);
				map.put("batch_company", rs[i++]);
				map.put("motorcade_id", rs[i++]);
				map.put("motorcade_name", rs[i++]);
				return map;
			}
		});
		return sqlObject;
	}

	@Override
	protected List<Column> getGridColumns() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(new IdColumn4MapKey("cc.id", "id"));
		columns.add(new TextColumn4MapKey("c.status_", "status_",
				getText("contract.status"), 35)
				.setSortable(true)
				.setValueFormater(new EntityStatusFormater(getEntityStatuses())));
		columns.add(new TextColumn4MapKey("car.company", "company",
				getText("contract.company"), 50).setSortable(true));
		columns.add(new TextColumn4MapKey("bia.name", "batch_company",
				getText("contract.batch.company"), 70).setSortable(true));
		columns.add(new TextColumn4MapKey("m.name", "motorcade_name",
				getText("contract.motorcadeName"), 70)
				.setSortable(true)
				.setUseTitleFromLabel(true)
				.setValueFormater(
						new LinkFormater4Id(this.getContextPath()
								+ "/bc-business/motorcade/edit?id={0}",
								"motorcade") {
							@SuppressWarnings("unchecked")
							@Override
							public String getIdValue(Object context,
									Object value) {
								return StringUtils
										.toString(((Map<String, Object>) context)
												.get("motorcade_id"));
							}
						}));
		columns.add(new TextColumn4MapKey("c.ext_str1", "ext_str1",
				getText("contract.car"), 85).setUseTitleFromLabel(true)
				.setValueFormater(
						new LinkFormater4Id(this.getContextPath()
								+ "/bc-business/car/edit?id={0}", "car") {
							@SuppressWarnings("unchecked")
							@Override
							public String getIdValue(Object context,
									Object value) {
								return StringUtils
										.toString(((Map<String, Object>) context)
												.get("carId"));
							}
						}));
		columns.add(new TextColumn4MapKey("c.word_no", "word_no",
				getText("contract4Charger.wordNo"), 70));
		columns.add(new TextColumn4MapKey("c.ext_str2", "ext_str2",
				getText("contract4Charger.charger"), 140).setUseTitleFromLabel(
				true).setValueFormater(
				new LinkFormater4ChargerInfo(this.getContextPath())));
		columns.add(new TextColumn4MapKey("cc.sign_type", "sign_type",
				getText("contract4Charger.signType"), 58).setSortable(true)
				.setUseTitleFromLabel(true));
		columns.add(new TextColumn4MapKey("cc.bs_type", "bs_type",
				getText("contract4Charger.businessType"), 100)
				.setUseTitleFromLabel(true));
		columns.add(new TextColumn4MapKey("cc.payment_date", "payment_date",
				getText("contract4Charger.paymentDate"), 50)
				.setValueFormater(new AbstractFormater<String>() {
					@Override
					public String format(Object context, Object value) {
						if (value == null)
							return "";
						else if (value.toString().equals("0"))
							return "月末";
						else
							return value.toString();
					}
				}));
		columns.add(new TextColumn4MapKey("c.start_date", "start_date",
				getText("contract4Charger.startDate"), 90)
				.setValueFormater(new CalendarFormater()));
		columns.add(new TextColumn4MapKey("c.end_date", "end_date",
				getText("contract4Charger.endDate"), 90)
				.setValueFormater(new CalendarFormater()));
		columns.add(new TextColumn4MapKey("c.sign_date", "sign_date",
				getText("contract.signDate"), 90).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		columns.add(new TextColumn4MapKey("cc.contract_version_no",
				"contract_version_no",
				getText("contract4Charger.contractVersionNo"), 180)
				.setUseTitleFromLabel(true));
		columns.add(new TextColumn4MapKey("c.transactor_name",
				"transactor_name", getText("contract.transactor"), 50)
				.setUseTitleFromLabel(true));
		// if(status.equals(String.valueOf(Contract.STATUS_LOGOUT)) ||
		// status.length() == 0){ //控制视图在在案注销下不显示注销人,注销时间
		columns.add(new TextColumn4MapKey("c.logout_date", "logout_date",
				getText("contract4Charger.logoutDate"), 90).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		columns.add(new TextColumn4MapKey("iah.actor_name", "actor_name",
				getText("contract4Charger.logoutId"), 50)
				.setUseTitleFromLabel(true));
		// }
		columns.add(new TextColumn4MapKey("c.code", "code",
				getText("contract.code"), 130).setUseTitleFromLabel(true));
		// columns.add(new TextColumn4MapKey("c.op_type", "op_type",
		// getText("contract4Labour.op"),
		// 35).setSortable(true).setUseTitleFromLabel(true)
		// .setValueFormater(new EntityStatusFormater(getEntityOpTypes())));
		columns.add(new TextColumn4MapKey("c.file_date", "fileDate",
				getText("label.fileDate"), 90).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		columns.add(new TextColumn4MapKey("c.ver_major", "ver_major",
				getText("contract4Labour.ver"), 40)
				.setValueFormater(new AbstractFormater<String>() {
					@SuppressWarnings("unchecked")
					@Override
					public String format(Object context, Object value) {
						Map<String, Object> ver = (Map<String, Object>) context;
						if (null == ver.get("ver_major")) {
							return "";
						} else if (null != ver.get("ver_major")
								&& null == ver.get("ver_minor")) {
							return ver.get("ver_major") + "." + "0";
						} else {
							return ver.get("ver_major") + "."
									+ ver.get("ver_minor");
						}
					}
				}));
		return columns;
	}

	@Override
	protected String getGridDblRowMethod() {
		// 强制为只读表单
		return "bc.page.open";
	}

	@Override
	protected String[] getGridSearchFields() {
		return new String[] { "c.code", "c.ext_str1", "c.ext_str2",
				"c.word_no", "cc.bs_type", "c.word_no", "car.company",
				"bia.name", "m.name" };
	}

	@Override
	protected String getFormActionName() {
		return "contract4Charger";
	}

	@Override
	protected PageOption getHtmlPageOption() {
		return super.getHtmlPageOption().setWidth(900).setMinWidth(400)
				.setHeight(490).setMinHeight(300);
	}

	@Override
	protected String getGridRowLabelExpression() {
		return "['ext_str1']+'的经济合同 \t-\t v'+['ver_major']+'.'+['ver_minor']";
	}

	@Override
	protected Condition getGridSpecalCondition() {
		// 状态条件
		Condition statusCondition = null;
		Condition mainsCondition = null;
		Condition patchCondtion = null;
		Condition typeCondtion = new EqualsCondition("c.type_", type);
		Condition carCondition = null;
		Condition driverCondition = null;

		if (this.contractId == null) {
			// 查看最新合同列表
			statusCondition = ConditionUtils.toConditionByComma4IntegerValue(
					this.status, "c.status_");
			// if (this.status.length() <= 0) { // 显示全部状态的时候只显示最新版本的记录
			// mainsCondition = ConditionUtils // 控制显示最新版本main为0的经济合同
			// .toConditionByComma4IntegerValue(this.mains, "c.main");
			// }
		} else {
			// 查看历史版本
			if (this.contractId != 0) {
				// 显示实例本身
				patchCondtion = new EqualsCondition("c.patch_no", patchNo);
			} else {
				// 不显示实例本身
				patchCondtion = new EqualsCondition("c.patch_no", patchNo);
				mainsCondition = new EqualsCondition("c.main",
						Contract.MAIN_HISTORY);

			}
		}

		if (carId != null) {
			carCondition = new EqualsCondition("carc.car_id", carId);
		}

		if (driverId != null) {
			driverCondition = new EqualsCondition("manc.man_id", driverId);
		}
		return ConditionUtils.mix2AndCondition(typeCondtion, statusCondition,
				patchCondtion, mainsCondition, carCondition, driverCondition);
	}

	@Override
	protected void extendGridExtrasData(Json json) {
		super.extendGridExtrasData(json);

		// 状态条件
		if (this.status != null && this.status.trim().length() > 0) {
			json.put("status", status);
		}

		if (contractId != null) {
			json.put("contractId", contractId);
		}

		if (patchNo != null) {
			json.put("patchNo", patchNo);
		}

		if (carId != null) {
			json.put("carId", carId);
		}

		if (driverId != null) {
			json.put("driverId", driverId);
		}
	}

	@Override
	protected Toolbar getHtmlPageToolbar() {
		if (contractId == null) {
			return super.getHtmlPageToolbar().addButton(
					Toolbar.getDefaultToolbarRadioGroup(
							this.getEntityStatuses(), "status", 0,
							getText("title.click2changeSearchStatus")));
		} else {
			return super.getHtmlPageToolbar();
		}
	}

	/**
	 * 状态值转换列表：正常|注销|离职|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getEntityStatuses() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(Contract.STATUS_NORMAL),
				getText("contract.status.normal"));
		statuses.put(String.valueOf(Contract.STATUS_LOGOUT),
				getText("contract.status.logout"));
		statuses.put("", getText("bs.status.all"));
		return statuses;
	}

	/**
	 * 获取Contract的操作类型列表
	 * 
	 * @return
	 */
	protected Map<String, String> getEntityOpTypes() {
		Map<String, String> types = new HashMap<String, String>();
		types.put(String.valueOf(Contract.OPTYPE_CREATE),
				getText("contract4Labour.optype.create"));
		types.put(String.valueOf(Contract.OPTYPE_MAINTENANCE),
				getText("contract4Labour.optype.maintenance"));
		types.put(String.valueOf(Contract.OPTYPE_CHANGECAR),
				getText("contract4Labour.optype.transfer"));
		types.put(String.valueOf(Contract.OPTYPE_RENEW),
				getText("contract4Charger.optype.renew"));
		types.put(String.valueOf(Contract.OPTYPE_RESIGN),
				getText("contract4Labour.optype.resign"));
		types.put(String.valueOf(Contract.OPTYPE_CHANGECHARGER),
				getText("contract4Charger.optype.changeCharger"));
		types.put(String.valueOf(Contract.OPTYPE_CHANGECHARGER2),
				getText("contract4Charger.optype.changeCharger2"));
		return types;
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

	// ==高级搜索代码开始==

	private OptionService optionService;
	private MotorcadeService motorcadeService;
	private ActorService actorService;

	@Autowired
	public void setOptionService(OptionService optionService) {
		this.optionService = optionService;
	}

	@Autowired
	public void setActorService(
			@Qualifier("actorService") ActorService actorService) {
		this.actorService = actorService;
	}

	@Autowired
	public void setMotorcadeService(MotorcadeService motorcadeService) {
		this.motorcadeService = motorcadeService;
	}

	public JSONArray units;// 分公司的下拉列表信息
	public JSONArray motorcades;// 车队的下拉列表信息
	public JSONArray contractVersionNos;// 合同版本号列表
	public JSONArray businessTypes;// 营运性质列表
	public JSONArray signTypes;// 签约类型列表

	@Override
	protected boolean useAdvanceSearch() {
		return true;
	}

	@Override
	protected void initConditionsFrom() throws Exception {
		// 可选分公司列表
		units = OptionItem.toLabelValues(this.actorService.find4option(
				new Integer[] { Actor.TYPE_UNIT }, (Integer[]) null), "name",
				"id");

		// 可选车队列表
		motorcades = OptionItem.toLabelValues(this.motorcadeService
				.find4Option(null));

		// 批量加载可选项列表
		Map<String, List<Map<String, String>>> optionItems = this.optionService
				.findOptionItemByGroupKeys(new String[] {
						OptionConstants.CONTRACT_SIGNTYPE,
						OptionConstants.CAR_BUSINESS_NATURE,
						OptionConstants.CONTRACT_VERSION_NO, });

		// 签约类型列表
		this.signTypes = OptionItem.toLabelValues(
				optionItems.get(OptionConstants.CONTRACT_SIGNTYPE), "value");

		// 营运性质列表
		this.businessTypes = OptionItem.toLabelValues(
				optionItems.get(OptionConstants.CAR_BUSINESS_NATURE), "value");

		// 合同版本号列表
		this.contractVersionNos = OptionItem.toLabelValues(optionItems
				.get(OptionConstants.CONTRACT_VERSION_NO));

	}

	// ==高级搜索代码结束==
}
