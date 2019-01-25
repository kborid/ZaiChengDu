package com.z012.chengdu.sc.net.entity;

import java.io.Serializable;

/**
 * 选择银行卡
 * 
 * @author LiaoBo
 */
public class SelectBankBean implements Serializable{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	public String	id;				// 1,
	public String	accountid;		// skcd9eo3skcd9eo3skcd9eo3skcd9eo3
	public String	accountname;	// 测试
	public String	bindtype;		// 001 //000:商户号；001: 普通账户
	public String	thirdaccount;	//第三方账户ID
	public String   thirdaccountname;//三方账户名称
	public String	thirdtype;		// 第三方平台 000-支付宝 ；001-微信；002银行
	public String	uidstr;			// //平台商户号使用：支付宝：合作身份者ID； 微信钱包：Id
	public String	keystr;			// //平台商户号使用：key
	public String	scretstr;		// //商家客户号：微信：scret；支付宝：getway
	public String	namestr;		// //第三方账号（邮箱，账号，用户号等）
	public String	cardtype;		// 银行卡的类型
	public String	cardname;		// 银行卡名称
	public String	bankname;		//归属银行
//	public String	cardprefixnum;	// 银行卡前缀
	public String	bindtime;		// Feb 17, 2016 1:07:05 PM
	public String	status;			// 1
}
