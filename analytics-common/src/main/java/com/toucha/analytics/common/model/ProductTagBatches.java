package com.toucha.analytics.common.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ProductTagBatches {
	@JSONField(name="pid")
	int productId;
	@JSONField(name="tbs")
	List<Integer> tagBatches;
	
	public ProductTagBatches() {
		tagBatches = new ArrayList<Integer>();
	}
	
	public ProductTagBatches(int productId, List<Integer> tagBatches) {
		super();
		this.productId = productId;
		this.tagBatches = tagBatches;
	}
	
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public List<Integer> getTagBatches() {
		return tagBatches;
	}
	public void setTagBatches(List<Integer> tagBatches) {
		this.tagBatches = tagBatches;
	}

	@Override
	public String toString() {
		return "ProductTags [productId=" + productId + ", tagBatches="
				+ tagBatches + "]";
	}
}
