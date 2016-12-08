package com.toucha.analytics.common.util.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.toucha.analytics.common.util.QueryPredicateHelper;

public class TestQueryPredicateHelper {
	
	@Test
	public void test() {
		List<Integer> pids = new ArrayList<Integer>();
		
		pids.add(1);
		pids.add(2);
		pids.add(3);
		
		System.out.println(QueryPredicateHelper.buildInPredicate("product",pids));
	}
}
