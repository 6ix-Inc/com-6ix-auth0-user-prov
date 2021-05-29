package com.six.auth0.user.prov;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Util {

	static ObjectMapper mapper = new ObjectMapper();

	public static String loggable(Object object) {
		try {
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String toJsonString(Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}
}