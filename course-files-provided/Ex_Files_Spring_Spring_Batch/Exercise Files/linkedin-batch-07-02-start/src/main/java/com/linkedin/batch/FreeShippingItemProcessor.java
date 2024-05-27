package com.linkedin.batch;

import java.math.BigDecimal;

import org.springframework.batch.item.ItemProcessor;

public class FreeShippingItemProcessor implements ItemProcessor<TrackedOrder, TrackedOrder> {

	@Override
	public TrackedOrder process(TrackedOrder item) throws Exception {
		
		if(item.getCost().compareTo(new BigDecimal("80")) == 1) {
			item.setFreeShipping(true);
		}else {
			item.setFreeShipping(false);
		}
		
		return item.isFreeShipping() ? item:null;
	}

}
