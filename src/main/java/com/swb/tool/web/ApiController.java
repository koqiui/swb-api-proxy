package com.swb.tool.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.swb.common.util.ExceptionUtil;
import com.swb.tool.cmn.SwbApiHelper;
import com.swb.tool.cmn.SwbResult;
import com.swb.tool.cmn.SwbResult.Type;

@RestController
@RequestMapping(value = "/api")
public class ApiController extends AbsController {

	private static Map<String, Object> extractRequestParams(HttpServletRequest request) {
		HashMap<String, Object> paramsMap = new LinkedHashMap<String, Object>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			paramsMap.put(paramName, request.getParameter(paramName));
		}
		return paramsMap;
	}

	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public SwbResult<?> doApiGet(HttpServletRequest request) {
		SwbResult<?> result = SwbResult.newOne();

		String apiUrl = request.getRequestURI();
		System.out.println("GET : " + apiUrl);

		Map<String, Object> params = extractRequestParams(request);
		//
		try {
			String accessToken = SwbApiHelper.getAccessToken();
			if (accessToken == null) {
				result.type = Type.error;
				result.message = "获取不到 access token";
			} else {
				return SwbApiHelper.sendGetRequest(apiUrl, params, accessToken);
			}
		} catch (Exception ex) {
			result.type = Type.error;
			result.message = ExceptionUtil.extractCauseMessage(ex, true);
		}

		return result;
	}

	@RequestMapping(value = "/**", method = { RequestMethod.POST })
	public SwbResult<?> doApiPost(HttpServletRequest request, @RequestBody(required = false) Object reqData) {
		SwbResult<?> result = SwbResult.newOne();

		String apiUrl = request.getRequestURI();
		System.out.println("POST : " + apiUrl);

		Map<String, Object> params = extractRequestParams(request);
		try {
			String accessToken = SwbApiHelper.getAccessToken();
			if (accessToken == null) {
				result.type = Type.error;
				result.message = "获取不到 access token";
			} else {
				return SwbApiHelper.sendPostRequest(apiUrl, params, reqData, accessToken);
			}
		} catch (Exception ex) {
			result.type = Type.error;
			result.message = ExceptionUtil.extractCauseMessage(ex, true);
		}

		return result;
	}

}
