/**
 * 
 */
package cn.bc.business.tempdriver.web.struts2;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.BCConstants;
import cn.bc.business.tempdriver.domain.TempDriver;
import cn.bc.business.web.struts2.ViewAction;
import cn.bc.core.query.condition.Condition;
import cn.bc.core.query.condition.Direction;
import cn.bc.core.query.condition.impl.AndCondition;
import cn.bc.core.query.condition.impl.EqualsCondition;
import cn.bc.core.query.condition.impl.InCondition;
import cn.bc.core.query.condition.impl.OrderCondition;
import cn.bc.core.util.StringUtils;
import cn.bc.db.jdbc.RowMapper;
import cn.bc.db.jdbc.SqlObject;
import cn.bc.identity.web.SystemContext;
import cn.bc.web.formater.CalendarFormater;
import cn.bc.web.formater.DateRangeFormater;
import cn.bc.web.formater.KeyValueFormater;
import cn.bc.web.ui.html.grid.Column;
import cn.bc.web.ui.html.grid.FooterButton;
import cn.bc.web.ui.html.grid.IdColumn4MapKey;
import cn.bc.web.ui.html.grid.TextColumn4MapKey;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.html.toolbar.Toolbar;
import cn.bc.web.ui.html.toolbar.ToolbarMenuButton;
import cn.bc.web.ui.json.Json;

import com.google.gson.JsonObject;

