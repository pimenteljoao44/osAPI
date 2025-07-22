package com.joao.osMarmoraria.controle.exceptions;

import java.io.Serializable;

public class StandardError implements Serializable {


	private static final long serialVersionUID = 1L;
	
	private Long timestmp;
	private Integer status;
	private String error;
	
	
	public StandardError(Long timestmp, Integer status, String error) {
		super();
		this.timestmp = timestmp;
		this.status = status;
		this.error = error;
	}
	public Long getTimestmp() {
		return timestmp;
	}
	public void setTimestmp(Long timestmp) {
		this.timestmp = timestmp;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	
}
