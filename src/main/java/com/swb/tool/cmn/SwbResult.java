package com.swb.tool.cmn;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class SwbResult<TData> implements Serializable {
	private static final long serialVersionUID = 1L;

	//
	public enum Type {
		unknown(-1), info(0), warn(1), error(2), fatal(4);

		//
		private int value = -1;

		Type(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	// 结果信息
	public Type type = Type.info;

	//
	public boolean isSuccess() {
		return Type.info.equals(this.type);
	}

	public String message = null;

	public TData data = null;

	public static <TData> SwbResult<TData> newOne() {
		return new SwbResult<TData>();
	}

	//
	public static Class<?> StringObjectMapClass = new LinkedHashMap<String, Object>(0).getClass();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SwbResult<?> fromJson(String jsonStr, Class<T> dataType) {
		SwbResult result = new SwbResult();
		if (jsonStr == null || !jsonStr.startsWith("{") || !jsonStr.endsWith("}")) {
			result.type = Type.error;
			result.message = "无结果";
			return result;
		}

		if (dataType == null) {// 未指定结果 对象 或 列表元素类型
			result = JSON.parseObject(jsonStr, SwbResult.class);
		} else {
			Map<String, Object> mapResult = (Map<String, Object>) JSON.parseObject(jsonStr, StringObjectMapClass);
			Boolean success = (Boolean) mapResult.getOrDefault("success", false);
			result.type = Boolean.TRUE.equals(success) ? Type.info : Type.error;
			result.message = (String) mapResult.get("message");

			Object dataObj = mapResult.remove("data");
			if (dataObj != null) {
				String dataJson = JSON.toJSONString(dataObj);
				if (dataObj instanceof List) {
					result.data = JSON.parseArray(dataJson, dataType);
				} else {
					result.data = JSON.parseObject(dataJson, dataType);
				}
			}
		}

		return result;
	}

}
