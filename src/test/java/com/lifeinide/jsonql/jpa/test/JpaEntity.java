package com.lifeinide.jsonql.jpa.test;

import com.lifeinide.jsonql.core.test.IJsonQLTestEntity;
import com.lifeinide.jsonql.core.test.IJsonQLTestParentEntity;
import com.lifeinide.jsonql.core.test.JsonQLTestEntityEnum;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Lukasz Frankowski
 */
@Entity
public class JpaEntity implements IJsonQLTestEntity<Long>, IJsonQLTestParentEntity<Long, JpaAssociatedEntity> {

	@Id private Long id;

	protected String stringVal;

	protected boolean booleanVal;

	protected Long longVal;

	protected BigDecimal decimalVal;

	protected LocalDate dateVal;

	@Enumerated(EnumType.STRING)
	protected JsonQLTestEntityEnum enumVal;

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
	public boolean isBooleanVal() {
		return booleanVal;
	}

	@Override
	public void setBooleanVal(boolean booleanVal) {
		this.booleanVal = booleanVal;
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
	public JsonQLTestEntityEnum getEnumVal() {
		return enumVal;
	}

	@Override
	public void setEnumVal(JsonQLTestEntityEnum enumVal) {
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
