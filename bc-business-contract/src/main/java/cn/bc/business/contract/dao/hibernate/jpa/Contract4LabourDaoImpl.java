/**
 * 
 */
package cn.bc.business.contract.dao.hibernate.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.util.StringUtils;

import cn.bc.BCConstants;
import cn.bc.business.contract.dao.Contract4LabourDao;
import cn.bc.business.contract.domain.Contract;
import cn.bc.business.contract.domain.Contract4Labour;
import cn.bc.core.Page;
import cn.bc.core.query.condition.Condition;
import cn.bc.orm.hibernate.jpa.HibernateCrudJpaDao;
import cn.bc.orm.hibernate.jpa.HibernateJpaNativeQuery;

/**
 * 司机劳动合同Dao的hibernate jpa实现
 * 
 * @author dragon
 */
public class Contract4LabourDaoImpl extends
		HibernateCrudJpaDao<Contract4Labour> implements Contract4LabourDao {

	protected final Log logger = LogFactory.getLog(getClass());
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * 删除合同与工伤的关联
	 * 
	 * @parma contractId
	 * @return
	 */
	public void deleteInjury(Long contractId) {
		if (contractId != null) {
			this.executeUpdate("delete Injury where contractId=?",
					new Object[] { contractId });
		}
	}

	@Override
	public void delete(Serializable[] ids) {
		if (ids == null || ids.length == 0)
			return;
		for (Serializable id : ids) {
			this.delete(id);
		}
	}

	@Override
	public void delete(Serializable pk) {
		Long contractId = (Long) pk;
		// 删除合同的工伤信息
		this.deleteInjury(contractId);

		// 删除合同与司机的关联信息
		this.deleteDriverRelation(contractId);

		// 删除合同与车辆的关联信息
		this.deleteCarRelation(contractId);

		// 删除合同自身
		Contract4Labour c = this.load(contractId);
	//	Long pid = c.getPid();
		this.getJpaTemplate().remove(c);
		// this.executeUpdate("delete Contract4Labour where id=?",
		// new Object[] { contractId });

		// 如有父级ID,递归删除父级记录
//		if (pid != null) {
//			delete(pid);
//		}
	}

	/**
	 * 删除合同与司机的关联信息
	 * 
	 * @parma contractId
	 * @return
	 */
	public void deleteDriverRelation(Long contractId) {
		if (contractId != null) {
			this.executeUpdate(
					"delete ContractCarManRelation where contractId=?",
					new Object[] { contractId });
		}
	}

	/**
	 * 删除合同与车辆的关联信息
	 * 
	 * @parma contractId
	 * @return
	 */
	public void deleteCarRelation(Long contractId) {
		if (contractId != null) {
			this.executeUpdate("delete ContractCarRelation where contractId=?",
					new Object[] { contractId });
		}
	}

	/**
	 * 保存合同与司机的关联表信息
	 * 
	 * @parma carManId
	 * @parma contractId
	 * @return
	 */
	public void carManNContract4Save(Long carManId, Long contractId) {
		String sql = "select * from BS_CARMAN_CONTRACT carmancontract where carmancontract.contract_id = ?";
		String insertSql = "";
		// jdbc查询BS_CARMAN_CONTRACT表是否存在对应carManId和contractId噶记录
		@SuppressWarnings("rawtypes")
		List list = this.jdbcTemplate.queryForList(sql,
				new Object[] { contractId });

		if (list == null || list.size() < 1) { // 不存在,插入新的记录
			insertSql = "insert into BS_CARMAN_CONTRACT(man_id,contract_id)values("
					+ carManId + "," + contractId + ")";
			this.jdbcTemplate.execute(insertSql);
		} else { // 存在,删除原来的记录,插入新的记录
			String delSql = "delete from BS_CARMAN_CONTRACT where BS_CARMAN_CONTRACT.contract_id="
					+ contractId;
			this.jdbcTemplate.execute(delSql);

			insertSql = "insert into BS_CARMAN_CONTRACT(man_id,contract_id)values("
					+ carManId + "," + contractId + ")";
			this.jdbcTemplate.execute(insertSql);
		}
	}

	/**
	 * 保存车辆与合同的关联信息 jdbc查询BS_CAR_CONTRACT表是否存在相应carId和contractId的记录
	 * 
	 * @param carId
	 * @param contractId
	 */
	public void updateCarContractRelation(Long carId, Long contractId) {
		String sql = "select * from BS_CAR_CONTRACT carcontract where carcontract.contract_id = ?";
		String insertSql = "";
		// jdbc查询BS_CAR_CONTRACT表是否存在相应carId和contractId的记录
		@SuppressWarnings("rawtypes")
		List list = this.jdbcTemplate.queryForList(sql,
				new Object[] { contractId });

		if (list == null || list.size() < 1) { // 不存在,插入新的记录
			insertSql = "insert into BS_CAR_CONTRACT(car_id,contract_id)values("
					+ carId + "," + contractId + ")";
			this.jdbcTemplate.execute(insertSql);
		} else { // 存在,删除原来的记录,插入新的记录
			String delSql = "delete from BS_CAR_CONTRACT where BS_CAR_CONTRACT.contract_id="
					+ contractId;
			this.jdbcTemplate.execute(delSql);

			insertSql = "insert into BS_CAR_CONTRACT(car_id,contract_id)values("
					+ carId + "," + contractId + ")";
			this.jdbcTemplate.execute(insertSql);
		}
	}

	/**
	 * 保存车辆与合同的关联信息 jdbc查询BS_CAR_CONTRACT表是否存在相应carId和contractId的记录
	 * 
	 * @param carId
	 * @param contractId
	 */
	public void carNContract4Save(Long carId, Long contractId) {
		String sql = "select * from BS_CAR_CONTRACT carcontract where carcontract.contract_id = ?";
		String insertSql = "";
		// jdbc查询BS_CAR_CONTRACT表是否存在相应carId和contractId的记录
		@SuppressWarnings("rawtypes")
		List list = this.jdbcTemplate.queryForList(sql,
				new Object[] { contractId });

		if (list == null || list.size() < 1) { // 不存在,插入新的记录
			insertSql = "insert into BS_CAR_CONTRACT(car_id,contract_id)values("
					+ carId + "," + contractId + ")";
			this.jdbcTemplate.execute(insertSql);
		} else { // 存在,删除原来的记录,插入新的记录
			String delSql = "delete from BS_CAR_CONTRACT where BS_CAR_CONTRACT.contract_id="
					+ contractId;
			this.jdbcTemplate.execute(delSql);

			insertSql = "insert into BS_CAR_CONTRACT(car_id,contract_id)values("
					+ carId + "," + contractId + ")";
			this.jdbcTemplate.execute(insertSql);
		}
	}

	/**
	 * 查找劳动合同列表
	 * 
	 * @parma condition
	 * @parma carId
	 * @return
	 */
	public List<Map<String, Object>> list4carMan(Condition condition,
			Long carManId) {
		ArrayList<Object> args = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer();

		hql.append(
				"select contract.id, contract.code, contract.type, contract.signDate,contract.startDate,contract.endDate,contract.transactorName,contract.ext_str1,contract.ext_str2,")
				.append(" from CarMan carman join carman.contracts contract")
				.append(" where carman.id=?");

		if (carManId != null && carManId > 0) {
			args.add(carManId);
		}
		if (condition != null && condition.getValues().size() > 0) {
			hql.append(" AND");
			hql.append(condition.getExpression());
			for (int i = 0; i < condition.getValues().size(); i++) {
				args.add(condition.getValues().get(i));
			}
		}

		// 排序
		hql.append(" order by contract.signDate DESC");
		if (logger.isDebugEnabled()) {
			logger.debug("hql=" + hql);
			logger.debug("args="
					+ (condition != null ? StringUtils
							.collectionToCommaDelimitedString(condition
									.getValues()) : "null"));
		}

		@SuppressWarnings("rawtypes")
		List list = this.getJpaTemplate().find(hql.toString(), args.toArray());

		// 组装map数据,模拟domain列显示
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;

		for (Object obj : list) {

			Object[] ary = (Object[]) obj;
			map = new HashMap<String, Object>();
			map.put("id", ary[0]);
			map.put("code", ary[1]);
			map.put("type", ary[2]);
			map.put("signDate", ary[3]);
			map.put("startDate", ary[4]);
			map.put("endDate", ary[5]);
			map.put("transactorName", ary[6]);
			map.put("ext_str1", ary[7]);
			map.put("ext_str2", ary[8]);

			result.add(map);
		}
		return result;
	}

	/**
	 * 查找劳动合同分页
	 * 
	 * @parma condition
	 * @parma carId
	 * @return
	 */
	public Page<Map<String, Object>> page4carMan(final Condition condition,
			final int pageNo, final int pageSize) {
		// 设置最大页数,如果小于1都按1计算
		final int _pageSize = pageSize < 1 ? 1 : pageSize;
		final StringBuffer hql = new StringBuffer();

		hql.append("select contract.id, contract.code, contract.type, contract.signDate,contract.startDate,contract.endDate,contract.transactorName,contract.ext_str1,contract.ext_str2");

		// 方便统计记录数
		String sqlStr = " from Contract contract WHERE contract.type="
				+ Contract.TYPE_LABOUR;
		// String sqlStr = " from CarMan carman join carman.contracts contract";

		hql.append(sqlStr);

		// 组合查询条件
		if (condition != null && condition.getValues().size() > 0) {
			setWhere(condition, hql);
		}
		// 排序
		hql.append(" order by contract.signDate DESC");
		if (logger.isDebugEnabled()) {
			logger.debug("pageNo=" + pageNo);
			logger.debug("pageSize=" + _pageSize);
			logger.debug("hql=" + hql);
			logger.debug("args="
					+ (condition != null ? StringUtils
							.collectionToCommaDelimitedString(condition
									.getValues()) : "null"));
		}

		@SuppressWarnings("rawtypes")
		// 实现getJapTemLate的execute方法返回一个list
		List list = this.getJpaTemplate().execute(
				new JpaCallback<List<Object[]>>() {
					@SuppressWarnings("unchecked")
					public List<Object[]> doInJpa(EntityManager em)
							throws PersistenceException {
						Query queryObject = em.createQuery(hql.toString());
						getJpaTemplate().prepareQuery(queryObject);
						if (condition != null && condition.getValues() != null) {
							int i = 0;
							for (Object value : condition.getValues()) {
								queryObject.setParameter(i + 1, value);// jpa的索引号从1开始
								i++;
							}
						}
						// 计算某页第一条数据的全局索引号，从0开始并设置到queryObject
						queryObject.setFirstResult(Page.getFirstResult(pageNo,
								_pageSize));
						// 设置最大页数
						queryObject.setMaxResults(_pageSize);
						// queryObject.getResultList()结果可返回任意类型需强制转换
						return (List<Object[]>) queryObject.getResultList();
					}
				});

		// 组装map数据,模拟domain列显示
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;

		for (Object obj : list) {

			Object[] ary = (Object[]) obj;
			map = new HashMap<String, Object>();
			map.put("id", ary[0]);
			map.put("code", ary[1]);
			map.put("type", ary[2]);
			map.put("signDate", ary[3]);
			map.put("startDate", ary[4]);
			map.put("endDate", ary[5]);
			map.put("transactorName", ary[6]);
			map.put("ext_str1", ary[7]);
			map.put("ext_str2", ary[8]);

			result.add(map);
		}

		// 创建一个Page对象
		Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageNo,
				pageSize, count(condition, sqlStr), result);

		return page;
	}

	/**
	 * 根据查询条件统计总记录数
	 * 
	 * @param condition
	 * @param sqlStr
	 * @return count
	 */
	public int count(final Condition condition, String sqlStr) {

		final StringBuffer hql4Count = new StringBuffer();

		sqlStr = "select count(*) " + sqlStr;

		hql4Count.append(sqlStr);

		// 组合查询条件
		if (condition != null && condition.getValues().size() > 0) {
			setWhere(condition, hql4Count);
		}

		final String queryString = hql4Count.toString();

		if (logger.isDebugEnabled()) {
			logger.debug("hql4Count=" + queryString);
			logger.debug("args4Count="
					+ (condition != null ? StringUtils
							.collectionToCommaDelimitedString(condition
									.getValues()) : "null"));
		}

		// 实现getJapTemLate的execute方法返回一个Long值
		Long count = getJpaTemplate().execute(new JpaCallback<Long>() {
			public Long doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createQuery(queryString);
				getJpaTemplate().prepareQuery(queryObject);
				if (condition != null && condition.getValues() != null) {
					int i = 0;
					for (Object value : condition.getValues()) {
						queryObject.setParameter(i + 1, value);// jpa的索引号从1开始
						i++;
					}
				}
				return (Long) queryObject.getSingleResult();
			}
		});
		return count.intValue();
	}

	/**
	 * 组合查询条件
	 * 
	 * @param condition
	 * @param hql
	 */
	public void setWhere(Condition condition, StringBuffer hql) {
		hql.append(" AND ");
		hql.append(condition.getExpression());
	}

	/**
	 * 根据carManId查找cert信息
	 * 
	 * @parma carManId
	 * @return
	 */
	public Map<String, Object> findCertByCarManId(Long carManId) {
		Map<String, Object> queryMap = null;
		String sql = "select cert.id,cert.cert_code from BS_CARMAN_CERT cc inner join BS_CERT cert on cc.cert_id = cert.id"
				+ " where cert.type_=4 and cc.man_id=" + carManId;

		// jdbc查询BS_CERT记录
		try {
			queryMap = this.jdbcTemplate.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {
			e.getStackTrace();
			// logger.error(e.getMessage(), e);
		}
		return queryMap;
	}

	/**
	 * 根据合同ID查找车辆ID
	 * 
	 * @param contractId
	 * @return
	 */
	public Long findCarIdByContractId(Long contractId) {
		String sql = "select cc.car_id from BS_CAR_CONTRACT cc where cc.contract_id="
				+ contractId;
		Long carId = jdbcTemplate.queryForLong(sql);
		return carId;
	}

	/**
	 * 根据合同ID查找司机ID
	 * 
	 * @param contractId
	 * @return
	 */
	public Long findCarManIdByContractId(Long contractId) {
		String sql = "select cc.man_id from BS_CARMAN_CONTRACT cc where cc.contract_id="
				+ contractId;
		Long carManId = jdbcTemplate.queryForLong(sql);
		return carManId;
	}

	/**
	 * 根据车辆Id查找司机
	 * 
	 * @param carId
	 * @return
	 */
	public Map<String, Object> findCarManByCarId(Long carId) {
		Map<String, Object> queryMap = null;
		String sql = "SELECT man.id,man.name,man.cert_fwzg FROM BS_CAR_DRIVER cd left join Bs_Carman man on cd.driver_id = man.id"
				+ " where cd.car_id=" + carId;

		// jdbc查询BS_CARMAN记录
		try {
			queryMap = this.jdbcTemplate.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {
			e.getStackTrace();
			// logger.error(e.getMessage(), e);
		}

		return queryMap;
	}

	/**
	 * 根据司机ID相应的车
	 * 
	 * @param carManId
	 * @return
	 */
	public List<Map<String, Object>> selectRelateCarByCarManId(Long carManId) {
		List<Map<String, Object>> list = null;
		String sql = "SELECT car.id,car.plate_type,car.plate_no,car.bs_type,car.factory_type,car.factory_model,car.register_date,car.scrap_date,car.level_,car.vin,car.engine_no"
				+ ",car.total_weight,car.dim_len,car.dim_width,car.dim_height,car.access_weight,car.access_count,cd.car_id FROM BS_CAR_DRIVER cd left join BS_CAR car on cd.car_id = car.id"
				+ " where cd.driver_id="
				+ carManId
				+ " and cd.status_ in (-1,0) order by cd.file_date DESC";

		list = this.jdbcTemplate.queryForList(sql);

		return list;
	}

	/**
	 * 根据司机ID相应的车
	 * 
	 * @param carManId
	 * @return
	 */
	public Map<String, Object> findCarManByCarManId(Long carManId) {
		Map<String, Object> queryMap = null;
		String sql = "SELECT man.id,man.name,man.sex,man.cert_fwzg,man.cert_identity,man.origin,man.house_type"
				+ ",man.birthdate,man.status_ FROM BS_CARMAN man"
				+ " where man.id=" + carManId;

		// jdbc查询BS_CARMAN记录
		try {
			queryMap = this.jdbcTemplate.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {
			e.getStackTrace();
			// logger.error(e.getMessage(), e);
		}

		return queryMap;
	}

	/**
	 * 根据司机ID查找车辆
	 * 
	 * @param carId
	 * @return
	 */
	public Map<String, Object> findCarByCarManId(Long carId) {
		Map<String, Object> queryMap = null;
		String sql = "SELECT car.id,car.plate_type,car.plate_no,car.bs_type,car.factory_type,car.factory_model,car.register_date,car.scrap_date,car.level_,car.vin,car.engine_no"
				+ ",car.total_weight,car.dim_len,car.dim_width,car.dim_height,car.access_weight,car.access_count FROM BS_CAR car"
				+ " where car.id=" + carId;

		// jdbc查询BS_CAR记录
		try {
			List<Map<String, Object>> list = this.jdbcTemplate
					.queryForList(sql);
			if (list.size() > 0) {
				queryMap = list.get(0);
			}
		} catch (EmptyResultDataAccessException e) {
			e.getStackTrace();
			// logger.error(e.getMessage(), e);
		}

		return queryMap;
	}

	/**
	 * 根据车辆ID查找关联的司机
	 * 
	 * @param carId
	 * @return
	 */
	public List<Map<String, Object>> selectRelateCarManByCarId(Long carId) {
		List<Map<String, Object>> list = null;
		String sql = "SELECT man.id,man.name,man.sex,man.cert_fwzg,man.cert_identity,man.origin,man.house_type"
				+ ",man.birthdate,cd.driver_id FROM BS_CARMAN man left join BS_CAR_DRIVER cd on man.id=cd.driver_id"
				+ " where cd.car_id=" + carId;

		list = this.jdbcTemplate.queryForList(sql);

		return list;
	}

	public boolean isExistContractByDriverId(Long driverId) {
		String sql = "select c.* from BS_CONTRACT c"
				+ " inner join BS_CARMAN_CONTRACT cc ON c.id = cc.contract_id where c.type_="
				+ Contract.TYPE_LABOUR + " and cc.man_id=" + driverId
				+ " and c.status_=" + BCConstants.STATUS_ENABLED;

		List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql);
		return list != null && list.size() > 0;
	}

	/**
	 * 更新指定司机的户口性质
	 * 
	 * @param driverId
	 * @param houseType
	 */
	public void updateCarMan4HouseType(Long driverId, String houseType) {
		ArrayList<Object> args = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer();
		hql.append("UPDATE CarMan c SET c.houseType=? WHERE c.id =?");
		args.add(houseType);
		args.add(driverId);
		this.executeUpdate(hql.toString(), args);
	}

	/**
	 * 更新指定司机的司机id,户口性质,区域
	 * 
	 * @param driverId
	 * @param houseType
	 * @param region
	 */
	public void updateCarMan4CarManInfo(Long driverId, String houseType,
			Integer region) {
		ArrayList<Object> args = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer();
		hql.append("UPDATE CarMan c SET c.houseType=?,c.region=?");
		hql.append(" WHERE c.id =?");
		args.add(houseType);
		args.add(region);
		args.add(driverId);
		this.executeUpdate(hql.toString(), args);

	}

	/**
	 * 判断经济合同自编号唯一
	 * 
	 * @param excludeId
	 * @param code
	 * @return
	 */
	public Long checkInsurCodeIsExist(Long excludeId, String insurCode) {
		String sql = "select c.id as id,manc.man_id as carManId from BS_CONTRACT c"
				+ " inner join BS_CONTRACT_LABOUR cl ON c.id = cl.id"
				+ " left join BS_CARMAN_CONTRACT manc ON c.id = manc.contract_id"
				+ " where c.status_=? and cl.insurcode=? ";
		Object[] args;
		if (excludeId != null) {
			sql += " and c.id!=?";
			args = new Object[] { new Integer(Contract.STATUS_NORMAL),
					insurCode, excludeId };
		} else {
			args = new Object[] { new Integer(Contract.STATUS_NORMAL),
					insurCode };
		}
		List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql,
				args);
		if (list != null && !list.isEmpty())
			return new Long(list.get(0).get("carManId").toString());
		else
			return null;
	}

	public Map<String, Object> findCarByCarId(Long carId) {
		Map<String, Object> queryMap = null;
		String sql = "SELECT car.id,car.plate_type,car.plate_no,car.status_,car.company FROM BS_CAR car "
				+ " WHERE car.id =" + carId;

		// jdbc查询BS_CAR记录
		try {
			queryMap = this.jdbcTemplate.queryForMap(sql);
		} catch (EmptyResultDataAccessException e) {
			e.getStackTrace();
			// logger.error(e.getMessage(), e);
		}

		return queryMap;
	}

	// /**
	// * 更新司机的备注列
	// * @param driverId
	// * @param description
	// */
	// public void updateCarMan4Description(Long driverId,String description) {
	// ArrayList<Object> args = new ArrayList<Object>();
	// StringBuffer hql = new StringBuffer();
	// hql.append("UPDATE CarMan c SET c.description=? WHERE c.id =?");
	// args.add(description);
	// args.add(driverId);
	// this.executeUpdate(hql.toString(), args);
	// }
	
	
	public List<Map<String,String>> findContract4ChargerByContarct4LabourId(Long contractId){
		String hql="SELECT  to_char(b.sign_date,'YYYY-MM-DD'),1";
			hql+=" from BS_CONTRACT_CHARGER a";
			hql+=" inner join BS_CONTRACT b on b.id = a.id";
			hql+=" left join BS_CAR_CONTRACT c on c.contract_id = b.id";
			hql+=" left join BS_CAR_CONTRACT d on d.car_id = c.car_id";
			hql+=" left join BS_CONTRACT_LABOUR e on e.id=d.contract_id";
			hql+=" where e.id=? and b.type_=?";
			hql+=" Order by b.status_ asc,b.file_date desc";
		return	HibernateJpaNativeQuery.executeNativeSql(getJpaTemplate(), hql,new Object[]{contractId,Contract.TYPE_CHARGER}
			 	,new cn.bc.db.jdbc.RowMapper<Map<String, String>>() {
					public Map<String, String> mapRow(Object[] rs, int rowNum) {
						Map<String, String> oi = new HashMap<String, String>();
						int i = 0;
						oi.put("signDate", rs[i++].toString());
						return oi;
					}
				});
	};
}