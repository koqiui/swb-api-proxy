package com.swb.tool.cmn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.swb.common.http.Ajax;
import com.swb.common.util.HtmlUtil;
import com.swb.common.util.StrUtil;
import com.swb.tool.cmn.SwbResult.Type;

public class SwbApiHelper {
	private final static Log logger = LogFactory.getLog(SwbApiHelper.class);

	public static byte[] toMd5Bytes(String inputStr) throws IOException {
		byte[] inputBytes = inputStr.getBytes("UTF-8");
		ByteArrayInputStream bis = new ByteArrayInputStream(inputBytes);
		return DigestUtils.md5(bis);
	}

	/** 先转md5，再转为hex */
	public static String toMd5HexStr(String inputStr) throws IOException {
		byte[] digest = toMd5Bytes(inputStr);
		//
		StringBuffer hexstr = new StringBuffer();
		String shaHex = "";
		for (int i = 0; i < digest.length; i++) {
			shaHex = Integer.toHexString(digest[i] & 0xFF);
			if (shaHex.length() < 2) {
				hexstr.append(0);
			}
			hexstr.append(shaHex);
		}
		return hexstr.toString();
	}

	// ------------------------------------------------------------------------------------------------------

	/** 生成获取access token 用的认证代码 */
	public static String makeAuthCode(String appKey, String appSecret, String timestamp) throws IOException {
		if (timestamp == null) {
			timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		}
		System.out.println("timestamp => " + timestamp);
		//
		String strForAuthCode = appKey + timestamp + appSecret;
		//
		return toMd5HexStr(strForAuthCode);
	}

	// ------------------------------------------------------------------------------------------------------

	// 环境（非必须，用于首页展示）
	private static String appEnvName;

	public static String getAppEnvName() {
		return appEnvName;
	}

	public static void setAppEnvName(String appEnvName) {
		SwbApiHelper.appEnvName = appEnvName;
	}

	// 基础访问信息（测试环境和生产环境不同）
	// 1、基础url
	// 测试环境 http://open.shangwb.com ，
	// 生产环境 https://open.sweib.com
	private static String appBaseUrl;

	public static void setAppBaseUrl(String appBaseUrl) {
		SwbApiHelper.appBaseUrl = appBaseUrl;
	}

	public static String getAppBaseUrl() {
		return SwbApiHelper.appBaseUrl;
	}

	// token获取url（相对于基础url，固定不变）
	private static final String tokenFetchUrl = "/oauth/access/token";

	// 2、认证信息

	private static String appKey;
	private static String appSecret;

	public static String getAppKey() {
		return appKey;
	}

	/** 设置appKey */
	public static void setAppKey(String appKey) {
		SwbApiHelper.appKey = appKey;
	}

	/** 设置appSecret */
	public static void setAppSecret(String appSecret) {
		SwbApiHelper.appSecret = appSecret;
	}

	/** 从属性文件配置 */
	public static void config(Properties appConf) {
		if (appConf == null || appConf.isEmpty()) {
			logger.error("无效配置信息");
		} else {
			String tmpValue = null;

			tmpValue = appConf.getProperty("appEnvName");
			if (StrUtil.hasText(tmpValue)) {
				appEnvName = tmpValue.trim();
				//
				logger.info("appEnvName : " + appEnvName);
			}

			int itemCount = 0;
			tmpValue = appConf.getProperty("appBaseUrl");
			if (StrUtil.hasText(tmpValue)) {
				appBaseUrl = tmpValue.trim();
				itemCount++;
				//
				logger.info("appBaseUrl : " + appBaseUrl);
			}

			tmpValue = appConf.getProperty("appKey");
			if (StrUtil.hasText(tmpValue)) {
				appKey = tmpValue.trim();
				itemCount++;
				//
				logger.info("appKey : " + appKey);
			}

			tmpValue = appConf.getProperty("appSecret");
			if (StrUtil.hasText(tmpValue)) {
				appSecret = tmpValue.trim();
				itemCount++;
				//
				logger.info("appSecret : " + appSecret);
			}

			if (itemCount == 3) {
				logger.info("app配置加载完毕");
			} else {
				logger.warn("app配置部分加载");
			}
		}
	}

	// -------------------------------------------- 示例代码 --------------------------------------------------

	private static String lastCachedToken = null;
	private static long nextRefreshTime = 0L;

	private static TokenRefresher tokenRefresher = null;
	private static Lock tokenLocker = new ReentrantLock();

