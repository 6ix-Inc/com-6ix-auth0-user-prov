package com.six.auth0.user.prov;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogToSheet {

	static Logger logger = LoggerFactory.getLogger(LogToSheet.class);
	static enum HEADERS {
		email, elasticemailResponse_Status, elasticemailResponse_StatusText, elasticemailResponse_Body, //
		adbutlerResponse_Status, adbutlerResponse_StatusText, adbutlerResponse_Body, //
		liveWebinarResponse_Status, liveWebinarResponse_StatusText, liveWebinarResponse_Body
	}
	
	public static void main(String[] args) throws IOException {
		List<String> lines = Files.readAllLines(Path.of("log-2021-06-05.log"));
		final CSVPrinter csv = new CSVPrinter(new FileWriter(new File("log-2021-06-05.csv")), CSVFormat.DEFAULT.withHeader(HEADERS.class));
		
		ArrayList<String> values = new ArrayList<>();

		for (String line : lines) {
			if (!line.contains("c.six.auth0.user.prov.RESTController - "))
				continue;
			String message = line.substring(line.indexOf("c.six.auth0.user.prov.RESTController - ")
					+ "c.six.auth0.user.prov.RESTController - ".length());

			if (message.startsWith("{\"email\":\"")) {
				if (values.size() > 0) {
					csv.printRecord(values);
				}
				values = new ArrayList<>();
				logger.info(message);
				String email = (String) Util.fromJsonString(message).get("email");
				logger.info(email);
				values.add(HEADERS.email.ordinal(), email);
			} else if (message.contains("##")) {
				String[] tokens = message.split("##");
				values.add(HEADERS.valueOf(tokens[0].trim()).ordinal(), tokens[1].trim());
				//System.out.println(HEADERS.valueOf(tokens[0]));
			}
			
	
		}
		csv.close();
	}

}
