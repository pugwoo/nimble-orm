package com.pugwoo.dbhelper.model;

import java.io.Serializable;
import java.util.List;

/**
 * 2015年4月22日 13:32:30 记录分页数据和总数
 */
public class PageData<T> implements Serializable {

	private static final long serialVersionUID = 3L;

	/**
	 * 总数
	 */
	private int total;

	/**
	 * 每页个数
	 */
	private int pageSize;

	/**
	 * 数据
	 */
	private List<T> data;

	public PageData() {
	}

	public PageData(int total, List<T> data, int pageSize) {
		this.total = total;
		this.data = data;
		this.pageSize = pageSize;
	}
	
	/**
	 * 总页数，通过计算得出来
	 * @return
	 */
	public int getTotalPage() {
		if(total <= 0) return 0;
		if(pageSize < 1) return total;
		return (total + pageSize - 1) / pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

}
