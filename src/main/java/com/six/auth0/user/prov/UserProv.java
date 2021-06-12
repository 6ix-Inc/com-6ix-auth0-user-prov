package com.six.auth0.user.prov;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class UserProv {

	static Logger logger = LoggerFactory.getLogger(UserProv.class);

	public static void main(String[] args) throws IOException, UnirestException {

		List<String> lines = FileUtils.readLines(new File("input/input.txt"), Charset.defaultCharset());
		for (String line : lines) {
			createUser(Util.fromJsonString(line));
		}

	}

	@Loggable(Loggable.DEBUG)
	public static Map<String, Integer> createUser(Map<String, Object> payload) throws UnirestException {
		logger.debug(Util.loggable(payload));
		logger.info("email## {}", payload.get("email"));

		Map<String, Integer> compositeReturn = new HashMap<>();

		HttpResponse<JsonNode> elasticemailResponse = createUserInEmail(payload);

		logger.info("elasticemailResponse_Status## {}", elasticemailResponse.getStatus());
		logger.info("elasticemailResponse_StatusText## {}", elasticemailResponse.getStatusText());
		logger.info("elasticemailResponse_Body## {}", elasticemailResponse.getBody());

		compositeReturn.put("elasticemailResponse", elasticemailResponse.getStatus());

		HttpResponse<JsonNode> liveWebinarResponse = createUserInLivewebinar(payload);

		compositeReturn.put("liveWebinarResponse", liveWebinarResponse.getStatus());

		logger.info("liveWebinarResponse_Status## {}", liveWebinarResponse.getStatus());
		logger.info("liveWebinarResponse_StatusText## {}", liveWebinarResponse.getStatusText());
		logger.info("liveWebinarResponse_Body## {}", liveWebinarResponse.getBody());

		JSONObject adsUser = new JSONObject();
		adsUser.put("can_change_password", true);
		adsUser.put("can_add_ad_items", true);
		adsUser.put("email", payload.get("email"));
		adsUser.put("name", payload.get("email"));

		Set<String> existingAdvertisers = getExistingAdvertisers();

		if (existingAdvertisers.contains(adsUser.getString("email")))
			return compositeReturn;
		
		existingAdvertisers.add(adsUser.getString("email"));

		HttpResponse<JsonNode> adbutlerResponse = createUserInAdbutler(adsUser);

		compositeReturn.put("adbutlerResponse", adbutlerResponse.getStatus());

		logger.info("adbutlerResponse_Status## {}", adbutlerResponse.getStatus());
		logger.info("adbutlerResponse_StatusText## {}", adbutlerResponse.getStatusText());
		logger.info("adbutlerResponse_Body## {}", adbutlerResponse.getBody());

		return compositeReturn;
	}

	@RetryOnFailure(attempts = 2, delay = 10, verbose = false)
	private static HttpResponse<JsonNode> createUserInLivewebinar(Map<String, Object> payload) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		HttpResponse<JsonNode> accessTokenResponse = Unirest.post("https://api.archiebot.com/api/oauth/access_token")
				.header("Accept", "application/vnd.archiebot.v1+json").field("grant_type", "password")
				.field("client_id", "QhEoz2HWCjrCXTaaMgyUJCqiIUAqgLNVkm1NO5hI")
				.field("client_secret", "0e06c140538f65adb7c3d62ae3704e9e").field("username", "friends@6ix.com")
				.field("password", "!2wtG:(Tgrte:O").asJson();

		JSONObject accessToken = accessTokenResponse.getBody().getObject().getJSONObject("accessToken");
		String authorizationHeader = MessageFormat.format("{0} {1}", accessToken.getString("token_type"),
				accessToken.getString("access_token"));

		HttpResponse<JsonNode> liveWebinarResponse = Unirest.post("https://api.archiebot.com/api/users")
				.header("Accept", "application/vnd.archiebot.v1+json").header("Authorization", authorizationHeader)
				.field("package_id", "338").field("email", payload.get("email"))
				.field("password", "U1" + payload.get("email")).field("status", "active")
				.field("country_code_iso2", "US").field("confirmed", "true").asJson();
		return liveWebinarResponse;
	}

	@RetryOnFailure(attempts = 2, delay = 10, verbose = false)
	private static HttpResponse<JsonNode> createUserInAdbutler(JSONObject adsUser) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		HttpResponse<JsonNode> adbutlerResponse = Unirest.post("https://api.adbutler.com/v2/advertisers") //
				.header("Accept", "application/json") //
				.header("Authorization", "Basic 18ffdf2254db3ece215df5264cef9bae") //
				.header("Content-Type", "application/json") //
				.body(adsUser).asJson();
		return adbutlerResponse;
	}

	@RetryOnFailure(attempts = 2, delay = 10, verbose = false)
	private static HttpResponse<JsonNode> createUserInEmail(Map<String, Object> payload) throws UnirestException {
		HttpResponse<JsonNode> elasticemailResponse = Unirest
				.get("https://api.elasticemail.com/v2/account/addsubaccount") //
				.queryString("apikey",
						"02BA2F49FF06186BB8D55FC5A0B168F4F676713FD1B2B9A4AF32775E340F88FCD3BE60E87629B669AED62CE6466BE11D")
				.queryString("email", payload.get("email")) //
				.queryString("password", payload.get("email")) //
				.queryString("confirmPassword", payload.get("email")).asJson();
		return elasticemailResponse;
	}

	@RetryOnFailure(attempts = 2, delay = 10, verbose = false)
	private static Set<String> getExistingAdvertisers() throws UnirestException {
		Set<String> existing = new HashSet<>();

		int offset = 0;

		boolean hasMore = true;
		while (hasMore) {

			HttpResponse<JsonNode> adbutlerResponse = Unirest.get("https://api.adbutler.com/v2/advertisers") //
					.header("Accept", "application/json") //
					.header("Authorization", "Basic 18ffdf2254db3ece215df5264cef9bae").queryString("offset", offset)
					.asJson();

			logger.info("adbutlerResponse_Status## {}", adbutlerResponse.getStatus());
			logger.info("adbutlerResponse_StatusText## {}", adbutlerResponse.getStatusText());
			logger.info("adbutlerResponse_Body## {}", adbutlerResponse.getBody());

			JSONArray data = adbutlerResponse.getBody().getObject().getJSONArray("data");
			hasMore = adbutlerResponse.getBody().getObject().getBoolean("has_more");
			offset += data.length();

			for (int i = 0, l = data.length(); i < l; i++) {
				existing.add(data.getJSONObject(i).getString("email"));
			}
		}
		return existing;
	}
}
