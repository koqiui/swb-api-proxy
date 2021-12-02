package com.swb.tool.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.swb.common.exception.InvalidAccessException;
import com.swb.common.exception.RscException;
import com.swb.common.exception.UnAuthenticatedException;
import com.swb.common.exception.UnAuthorizedException;
import com.swb.common.exception.UnTrustedAppException;
import com.swb.common.exception.UnVerifiedAppException;
import com.swb.common.exception.XRuntimeException;
import com.swb.common.http.HttpMethod;
import com.swb.common.model.Result;
import com.swb.common.model.Result.Type;
import com.swb.common.model.ResultCode;
import com.swb.common.util.ExceptionUtil;
import com.swb.common.util.JsonUtil;
import com.swb.common.util.StrUtil;
import com.swb.common.util.WebUtil;

//
@Order(value = 1)
public class AppExceptionResolver implements HandlerExceptionResolver {
	protected final Log logger = LogFactory.getLog(this.getClass());

	public static final String ERROR_INFO_KEY = "error_info";
	public static final String REDIRECT_URL_KEY = AppConst.SESSION_KEY_REDIRECT_URL;
	public static final String DEFAULT_ERROR_URL = "/error/error";
	public static final int DEFAULT_STATUS_CODE = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	// 未完成的页面替代页面url
	public static final String ERROR_404_URL = "/error/error-404";
	public static final String LOGIN_URL = "";
	//
	private String jsonViewName = AppConst.JSON_VIEW_NAME;
	private String errorInfoKey = ERROR_INFO_KEY;
	private String redirectUrlKey = REDIRECT_URL_KEY;
	private String defaultErrorUrl = DEFAULT_ERROR_URL;
	private int defaultStatusCode = DEFAULT_STATUS_CODE;
	private String error404url = ERROR_404_URL;
	private String loginUrl = LOGIN_URL;

	//
	public void setJsonViewName(String jsonViewName) {
		this.jsonViewName = jsonViewName;
	}

	public String getErrorInfoKey() {
		return errorInfoKey;
	}

	public void setErrorInfoKey(String errorInfoKey) {
		if (StrUtil.hasText(errorInfoKey)) {
			this.errorInfoKey = errorInfoKey;
		}
	}

	public String getRedirectUrlKey() {
		return redirectUrlKey;
	}

	public void setRedirectUrlKey(String redirectUrlKey) {
		this.redirectUrlKey = redirectUrlKey;
	}

	public String getDefaultErrorUrl() {
		return defaultErrorUrl;
	}

	public void setDefaultErrorUrl(String defaultErrorUrl) {
		if (StrUtil.hasText(defaultErrorUrl)) {
			this.defaultErrorUrl = defaultErrorUrl;
		}
	}

	public int getDefaultStatusCode() {
		return defaultStatusCode;
	}

	public void setDefaultStatusCode(int defaultStatusCode) {
		this.defaultStatusCode = defaultStatusCode;
	}

	public String getError404url() {
		return error404url;
	}

	public void setError404url(String error404url) {
		this.error404url = error404url;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		String requestUri = request.getServletPath();
		this.logger.debug("RequestPath >> " + requestUri);
		//
		Result<String> result = Result.newOne();
		result.type = Type.error;

		//
		int statusCode = response.getStatus();
		logger.debug(">> 异常发生时 statusCode ：" + statusCode);
		//
		boolean isAjaxRequest = WebUtil.isAjaxAlikeRequest(request, handler);

		String requestUriWithParams = WebUtil.getRequestUriWithParams(request, true);
		//
		if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			response.setStatus(this.defaultStatusCode);
		}
		//
		Throwable cause = ExceptionUtil.getExceptionCause(ex, RscException.class);
		//
		result.message = ExceptionUtil.extractMessage(cause, true);
		result.data = requestUriWithParams;
		//
		if (cause instanceof InvalidAccessException) {
			result.type = Type.warn;
			result.code = ResultCode.SYS.SysResc_Invalid_Access;
			response.setStatus(HttpStatus.SC_FORBIDDEN);
			//
			this.logger.warn(requestUriWithParams);
			this.logger.warn(ex);
		} else if (cause instanceof UnAuthenticatedException || cause instanceof UnTrustedAppException) {
			result.type = Type.warn;
			result.code = ResultCode.Auth.Authenticate_Fail;
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);
			//
			if (!isAjaxRequest) {
				request.getSession().setAttribute(this.redirectUrlKey, requestUriWithParams);
			}
			//
			this.logger.warn(requestUriWithParams);
			this.logger.warn(ex);
		} else if (cause instanceof UnAuthorizedException || cause instanceof UnVerifiedAppException) {
			result.type = Type.warn;
			result.code = ResultCode.Auth.Authorize_Fail;
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);
			//
			this.logger.warn(requestUriWithParams);
			this.logger.warn(ex);
		} else if (cause instanceof JsonMappingException || cause instanceof JsonParseException) {
			result.type = Type.error;
			result.message = "JSON解析/转换/映射错误";
		} else {
			boolean isWarningError = cause instanceof XRuntimeException;
			//
			if (isWarningError) {
				result.type = Type.warn;
				//
				this.logger.warn(requestUriWithParams);
				this.logger.warn(ex);
			} else {
				if (cause instanceof HttpRequestMethodNotSupportedException) {
					result.message = "给定的Http请求方法（" + request.getMethod() + "）不被支持";
				} else if (cause instanceof MissingServletRequestParameterException) {
					result.message = "缺少必要的请求参数";
				} else if (cause instanceof HttpMediaTypeNotSupportedException) {
					result.message = "请求体内容类型（" + request.getContentType() + "）不被支持";
				} else if (cause instanceof HttpMessageNotReadableException) {
					result.message = "消息体内容无法读取";
				}
				this.logger.error(requestUriWithParams);
				this.logger.error(ex);
			}
		}
		//
		ModelAndView mv = new ModelAndView();
		if (isAjaxRequest) {
			mv.setViewName(this.jsonViewName);
			mv.addAllObjects(result.toMap());
		} else {
			String errorJson = JsonUtil.toJson(result);
			if (result.code.intValue() == ResultCode.Auth.Authenticate_Fail) {
				if (StrUtil.hasText(this.loginUrl)) {
					mv.setViewName("redirect:" + this.loginUrl);
				} else {
					if (HttpMethod.GET.name().equals(request.getMethod())) {
						mv.setViewName("forward:" + this.defaultErrorUrl);
						mv.addObject(this.errorInfoKey, errorJson);
					} else {
						mv.setViewName("redirect:" + WebUtil.getAppAbsUrl(request, this.defaultErrorUrl));
					}
				}
			} else {
				String theErrorUrl = this.defaultErrorUrl;
				if (statusCode == HttpStatus.SC_NOT_FOUND && StrUtil.hasText(this.error404url)) {
					theErrorUrl = this.error404url;
				}
				//
				if (HttpMethod.GET.name().equals(request.getMethod())) {
					mv.setViewName("forward:" + theErrorUrl);
					mv.addObject(this.errorInfoKey, errorJson);
				} else {
					mv.setViewName("redirect:" + WebUtil.getAppAbsUrl(request, theErrorUrl));
				}
			}
		}
		//
		return mv;
	}

}
