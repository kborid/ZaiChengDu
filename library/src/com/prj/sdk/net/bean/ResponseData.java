package com.prj.sdk.net.bean;

/**
 * 服务器请求数据及返回
 * 
 * @author Liao
 * 
 */
public class ResponseData {
	public int					flag;
	public boolean				isForm;				// 是否是form表单数据提交
	public String				path;
	public String				type;
	public boolean				isLocal;

	public String				code;		// 出错代码
	public Object				body;
	public Object				head;
	public Object				data;		// 数据对象
}
