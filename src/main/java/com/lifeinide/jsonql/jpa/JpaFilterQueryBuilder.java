package com.lifeinide.jsonql.jpa;

import com.lifeinide.jsonql.core.BaseFilterQueryBuilder;
import com.lifeinide.jsonql.core.dto.BasePageableRequest;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.enums.QueryConjunction;
import com.lifeinide.jsonql.core.filters.*;
import com.lifeinide.jsonql.core.intr.FilterQueryBuilder;
import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.Sortable;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link FilterQueryBuilder} with JPA {@link CriteriaBuilder}. Use with dependency:
 *
 * <pre>{@code compile group: 'javax.persistence', name:'javax.persistence-api', version: '2.2'}</pre>
 *
 * @author Lukasz Frankowski
 */
public class JpaFilterQueryBuilder<E, P extends Page<E>>
extends BaseFilterQueryBuilder<E, P, CriteriaQuery<E>, JpaQueryBuilderContext<E>, JpaFilterQueryBuilder<E, P>> {

	public static final Logger logger = LoggerFactory.getLogger(JpaFilterQueryBuilder.class);

	protected JpaQueryBuilderContext<E> context;
	protected QueryConjunction conjunction = QueryConjunction.and;

	protected JpaFilterQueryBuilder(@Nonnull EntityManager entityManager) {
		this.context = new JpaQueryBuilderContext<>(entityManager, entityManager.getCriteriaBuilder());
	}

	public JpaFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nonnull CriteriaQuery<E> query, @Nonnull Root<?> root) {
		this(entityManager);
		init(query, root);
	}

	public JpaFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nonnull Class<E> rootClass) {
		this(entityManager);
		CriteriaQuery<E> query = context.getCb().createQuery(rootClass);
		init(query, query.from(rootClass));
	}

	protected void init(CriteriaQuery<E> query, Root<?> root) {
		context.setQuery(query);
		context.setRoot(root);
		if (root!=null && root.getJavaType()!=null)
			root.alias(createAlias(root.getJavaType()));
	}

	public JpaFilterQueryBuilder<E, P> withOrConjunction() {
		conjunction = QueryConjunction.or;
		return this;
	}

	@Nonnull
	@Override
	public JpaQueryBuilderContext<E> context() {
		return context;
	}

	@Nonnull
	@Override
	public JpaFilterQueryBuilder<E, P> add(@Nonnull String field, DateRangeQueryFilter filter) {
		if (filter!=null) {
			LocalDate from = filter.calculateFrom();
			LocalDate to = filter.calculateTo();

			Predicate predicate = from==null ? null : context.getCb().greaterThanOrEqualTo(context.getRoot().get(field), from);
			Predicate toPredicate = to==null ? null : context.getCb().lessThan(context.getRoot().get(field), to);

			predicate = (predicate!=null && toPredicate!=null)
				? context.getCb().and(predicate, toPredicate)
				: predicate != null ? predicate : toPredicate;

			if (predicate!=null)
				context.getPredicates().add(predicate);
		}

		return this;
	}

	@Nonnull
	@Override
	public JpaFilterQueryBuilder<E, P> add(@Nonnull String field, EntityQueryFilter<?> filter) {
		if (filter!=null) {
			// discover id field name of the associated entity
			Class<?> associatedEntityJavaType = context.getRoot().get(field).getJavaType();
			EntityType<?> associatedEntityType = context.getEntityManager().getEntityManagerFactory().getMetamodel().entity(associatedEntityJavaType);
			Class<?> idJavaType = associatedEntityType.getIdType().getJavaType();
			SingularAttribute<?, ?> id = associatedEntityType.getId(idJavaType);

			context.getPredicates().add(JpaCriteriaBuilderHelper.INSTANCE.buildCriteria(filter.getCondition(), context.getCb(),
				context.getRoot().get(field).get(id.getName()), filter.getValue()));
		}

		return this;
	}

	@Nonnull
	@Override
	public JpaFilterQueryBuilder<E,P> add(@Nonnull String field, ListQueryFilter<?> filter) {
		if (filter!=null) {
			JpaFilterQueryBuilder<E, P> internalBuilder =
				new JpaFilterQueryBuilder<>(context.getEntityManager(), context.getQuery(), context.getRoot());

			if (QueryConjunction.or.equals(filter.getConjunction()))
				internalBuilder.withOrConjunction();

			filter.getFilters().forEach(f -> f.accept(internalBuilder, field));
			internalBuilder.buildPredicate().ifPresent(predicate -> context.getPredicates().add(predicate));
		}

		return this;
	}

	@Nonnull
	@Override
	public JpaFilterQueryBuilder<E,P> add(@Nonnull String field, LikeQueryFilter filter) {
		if (filter!=null) {
			String pattern = filter.getPattern().toString();
			Predicate predicate = pattern==null ? null : context.getCb().like(context.getRoot().get(field), pattern);
			context.getPredicates().add(predicate);
		}

		return this;
	}

	@Nonnull
	@Override
	public JpaFilterQueryBuilder<E, P> add(@Nonnull String field, SingleValueQueryFilter<?> filter) {
		if (filter!=null) {
			context.getPredicates().add(JpaCriteriaBuilderHelper.INSTANCE.buildCriteria(filter.getCondition(),
				context.getCb(), context.getRoot().get(field), filter.getValue()));
		}
		
		return this;
	}

	@Nonnull
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public JpaFilterQueryBuilder<E, P> add(@Nonnull String field, ValueRangeQueryFilter<? extends Number> filter) {
		if (filter!=null) {
			Number from = filter.getFrom();
			Number to = filter.getTo();

			Predicate predicate = from==null ? null : context.getCb().greaterThanOrEqualTo(context.getRoot().get(field), (Comparable) from);
			Predicate toPredicate = to==null ? null : context.getCb().lessThanOrEqualTo(context.getRoot().get(field), (Comparable) to);

			predicate = (predicate!=null && toPredicate!=null)
				? context.getCb().and(predicate, toPredicate)
				: predicate != null ? predicate : toPredicate;

			if (predicate!=null)
				context.getPredicates().add(predicate);
		}

		return this;
	}

	@Nonnull
	@Override
	public JpaFilterQueryBuilder<E, P> or(@Nonnull Runnable r) {
		List<Predicate> orPredicates = context.doWithNewPredicates(r);
		if (!orPredicates.isEmpty())
			context.getPredicates().add(context.getCb().or(orPredicates.toArray(new Predicate[0])));
		return this;
	}

	@Nonnull
	@Override
	public JpaFilterQueryBuilder<E, P> and(@Nonnull Runnable r) {
		List<Predicate> andPredicates = context.doWithNewPredicates(r);
		if (!andPredicates.isEmpty())
			context.getPredicates().add(context.getCb().and(andPredicates.toArray(new Predicate[0])));
		return this;
	}

	@Nonnull
	@Override
	public CriteriaQuery<E> build(@Nonnull Pageable pageable, @Nonnull Sortable<?> sortable) {
		return context.getQuery();
	}

	protected Optional<Predicate> buildPredicate() {
		if (!context.getPredicates().isEmpty()) {
			if (QueryConjunction.and.equals(conjunction)) {
				return Optional.of(context.getCb().and(context.getPredicates().toArray(new Predicate[]{})));
			} else {
				return Optional.of(context.getCb().or(context.getPredicates().toArray(new Predicate[]{})));
			}
		}

		return Optional.empty();
	}

	@Nonnull
	@SuppressWarnings({"unchecked", "rawtypes", "ConstantConditions"})
	@Override
	public P list(Pageable pageable, Sortable<?> sortable) {
		if (pageable==null)
			pageable = BasePageableRequest.ofUnpaged();
		if (sortable==null)
			sortable = BasePageableRequest.ofUnpaged();

		// apply predicates
		buildPredicate().ifPresent(predicate -> context.getQuery().where(predicate));

		// first we calculate count
		Selection<E> selection = context.getQuery().getSelection();
		CriteriaQuery countQuery = context.getQuery();
		countQuery.select(context.getCb().count(context.getRoot()));
		TypedQuery cq = context.getEntityManager().createQuery(countQuery);
		if (logger.isTraceEnabled() && isHibernate())
			logger.trace("Executing JPA query: {}", ((Query) cq).getQueryString());
		Long count = (Long) cq.getSingleResult();
		context.getQuery().select(selection); // restore selection afterwards

		// apply orders
		List<Order> orders = sortable.getSort().stream()
			.map(sort -> sort.isAsc()
				? context.getCb().asc(context.getRoot().get(sort.getSortField()))
				: context.getCb().desc(context.getRoot().get(sort.getSortField())))
			.collect(Collectors.toList());
		if (!orders.isEmpty())
			context.getQuery().orderBy(orders);

		// apply pagination
		TypedQuery<E> q = context.getEntityManager().createQuery(context.getQuery());
		if (pageable.isPaged())
			q.setFirstResult(pageable.getOffset()).setMaxResults(getPageSize(pageable));
		else if (maxResults!=null)
			q.setMaxResults(maxResults);
		if (logger.isTraceEnabled() && isHibernate())
			logger.trace("Executing JPA query: {}", ((Query) q).getQueryString());

		// create and execute main query
		return (P) buildPageableResult(getPageSize(pageable), pageable.getPage(), count, q.getResultList());
	}

	protected boolean isHibernate() {
		try {
			Class.forName("org.hibernate.query.Query");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
}