	private static long refreshInterval = 10 * 60 * 1000;

	private static class TokenRefresher extends Thread {

		@Override
		public void run() {
			logger.info("Token刷新线程已开启");
			while (true) {
				if (nextRefreshTime <= 0 || nextRefreshTime >= System.currentTimeMillis()) {
					try {
						getAccessToken(true);
					} catch (IOException e) {
						logger.error(e);
					}
				}
				//
				try {
					long interval = refreshInterval;
					if (lastCachedToken == null) {
						interval = 30000; // 如果没值，间隔放短为30秒
					}
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					logger.debug("Token刷新线程已中断");
					break;
				}
			}
			logger.info("Token刷新线程已结束");
		}
	}

	/** 启动token刷新 */
	public static void startTokenRefresher() {
		try {
			tokenLocker.lock();
			//
			if (tokenRefresher == null || tokenRefresher.isInterrupted()) {
				tokenRefresher = new TokenRefresher();
				tokenRefresher.start();
			}
		} finally {
			tokenLocker.unlock();
		}

		logger.debug("start TokenRefresher");
	}

	/** 停止token刷新 */
	public static void stopTokenRefresher() {
		try {
			tokenLocker.lock();
			//
			if (tokenRefresher != null && tokenRefresher.isAlive()) {
				tokenRefresher.interrupt();
			}
			tokenRefresher = null;
			//
			lastCachedToken = null;
			nextRefreshTime = 0L;
		} finally {
			tokenLocker.unlock();
		}

		logger.debug("stop TokenRefresher");
	}

	// 示例代码（演示如何获取 应用访问所需的 accessToken，建议使用自己封装的的http请求工具实现）
	public static String getAccessToken() throws IOException {
		return getAccessToken(false);
	}

	@SuppressWarnings("unchecked")
	public static String getAccessToken(boolean refresh) throws IOException {
		if (lastCachedToken == null || refresh) {
			// 1、生成时间戳
			String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			// 2、生成authCode
			String authCode = makeAuthCode(appKey, appSecret, timestamp);
			// 3、设置请求参数
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("client_id", appKey);
			params.put("timestamp", timestamp);
			params.put("auth_code", authCode);
			params.put("grant_type", "client_credentials");

			try {
				String pureUrl = appBaseUrl + tokenFetchUrl;
				//
				Ajax ajax = new Ajax();
				ajax.get(pureUrl);
				ajax.params(params);
				ajax.asJson();
				ajax.forJson();
				ajax.send();

				String httpResult = ajax.resultAsText();
				logger.debug(pureUrl + " 返回结果：" + httpResult);

				String jsonStr = null;
				String lastErrorText = null;
				if (StrUtil.isNullOrBlank(httpResult)) {
					lastErrorText = ajax.getLastErrorText();
					System.err.println(pureUrl + " 调用 错误：" + lastErrorText);
					logger.error(pureUrl + " 调用 错误：" + lastErrorText);
				} else {
					httpResult = httpResult.trim();
					if (httpResult.startsWith("{") && httpResult.endsWith("}")) {// 有效Json
						jsonStr = httpResult;
					} else {// 不是Json
						lastErrorText = HtmlUtil.extractHtmlText(httpResult);
					}
				}
				//
				ajax.over();

				SwbResult<Map<String, Object>> result = null;
				if (jsonStr == null) {
					result = SwbResult.newOne();
					result.type = Type.error;
					result.message = lastErrorText;
				} else {
					result = (SwbResult<Map<String, Object>>) SwbResult.fromJson(jsonStr, SwbResult.StringObjectMapClass);
				}

				String accessToken = null;
				if (result.isSuccess()) {
					Map<String, Object> data = result.data;
					accessToken = (String) data.get("access_token");
					// 缓存access_token
					if (accessToken != null) {
						lastCachedToken = accessToken;
						Long expiresInSeconds = (Long) data.get("expires_in");
						if (expiresInSeconds == null || expiresInSeconds <= 0) {
							expiresInSeconds = 7200L; // 默认：2小时
						}
						nextRefreshTime = System.currentTimeMillis() + (expiresInSeconds * 1000 / 2);
						//
						logger.info("access_token 已获取/刷新 " + lastCachedToken);
					}
				}
				if (accessToken == null) {
					String message = result.message;
					System.err.println("获取access_token失败：" + message);
					logger.error("获取access_token失败：" + message);
				}
			} catch (Exception ex) {
				System.err.println(ex);
				logger.error(ex);
			}
		}
		//
		return lastCachedToken;
	}

