package com.swb.tool.cmn;

import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class SwbUtil {
	public static final Charset UTF8 = Charset.forName("UTF-8");
	private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";

	private static String getHex(byte buf[]) {
		StringBuilder sb = new StringBuilder(buf.length * 3);
		for (int i = 0; i < buf.length; i++) {
			int n = (int) buf[i] & 0xff;
			sb.append("%");
			if (n < 0x10) {
				sb.append("0");
			}
			sb.append(Long.toString(n, 16).toUpperCase());
		}
		return sb.toString();
	}

	//
	public static String encodeURIComponent(String srcStr) {
		return encodeURIComponent(srcStr, null);
	}

	public static String encodeURIComponent(String srcStr, Charset charset) {
		if (StringUtils.isEmpty(srcStr)) {
			return srcStr;
		}
		if (charset == null) {
			charset = UTF8;
		}
		int strLen = srcStr.length();
		StringBuilder sb = new StringBuilder(strLen * 3);
		String tmpStr = null;
		byte[] tmpBytes = null;
		for (int i = 0; i < strLen; i++) {
			tmpStr = srcStr.substring(i, i + 1);
			if (ALLOWED_CHARS.indexOf(tmpStr) == -1) {
				tmpBytes = tmpStr.getBytes(charset);
				sb.append(getHex(tmpBytes));
				continue;
			}
			sb.append(tmpStr);
		}
		return sb.toString();
	}

	//
	private static String toUrlParamString(Map<String, ?> params) {
		StringBuilder qryStr = new StringBuilder();
		if (params != null) {
			boolean first = true;
			for (Map.Entry<String, ?> item : params.entrySet()) {
				String name = item.getKey();
				Object value = item.getValue();
				if (value != null) {
					if (first) {
						first = false;
					} else {
						qryStr.append("&");
					}
					qryStr.append(encodeURIComponent(name) + "=" + encodeURIComponent(String.valueOf(value)));
				}
			}
		}
		return qryStr.toString();
	}

	// String httpPath
	public static String makeUrl(String httpPath, Map<String, ?> params) {
		String urlParamString = toUrlParamString(params);
		if (StringUtils.isEmpty(urlParamString)) {
			return httpPath;
		}
		//
		if (httpPath.indexOf("?") == -1) {
			httpPath += "?";
		} else {
			if (!httpPath.endsWith("?")) {
				httpPath += "&";
			}
		}
		return httpPath + urlParamString;
	}
}
