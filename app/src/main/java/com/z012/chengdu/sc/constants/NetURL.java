package com.z012.chengdu.sc.constants;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.ApiManager;

/**
 * 接口常量地址
 * 
 * @author kborid
 */
public final class NetURL {

	public static final String		API_LINK				= ApiManager.getBaseUrl();

    public static final String		PORTAL_SERVICE			= API_LINK + AppContext.mMainContext.getString(R.string.portal_server);										// portal地址
    public static final String		SMPAY_SERVICE			= API_LINK + "smpay/service/";											// smpay地址
    public static final String		SSO_SERVICE				= API_LINK + "sso/service/";
    public static final String		APPSTORE				= API_LINK + AppContext.mMainContext.getString(R.string.portal) + "appstoreserver/service/CW0001";
    public static final String		WEATHER_SERVER			= API_LINK + "center_weatherserver/service/CW0101";					    // 天气
    public static final String		UPLOAD					= API_LINK + "img/base64upload";										// H5上传图片到服务器

    public static final String		APP_SCITY_CN			= "http://app.scity.cn";
    public static final String		AVATAR					= APP_SCITY_CN + "image/get/system/avatar/";							// +userid.jpg
    public static final String		GET_SC_LIST				= APP_SCITY_CN + "/center_appservice/service/CW0207";					// 获取收藏列表
    public static final String		IS_SC					= APP_SCITY_CN + "/center_appservice/service/CW0210";					// 校验是否收藏
    public static final String		ADD_SC					= APP_SCITY_CN + "/center_appservice/service/CW0203";					// 添加收藏
    public static final String		REMOVE_SC				= APP_SCITY_CN + "/center_appservice/service/CW0204";					// 取消收藏

