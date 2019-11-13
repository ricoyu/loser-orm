package com.loserico.orm.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.NullPrecedence;
import org.hibernate.criterion.Order;

import java.io.Serializable;
import java.util.Objects;

public class OrderBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String orderBy = "create_time";
	
	private ORDER_BY direction = ORDER_BY.DESC;

	public OrderBean() {

	}

	public OrderBean(String orderBy, ORDER_BY direction) {
		this.setOrderBy(orderBy);
		this.setDirection(direction);
	}

	public enum ORDER_BY {
		ASC, DESC
	}

	@JsonIgnore
	public Order getOrder() {
		Objects.requireNonNull(getDirection(), "排序方向不可以为null");
		Order order = null;

		switch (getDirection()) {
		case ASC:
			order = Order.asc(getOrderBy());
			break;
		case DESC:
			order = Order.desc(getOrderBy());
		}

		return order.nulls(NullPrecedence.LAST).ignoreCase();
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public ORDER_BY getDirection() {
		return direction;
	}

	public void setDirection(ORDER_BY direction) {
		this.direction = direction;
	}

}
