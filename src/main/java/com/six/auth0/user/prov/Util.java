package com.six.auth0.user.prov;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

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
	
	public static Map<String, Integer> createUser(Map<String, Object> payload) throws UnirestException {
		logger.debug(Util.loggable(payload));

		Map<String, Integer> compositeReturn = new HashMap<>();

		HttpResponse<JsonNode> elasticemailResponse = Unirest
				.get("https://api.elasticemail.com/v2/account/addsubaccount") //
				.queryString("apikey",
						"02BA2F49FF06186BB8D55FC5A0B168F4F676713FD1B2B9A4AF32775E340F88FCD3BE60E87629B669AED62CE6466BE11D")
				.queryString("email", payload.get("email")) //
				.queryString("password", payload.get("email")) //
				.queryString("confirmPassword", payload.get("email")).asJson();

		logger.info("elasticemailResponse:Status: {}", elasticemailResponse.getStatus());
		logger.info("elasticemailResponse:StatusText {}", elasticemailResponse.getStatusText());
		logger.info("elasticemailResponse:Body {}", elasticemailResponse.getBody());

		compositeReturn.put("elasticemailResponse", elasticemailResponse.getStatus());

		JSONObject adsUser = new JSONObject();
		adsUser.put("can_change_password", true);
		adsUser.put("can_add_ad_items", true);
		adsUser.put("email", payload.get("email"));
		adsUser.put("name", payload.get("email"));

		Unirest.setTimeouts(0, 0);
		HttpResponse<JsonNode> adbutlerResponse = Unirest.post("https://api.adbutler.com/v2/advertisers") //
				.header("Accept", "application/json") //
				.header("Authorization", "Basic 18ffdf2254db3ece215df5264cef9bae") //
				.header("Content-Type", "application/json") //
				.body(adsUser).asJson();

		compositeReturn.put("adbutlerResponse", adbutlerResponse.getStatus());

		logger.info("adbutlerResponse:Status: {}", adbutlerResponse.getStatus());
		logger.info("adbutlerResponse:StatusText {}", adbutlerResponse.getStatusText());
		logger.info("adbutlerResponse:Body {}", adbutlerResponse.getBody());

		Unirest.setTimeouts(0, 0);
		HttpResponse<JsonNode> accessTokenResponse = Unirest.post("https://api.archiebot.com/api/oauth/access_token")
				.header("Accept", "application/vnd.archiebot.v1+json").field("grant_type", "password")
				.field("client_id", "QhEoz2HWCjrCXTaaMgyUJCqiIUAqgLNVkm1NO5hI")
				.field("client_secret", "0e06c140538f65adb7c3d62ae3704e9e").field("username", "friends@6ix.com")
				.field("password", "!2wtG:(Tgrte:O").asJson();

		JSONObject accessToken = accessTokenResponse.getBody().getObject().getJSONObject("accessToken");
		String authorizationHeader = MessageFormat.format("{0} {1}", accessToken.getString("token_type"),
				accessToken.getString("access_token"));

		HttpResponse<String> liveWebinarResponse = Unirest.post("https://api.archiebot.com/api/users")
				.header("Accept", "application/vnd.archiebot.v1+json").header("Authorization", authorizationHeader)
				.field("package_id", "338").field("email", payload.get("email"))
				.field("password", "U" + payload.get("email")).field("status", "active")
				.field("country_code_iso2", "US").field("confirmed", "true").asString();

		compositeReturn.put("liveWebinarResponse", liveWebinarResponse.getStatus());

		logger.info("liveWebinarResponse: {}", liveWebinarResponse.getStatus());
		logger.info("liveWebinarResponse:StatusText {}", liveWebinarResponse.getStatusText());
		logger.info("liveWebinarResponse:Body {}", liveWebinarResponse.getBody());

		return compositeReturn;
	}
}