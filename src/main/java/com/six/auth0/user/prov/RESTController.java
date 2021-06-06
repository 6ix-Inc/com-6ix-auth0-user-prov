package com.six.auth0.user.prov;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mashape.unirest.http.exceptions.UnirestException;

@RestController
public class RESTController {

	Logger logger = LoggerFactory.getLogger(RESTController.class);

	@RequestMapping(value = "/post-user-registeration", method = RequestMethod.POST)
	public Map<String, Integer> postUserRegisteration(@RequestBody Map<String, Object> payload)
			throws UnirestException {
		logger.info("post-user-registeration");
		return UserProv.createUser(payload);
	}

}