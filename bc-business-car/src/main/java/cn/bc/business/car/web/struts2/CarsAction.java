/**
 * 
 */
package cn.bc.business.car.web.struts2;

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

import cn.bc.BCConstants;
import cn.bc.business.car.domain.Car;
import cn.bc.business.motorcade.service.MotorcadeService;
import cn.bc.business.web.struts2.LinkFormater4ChargerInfo;
import cn.bc.business.web.struts2.LinkFormater4DriverInfo;
import cn.bc.business.web.struts2.ViewAction;
import cn.bc.core.query.condition.Condition;
import cn.bc.core.query.condition.ConditionUtils;
import cn.bc.core.query.condition.Direction;
import cn.bc.core.query.condition.impl.EqualsCondition;
import cn.bc.core.query.condition.impl.InCondition;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.core.util.StringUtils;
import cn.bc.db.jdbc.RowMapper;
import cn.bc.db.jdbc.SqlObject;
import cn.bc.identity.domain.Actor;
import cn.bc.identity.service.ActorService;
import cn.bc.identity.web.SystemContext;
import cn.bc.option.domain.OptionItem;
import cn.bc.web.formater.AbstractFormater;
import cn.bc.web.formater.CalendarFormater;
import cn.bc.web.formater.EntityStatusFormater;
import cn.bc.web.formater.LinkFormater4Id;
import cn.bc.web.ui.html.grid.Column;
import cn.bc.web.ui.html.grid.IdColumn4MapKey;
import cn.bc.web.ui.html.grid.TextColumn4MapKey;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.html.toolbar.Toolbar;
import cn.bc.web.ui.html.toolbar.ToolbarButton;
import cn.bc.web.ui.html.toolbar.ToolbarMenuButton;
import cn.bc.web.ui.json.Json;

