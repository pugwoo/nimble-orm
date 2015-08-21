package com.pugwoo.dbhelper.model;

import java.util.List;

/**
 * 2015年4月22日 13:32:30
 * 记录分页数据和总数
 */
public class PageData <T> {

	private int total; // 总数
	
	private List<T> data; // 数据
	
	public PageData() {
	}
	
	public PageData(int total, List<T> data) {
		this.total = total;
		this.data = data;
	}
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public List<T> getData() {
		return data;
	}
	public void setData(List<T> data) {
		this.data = data;
	}
	
}
