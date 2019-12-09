package com.lifeinide.jsonql.jpa.test;

import com.lifeinide.jsonql.core.test.IJsonQLBaseTestEntity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Lukasz Frankowski
 */
@Entity
public class JpaAssociatedEntity implements IJsonQLBaseTestEntity<Long> {

	@Id Long id;

	public JpaAssociatedEntity() {
	}

	public JpaAssociatedEntity(Long id) {
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
	
}