/**
 * 车辆视图Action
 * 
 * @author dragon
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class CarsAction extends ViewAction<Map<String, Object>> {
	private static final long serialVersionUID = 1L;
	public String status = String.valueOf(BCConstants.STATUS_ENABLED); // 车辆的状态，多个用逗号连接
	public Long carManId;
	public Long carId;

	@Override
	public boolean isReadonly() {
		// 车辆管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.car"),
				getText("key.role.bc.admin"));
	}

	public boolean isEntering() {
		// 车辆草稿信息录入
		SystemContext context = (SystemContext) this.getContext();
		return context.hasAnyRole(getText("key.role.bs.car.entering"));
	}

	public boolean isNewBuyEntering() {
		// 新购车辆信息录入
		SystemContext context = (SystemContext) this.getContext();
		return context.hasAnyRole(getText("key.role.bs.car.newBuy.entering"));
	}

	public boolean isCheck() {
		// 车辆草稿信息查看
		SystemContext context = (SystemContext) this.getContext();
		return context.hasAnyRole(getText("key.role.bs.car.check"));
	}

	public boolean isCheckOwner() {
		// 车辆产权查看
		SystemContext context = (SystemContext) this.getContext();
		return context.hasAnyRole(getText("key.role.bs.car.owner.check"),
				getText("key.role.bs.ownership"),
				getText("key.role.bs.ownership.check"),
				getText("key.role.bs.ownership.check_advanced"));
	}

	public boolean isCheckOwnershipNumber() {
		// 车辆经营权号查看
		SystemContext context = (SystemContext) this.getContext();
		return context.hasAnyRole(
				getText("key.role.bs.car.ownershipNumber.check"),
				getText("key.role.bs.ownership"),
				getText("key.role.bs.ownership.check"),
				getText("key.role.bs.ownership.check_advanced"),
				getText("key.role.bs.car"), getText("key.role.bc.admin"));
	}

	private boolean isScrapTo() {
		// 是否能读写经济合同残值归属管理角色
		SystemContext context = (SystemContext) this.getContext();
		return context.hasAnyRole(
				getText("key.role.bs.contract4charger.scrapTo"),
				getText("key.role.bc.admin"),
				getText("key.role.bs.contract4charger"),
				getText("key.role.bs.contract4charger.entering"));
	}

	@Override
	protected OrderCondition getGridDefaultOrderCondition() {
		// 默认排序方向：状态|登记日期|自编号
		return new OrderCondition("c.status_", Direction.Asc).add(
				"c.register_date", Direction.Desc)
				.add("c.code", Direction.Desc);
	}

	@Override
	protected SqlObject<Map<String, Object>> getSqlObject() {
		SqlObject<Map<String, Object>> sqlObject = new SqlObject<Map<String, Object>>();

		// 构建查询语句,where和order by不要包含在sql中(要统一放到condition中)
		StringBuffer sql = new StringBuffer();
		sql.append("select c.id,c.status_,c.company,bia.name as unit_name,m.name,c.code,c.plate_type,c.plate_no");
		sql.append(",c.driver,c.charger,c.bs_type,c.register_date,c.origin_no");
		sql.append(",c.cert_no2,c.cert_no1,c.cert_no3,c.cert_no4,c.original_value");
		sql.append(",c.vin ,c.engine_no,c.factory_type,c.factory_model");
		sql.append(",c.taximeter_no,c.taximeter_factory,c.taximeter_type");
		sql.append(",m.id as motorcade_id,c.scrap_date,c.return_date,c.fuel_type");
		sql.append(",getContract4ChargerScrapTo(c.id) scrapto,c.modified_date,md.actor_name modifier");
		sql.append(",getContract4ChargerCarmaintain(c.id) carmaintain,c.file_date,ad.actor_name author");
		sql.append(",c.manage_no,c.operate_date,c.owner_");
		sql.append(" from bs_car c");
		sql.append(" left join bs_motorcade m on m.id=c.motorcade_id");
		sql.append(" left join bc_identity_actor bia on bia.id=m.unit_id");
		sql.append(" left join BC_IDENTITY_ACTOR_HISTORY md on md.id=c.modifier_id");
		sql.append(" left join BC_IDENTITY_ACTOR_HISTORY ad on ad.id=c.author_id");
		sqlObject.setSql(sql.toString());

		// 注入参数
		sqlObject.setArgs(null);

		// 数据映射器
		sqlObject.setRowMapper(new RowMapper<Map<String, Object>>() {
			public Map<String, Object> mapRow(Object[] rs, int rowNum) {
				Map<String, Object> map = new HashMap<String, Object>();
				int i = 0;
				map.put("id", rs[i++]);
				map.put("status_", rs[i++]); // 状态
				map.put("company", rs[i++]);// 公司
				map.put("unit_name", rs[i++]); // 分公司
				map.put("motorcade_name", rs[i++]);// 车队
				map.put("code", rs[i++]); // 自编号
				map.put("plate_type", rs[i++]); // 车牌号码type
				map.put("plate_no", rs[i++]); // 车牌号码no
				map.put("driver", rs[i++]); // 营运司机
				map.put("charger", rs[i++]); // 责任人
				map.put("bs_type", rs[i++]); // 营运性质
				map.put("register_date", rs[i++]);// 登记日期
				map.put("origin_no", rs[i++]); // 原车号
				map.put("cert_no2", rs[i++]); // 经营权证
				map.put("cert_no1", rs[i++]); // 购置税证号
				map.put("cert_no3", rs[i++]); // 强检证号
				map.put("cert_no4", rs[i++]); // 道路运输证号
				map.put("original_value", rs[i++]);// 固定资产原值
				map.put("vin", rs[i++]); // 车架号
				map.put("engine_no", rs[i++]); // 发动机号
				map.put("factory_type", rs[i++]); // 厂牌类型
				map.put("factory_model", rs[i++]);// 厂牌型号
				map.put("taximeter_no", rs[i++]); // 计价器出厂编号
				map.put("taximeter_factory", rs[i++]);// 计价器制造厂
				map.put("taximeter_type", rs[i++]);// 计价器型号
				map.put("motorcade_id", rs[i++]);// 车队id
				map.put("scrap_date", rs[i++]);// 报废日期
				map.put("return_date", rs[i++]);// 交车日期
				map.put("fuel_type", rs[i++]);//
				map.put("scrapto", rs[i++]);// 经济合同残值
				map.put("modified_date", rs[i++]);
				map.put("modifier", rs[i++]);
				map.put("carmaintain", rs[i++]);// 经济合同车辆包修
				map.put("file_date", rs[i++]);
				map.put("author", rs[i++]);
				map.put("manage_no", rs[i++]);
				map.put("operate_date", rs[i++]);
				map.put("owner_", rs[i++]);// 产权

				return map;
			}
		});
		return sqlObject;
	}

	@Override
	protected List<Column> getGridColumns() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(new IdColumn4MapKey("c.id", "id"));
		// 状态
		columns.add(new TextColumn4MapKey("c.status_", "status_",
				getText("car.status"), 40).setSortable(true).setValueFormater(
				new EntityStatusFormater(getCarStatuses4All())));
		// 公司
		columns.add(new TextColumn4MapKey("c.company", "company",
				getText("label.carCompany"), 40).setSortable(true)
				.setUseTitleFromLabel(true));
		// 分公司
		columns.add(new TextColumn4MapKey("bia.name", "unit_name",
				getText("label.carUnit"), 60).setSortable(true)
				.setUseTitleFromLabel(true));
		// 车队
		columns.add(new TextColumn4MapKey("m.name", "motorcade_name",
				getText("car.motorcade"), 70)
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
		// 自编号
		columns.add(new TextColumn4MapKey("c.code", "code",
				getText("car.code"), 50).setSortable(true)
				.setUseTitleFromLabel(true));
		// 管理号
		columns.add(new TextColumn4MapKey("c.manage_no", "manage_no",
				getText("car.manageNo"), 50).setSortable(true)
				.setUseTitleFromLabel(true));
		// 车牌号码
		columns.add(new TextColumn4MapKey("c.plate_no", "plate_no",
				getText("car.plate"), 80).setUseTitleFromLabel(true)
				.setValueFormater(new AbstractFormater<String>() {
					@SuppressWarnings("unchecked")
					@Override
					public String format(Object context, Object value) {
						Map<String, Object> car = (Map<String, Object>) context;
						return car.get("plate_type") + "."
								+ car.get("plate_no");
					}
				}));
		// 产权
		if (isCheckOwner()) {
			columns.add(new TextColumn4MapKey("c.owner_", "owner_",
					getText("car.owner"), 50).setSortable(true)
					.setUseTitleFromLabel(true));
		}
		// 登记日期
		columns.add(new TextColumn4MapKey("c.register_date", "register_date",
				getText("car.registerDate"), 90).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		// 营运司机
		columns.add(new TextColumn4MapKey("c.driver", "driver",
				getText("car.carMan"), 200)
				.setValueFormater(new LinkFormater4DriverInfo(this
						.getContextPath())));
		// 责任人
		columns.add(new TextColumn4MapKey("c.charger", "charger",
				getText("car.charger"), 130)
				.setValueFormater(new LinkFormater4ChargerInfo(this
						.getContextPath())));
		// 营运性质
		columns.add(new TextColumn4MapKey("c.bs_type", "bs_type",
				getText("car.businessType"), 70).setUseTitleFromLabel(true));

		// 原车号
		columns.add(new TextColumn4MapKey("c.origin_no", "origin_no",
				getText("car.originNo"), 55).setSortable(true)
				.setUseTitleFromLabel(true));
		// 经营权证:权限控制
		if (isCheckOwnershipNumber()) {
			columns.add(new TextColumn4MapKey("c.cert_no2", "cert_no2",
					getText("car.certNo2"), 100).setUseTitleFromLabel(true));
		}
		// 购置税证号
		columns.add(new TextColumn4MapKey("c.cert_no1", "cert_no1",
				getText("car.certNo1"), 100).setUseTitleFromLabel(true));
		// 道路运输证号
		columns.add(new TextColumn4MapKey("c.cert_no4", "cert_no4",
				getText("car.certNo4"), 90).setUseTitleFromLabel(true));
		// 强检证号
		columns.add(new TextColumn4MapKey("c.cert_no3", "cert_no3",
				getText("car.certNo3"), 70).setUseTitleFromLabel(true));
		// 车架号
		columns.add(new TextColumn4MapKey("c.vin", "vin", getText("car.vin"),
				160).setUseTitleFromLabel(true));
		// 发动机号
		columns.add(new TextColumn4MapKey("c.engine_no", "engine_no",
				getText("car.engineNo"), 70).setUseTitleFromLabel(true));
		// 车型 -厂牌类型、厂牌型号
		columns.add(new TextColumn4MapKey("c.factory_type", "factory_type",
				getText("car.factory"), 150).setUseTitleFromLabel(true)
				.setValueFormater(new AbstractFormater<String>() {
					@Override
					public String format(Object context, Object value) {
						// 从上下文取出元素Map
						@SuppressWarnings("unchecked")
						Map<String, Object> car = (Map<String, Object>) context;
						if (car.get("factory_type") != null
								&& car.get("factory_model") != null) {
							return car.get("factory_type") + " "
									+ car.get("factory_model");
						} else if (car.get("factory_model") != null) {
							return car.get("factory_model").toString();
						} else if (car.get("factory_type") != null) {
							return car.get("factory_type") + " ";
						} else {
							return "";
						}
					}
				}));
		// 计价器制造厂
		columns.add(new TextColumn4MapKey("c.taximeter_factory",
				"taximeter_factory", getText("car.taximeterFactory"), 80)
				.setUseTitleFromLabel(true));
		// 计价器出厂编号
		columns.add(new TextColumn4MapKey("c.taximeter_no", "taximeter_no",
				getText("car.taximeterNo"), 110).setUseTitleFromLabel(true));

		// 计价器型号
		columns.add(new TextColumn4MapKey("c.taximeter_type", "taximeter_type",
				getText("car.taximeterType"), 80).setUseTitleFromLabel(true));
		columns.add(new TextColumn4MapKey("c.fuel_type", "fuel_type",
				getText("car.fuelType"), 60).setUseTitleFromLabel(true));

		// 投产日期
		columns.add(new TextColumn4MapKey("c.operate_date", "operate_date",
				getText("car.operateDate"), 90).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		// 报废日期
		columns.add(new TextColumn4MapKey("c.scrap_date", "scrap_date",
				getText("car.scrapDate"), 90).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		// 交车日期
		columns.add(new TextColumn4MapKey("c.return_date", "return_date",
				getText("car.returnDate"), 90).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		if (isScrapTo()) {
			// 残值归属
			columns.add(new TextColumn4MapKey("c.fuel_type", "scrapto",
					getText("car.scrapto"), 60).setUseTitleFromLabel(true)
					.setSortable(true));
		}
		columns.add(new TextColumn4MapKey("c.fuel_type", "carmaintain",
				getText("car.carmaintain"), 60).setUseTitleFromLabel(true)
				.setSortable(true).setSortable(true));
		columns.add(new TextColumn4MapKey("ad.actor_name", "author",
				getText("car.author"), 80).setSortable(true));
		columns.add(new TextColumn4MapKey("c.file_date", "file_date",
				getText("car.fileDate"), 120).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		columns.add(new TextColumn4MapKey("md.actor_name", "modifier",
				getText("car.modifier"), 80).setSortable(true));
		columns.add(new TextColumn4MapKey("c.modified_date", "modified_date",
				getText("car.modifiedDate"), 120).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		return columns;
	}

	@Override
	protected String[] getGridSearchFields() {
		return new String[] { "c.plate_no", "c.driver", "c.charger",
				"c.cert_no2", "c.factory_type", "m.name", "c.engine_no",
				"c.manage_no", "c.code", "c.bs_type",
				"getContract4ChargerCarmaintain(c.id)" };
	}

	@Override
	protected String getFormActionName() {
		return "car";
	}

	@Override
	protected PageOption getHtmlPageOption() {
		return super.getHtmlPageOption().setWidth(900).setMinWidth(400)
				.setHeight(400).setMinHeight(300).setHelp("cheliangxinxi");
	}

	@Override
	protected String getGridRowLabelExpression() {
		return "['plate_type'] + '.' + ['plate_no']";
	}

	@Override
	protected Condition getGridSpecalCondition() {
		// 状态条件
		Condition statusCondition = null;
		if (status != null && status.length() > 0) {
			String[] ss = status.split(",");
			if (ss.length == 1) {
				statusCondition = new EqualsCondition("c.status_", new Integer(
						ss[0]));
			} else {
				statusCondition = new InCondition("c.status_",
						StringUtils.stringArray2IntegerArray(ss));
			}
		} else {
			return null;
		}
		// 合并条件
		return ConditionUtils.mix2AndCondition(statusCondition);
	}

	@Override
	protected void extendGridExtrasData(Json json) {
		super.extendGridExtrasData(json);

		// 状态条件
		if (this.status != null && this.status.trim().length() > 0) {
			json.put("status", status);
		}
	}

	@Override
	protected Toolbar getHtmlPageToolbar() {
		Toolbar tb = new Toolbar();
		if (this.isReadonly()) {
			// 查看按钮
			tb.addButton(this.getDefaultOpenToolbarButton());
			// 如果有录入权限的有新建按钮
			if (this.isEntering()) {
				// 新建按钮
				tb.addButton(this.getDefaultCreateToolbarButton());

			}
		} else {
			// 新建按钮
			tb.addButton(this.getDefaultCreateToolbarButton());

			// 编辑按钮
			tb.addButton(this.getDefaultEditToolbarButton());
			// 删除按钮
			tb.addButton(new ToolbarButton().setIcon("ui-icon-trash")
					.setText("删除").setAction("delete"));
			//
		}

		// 搜索按钮
		tb.addButton(this.getDefaultSearchToolbarButton());

		// 状态单选按钮组
		// 如果有权限的用户可以看到草稿状态的车
		if ((!isReadonly() || this.isEntering() || this.isCheck())
				&& !isNewBuyEntering()) {
			tb.addButton(Toolbar.getDefaultToolbarRadioGroup(
					this.getCarStatuses4Draft(), "status", 0,
					getText("title.click2changeSearchStatus")));

		} else if (isNewBuyEntering()
				&& !(!isReadonly() || this.isEntering() || this.isCheck())) {
			// 拥有新购车辆录入权限
			tb.addButton(Toolbar.getDefaultToolbarRadioGroup(
					this.getCarStatuses4NewBuy(), "status", 0,
					getText("title.click2changeSearchStatus")));
		} else if (isNewBuyEntering()
				&& (!isReadonly() || this.isEntering() || this.isCheck())) {
			// 查看全部
			tb.addButton(Toolbar.getDefaultToolbarRadioGroup(
					this.getCarStatuses4All(), "status", 0,
					getText("title.click2changeSearchStatus")));
		} else {
			// 无权限
			tb.addButton(Toolbar.getDefaultToolbarRadioGroup(
					this.getBSStatuses1(), "status", 0,
					getText("title.click2changeSearchStatus")));
		}
		// 辅助操作
		tb.addButton(new ToolbarMenuButton("辅助操作")
				.addMenuItem("金盾网交通违法查询：跳转", "jinDun-jiaoTongWeiFa")
				.addMenuItem("金盾网交通违法查询：抓取", "jinDun-jiaoTongWeiFa-spider")
				.setChange("bs.carView.selectMenuButtonItem"));
		return tb;
	}

	/**
	 * 状态值转换列表：在案|注销|新购|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getCarStatuses4NewBuy() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(Car.CAR_STAUTS_NORMAL),
				getText("bs.status.active"));
		statuses.put(String.valueOf(Car.CAR_STAUTS_LOGOUT),
				getText("bs.status.logout"));
		statuses.put(String.valueOf(Car.CAR_STAUTS_NEWBUY),
				getText("bc.status.newBuy"));
		statuses.put("-2,0,1", getText("bs.status.all"));
		return statuses;
	}

	/**
	 * 状态值转换列表：在案|注销|草稿|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getCarStatuses4Draft() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(Car.CAR_STAUTS_NORMAL),
				getText("bs.status.active"));
		statuses.put(String.valueOf(Car.CAR_STAUTS_LOGOUT),
				getText("bs.status.logout"));
		statuses.put(String.valueOf(BCConstants.STATUS_DRAFT),
				getText("bc.status.draft"));
		statuses.put("-1,0,1", getText("bs.status.all"));
		return statuses;
	}

	/**
	 * 状态值转换列表：在案|注销|草稿|新购|全部
	 * 
	 * @return
	 */
	protected Map<String, String> getCarStatuses4All() {
		Map<String, String> statuses = new LinkedHashMap<String, String>();
		statuses.put(String.valueOf(Car.CAR_STAUTS_NORMAL),
				getText("bs.status.active"));
		statuses.put(String.valueOf(Car.CAR_STAUTS_LOGOUT),
				getText("bs.status.logout"));
		statuses.put(String.valueOf(BCConstants.STATUS_DRAFT),
				getText("bc.status.draft"));
		statuses.put(String.valueOf(Car.CAR_STAUTS_NEWBUY),
				getText("bc.status.newBuy"));
		statuses.put("-2,-1,0,1", getText("bs.status.all"));
		return statuses;
	}

	@Override
	protected String getHtmlPageJs() {
		return this.getContextPath() + "/bc-business/car/view.js";
	}

	// ==高级搜索代码开始==
	@Override
	protected boolean useAdvanceSearch() {
		return true;
	}

	private MotorcadeService motorcadeService;
	private ActorService actorService;

	@Autowired
	public void setActorService(
			@Qualifier("actorService") ActorService actorService) {
		this.actorService = actorService;
	}

	@Autowired
	public void setMotorcadeService(MotorcadeService motorcadeService) {
		this.motorcadeService = motorcadeService;
	}

	public JSONArray motorcades;// 车队的下拉列表信息
	public JSONArray units;// 分公司的下拉列表信息

	@Override
	protected void initConditionsFrom() throws Exception {
		// 可选分公司列表
		units = OptionItem.toLabelValues(this.actorService.find4option(
				new Integer[] { Actor.TYPE_UNIT }, (Integer[]) null), "name",
				"id");

		// 可选车队列表
		motorcades = OptionItem.toLabelValues(this.motorcadeService
				.find4Option(null));
	}

	// ==高级搜索代码结束==
}
