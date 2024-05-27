package com.linkedin.batch;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;

public class TrackedOrderItemProcessor implements ItemProcessor<Order, TrackedOrder> {

	@Override
	public TrackedOrder process(Order item) throws Exception {
		TrackedOrder trackedOrder = new TrackedOrder(item);
		trackedOrder.setTrackingNumber(this.getTrackingNumber());
		return trackedOrder;
	}

	private String getTrackingNumber() throws OrderProcessingException {
		
		if(Math.random() < .30) {
			throw new OrderProcessingException();
		}

		return UUID.randomUUID().toString(); 
	}

}
