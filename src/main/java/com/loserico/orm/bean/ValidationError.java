package com.loserico.orm.bean;

import java.io.Serializable;

public class ValidationError implements Serializable{
	private static final long serialVersionUID = 6692252148592620144L;
	private String code;
	private String message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}