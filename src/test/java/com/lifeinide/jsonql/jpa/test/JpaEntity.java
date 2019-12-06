package com.lifeinide.jsonql.jpa.test;

import com.lifeinide.jsonql.core.test.EntityEnum;
import com.lifeinide.jsonql.core.test.IEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Lukasz Frankowski
 */
@Entity
public class JpaEntity implements IEntity<Long, JpaAssociatedEntity> {

	@Id private Long id;

	protected String stringVal;

	protected Long longVal;

	protected BigDecimal decimalVal;

	protected LocalDate dateVal;

	@Enumerated(EnumType.STRING)
	protected EntityEnum enumVal;

	@ManyToOne
	protected JpaAssociatedEntity entityVal;

	public JpaEntity() {
	}

	public JpaEntity(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getStringVal() {
		return stringVal;
	}

	@Override
	public void setStringVal(String stringVal) {
		this.stringVal = stringVal;
	}

	@Override
	public Long getLongVal() {
		return longVal;
	}

	@Override
	public void setLongVal(Long longVal) {
		this.longVal = longVal;
	}

	@Override
	public BigDecimal getDecimalVal() {
		return decimalVal;
	}

	@Override
	public void setDecimalVal(BigDecimal decimalVal) {
		this.decimalVal = decimalVal;
	}

	@Override
	public LocalDate getDateVal() {
		return dateVal;
	}

	@Override
	public void setDateVal(LocalDate dateVal) {
		this.dateVal = dateVal;
	}

	@Override
	public EntityEnum getEnumVal() {
		return enumVal;
	}

	@Override
	public void setEnumVal(EntityEnum enumVal) {
		this.enumVal = enumVal;
	}

	@Override
	public JpaAssociatedEntity getEntityVal() {
		return entityVal;
	}

	@Override
	public void setEntityVal(JpaAssociatedEntity entityVal) {
		this.entityVal = entityVal;
	}
	
}
