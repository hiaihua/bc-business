/**
 * 
 */
package cn.bc.business.car.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import cn.bc.core.EntityImpl;

/**
 * ����
 * 
 * @author dragon
 */
@Entity
@Table(name = "BS_CAR")
public class Car extends EntityImpl {
	private static final long serialVersionUID = 1L;

	private String name;// ����

	private String description;// ��ע

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "DESC_")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}