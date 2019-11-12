package com.loserico.elastic.orm.bean;

import com.loserico.elastic.orm.bean.OrderBean.ORDER_BY;

public final class Orders {

	private Orders() {

	}

	public static OrderBean asc(String propertyName) {
		return new OrderBean(propertyName, ORDER_BY.ASC);
	}

	public static OrderBean desc(String propertyName) {
		return new OrderBean(propertyName, ORDER_BY.DESC);
	}

}