/**
 * 司机招聘信息的视图Action
 * 
 * @author lbj
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class TempDriversAction extends ViewAction<Map<String, Object>> {
	private static final long serialVersionUID = 1L;
	public String status ; // 聘用的状态，多个用逗号连接

	@Override
	public boolean isReadonly() {
		// 司机招聘管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		return !context.hasAnyRole(getText("key.role.bs.tempDriver"),
				getText("key.role.bc.admin"));
	}
	
	public boolean isAdvancedRead(){
		// 司机招聘高级查询角色
		SystemContext context = (SystemContext) this.getContext();
		return context.hasAnyRole(getText("key.role.bs.tempDriver.read.advanced"));
	}

	@Override
	protected OrderCondition getGridDefaultOrderCondition() {
		// 默认排序方向：最新修改日期|创建日期
		return new OrderCondition("t.modified_date", Direction.Desc).add("t.file_date", Direction.Desc);
	}

	@Override
	protected SqlObject<Map<String, Object>> getSqlObject() {
		SqlObject<Map<String, Object>> sqlObject = new SqlObject<Map<String, Object>>();
		// 构建查询语句,where和order by不要包含在sql中(要统一放到condition中)
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT t.id,t.name,t.sex,t.birthdate,t.origin,t.region_ as region,t.address,t.new_addr as newAddress,t.phone");
		sql.append(",t.cert_identity as certIdentity,t.cert_fwzg as fwzg,t.cert_cyzg as cyzg,t.education,t.nation");
		sql.append(",t.marry,t.desc_ as desc,t.phone");
		//sql.append(",w.offer_status as ostatus");
		sql.append(",t.status_ as status,d.status_ as bstatus,t.valid_start_date as validStartDate ,t.valid_end_date as validEndDate");
		sql.append(",t.file_date,u.actor_name as aname,t.modified_date,m.actor_name as mname");

		sql.append(",t.interview_date as interviewDate,t.register_date as registerDate,t.credit_desc as creditDesc");
		sql.append(",t.crime_recode as crimeRecode,t.back_ground as backGround,t.entry_car as entryCar");
		sql.append(",t.apply_attribute as applyAttr,t.former_unit as formerUnit,t.issue,t.is_crime_recode");
		sql.append(" FROM bs_temp_driver t");
		sql.append(" INNER JOIN bc_identity_actor_history u on u.id=t.author_id");
		//sql.append(" LEFT JOIN bs_temp_driver_workflow w on w.pid=t.id");
		sql.append(" LEFT JOIN bs_carman d on d.cert_identity = t.cert_identity");
		sql.append(" LEFT JOIN bc_identity_actor_history m on m.id=t.modifier_id");

		sqlObject.setSql(sql.toString());

		// 注入参数
		sqlObject.setArgs(null);

		// 数据映射器
		sqlObject.setRowMapper(new RowMapper<Map<String, Object>>() {
			public Map<String, Object> mapRow(Object[] rs, int rowNum) {
				Map<String, Object> map = new HashMap<String, Object>();
				int i = 0;
				map.put("id",rs[i++]);
				map.put("name",rs[i++]);
				map.put("sex",rs[i++]);
				map.put("birthdate",rs[i++]);
				map.put("origin",rs[i++]);
				map.put("region",rs[i++]);
				map.put("address",rs[i++]);
				map.put("newAddress",rs[i++]);
				map.put("phone",rs[i++]);
				map.put("certIdentity",rs[i++]);
				map.put("fwzg",rs[i++]);
				map.put("cyzg",rs[i++]);
				map.put("education",rs[i++]);
				map.put("nation",rs[i++]);
				map.put("marry",rs[i++]);
				map.put("desc",rs[i++]);
				map.put("phone",rs[i++]);
				//map.put("ostatus",rs[i++]);
				map.put("status",rs[i++]);
				map.put("bstatus",rs[i++]);
				map.put("validStartDate",rs[i++]);
				map.put("validEndDate",rs[i++]);
				map.put("file_date",rs[i++]);
				map.put("aname",rs[i++]);
				map.put("modified_date",rs[i++]);
				map.put("mname",rs[i++]);
				map.put("interviewDate",rs[i++]);
				map.put("registerDate",rs[i++]);
				map.put("creditDesc",rs[i++]);
				map.put("crimeRecode",rs[i++]);
				map.put("backGround",rs[i++]);
				map.put("entryCar",rs[i++]);
				map.put("applyAttr",rs[i++]);
				map.put("formerUnit",rs[i++]);
				map.put("issue",rs[i++]);
				map.put("isCrimeRecode",rs[i++]);
				
				return map;
			}
		});
		return sqlObject;
	}
	
	/**
	 * 聘用状态值转换列表：
	 * 
	 * @return
	 */
	protected Map<String, String> getStatusValues() {
		Map<String, String> s = new LinkedHashMap<String, String>();
		s.put(String.valueOf(TempDriver.STATUS_RESERVE),
				getText("tempDriver.status.reserve"));
		s.put(String.valueOf(TempDriver.STATUS_CHECK),
				getText("tempDriver.status.check"));
		s.put(String.valueOf(TempDriver.STATUS_PASS),
				getText("tempDriver.status.pass"));
		s.put(String.valueOf(TempDriver.STATUS_GIVEUP),
				getText("tempDriver.status.giveup"));
		s.put("",getText("bs.status.all"));
		return s;
	}
	
	/**
	 * 营运状态值转换列表：
	 * 
	 * @return
	 */
	protected Map<String, String> getBusinessValues() {
		Map<String, String> s = new LinkedHashMap<String, String>();
		s.put(String.valueOf(BCConstants.STATUS_ENABLED),
				getText("bs.status.active"));
		s.put(String.valueOf(BCConstants.STATUS_DRAFT),
				getText("bc.status.draft"));
		s.put(String.valueOf(BCConstants.STATUS_DISABLED),
				getText("bs.status.logout"));
		return s;
	}
	
	/**
	 * 性别状态值转换列表：1=男，2=女
	 * 
	 * @return
	 */
	protected Map<String, String> getSexValues() {
		Map<String, String> s = new LinkedHashMap<String, String>();
		s.put(String.valueOf(TempDriver.SEX_MAN),
				getText("tempDriver.sex.man"));
		s.put(String.valueOf(TempDriver.SEX_WOMAN),
				getText("tempDriver.sex.woman"));
		return s;
	}
	
	/**
	 * 区域状态值转换列表：0=空，1=本市，2=本省 3=外省
	 * 
	 * @return
	 */
	protected Map<String, String> getRegionValues() {
		Map<String, String> s = new LinkedHashMap<String, String>();
		s.put(String.valueOf(TempDriver.REGION_),
				getText("tempDriver.region.empty"));
		s.put(String.valueOf(TempDriver.REGION_BEN_SHI),
				getText("tempDriver.region.benshi"));
		s.put(String.valueOf(TempDriver.REGION_BEN_SHENG),
				getText("tempDriver.region.bensheng"));
		s.put(String.valueOf(TempDriver.REGION_WAI_SHENG),
				getText("tempDriver.region.waisheng"));
		return s;
	}
	
	/**
	 * 是否有犯罪记录值转换列表：0 无 ，1 有
	 * 
	 * @return
	 */
	protected Map<String, String> getIsCrimeRecodeValues() {
		Map<String, String> s = new LinkedHashMap<String, String>();
		s.put(String.valueOf(TempDriver.IS_CRIME_RECODE_HAVE),
				getText("tempDriver.isCrimeRecode.have"));
		s.put(String.valueOf(TempDriver.IS_CRIME_RECODE_NONE),
				getText("tempDriver.isCrimeRecode.none"));
		s.put("","");
		return s;
	}
	
	/**
	 * 录用状态值转换列表：0=审核中，1=录用，2=不录用
	 * 
	 * @return
	 */
	/*protected Map<String, String> getOfferStatusValues() {
		Map<String, String> s = new LinkedHashMap<String, String>();
		s.put(String.valueOf(TempDriverWorkFlow.OFFER_STATUS_CHECK),
				getText("tempDriverWorkFlow.offerStatus.check"));
		s.put(String.valueOf(TempDriverWorkFlow.OFFER_STATUS_PASS),
				getText("tempDriverWorkFlow.offerStatus.pass"));
		s.put(String.valueOf(TempDriverWorkFlow.OFFER_STATUS_NOPASS),
				getText("tempDriverWorkFlow.offerStatus.noPass"));
		s.put("","未参与");
		return s;
	}*/

	@Override
	protected List<Column> getGridColumns() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(new IdColumn4MapKey("t.id", "id"));
		//录用状态
		/*columns.add(new TextColumn4MapKey("w.offer_status", "ostatus",
				getText("tempDriverWorkFlow.newOfferStatus"), 80).setSortable(true)
				.setValueFormater(new KeyValueFormater(getOfferStatusValues())));*/
		//状态
		columns.add(new TextColumn4MapKey("t.status_", "status",
				getText("tempDriver.status"), 45).setSortable(true)
				.setValueFormater(new KeyValueFormater(getStatusValues())));
		//营运状态
		columns.add(new TextColumn4MapKey("d.status_", "bstatus",
				getText("tempDriver.statusBusiness"), 60).setSortable(true)
				.setValueFormater(new KeyValueFormater(getBusinessValues())));
		//姓名
		columns.add(new TextColumn4MapKey("t.name", "name",
				getText("tempDriver.name"),60).setSortable(true)
				.setUseTitleFromLabel(true));
		//性别
		columns.add(new TextColumn4MapKey("t.sex", "sex",
				getText("tempDriver.sex"), 40).setSortable(true)
				.setValueFormater(new KeyValueFormater(getSexValues())));
		//出生日期
		columns.add(new TextColumn4MapKey("t.birthdate", "birthdate",
				getText("tempDriver.birthdate"), 80).setSortable(true)
				.setUseTitleFromLabel(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		//面试日期
		columns.add(new TextColumn4MapKey("t.interview_date", "interviewDate",
				getText("tempDriver.interviewDate"), 80).setSortable(true)
				.setUseTitleFromLabel(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		//报名日期
		columns.add(new TextColumn4MapKey("t.register_date", "registerDate",
				getText("tempDriver.registerDate"), 80).setSortable(true)
				.setUseTitleFromLabel(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd")));
		//籍贯
		columns.add(new TextColumn4MapKey("t.origin", "origin",
				getText("tempDriver.origin"),150).setSortable(true)
				.setUseTitleFromLabel(true));
		//区域
		columns.add(new TextColumn4MapKey("t.region_", "region",
				getText("tempDriver.region"), 40).setSortable(true)
				.setValueFormater(new KeyValueFormater(getRegionValues())));
		//身份证
		columns.add(new TextColumn4MapKey("t.cert_identity", "certIdentity",
				getText("tempDriver.certIdentity"),150).setSortable(true)
				.setUseTitleFromLabel(true));
		//身份证有效期
		columns.add(new TextColumn4MapKey("t.valid_start_date",
				"validStartDate", getText("tempDriver.validDate"),
				180).setValueFormater(new DateRangeFormater("yyyy-MM-dd") {
			@Override
			public Date getToDate(Object context, Object value) {
				@SuppressWarnings("rawtypes")
				Map contract = (Map) context;
				return (Date) contract.get("validEndDate");
			}
		}));
		//签发机关
		columns.add(new TextColumn4MapKey("t.issue", "issue",
				getText("tempDriver.issue"),150).setSortable(true)
				.setUseTitleFromLabel(true));
		//身份证地址
		columns.add(new TextColumn4MapKey("t.address", "address",
				getText("tempDriver.address"),150).setSortable(true)
				.setUseTitleFromLabel(true));
		//最新地址
		columns.add(new TextColumn4MapKey("t.new_addr", "newAddress",
				getText("tempDriver.newAddress"),150).setSortable(true)
				.setUseTitleFromLabel(true));

		//服务资格证
		columns.add(new TextColumn4MapKey("t.cert_fwzg", "fwzg",
				getText("tempDriver.fwzg"),80).setSortable(true)
				.setUseTitleFromLabel(true));
		
		if(!isReadonly()||isAdvancedRead()){
			//电话号码
			columns.add(new TextColumn4MapKey("t.phone", "phone",
					getText("tempDriver.phone"),100).setSortable(true)
					.setUseTitleFromLabel(true));
		}
		//是否有犯罪记录
		columns.add(new TextColumn4MapKey("t.is_crime_recode", "isCrimeRecode",
				getText("tempDriver.isCrimeRecode"), 100).setSortable(true)
				.setValueFormater(new KeyValueFormater(getIsCrimeRecodeValues())));
		//从业资格证
		columns.add(new TextColumn4MapKey("t.cert_cyzg", "cyzg",
				getText("tempDriver.cyzg"),120).setSortable(true)
				.setUseTitleFromLabel(true));
		
		//婚姻状况
		columns.add(new TextColumn4MapKey("t.marry", "marry",
				getText("tempDriver.marry"),80).setSortable(true)
				.setUseTitleFromLabel(true));
		
		//信誉档案简述
		columns.add(new TextColumn4MapKey("t.credit_desc", "creditDesc",
				getText("tempDriver.creditDesc"),100).setSortable(true)
				.setUseTitleFromLabel(true));
		
		if(!isReadonly()||isAdvancedRead()){
			//犯罪记录
			columns.add(new TextColumn4MapKey("t.crime_recode", "crimeRecode",
					getText("tempDriver.crimeRecode"),100).setSortable(true)
					.setUseTitleFromLabel(true));
		}
		//背景调查
		columns.add(new TextColumn4MapKey("t.back_ground", "backGround",
				getText("tempDriver.backGround"),100).setSortable(true)
				.setUseTitleFromLabel(true));
		//将入车号
		columns.add(new TextColumn4MapKey("t.entry_car", "entryCar",
				getText("tempDriver.entryCar"),100).setSortable(true)
				.setUseTitleFromLabel(true));
		//申请属性
		columns.add(new TextColumn4MapKey("t.apply_attribute", "applyAttr",
				getText("tempDriver.applyAttr"),100).setSortable(true)
				.setUseTitleFromLabel(true));
		//原单位
		columns.add(new TextColumn4MapKey("t.former_unit", "formerUnit",
				getText("tempDriver.formerUnit"),100).setSortable(true)
				.setUseTitleFromLabel(true));
		//学历
		columns.add(new TextColumn4MapKey("t.education", "education",
				getText("tempDriver.education"),60).setSortable(true)
				.setUseTitleFromLabel(true));
		//民族
		columns.add(new TextColumn4MapKey("t.nation", "nation",
				getText("tempDriver.nation"),100).setSortable(true)
				.setUseTitleFromLabel(true));
				
		//备注
		columns.add(new TextColumn4MapKey("t.desc_", "desc",
				getText("tempDriver.desc")).setSortable(true)
				.setUseTitleFromLabel(true));
		
		columns.add(new TextColumn4MapKey("u.actor_name", "aname",
				getText("label.authorName"), 80).setSortable(true));
		columns.add(new TextColumn4MapKey("t.file_date", "file_date",
				getText("label.fileDate"), 120).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd HH:mm")));
		columns.add(new TextColumn4MapKey("m.actor_name", "mname",
				getText("tempDriver.modifier"), 80).setSortable(true));
		columns.add(new TextColumn4MapKey("t.modified_date", "modified_date",
				getText("tempDriver.modifiedDate"), 120).setSortable(true)
				.setValueFormater(new CalendarFormater("yyyy-MM-dd HH:mm")));
		return columns;
	}

	@Override
	protected String[] getGridSearchFields() {
		return new String[] { "t.name","t.origin","t.address","t.new_addr" ,"t.cert_identity"
				,"t.cert_fwzg","t.cert_cyzg","t.education","t.nation", "t.marry","u.actor_name","m.actor_name"
				,"t.back_ground","t.entry_car","t.apply_attribute","t.former_unit"};
	}

	@Override
	protected String getFormActionName() {
		return "tempDriver";
	}

	@Override
	protected PageOption getHtmlPageOption() {
		return super.getHtmlPageOption().setWidth(850).setMinWidth(300)
				.setHeight(400).setMinHeight(300);
	}

	@Override
	protected String getGridRowLabelExpression() {
		return "['name']";
	}

	@Override
	protected Condition getGridSpecalCondition() {
		AndCondition andCondition = new AndCondition();
		if(status !=null && status.length()>0){
			String[] ss = status.split(",");
			if (ss.length == 1) {
				andCondition.add(new EqualsCondition("t.status_", new Integer(
						ss[0])));
			} else {
				andCondition.add(new InCondition("t.status_",
						StringUtils.stringArray2IntegerArray(ss)));
			}
		}
		
		//过滤旧的流程
		//andCondition.add(new QlCondition("NOT EXISTS(select 1 from bs_temp_driver_workflow w2 where w2.pid=t.id and w.start_time>w2.start_time)"));
		return andCondition.isEmpty()?null:andCondition;
	}

	@Override
	protected Toolbar getHtmlPageToolbar() {
		Toolbar tb = new Toolbar();
		if (this.isReadonly()) {
			// 查看按钮
			tb.addButton(getDefaultOpenToolbarButton());
		} else {
			// 新建按钮
			tb.addButton(getDefaultCreateToolbarButton());

			// 编辑按钮
			tb.addButton(getDefaultEditToolbarButton());
			
			// 发起流程
			/*tb.addButton(new ToolbarButton().setIcon("ui-icon-play")
					.setText(getText("tempDriverWorkFlow.startFlow"))
					.setClick("bs.tempDriverView.startFlow"));*/
			
			// "更多"按钮
			ToolbarMenuButton menuButton = new ToolbarMenuButton(
					getText("label.operate"))
					.setChange("bs.tempDriverView.selectMenuButtonItem");
			tb.addButton(menuButton);
			// --批量发起新司机入职处理流程
			menuButton.addMenuItem(getText("tempDriver.workflow.carManEntry"),
					"workflow.carManEntry");
			// --批量发起司机服务资格证办理流程
			menuButton.addMenuItem(getText("tempDriver.workflow.requestDerviceCertficate"),
					"workflow.requestDerviceCertficate");
			// --批量修改面试日期
			menuButton.addMenuItem(getText("tempDriver.operate.interviewDate"),
					"operate.interviewDate");
			// --批量修改状态
			menuButton.addMenuItem(getText("tempDriver.operate.status"),
					"operate.status");
			
		}
		
		tb.addButton(Toolbar.getDefaultToolbarRadioGroup(
				this.getStatusValues(), "status",4,
				getText("title.click2changeSearchStatus")));
		
		// 搜索按钮
		tb.addButton(getDefaultSearchToolbarButton());
		
		return tb;
	}
	
	
	@Override
	protected String getHtmlPageJs() {
		return this.getContextPath() + "/bc-business/tempDriver/view.js";
	}

	@Override
	protected Json getGridExtrasData() {
		Json json = new Json();
		json.put("status", status);
		return status==null||status.length()==0?null:json;
	}
	
	@Override
	protected FooterButton getGridFooterImportButton() {
		// 获取默认的导入按钮设置
		FooterButton fb = this.getDefaultGridFooterImportButton();

		// 配置特殊参数
		JsonObject cfg = new JsonObject();
		cfg.addProperty("tplCode", "IMPORT_TEMPDRIVER");// 模板编码
		cfg.addProperty("importAction", "bc-business/tempDriver/import");// 导入数据的action路径(使用相对路径)
		cfg.addProperty("headerRowIndex", 0);// 列标题所在行的索引号(0-based)
		fb.setAttr("data-cfg", cfg.toString());

		// 返回导入按钮
		return fb;
	}

	//高级搜索
	@Override
	protected boolean useAdvanceSearch() {
		return true;
	}

	
}
