package com.swb.tool.cmn;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class SwbApiHelperTest {

	@Test
	public void test_makeAuthCode() throws IOException {
		String appKey = "xxxxxxx";
		String appSecret = "123456";
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		String authCode = SwbApiHelper.makeAuthCode(appKey, appSecret, timestamp);

		System.out.println(authCode);
	}

	@Test
	public void test_makeUrl() throws IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", "koqiui");
		params.put("com", "商维宝");
		params.put("age", 100);

		String httpUrl = SwbUtil.makeUrl("https://www.baidu.com", params);
		System.out.println(httpUrl);
	}

	// ----------------------------------------------------------------------------------------
	@BeforeClass
	public static void init() {
		// 初始基础配置（测试和生产环境不同）
		SwbApiHelper.setAppBaseUrl("http://open.shangwb.com");
		//
		SwbApiHelper.setAppKey("xxxxx");
		SwbApiHelper.setAppSecret("123456");
	}

	@Test
	public void test_getAccessToken() throws IOException {
		// 获取access_token信息
		String accessToken = SwbApiHelper.getAccessToken();

		System.out.println("accessToken : " + accessToken);
	}

	@Test
	public void test_makeAccessUrl() throws IOException {
		// 组成 app访问认证所需的 access_token, timestamp, auth_code 公共参数的url
		String accessToken = "d20fef2993c741569b2ebd4fa43806d1";

		String apiUrl = "/api/oa/audit/process/state";

		Map<String, Object> params = new HashMap<>();
		params.put("bpInstId", "xxx");
		params.put("bpState", 1);
		params.put("submitterId", "00001223");
		params.put("submitterName", "刘进秋");

		String accessUrl = SwbApiHelper.makeAccessUrl(apiUrl, params, accessToken);

		System.out.println(accessUrl);
	}

	@Test
	public void test_sendGetRequest() {
		String accessToken = null;

		String apiUrl = "/api/cmn/sys/domain/name/get";

		Map<String, Object> params = new HashMap<>();
		params.put("orderNo", "xxxxx");
		params.put("orderId", 100);

		SwbResult<?> result = SwbApiHelper.sendGetRequest(apiUrl, params, accessToken);
		System.out.println(JSON.toJSONString(result));
	}

	@Test
	public void test_sendPostRequest() {
		String accessToken = "309467b79a4b497a93e1372c8cc3f10b";

		String apiUrl = "/api/demo/merchant/app/get";

		Map<String, Object> params = null;
		Map<String, Object> data = null;

		SwbResult<?> result = SwbApiHelper.sendPostRequest(apiUrl, params, data, accessToken);
		System.out.println(JSON.toJSONString(result));
	}
}
