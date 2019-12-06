package com.lifeinide.jsonql.jpa;

import com.lifeinide.jsonql.core.dto.Page;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * {@link JpaFilterQueryBuilder} with default {@link Page} results.
 *
 * @author lukasz.frankowski@gmail.com
 */
public class DefaultJpaFilterQueryBuilder<E> extends JpaFilterQueryBuilder<E, Page<E>> {

	public DefaultJpaFilterQueryBuilder(EntityManager entityManager, CriteriaQuery<E> query, Root<?> root) {
		super(entityManager, query, root);
	}

	public DefaultJpaFilterQueryBuilder(EntityManager entityManager, Class<E> rootClass) {
		super(entityManager, rootClass);
	}

}
