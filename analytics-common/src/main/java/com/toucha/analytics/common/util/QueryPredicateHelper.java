package com.toucha.analytics.common.util;

import java.util.List;

public class QueryPredicateHelper {

    private static String AND_SIGN = " AND ";

	public static <T> String buildInPredicate(String fieldName, List<T> values) {
		StringBuilder predicates = new StringBuilder();

		if (values != null && !values.isEmpty()) {
            predicates.append(AND_SIGN);
			predicates.append(fieldName);
			predicates.append(" IN (");
			for (T value : values) {
                predicates.append("'").append(value).append("'").append(",");
			}
			predicates.deleteCharAt(predicates.lastIndexOf(","));
			predicates.append(") ");
		}

		return predicates.toString();
	}

	public static String appendQuery(String query, String queryEnd, String... predicates) {
		if (predicates != null && predicates.length != 0) {
            int index = 0;
			for (String pred : predicates) {
				if (pred != null && !pred.isEmpty()) {
                    if (index == 0 && query.endsWith("WHERE")) {
                        query += (" " + pred.substring(AND_SIGN.length()));
                    } else {
                        query += pred;
                    }

				}
                index++;
			}
		}

        if (query.endsWith("WHERE")) {
            query += " " + queryEnd;
        } else {
            query += AND_SIGN + queryEnd;
        }

		return query;
	}

	public static String buildStatementQuery(String query, String queryEnd, String[] fields, List<?>... values) {
		if (fields.length != 0 && values.length != 0 && fields.length != values.length) {
			throw new IllegalArgumentException("Statement query fields and values length not equals.");
		}

		String[] predicates = new String[fields.length];
		for (int i=0; i<fields.length; i++) {
			String tmp = buildInPredicate(fields[i], values[i]);
			predicates[i] = tmp;
		}

		query = appendQuery(query, queryEnd, predicates);

		return query;
	}

	public static String[] getShopStaticFields() {
		return new String[] { "did", "promotion", "oid" };
	}
}