	/**
	 * 生成访问url（如果有accessToken则会追加 access_token, timestamp, auth_code）
	 * 
	 * @param apiUrl
	 *            接口url（相对于基础url的路径，比如 /api/demo/merchant/app/get）
	 * @param params
	 *            接口参数
	 * 
	 * @return 附加了app访问认证所需的 access_token, timestamp, auth_code 公共参数的url
	 */
	public static String makeAccessUrl(String apiUrl, Map<String, Object> params, String accessToken) throws IOException {
		// 组成Url部分
		String pureUrl = (apiUrl.startsWith("http://") || apiUrl.startsWith("https://")) ? apiUrl : appBaseUrl + apiUrl;
		//
		if (params == null) {
			params = new HashMap<String, Object>();
		}
		// 随业务参数 追加公共访问参数
		if (accessToken != null) {
			// 0、Token
			params.put("access_token", accessToken);
			// 1、时间戳
			String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			params.put("timestamp", timestamp);
			// 2、authCode
			String authCode = makeAuthCode(appKey, appSecret, timestamp);
			params.put("auth_code", authCode);
		}

		return SwbUtil.makeUrl(pureUrl, params);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static SwbResult<?> sendGetRequest(String apiUrl, Map<String, Object> params, String accessToken) {
		SwbResult result = null;
		try {
			//
			String pureUrl = appBaseUrl + apiUrl;
			String fullUrl = makeAccessUrl(pureUrl, params, accessToken);
			System.out.println(fullUrl);
			//
			Ajax ajax = new Ajax();
			ajax.get(fullUrl);
			ajax.asJson();
			ajax.forJson();
			ajax.send();

			String httpResult = ajax.resultAsText();
			logger.debug(pureUrl + " 返回结果：" + httpResult);

			String jsonStr = null;
			String lastErrorText = null;
			if (StrUtil.isNullOrBlank(httpResult)) {
				lastErrorText = ajax.getLastErrorText();
				System.err.println(pureUrl + " 调用 错误：" + lastErrorText);
				logger.error(pureUrl + " 调用 错误：" + lastErrorText);
			} else {
				httpResult = httpResult.trim();
				if (httpResult.startsWith("{") && httpResult.endsWith("}")) {// 有效Json
					jsonStr = httpResult;
				} else {// 不是Json
					lastErrorText = HtmlUtil.extractHtmlText(httpResult);
				}
			}
			//
			ajax.over();

			if (jsonStr == null) {
				result = SwbResult.newOne();
				result.type = Type.error;
				result.message = lastErrorText;
			} else {
				result = SwbResult.fromJson(jsonStr, null);
			}

		} catch (Exception ex) {
			System.err.println(ex);
			logger.error(ex);

			result = new SwbResult();
			result.type = SwbResult.Type.error;
			result.message = ex.getMessage();
		}

		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static SwbResult<?> sendPostRequest(String apiUrl, Map<String, Object> params, Object data, String accessToken) {
		SwbResult result = null;
		try {
			String pureUrl = appBaseUrl + apiUrl;
			String fullUrl = makeAccessUrl(pureUrl, params, accessToken);
			System.out.println(fullUrl);
			//
			Ajax ajax = new Ajax();
			ajax.post(fullUrl);
			ajax.data(data);
			ajax.asJson();
			ajax.forJson();
			ajax.send();

			String httpResult = ajax.resultAsText();
			logger.debug(pureUrl + " 返回结果：" + httpResult);

			String jsonStr = null;
			String lastErrorText = null;
			if (StrUtil.isNullOrBlank(httpResult)) {
				lastErrorText = ajax.getLastErrorText();
				logger.error(pureUrl + " 调用 错误 " + lastErrorText);
			} else {
				httpResult = httpResult.trim();
				if (httpResult.startsWith("{") && httpResult.endsWith("}")) {// 有效Json
					jsonStr = httpResult;
				} else {// 不是Json
					lastErrorText = HtmlUtil.extractHtmlText(httpResult);
				}
			}
			//
			ajax.over();

			if (jsonStr == null) {
				result = SwbResult.newOne();
				result.type = Type.error;
				result.message = lastErrorText;
			} else {
				result = SwbResult.fromJson(jsonStr, null);
			}

		} catch (Exception ex) {
			System.err.println(ex);

			result = new SwbResult();
			result.type = SwbResult.Type.error;
			result.message = ex.getMessage();
		}

		return result;
	}

}
