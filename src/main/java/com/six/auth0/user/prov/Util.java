package com.six.auth0.user.prov;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.JsonNode;

public class Util {

	static ObjectMapper mapper = new ObjectMapper();
	static Logger logger = LoggerFactory.getLogger(RESTController.class);

	public static String loggable(Object object) {
		try {
			return loggable2(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String loggable2(Object object) throws JsonProcessingException {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		return mapper.writeValueAsString(object);
	}

	public static String toJsonString(Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}
	
	public static Map<String,Object> fromJsonString(String object) throws JsonProcessingException {
		return mapper.readValue(object,
			    new TypeReference<Map<String,Object>>(){});
	}
	
	public static JsonNode toJsonNode(String object) throws JsonProcessingException {
		return mapper.readValue(object,JsonNode.class);
	}

}