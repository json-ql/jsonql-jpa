package com.lifeinide.jsonql.jpa.test;

import com.lifeinide.jsonql.core.dto.BasePageableRequest;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.intr.PageableResult;
import com.lifeinide.jsonql.core.test.JsonQLBaseQueryBuilderTest;
import com.lifeinide.jsonql.jpa.JpaFilterQueryBuilder;
import com.lifeinide.jsonql.jpa.filter.LikeQueryFilter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Main JPA filtering test.
 *
 * @author Lukasz Frankowski
 */
public class JpaQueryBuilderTest extends JsonQLBaseQueryBuilderTest<
	EntityManager,
	Long,
	JpaEntity,
	JpaFilterQueryBuilder<JpaEntity, Page<JpaEntity>>
> {

	public static final String PERSISTENCE_UNIT_NAME = "test-jpa";

	public static final String SEARCHED_STRING_MATCHES = "phrase-%";
	public static final String SEARCHED_STRING_NOT_MATCHES = "phrase-not-exists-%";
	public static final String SEARCHED_STRING_FIRST_MATCH = "phrase-1";

	protected EntityManagerFactory entityManagerFactory;

	@BeforeAll
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		doWithEntityManager(em -> populateData(em::persist));
	}

	@AfterAll
	public void done() {
		if (entityManagerFactory!=null)
			entityManagerFactory.close();
	}

	@Nonnull
	@Override
	protected JpaEntity buildEntity(Long previousId) {
		return new JpaEntity(previousId==null ? 1L : previousId+1);
	}

	@Override
	protected JpaAssociatedEntity buildAssociatedEntity() {
		return new JpaAssociatedEntity(1L);
	}

	@Override
	protected void doTest(BiConsumer<EntityManager, JpaFilterQueryBuilder<JpaEntity, Page<JpaEntity>>> c) {
		doWithEntityManager(em -> c.accept(em, new JpaFilterQueryBuilder<>(em, JpaEntity.class).withUnlimitedResults()));
	}

	@Test
	public void testMatchingLikeQuery() {
		doTest((pc, qb) -> {
			PageableResult<JpaEntity> res = qb
					.add("stringVal", LikeQueryFilter.of(SEARCHED_STRING_MATCHES))
					.list(BasePageableRequest.ofUnpaged());
			Assertions.assertEquals(100, res.getCount());
			Assertions.assertEquals(SEARCHED_STRING_FIRST_MATCH, res.getData().iterator().next().getStringVal());
		});
	}

	@Test
	public void testNotMatchingLikeQuery() {
		doTest((pc, qb) -> {
			PageableResult<JpaEntity> res = qb
					.add("stringVal", LikeQueryFilter.of(SEARCHED_STRING_NOT_MATCHES))
					.list(BasePageableRequest.ofUnpaged());
			Assertions.assertEquals(0, res.getCount());
		});
	}

	protected void doWithEntityManager(Consumer<EntityManager> c) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		try {
			c.accept(entityManager);
		} finally {
			entityManager.getTransaction().commit();
			entityManager.close();
		}
	}

}
