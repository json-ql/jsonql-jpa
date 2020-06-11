package com.lifeinide.jsonql.jpa.filter;

import com.lifeinide.jsonql.core.intr.FilterQueryBuilder;
import com.lifeinide.jsonql.core.intr.QueryFilter;

/**
 * Filter for for like pattern matching.
 *
 * @author Eric Wright
 */
public class LikeQueryFilter implements QueryFilter {

	protected String pattern;

	public LikeQueryFilter() {
	}

	public LikeQueryFilter with(String value) {
		setPattern(value);
		return this;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public void accept(FilterQueryBuilder builder, String field) {
		builder.add(field, this);
	}

	public static LikeQueryFilter of(String pattern) {
		return new LikeQueryFilter().with(pattern);
	}

}
