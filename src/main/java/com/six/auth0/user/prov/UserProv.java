package com.six.auth0.user.prov;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
	static Map<String, HashSet<String>> existingAdvertisersCache = new HashMap<>();

	@Loggable(Loggable.DEBUG)
	public static void cleanupAdvertiser(Map<String, Object> payload) throws UnirestException {
		final String email = (String) payload.get("email");

		logger.info("cleaning advertiser: {}", payload.get("email"));

		if (getExistingAdvertisersCache().containsKey(email)) {
			final List<String> existingAdvertisers = new ArrayList<>(getExistingAdvertisersCache().get(email));
			logger.info("found {} advertiser(s): {}", existingAdvertisers.size(),
					String.join(",", existingAdvertisers));

			if (existingAdvertisers.size() <= 1) {
				return;
			}
			existingAdvertisers.remove(0);

			for (final String adv : existingAdvertisers) {
				logger.info("deleting advertiser with id: {}", adv);

				Unirest.setTimeouts(0, 0);
				final HttpResponse<JsonNode> adbutlerResponse = Unirest
						.delete("https://api.adbutler.com/v2/advertisers/" + adv) //
						.header("Accept", "application/json") //
						.header("Authorization", "Basic 18ffdf2254db3ece215df5264cef9bae") //
						.header("Content-Type", "application/json").asJson();

				logger.info("adbutlerResponse_Status## {}", adbutlerResponse.getStatus());
				logger.info("adbutlerResponse_StatusText## {}", adbutlerResponse.getStatusText());
				logger.info("adbutlerResponse_Body## {}", adbutlerResponse.getBody());
			}
		}
	}

	@Loggable(Loggable.DEBUG)
	public static Map<String, Integer> createUser(Map<String, Object> payload) throws UnirestException {
		logger.debug(Util.loggable(payload));
		logger.info("email## {}", payload.get("email"));

		intitalizeExistingAdvertisersCache();

		final Map<String, Integer> compositeReturn = new HashMap<>();

		final HttpResponse<JsonNode> elasticemailResponse = createUserInEmail(payload);

		logger.info("elasticemailResponse_Status## {}", elasticemailResponse.getStatus());
		logger.info("elasticemailResponse_StatusText## {}", elasticemailResponse.getStatusText());
		logger.info("elasticemailResponse_Body## {}", elasticemailResponse.getBody());

		compositeReturn.put("elasticemailResponse", elasticemailResponse.getStatus());

		final HttpResponse<JsonNode> liveWebinarResponse = createUserInLivewebinar(payload);

		compositeReturn.put("liveWebinarResponse", liveWebinarResponse.getStatus());

		logger.info("liveWebinarResponse_Status## {}", liveWebinarResponse.getStatus());
		logger.info("liveWebinarResponse_StatusText## {}", liveWebinarResponse.getStatusText());
		logger.info("liveWebinarResponse_Body## {}", liveWebinarResponse.getBody());

		final JSONObject adsUser = new JSONObject();
		adsUser.put("can_change_password", true);
		adsUser.put("can_add_ad_items", true);
		adsUser.put("email", payload.get("email"));
		adsUser.put("name", payload.get("email"));

		if (getExistingAdvertisersCache().containsKey(adsUser.getString("email"))) {
			logger.info("advertiser with email: {} alreday exist in liveWebinar", adsUser.getString("email"));
			return compositeReturn;
		}

		updateExistingAdvertisersCache(adsUser.getString("email"), "--");

		final HttpResponse<JsonNode> adbutlerResponse = createUserInAdbutler(adsUser);

		compositeReturn.put("adbutlerResponse", adbutlerResponse.getStatus());

		logger.info("adbutlerResponse_Status## {}", adbutlerResponse.getStatus());
		logger.info("adbutlerResponse_StatusText## {}", adbutlerResponse.getStatusText());
		logger.info("adbutlerResponse_Body## {}", adbutlerResponse.getBody());

		return compositeReturn;
	}

	@RetryOnFailure(attempts = 2, delay = 10, verbose = false)
	private static HttpResponse<JsonNode> createUserInAdbutler(JSONObject adsUser) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		final HttpResponse<JsonNode> adbutlerResponse = Unirest.post("https://api.adbutler.com/v2/advertisers") //
				.header("Accept", "application/json") //
				.header("Authorization", "Basic 18ffdf2254db3ece215df5264cef9bae") //
				.header("Content-Type", "application/json") //
				.body(adsUser).asJson();
		return adbutlerResponse;
	}

	@RetryOnFailure(attempts = 2, delay = 10, verbose = false)
	private static HttpResponse<JsonNode> createUserInEmail(Map<String, Object> payload) throws UnirestException {
		final HttpResponse<JsonNode> elasticemailResponse = Unirest
				.get("https://api.elasticemail.com/v2/account/addsubaccount") //
				.queryString("apikey",
						"78CB2E341065237E7279E9134E15D62D9552BF79E06A9FB0E84A5CC691187374AC2BB16D60CDF187EC85EB1C97966CE4")
				.queryString("email", payload.get("email")) //
				.queryString("password", payload.get("email")) //
				.queryString("confirmPassword", payload.get("email")).asJson();
		return elasticemailResponse;
	}

	@RetryOnFailure(attempts = 2, delay = 10, verbose = false)
	private static HttpResponse<JsonNode> createUserInLivewebinar(Map<String, Object> payload) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		final HttpResponse<JsonNode> accessTokenResponse = Unirest
				.post("https://api.archiebot.com/api/oauth/access_token")
				.header("Accept", "application/vnd.archiebot.v1+json").field("grant_type", "password")
				.field("client_id", "QhEoz2HWCjrCXTaaMgyUJCqiIUAqgLNVkm1NO5hI")
				.field("client_secret", "0e06c140538f65adb7c3d62ae3704e9e").field("username", "friends@6ix.com")
				.field("password", "!2wtG:(Tgrte:O").asJson();

		final JSONObject accessToken = accessTokenResponse.getBody().getObject().getJSONObject("accessToken");
		final String authorizationHeader = MessageFormat.format("{0} {1}", accessToken.getString("token_type"),
				accessToken.getString("access_token"));

		final HttpResponse<JsonNode> liveWebinarResponse = Unirest.post("https://api.archiebot.com/api/users")
				.header("Accept", "application/vnd.archiebot.v1+json").header("Authorization", authorizationHeader)
				.field("package_id", "338").field("email", payload.get("email"))
				.field("password", "U1" + payload.get("email")).field("status", "active")
				.field("country_code_iso2", "US").field("confirmed", "true").asJson();
		return liveWebinarResponse;
	}

	public static Map<String, HashSet<String>> getExistingAdvertisersCache() {
		return existingAdvertisersCache;
	}

	@RetryOnFailure(attempts = 2, delay = 10, verbose = false)
	private static void intitalizeExistingAdvertisersCache() throws UnirestException {
		int offset = 0;
		boolean hasMore = getExistingAdvertisersCache().isEmpty();
		while (hasMore) {

			final HttpResponse<JsonNode> adbutlerResponse = Unirest.get("https://api.adbutler.com/v2/advertisers") //
					.header("Accept", "application/json") //
					.header("Authorization", "Basic 18ffdf2254db3ece215df5264cef9bae").queryString("offset", offset)
					.asJson();

			//logger.info("adbutlerResponse_Status## {}", adbutlerResponse.getStatus());
			//logger.info("adbutlerResponse_StatusText## {}", adbutlerResponse.getStatusText());
			//logger.info("adbutlerResponse_Body## {}", adbutlerResponse.getBody());

			final JSONArray data = adbutlerResponse.getBody().getObject().getJSONArray("data");
			hasMore = adbutlerResponse.getBody().getObject().getBoolean("has_more");
			offset += data.length();

			for (int i = 0, l = data.length(); i < l; i++) {
				updateExistingAdvertisersCache(data.getJSONObject(i).getString("email"),
						String.valueOf(data.getJSONObject(i).get("id")));
			}
		}
	}

	public static void main(String[] args) throws IOException, UnirestException {

		intitalizeExistingAdvertisersCache();

		final List<String> lines = FileUtils.readLines(new File("input/input.txt"), Charset.defaultCharset());
		for (final String line : lines) {
			cleanupAdvertiser(Util.fromJsonString(line));
			// createUser(Util.fromJsonString(line));
		}

	}

	private static void updateExistingAdvertisersCache(String email, String id) {
		if (!getExistingAdvertisersCache().containsKey(email)) {
			getExistingAdvertisersCache().put(email, new HashSet<String>());
		}

		getExistingAdvertisersCache().get(email).add(id);
	}
}
