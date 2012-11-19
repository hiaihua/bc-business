package cn.bc.business.ownership.web.struts2;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.google.gson.JsonObject;

import cn.bc.BCConstants;
import cn.bc.business.ownership.domain.Ownership;
import cn.bc.business.ownership.service.OwnershipService;
import cn.bc.docs.web.struts2.ImportDataAction;
import cn.bc.identity.web.SystemContext;
import cn.bc.identity.web.SystemContextHolder;

/**
 * 导入经营权数据的Action
 * 
 * @author zxr
 * 
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Controller
public class ImportOwnershipAction extends ImportDataAction {
	private static final long serialVersionUID = 1L;
	private final static Log logger = LogFactory
			.getLog(ImportOwnershipAction.class);
	public OwnershipService ownershipService;

	@Autowired
	public void setOwnershipService(OwnershipService ownershipService) {
		this.ownershipService = ownershipService;
	}

	@Override
	protected void importData(List<Map<String, Object>> data, JsonObject json,
			String fileType) {
		Map<String, Object> map;
		Ownership ownership;
		for (int i = 0; i < data.size(); i++) {
			map = new HashMap<String, Object>();
			ownership = new Ownership();
			map = data.get(i);
			// 判断数据库是否存在相同经营权号的数据，如果存在就不插入
			if (map.get("权证号") != null) {
				Ownership ownershipByNumber = this.ownershipService
						.getOwershipByNumber(map.get("权证号").toString());
				if (ownershipByNumber == null) {
					// 状态
					ownership.setStatus(BCConstants.STATUS_ENABLED);
					// 经营权证号
					if (map.get("权证号") != null) {
						ownership.setNumber(map.get("权证号").toString().trim());
					}
					// 经营权性质
					if (map.get("经营权性质") != null) {
						ownership.setNature(map.get("经营权性质").toString().trim());
					}
					// 经营权情况
					if (map.get("权证抵押情况") != null) {
						ownership.setSituation(map.get("权证抵押情况").toString()
								.trim());
					}
					// 经营权来源
					if (map.get("经营权来源") != null) {
						ownership.setSource(map.get("经营权来源").toString().trim());
					}
					// 车辆产权
					if (map.get("车辆产权") != null) {
						ownership.setOwner(map.get("车辆产权").toString().trim());
					}
					// 设置创建人信息
					SystemContext context = (SystemContext) SystemContextHolder
							.get();
					ownership.setFileDate(Calendar.getInstance());
					ownership.setAuthor(context.getUserHistory());
					this.ownershipService.save(ownership);
				}
			}
		}
		json.addProperty("msg", "成功导入" + data.size() + "条数据！");
		logger.fatal("TODO: ImportOptionAction.importData");
	}
}
