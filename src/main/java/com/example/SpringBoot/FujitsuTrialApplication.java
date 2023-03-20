package com.example.SpringBoot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

@SpringBootApplication
@EnableScheduling
@RestController
public class FujitsuTrialApplication {
	static int id = 1;
	static FeeCalculator feeCalculator = new FeeCalculator();

	/**
	 * Initialize table “weather” if it doesn't exist.
	 * Start the application
	 */
	public static void main(String[] args) {
		try {
			Connection connection = DriverManager.getConnection("jdbc:h2:file:./src/main/resources/data", "fujitsu", "fujitsu");
			Statement statement = connection.createStatement();
			String sql = "create table if not exists weather " +
					"(id int, " +
					"city varchar(255), " +
					"WMO int, " +
					"temperature float, " +
					"windspeed float, " +
					"phenomenon varchar(255), " +
					"timestamp int)";
			statement.executeUpdate(sql);
			connection.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new RuntimeException("Database broken!");
		}

		SpringApplication.run(FujitsuTrialApplication.class, args);
	}

	/**
	 * Method calculates delivery fee based on latest weather data
	 * @param cityName the city where the delivery is about to happen
	 * @param vehicleType the type of vehicle used by the courier
	 * @return returns a delivery fee calculated based on the latest weather data and given city and vehicle type
	 * @throws SQLException if there is no data in the database
	 */
	@GetMapping("/{cityName}/{vehicleType}")
	public static double calculateFee(@PathVariable String cityName, @PathVariable String vehicleType) throws Exception {
		double fee = feeCalculator.calculateBaseFee(cityName.toLowerCase(), vehicleType.toLowerCase());

		Connection connection = DriverManager.getConnection("jdbc:h2:file:./src/main/resources/data", "fujitsu", "fujitsu");
		Statement statement = connection.createStatement();
		String sql = "select temperature, windspeed, phenomenon from weather where city = '%s' order by timestamp desc"
				     .formatted(cityName.toLowerCase());
		ResultSet entry = statement.executeQuery(sql);
		entry.next();
		fee += feeCalculator.airTempExtraFee(vehicleType.toLowerCase(), entry.getDouble("temperature"));
		fee += feeCalculator.windSpeedExtraFee(vehicleType.toLowerCase(), entry.getDouble("windSpeed"));
		fee += feeCalculator.phenomenonExtraFee(vehicleType.toLowerCase(), entry.getString("phenomenon").toLowerCase());
		return fee;
	}

	/**
	 * Method calculates delivery fee based on weather data at a given timestamp
	 * @param cityName the city where the delivery is about to happen
	 * @param vehicleType the type of vehicle used by the courier
	 * @param timestamp the timestamp for which we want the fee to be calculated
	 * @return returns a delivery fee calculated based on the weather data at the given timestamp and city and vehicle type
	 * @throws SQLException if there is no data in the database
	 */
	@GetMapping("/{cityName}/{vehicleType}/{timestamp}")
	public static double calculateFeeOnGivenTimestamp(@PathVariable String cityName, @PathVariable String vehicleType, @PathVariable String timestamp) throws Exception {
		double fee = feeCalculator.calculateBaseFee(cityName.toLowerCase(), vehicleType.toLowerCase());

		Connection connection = DriverManager.getConnection("jdbc:h2:file:./src/main/resources/data", "fujitsu", "fujitsu");
		Statement statement = connection.createStatement();
		String sql = "select temperature, windspeed, phenomenon from weather where city = '%s' and timestamp = %d"
				.formatted(cityName.toLowerCase(), Integer.parseInt(timestamp));
		ResultSet entry = statement.executeQuery(sql);
		entry.next();
		fee += feeCalculator.airTempExtraFee(vehicleType.toLowerCase(), entry.getDouble("temperature"));
		fee += feeCalculator.windSpeedExtraFee(vehicleType.toLowerCase(), entry.getDouble("windSpeed"));
		fee += feeCalculator.phenomenonExtraFee(vehicleType.toLowerCase(), entry.getString("phenomenon").toLowerCase());
		return fee;
	}


	/**
	 * Scheduled method to get weather data from given url (HH:15:00 by default)
	 * @throws IOException if the endpoint is not responding
	 */
	@Scheduled(cron = "15 * * * * *")
	public static void fetchData() throws IOException {
		URL url = new URL("https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("accept", "application/json");
		Document doc = parseToXML(connection.getInputStream());
		doc.getDocumentElement().normalize();
		int timeStamp = Integer.parseInt(doc.getElementsByTagName("observations").item(0)
										 .getAttributes().item(0).getTextContent());
		NodeList stations = doc.getElementsByTagName("station");
		for (int i = 0; i < stations.getLength(); i++) {
			Node station = stations.item(i);
			if (station.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) station;
				String stationName = element.getElementsByTagName("name").item(0).getTextContent();
				if (stationName.equals("Tallinn-Harku") ||
					stationName.equals("Tartu-Tõravere") ||
				    stationName.equals("Pärnu")) {
					int WMO = Integer.parseInt(element.getElementsByTagName("wmocode").item(0).getTextContent());
					double temperature = Double.parseDouble(element.getElementsByTagName("airtemperature").item(0).getTextContent());
					double windSpeed = Double.parseDouble(element.getElementsByTagName("windspeed").item(0).getTextContent());
					String phenomenon = element.getElementsByTagName("phenomenon").item(0).getTextContent();
					insert(stationName.toLowerCase(), WMO, temperature, windSpeed, phenomenon.toLowerCase(), timeStamp);
				}
			}
		}
	}

	/**
	 * Method to parse inputstream to an XML file
	 * @param inputStream is the given input stream which we are parsing
	 * @return parsed XML file
	 */
	public static Document parseToXML(InputStream inputStream) {
		try {
			DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFac.newDocumentBuilder();
			return docBuilder.parse(inputStream);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e);
		}
	}


	/**
	 * Method inserts an observation into the database
	 * @param cityName name of the city
	 * @param WMOcode WMO code of the station
	 * @param temperature air temperature at the time of the observation
	 * @param windSpeed wind speed at the time of the observation
	 * @param phenomenon weather phenomenon
	 * @param timeStamp timestamp of the observation
	 */
	public static void insert(String cityName, int WMOcode, double temperature, double windSpeed, String phenomenon, int timeStamp) {
		try {
			Connection connection = DriverManager.getConnection("jdbc:h2:file:./src/main/resources/data", "fujitsu", "fujitsu");
			Statement statement = connection.createStatement();
			String sql = "insert into weather values (%d, '%s', %d, %.2f, %.2f, '%s', %d)"
						 .formatted(id++, cityName.split("-")[0], WMOcode, temperature, windSpeed, phenomenon, timeStamp);
			statement.executeUpdate(sql);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
