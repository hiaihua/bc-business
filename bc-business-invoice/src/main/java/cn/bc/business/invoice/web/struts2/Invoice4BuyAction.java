/**
 * 
 */
package cn.bc.business.invoice.web.struts2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import cn.bc.BCConstants;
import cn.bc.business.OptionConstants;
import cn.bc.business.invoice.domain.Invoice4Buy;
import cn.bc.business.invoice.service.Invoice4BuyService;
import cn.bc.business.web.struts2.FileEntityAction;
import cn.bc.identity.web.SystemContext;
import cn.bc.option.domain.OptionItem;
import cn.bc.option.service.OptionService;
import cn.bc.web.ui.html.page.ButtonOption;
import cn.bc.web.ui.html.page.PageOption;
import cn.bc.web.ui.json.Json;

/**
 * 票务采购Action
 * 
 * @author wis
 * 
 */

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class Invoice4BuyAction extends FileEntityAction<Long, Invoice4Buy> {
	// private static Log logger = LogFactory.getLog(CarAction.class);
	private static final long serialVersionUID = 1L;
	private Invoice4BuyService invoice4BuyService;
	private OptionService optionService;

	public List<Map<String, String>> companyList; // 所属公司列表（宝城、广发）
	public List<Map<String, String>> typeList; // 发票类型列表（打印票、手撕票）
	public List<Map<String, String>> unitList; // 发票单位列表（卷、本）

	public String amount;// 合计
	public String balanceNumber;// 剩余号码段
	public String balanceCount;//剩余数量

	@Autowired
	public void setInvoice4BuyService(Invoice4BuyService invoice4BuyService) {
		this.setCrudService(invoice4BuyService);
		this.invoice4BuyService = invoice4BuyService;
	}

	@Autowired
	public void setOptionService(OptionService optionService) {
		this.optionService = optionService;
	}

	@Override
	public boolean isReadonly() {
		// 票务管理员或系统管理员
		SystemContext context = (SystemContext) this.getContext();
		// 配置权限：发票管理员、发票采购管理员
		return !context.hasAnyRole(getText("key.role.bs.invoice"),
				getText("key.role.bs.invoice4buy"),
				getText("key.role.bc.admin"));
	}

	@Override
	protected PageOption buildFormPageOption(boolean editable) {
		return super.buildFormPageOption(editable).setWidth(680)
				.setMinWidth(250).setMinHeight(200);
	}

	@Override
	protected void afterCreate(Invoice4Buy entity) {
		super.afterCreate(entity);
		entity.setBuyDate(Calendar.getInstance());
		entity.setBuyPrice(7F);
		entity.setStatus(BCConstants.STATUS_ENABLED);
		// 每（卷/本）默认值为100
		entity.setEachCount(100);
	}

	@Override
	protected void afterEdit(Invoice4Buy entity) {
		DecimalFormat format = new DecimalFormat("###,##0.00");
		// 计算合计
		this.amount = format.format(entity.getCount()*entity.getBuyPrice());
		this.balanceNumber = this.invoice4BuyService
				.findBalanceNumberByInvoice4BuyId(entity.getId()).get(0);
		this.balanceCount = this.invoice4BuyService.findBalanceCountByInvoice4BuyId(entity.getId()).get(0);
		super.afterEdit(entity);
	}

	@Override
	protected void afterOpen(Invoice4Buy entity) {
		DecimalFormat format = new DecimalFormat("###,##0.00");
		// 计算合计
		this.amount = format.format(entity.getCount()*entity.getBuyPrice());
		this.balanceNumber = this.invoice4BuyService
				.findBalanceNumberByInvoice4BuyId(entity.getId()).get(0);
		this.balanceCount = this.invoice4BuyService.findBalanceCountByInvoice4BuyId(entity.getId()).get(0);
		super.afterOpen(entity);

	}

	@Override
	protected void initForm(boolean editable) throws Exception {
		super.initForm(editable);
		// 批量加载可选项列表
		Map<String, List<Map<String, String>>> optionItems = this.optionService
				.findOptionItemByGroupKeys(new String[] { OptionConstants.CAR_COMPANY, });

		// 所属公司列表
		this.companyList = optionItems.get(OptionConstants.CAR_COMPANY);
		OptionItem.insertIfNotExist(companyList, null, getE().getCompany());

		// 发票类型
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		list.add(this.getOptiomItems(String.valueOf(Invoice4Buy.TYPE_PRINT),
				getText("invoice.type.dayinpiao")));
		list.add(this.getOptiomItems(String.valueOf(Invoice4Buy.TYPE_TORE),
				getText("invoice.type.shousipiao")));
		this.typeList = list;

		// 发票单位
		list = new ArrayList<Map<String, String>>();
		list.add(this.getOptiomItems(String.valueOf(Invoice4Buy.UNIT_JUAN),
				getText("invoice.unit.juan")));
		list.add(this.getOptiomItems(String.valueOf(Invoice4Buy.UNIT_BEN),
				getText("invoice.unit.ben")));
		this.unitList = list;

	}

	/**
	 * 生成OptiomItem key、value值
	 */
	private Map<String, String> getOptiomItems(String key, String value) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", key);
		map.put("value", value);
		return map;
	}

	@Override
	protected void buildFormPageButtons(PageOption pageOption, boolean editable) {
		if (!this.isReadonly()) {
			if (editable) {// 可编辑时显示保存按钮
				pageOption.addButton(new ButtonOption(getText("label.save"),
						null, "bs.invoice4BuyForm.save")
						.setId("invoice4BuySave"));
			} else {// open时
				if (this.getE().getStatus() == Invoice4Buy.STATUS_NORMAL) {
					// 维护
					pageOption.addButton(new ButtonOption(
							getText("invoice.optype.edit"), null,
							"bs.invoice4BuyForm.doMaintenance")
							.setId("invoice4BuyEdit"));
				}
			}
		}

	}

	// ---检查相同发票代码的采购开始号和结束号是否出现在已保存的采购编码范围内---开始--
	public String code;// 发票代码
	public String startNo;
	public String endNo;
	public Long id4Buy;// 采购单Id
	public int status;

	public String checkSameCode4StartNoAndEndNo() {
		Json json = new Json();
		List<Invoice4Buy> bList = null;
		List<Map<String, String>> sellList = null;
		Long startNo4Long = Long.parseLong(startNo.trim());
		Long endNo4Long = Long.parseLong(endNo.trim());
		if (id4Buy != null) {// 编辑
			sellList = invoice4BuyService.findSellDetail(id4Buy);
			// 判断采购单对应的销售单号码范围。
			if (sellList != null && sellList.size() > 0
					&& sellList.get(0) != null) {
				// 作废处理
				if (this.status == Invoice4Buy.STATUS_INVALID) {// 此采购单带有销售单
																// 不能够作废
					json.put("checkResult", "3");
					this.json = json.toString();
					return "json";
				} else {// 不是作废
					this.json = this.checkSellNumberRange(sellList,
							startNo4Long, endNo4Long, json);
					// 返回不为{}时，范围已不正确不能够保存
					if (!this.json.equals("{}")) {
						return "json";
					}
				}
			}

			bList = invoice4BuyService.selectInvoice4BuyByCode(code, id4Buy);
		} else {// 新建
			bList = invoice4BuyService.selectInvoice4BuyByCode(code);
		}
		if (bList == null || bList.size() == 0) {// 没有这钟发票代码的采购单
			// 检查结果：checkResult 0表示没有这钟发票代码的采购单，1表示有且开始号或结束号在其它采购单编码范围内；
			json.put("checkResult", "0");
			this.json = json.toString();
			return "json";
		} else {
			this.json = this.eachListInvoice4Buy(bList, startNo4Long,
					endNo4Long, json);
			return "json";
		}
	}

	// 遍历采购单集合，检查相同发票代码的采购单开始号和结束号是否出现在已保存的采购编码范围内
	private String eachListInvoice4Buy(List<Invoice4Buy> bList, Long startNo,
			Long endNo, Json json) {
		for (int i = 0; i < bList.size(); i++) {
			Invoice4Buy i4Buy = bList.get(i);
			if (endNo < Long.parseLong(i4Buy.getStartNo().trim())) {
				json.put("checkResult", "0");
				return json.toString();
			} else if (bList.size() == 1
					&& endNo > Long.parseLong(i4Buy.getEndNo().trim())
					&& startNo > Long.parseLong(i4Buy.getEndNo().trim())) {// 遍历到集合的最后一个对象时
				json.put("checkResult", "0");
				return json.toString();
			} else if (endNo > Long.parseLong(i4Buy.getEndNo().trim())
					&& startNo > Long.parseLong(i4Buy.getEndNo().trim())) {
				bList.remove(0);// 比较完第一个对象，下次再比较时已不需要再比较这个对象
				eachListInvoice4Buy(bList, startNo, endNo, json);// 继续递归
			} else {
				json.put("checkResult", "1");
				return json.toString();
			}
		}

		return json.toString();
	}

	// 检测需要保存的采购单 号码范围不能够小于对应销售单的号码范围
	private String checkSellNumberRange(List<Map<String, String>> list,
			Long startNo, Long endNo, Json json) {
		if(!(Long.parseLong(list.get(0).get("startNo").trim())>=startNo
				&& Long.parseLong(list.get(list.size()-1).get("endNo").trim())<=endNo)){
			json.put("checkResult", "2");
			return json.toString();
		}
		return json.toString();
	}

	// ---检查相同发票代码的采购开始号和结束号是否出现在已保存的采购编码范围内---结束--
}