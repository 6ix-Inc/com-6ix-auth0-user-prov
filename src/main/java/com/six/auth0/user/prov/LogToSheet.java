package com.six.auth0.user.prov;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogToSheet {

	enum HEADERS {
		email, elasticemailResponse_Status, elasticemailResponse_StatusText, elasticemailResponse_Body, //
		adbutlerResponse_Status, adbutlerResponse_StatusText, adbutlerResponse_Body, //
		liveWebinarResponse_Status, liveWebinarResponse_StatusText, liveWebinarResponse_Body
	}

	static Logger logger = LoggerFactory.getLogger(LogToSheet.class);

	public static void main(String[] args) throws IOException {
		final List<String> lines = FileUtils.readLines(new File("log-2021-06-05.log"), Charset.defaultCharset());
		final CSVPrinter csv = new CSVPrinter(new FileWriter(new File("log-2021-06-05.csv")),
				CSVFormat.DEFAULT.withHeader(HEADERS.class));

		ArrayList<String> values = new ArrayList<>();

		for (final String line : lines) {
			if (!line.contains("com.six.auth0.user.prov.UserProv - ")) {
				continue;
			}
			final String message = line.substring(line.indexOf("com.six.auth0.user.prov.UserProv - ")
					+ "com.six.auth0.user.prov.UserProv - ".length());

			final String[] tokens = message.split("##");
			values.add(HEADERS.valueOf(tokens[0].trim()).ordinal(), tokens[1].trim());

			if (message.startsWith("email##")) {
				if (values.size() > 0) {
					csv.printRecord(values);
				}
				values = new ArrayList<>();
			}
		}
		csv.close();
	}

}
