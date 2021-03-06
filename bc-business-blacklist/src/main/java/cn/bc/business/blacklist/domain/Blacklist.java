/**
 * 
 */
package cn.bc.business.blacklist.domain;

import java.util.Calendar;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cn.bc.business.car.domain.Car;
import cn.bc.business.carman.domain.CarMan;
import cn.bc.business.motorcade.domain.Motorcade;
import cn.bc.identity.domain.Actor;
import cn.bc.identity.domain.FileEntityImpl;

/**
 * 黑名单
 * 
 * @author dragon
 */
@Entity
@Table(name = "BS_BLACKLIST")
@Inheritance(strategy = InheritanceType.JOINED)
public class Blacklist extends FileEntityImpl {
	private static final long serialVersionUID = 1L;
	public static final String KEY_CODE = "blacklist.code";
	// /** 状态：新建 */
	// public static final int STATUS_CREATE = 2;
	/** 状态：锁定 */
	public static final int STATUS_LOCK = 0;
	/** 状态：解锁 */
	public static final int STATUS_UNLOCK = 1;

	private int status;// 状态
	private String subject;// 主题
	private String type;// 类型，如提醒、限制一切业务
	private String level;// 等级
	private String code;// 编号
	private String lockReason;// 锁定原因
	private String unlockReason;// 解锁原因
	private Actor locker;// 锁定人
	private Actor unlocker;// 解锁人

	private Calendar lockDate;// 锁定时间
	private Calendar unlockDate;// 解锁时间

	private Calendar appointDate;// 指定日期
	private String conversionType;// 替换的限制项目，如提醒、限制一切业务

	private String company;// 车属公司：如宝城、广发
	private Motorcade motorcade;// 车队
	private Car car;// 车辆
	private String drivers;// 司机信息
	private Set<CarMan> carMan;// 司机列表
	private Long relatedDepartmenntsId;// 相关部门Id

	@Column(name = "APPOINT_DATE ")
	public Calendar getAppointDate() {
		return appointDate;
	}

	public void setAppointDate(Calendar appointDate) {
		this.appointDate = appointDate;
	}

	@Column(name = "CONVERSION_TYPE ")
	public String getConversionType() {
		return conversionType;
	}

	public void setConversionType(String conversionType) {
		this.conversionType = conversionType;
	}

	public String getDrivers() {
		return drivers;
	}

	public void setDrivers(String drivers) {
		this.drivers = drivers;
	}

	@Column(name = "STATUS_")
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "TYPE_")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "LEVEL_")
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Column(name = "LOCK_REASON")
	public String getLockReason() {
		return lockReason;
	}

	public void setLockReason(String lockReason) {
		this.lockReason = lockReason;
	}

	@Column(name = "UNLOCK_REASON")
	public String getUnlockReason() {
		return unlockReason;
	}

	public void setUnlockReason(String unlockReason) {
		this.unlockReason = unlockReason;
	}

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "LOCKER_ID", referencedColumnName = "ID")
	public Actor getLocker() {
		return locker;
	}

	public void setLocker(Actor locker) {
		this.locker = locker;
	}

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "UNLOCKER_ID", referencedColumnName = "ID")
	public Actor getUnlocker() {
		return unlocker;
	}

	public void setUnlocker(Actor unlocker) {
		this.unlocker = unlocker;
	}

	@Column(name = "LOCK_DATE")
	public Calendar getLockDate() {
		return lockDate;
	}

	public void setLockDate(Calendar lockDate) {
		this.lockDate = lockDate;
	}

	@Column(name = "UNLOCK_DATE")
	public Calendar getUnlockDate() {
		return unlockDate;
	}

	public void setUnlockDate(Calendar unlockDate) {
		this.unlockDate = unlockDate;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	@Column(name = "RELATED_DEPARTMENNTS_ID")
	public Long getRelatedDepartmenntsId() {
		return relatedDepartmenntsId;
	}

	public void setRelatedDepartmenntsId(Long relatedDepartmenntsId) {
		this.relatedDepartmenntsId = relatedDepartmenntsId;
	}

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "MOTORCADE_ID", referencedColumnName = "ID")
	public Motorcade getMotorcade() {
		return motorcade;
	}

	public void setMotorcade(Motorcade motorcade) {
		this.motorcade = motorcade;
	}

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "CAR_ID", referencedColumnName = "ID")
	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "BS_CARMAN_BLACKLIST", joinColumns = @JoinColumn(name = "BLACKLIST_ID", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "MAN_ID", referencedColumnName = "ID"))
	public Set<CarMan> getCarMan() {
		return carMan;
	}

	public void setCarMan(Set<CarMan> carMan) {
		this.carMan = carMan;
	}

}