    public static final String		PUSH_COLUMN				= PORTAL_SERVICE + "CW1004";											// 获取手机端配置的快捷栏目及栏目下的应用
    public static final String		PUSH_MORE_SERVICE		= PORTAL_SERVICE + "CW10051";											// 加载首页更多服务
    public static final String		PUSH_SERVICE			= PORTAL_SERVICE + "CW1003";											// 7个推荐应用
    public static final String		PUSH_SERVICE_			= PORTAL_SERVICE + "CW10031";											// 新首页推荐服务
    public static final String		MORE_COLUMN				= PORTAL_SERVICE + "CW1005";											// 获取所有应用和栏目
    public static final String		BANNER					= PORTAL_SERVICE + "CW1006";											// 首页banner以及城市新鲜事
    public static final String		PM_2_5					= PORTAL_SERVICE + "CW1007";											// 获取空气质量
    public static final String		LIMIT_LINE				= PORTAL_SERVICE + "CW1008";											// 限行号
    public static final String		ALL_SERVICE_COLUMN		= PORTAL_SERVICE + "CW1012";											// 获得所有栏目以及所属的所有服务
    public static final String		NEWS					= PORTAL_SERVICE + "CW0005";											// 今日头条
    public static final String		ALL_APP					= PORTAL_SERVICE + "CW1001";											// 所有应用
	public static final String		GET_TICKET				= PORTAL_SERVICE + "CW9006";											// 登录获取票据
	public static final String		GET_USER_INFO			= PORTAL_SERVICE + "CW9012";											// 获取用户信息
	public static final String		UPDATE_USER_INFO		= PORTAL_SERVICE + "CW9013";											// 修改用户信息
    public static final String		FEEDBACK				= PORTAL_SERVICE + "CW9023";											// APP_SCITY_CN + "/center_complain/service/CW1001";
    public static final String		VERIFY_PWD				= PORTAL_SERVICE + "CW9014";											// 验证用户登录密码
    public static final String		GET_YZM					= PORTAL_SERVICE + "CW9003";											// 获取验证码
    public static final String		REGISTER				= PORTAL_SERVICE + "CW9005";											// 注册
    public static final String		UPDATA_PHONE			= PORTAL_SERVICE + "CW9015";											// 修改绑定手机号
    public static final String		FORGET_PWD				= PORTAL_SERVICE + "CW9021";											// 忘记密码
    public static final String		UPDATA_LOGIN_PWD		= PORTAL_SERVICE + "CW9018";											// 修改密码
    public static final String		CHECK_PHONE				= PORTAL_SERVICE + "CW9002";											// 手机号是否被占用
    public static final String		REMOVE_TICKET			= PORTAL_SERVICE + "CW9009";											// 注销票据
    public static final String		IDENTITY_VERIFICATION	= PORTAL_SERVICE + "CW0234";											// 实名认证
    public static final String		UPDATA_PHOTO			= PORTAL_SERVICE + "CW9022";											// 修改头像
    public static final String		NODE					= PORTAL_SERVICE + "CW1009";											// 埋点
    public static final String		VALIDATE_TICKET			= PORTAL_SERVICE + "CW1014";											// 判断票据是否过期
	// -----------------微观-----------------------------------------------
	public static final String		WG_ALL					= PORTAL_SERVICE + "CW1701";											// 所有有问必答列表
	public static final String		WG_MY_RELEASED			= PORTAL_SERVICE + "CW1703";											// 我发布的微观列表
	public static final String		WG_MY_ATTENTION			= PORTAL_SERVICE + "CW1704";											// 我关注的微观列表
	public static final String		WG_DETAILS				= PORTAL_SERVICE + "CW1721";											// 有问必答详情
	public static final String		WG_STATUS				= PORTAL_SERVICE + "CW1720";											// 微观关注状态
	public static final String		WG_UPLOD_IMG			= PORTAL_SERVICE + "CW1705";											// 上传微观图片
	public static final String		WG_RELEASE				= PORTAL_SERVICE + "CW1706";											// 发布微观
	public static final String		WG_ATTENTION			= PORTAL_SERVICE + "CW1707";											// 关注微观
	public static final String		WG_CANCEL_ATTENTION		= PORTAL_SERVICE + "CW1708";											// 取消关注微观
	public static final String		QA_PURSUE_ERROR			= PORTAL_SERVICE + "CW10002";											// 追问纠错
	public static final String		QA_PRAISE				= PORTAL_SERVICE + "CW10001";											// 点赞
	// -----------------地址管理-----------------------------------------
	public static final String		ADD_ADDRESS				= PORTAL_SERVICE + "UA0001";											// 新增地址
	public static final String		EDIT_ADDRESS			= PORTAL_SERVICE + "UA0002";											// 编辑地址
	public static final String		DELETE_ADDRESS			= PORTAL_SERVICE + "UA0003";											// 删除地址
	public static final String		SELECT_ADDRESS			= PORTAL_SERVICE + "UA0004";											// 查询全部地址
	// -----------------使用第三方登录绑定--------------------------------
	public static final String		BIND_CHECK				= PORTAL_SERVICE + "CW9025";											// 检查是否绑定了第三方
	public static final String		BIND_ACCESS				= PORTAL_SERVICE + "CW9026";											// 绑定三方访问,返回访问票据
	public static final String		BIND_LIST				= PORTAL_SERVICE + "CW1013";											// 绑定列表
	public static final String		UNBIND					= PORTAL_SERVICE + "CW1015";											// 解绑
	public static final String		BIND					= PORTAL_SERVICE + "CW1016";											// 绑定
	// -----------------app信息（强制升级、邀请信息等）、广告---------------------------------
	public static final String		APP_INFO				= PORTAL_SERVICE + "CW1010";											// app基本信息
	public static final String		ADVERTISEMENT			= PORTAL_SERVICE + "CW1011";											// 广告
	// -----------------邀请人员列表-----------------------------------
	public static final String		INVITE_LIST				= PORTAL_SERVICE + "CW9027";											// 邀请人员列表
	// -----------------账金-----------------------------------
	public static final String		TRANSACTION_LIST		= SMPAY_SERVICE + "CW0013";												// 获得某用户交易信息
	public static final String		BIND_BANK_LIST			= SMPAY_SERVICE + "CW1004";											    // 绑定的银行列表
	public static final String		BIND_BANK				= SMPAY_SERVICE + "CW1003";											    // 绑定银行卡
	public static final String		PAYORDER_LIST			= API_LINK + "smpay/public/order/service/PayOrder_list.jsp?userid=";	// 订单列表
	public static final String		CASHING					= SMPAY_SERVICE + "CW2001";											    // 提现
    // -----------------意见反馈-----------------------------------
//	public static final String		ABOUT_AS				= "http://m.scity.cn/appstore/html/510100_AboutInfo.html";				// 关于我们
//	public static final String		PROBLEM					= "http://m.scity.cn/appstore/html/510100_Faq.html";					// 常见问题
//	public static final String		REGISTER_AGEMENNT		= "http://m.scity.cn/appstore/html/510100_RegisterProtocol.html";		// 注册协议
//	public static final String		IDENTITY_PROTOCOL		= "http://m.scity.cn/appstore/html/510100_IdentityProtocol.html";		// 实名认证协议
    public static final String		IDENTITY_H5				= "http://www.cdwh.org/ht/ident/html/index.html";						// 实名认证H5
    public static final String		GET_CITY_INFO			= "http://open.scity.cn/appstore/api/menu/GetCityInfo";				    // 获取城市信息
	//------------------平安---------------------------------------
	public static final String      CERT_TIMES              = PORTAL_SERVICE + "PA10001"; //认证次数查询
    public static final String      CERT_STATUS_BY_UID      = PORTAL_SERVICE + "PA10002"; //根据用户id查询认证结果
    public static final String      CERT_STATUS_BY_CID      = PORTAL_SERVICE + "PA10003"; //根据身份证查询认证结果
    public static final String      CERT_INFO               = PORTAL_SERVICE + "PA10004"; //认证信息
	public static final String      CERT                    = PORTAL_SERVICE + "PA10005"; //认证
	public static final String      REGISTER_URL            = "http://www.zaichongqing.com/cq_portal/portal/public/common/register.jsp";
	public static final String      ABOUT_URL               = "http://www.zaichongqing.com/cq_portal/portal/public/common/aboutus.jsp";

	public static final String[]	CACHE_URL				= {ALL_APP, PUSH_SERVICE_, PUSH_MORE_SERVICE, ALL_SERVICE_COLUMN, NEWS, BANNER, MORE_COLUMN};
}
