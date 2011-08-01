package cn.bc.business.motorcade.service;

import cn.bc.business.motorcade.dao.HistoryCarQuantityDao;
import cn.bc.business.motorcade.domain.HistoryCarQuantity;
import cn.bc.core.service.DefaultCrudService;

public class HistoryCarQuantityServiceImpl extends DefaultCrudService<HistoryCarQuantity> implements HistoryCarQuantityService{
	private HistoryCarQuantityDao historyCarQuantityDao;

	public void setHistoryCarQuantityDao(HistoryCarQuantityDao historyCarQuantityDao) {
		this.historyCarQuantityDao = historyCarQuantityDao;
		this.setCrudDao(historyCarQuantityDao);
	}
	
}