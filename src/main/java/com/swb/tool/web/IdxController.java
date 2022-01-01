package com.swb.tool.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.swb.common.model.Result;
import com.swb.common.model.Result.Type;
import com.swb.common.util.ExceptionUtil;
import com.swb.tool.cmn.SwbApiHelper;

@Controller
public class IdxController extends AbsController {

	@RequestMapping(value = { "/", "/index" }, method = RequestMethod.GET)
	public String toIndexPage() {
		return "index";
	}

	@RequestMapping(value = "/error/error", method = { RequestMethod.GET, RequestMethod.POST })
	public String toErrorPage() {
		return "error/error";
	}

	@RequestMapping(value = "/app/conf/get", method = RequestMethod.GET)
	@ResponseBody
	public Result<Map<String, Object>> getAppConf(HttpServletRequest request) {
		Result<Map<String, Object>> result = Result.newOne();

		try {
			Map<String, Object> confInfo = new HashMap<>();

			confInfo.put("appEnvName", SwbApiHelper.getAppEnvName());
			confInfo.put("appBaseUrl", SwbApiHelper.getAppBaseUrl());
			confInfo.put("appKey", SwbApiHelper.getAppKey());

			result.data = confInfo;
		} catch (Exception ex) {
			result.type = Type.error;
			result.message = ExceptionUtil.extractCauseMessage(ex, true);
		}

		return result;
	}
}
