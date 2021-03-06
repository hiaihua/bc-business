package cn.bc.business.invoice.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import cn.bc.business.invoice.dao.Invoice4SellDao;
import cn.bc.business.invoice.domain.Invoice4Sell;
import cn.bc.core.service.DefaultCrudService;

/**
 * 票务销售Service实现类
 * 
 * @author wis
 */
public class Invoice4SellServiceImpl extends DefaultCrudService<Invoice4Sell> implements
		Invoice4SellService {
	private Invoice4SellDao invoice4SellDao;
	
	@Autowired
	public void setInvoice4SellDao(Invoice4SellDao invoice4SellDao) {
		this.invoice4SellDao = invoice4SellDao;
		this.setCrudDao(invoice4SellDao);
	}

	public List<Map<String,String>>  selectSellDetailByCode(Long buyId) {
		return this.invoice4SellDao.selectSellDetailByCode(buyId);
	}

	public List<Map<String,String>> selectSellDetailByCode(Long buyId,
			Long sellId) {
		return this.invoice4SellDao.selectSellDetailByCode(buyId,sellId);
	}

	public int countInvoiceBuyCountByBuyDate(Integer type, Calendar buyDate,
			String company,boolean flag) {
		return this.invoice4SellDao.countInvoiceBuyCountByBuyDate(type, buyDate, company,flag);
	}

	public int countInvoiceBuyCountByBuyDate(Integer type,
			Calendar buyDateFrom, Calendar buyDateTo, String company) {
		return this.invoice4SellDao.countInvoiceBuyCountByBuyDate(type, buyDateFrom, buyDateTo, company);
	}

	public int countInvoiceSellCountBySellDate(Integer type, Calendar SellDate,
			String company,boolean flag) {
		return this.invoice4SellDao.countInvoiceSellCountBySellDate(type, SellDate, company,flag);
	}

	public int countInvoiceSellCountBySellDate(Integer type,
			Calendar SellDateFrom, Calendar SellDateTo, String company) {
		return this.invoice4SellDao.countInvoiceSellCountBySellDate(type, SellDateFrom, SellDateTo, company);
	}

	public List<Map<String, String>> findCompany4Option() {
		return this.invoice4SellDao.findCompany4Option();
	}

	public List<Map<String, String>> selectListSellDetailByCode(Long buyId) {
		return this.invoice4SellDao.selectListSellDetailByCode(buyId);
	}

	public int countInvoiceRefundCountBySellDate(Integer type,
			Calendar SellDate, String company, boolean flag) {
		return this.invoice4SellDao.countInvoiceRefundCountBySellDate(type, SellDate, company, flag);
	}

	public int countInvoiceRefundCountBySellDate(Integer type,
			Calendar SellDateFrom, Calendar SellDateTo, String company) {
		return this.invoice4SellDao.countInvoiceRefundCountBySellDate(type, SellDateFrom, SellDateTo, company);
	}

}