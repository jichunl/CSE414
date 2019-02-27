import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
/**
 * Runs queries against a back-end database
 */
public class Query
{
  private String configFilename;
  private Properties configProps = new Properties();

  private String jSQLDriver;
  private String jSQLUrl;
  private String jSQLUser;
  private String jSQLPassword;
  private ArrayList<ArrayList<Flight>> searchResult;
  
  // DB Connection
  private Connection conn;

  // Logged In User
  private String username; // customer username is unique

  // Canned queries
  
  private static final String SEARCH_DIRECT_FLIGHT = "SELECT TOP (?) F.day_of_month as Day, "
  		+ "F.carrier_id as Carrier, F.flight_num as Number, F.fid as fid, "
  		+ "F.origin_city as Origin, F.dest_city as Destination, "
  		+ "F.actual_time as Duration, F.capacity as Capacity, F.price as Price\n "
  		+ "FROM FLIGHTS as F "
  		+ "WHERE F.origin_city = ? AND F.dest_city = ? AND F.day_of_month = ? "
  		+ "AND F.canceled != 1 "
  		+ "ORDER BY F.actual_time, F.fid ASC";
  private PreparedStatement searchDirectFlightStatement;
  
  private static final String SEARCH_INDIRECT_FLIGHT = "SELECT TOP (?) F1.day_of_month as Day1, "
  		+ "F1.carrier_id as Carrier1, F1.flight_num as Number1, F1.origin_city as Origin1, "
  		+ "F1.dest_city as Destination1, F1.actual_time as Duration1, F1.capacity as Capacity1, "
  		+ "F1.price as Price1, F2.day_of_month as Day2, F2.carrier_id as Carrier2, "
  		+ "F2.flight_num as Number2, F2.origin_city as Origin2, F2.dest_city as Destination2, "
  		+ "F2.actual_time as Duration2, F2.capacity as Capacity2, F2.price as Price2, "
  		+ "F1.fid as fid1, F2.fid as fid2, F1.actual_time + F2.actual_time as Total_time "
  		+ "FROM FLIGHTS as F1, FLIGHTS as F2 "
  		+ "WHERE F1.origin_city = ? AND F1.dest_city = F2.origin_city AND F2.dest_city = ? "
  		+ "AND F1.day_of_month = ? AND F2.day_of_month = F1.day_of_month AND F1.canceled != 1 " 
  		+ "AND F2.canceled != 1 "
  		+ "ORDER BY Total_time, F1.fid, F2.fid ASC";
  private PreparedStatement searchIndirectFlightStatement;
  
  private static final String SEARCH_USER = "SELECT U.password as password FROM Users as U WHERE "
  		+ "U.username = ? AND U.password = ? ";
  private PreparedStatement searchUserStatement;
  
  private static final String CHECK_USERNAME_EXISTS = "SELECT count(*) as count FROM Users as U WHERE U.username = ? ";
  private PreparedStatement checkUsernameExistsStatement;
  
  private static final String INSERT_NEW_USER = "Insert into Users values (?, ?, ?)";
  private PreparedStatement insertNewUserStatement;
  
  private static final String CHECK_RESERVATION_DATE = "SELECT * FROM Reservation as R WHERE R.username = ? AND R.Day = ? ";
  private PreparedStatement checkReservationDateStatement;
  
  private static final String GET_CURRENT_RESERVATIONID = "SELECT reservationId FROM ID";
  private PreparedStatement getCurrentReservationidStatement;
  
  private static final String UPDATE_RESERVATIONID  ="UPDATE ID SET reservationId = ? WHERE reservationId = ? ";
  private PreparedStatement updateReservationidStatement;
  
  private static final String ADD_RESERVATION = "INSERT into Reservation values (?, ?, ?, ?, ?, ?, ?) ";
  private PreparedStatement addReservationStatement;

  private static final String CHECK_AVAILABLE_SEAT = "SELECT count(*) FROM Reservation as R WHERE R.fid1 = ? OR R.fid2 = ? ";
  private PreparedStatement checkAvailableSeatStatement;
  // used in reservation method
  private static final String SEARCH_RESERVATION = "SELECT * FROM Reservation as R WHERE R.username = ?";
  private PreparedStatement searchReservationStatement;
  
  private static final String SEARCH_RESERVED_FLIGHT = "SELECT F.day_of_month as Day, "
	  		+ "F.carrier_id as Carrier, F.flight_num as Number, F.fid as fid, "
	  		+ "F.origin_city as Origin, F.dest_city as Destination, "
	  		+ "F.actual_time as Duration, F.capacity as Capacity, F.price as Price "
	  		+ "FROM FLIGHTS as F WHERE F.fid = ?";
  private PreparedStatement searchReservedFlightStatement;
  
  // used in pay method
  private static final String SEARCH_USERNAME_OF_RESERVATIONID = "SELECT sum(price) as price "
  		+ "FROM Reservation as R where R.reservationId = ? AND R.username = ? AND R.pay = ? Group by R.reservationId";
  private PreparedStatement searchUsernameOfReservationidStatement;
  
  private static final String SEARCH_USER_BALANCE = "SELECT U.balance FROM Users as U WHERE U.username = ? ";
  private PreparedStatement searchUserBalanceStatement;
  
  private static final String UPDATE_USER_BALANCE = "UPDATE Users SET balance = ? WHERE username = ?";
  private PreparedStatement updateUserBalanceStatement;
  
  private static final String UPDATE_PAY_STATUS = "UPDATE Reservation SET pay = ? WHERE username = ? AND reservationId = ?";
  private PreparedStatement updatePayStatusStatement;
  
  // used in cancel method
  private static final String RESERVATION_INFO_OF_CANCEL = "SELECT R.pay, R.price FROM Reservation as R WHERE "
  		+ "R.reservationId = ? AND R.username = ?";
  private PreparedStatement reservationInfoOfCancelStatement;
  
  private static final String CANCEL_RESERVATION = "DELETE FROM Reservation WHERE reservationId = ? ";
  private PreparedStatement cancelReservationStatement;
  
  private static final String CLEAR_TABLE = "DELETE FROM Reservation; DELETE FROM Users; "
  		+ "DELETE FROM ID; INSERT INTO ID values (0) ";
  private PreparedStatement clearTableStatement;
  
  class Flight
  {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    @Override
    public String toString()
    {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId +
              " Number: " + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time +
              " Capacity: " + capacity + " Price: " + price;
    }
  }

  public Query(String configFilename)
  {
    this.configFilename = configFilename;
  }

  /* Connection code to SQL Azure.  */
  public void openConnection() throws Exception
  {
    configProps.load(new FileInputStream(configFilename));

    jSQLDriver = configProps.getProperty("flightservice.jdbc_driver");
    jSQLUrl = configProps.getProperty("flightservice.url");
    jSQLUser = configProps.getProperty("flightservice.sqlazure_username");
    jSQLPassword = configProps.getProperty("flightservice.sqlazure_password");

    /* load jdbc drivers */
    Class.forName(jSQLDriver).newInstance();

    /* open connections to the flights database */
    conn = DriverManager.getConnection(jSQLUrl, // database
            jSQLUser, // user
            jSQLPassword); // password

    conn.setAutoCommit(true); //by default automatically commit after each statement

    /* You will also want to appropriately set the transaction's isolation level through:
       conn.setTransactionIsolation(...)
       See Connection class' JavaDoc for details.
    */
  }

  public void closeConnection() throws Exception
  {
    conn.close();
  }

  /**
   * Clear the data in any custom tables created. Do not drop any tables and do not
   * clear the flights table. You should clear any tables you use to store reservations
   * and reset the next reservation ID to be 1.
   */
  public void clearTables ()
  {
	  try {
		  clearTableStatement.executeUpdate();
	  } catch (SQLException e) { e.printStackTrace();}
  }

  /**
   * prepare all the SQL statements in this method.
   * "preparing" a statement is almost like compiling it.
   * Note that the parameters (with ?) are still not filled in
   */
  public void prepareStatements() throws Exception
  {
    searchDirectFlightStatement = conn.prepareStatement(SEARCH_DIRECT_FLIGHT);
    searchIndirectFlightStatement = conn.prepareStatement(SEARCH_INDIRECT_FLIGHT);
    searchUserStatement = conn.prepareStatement(SEARCH_USER);
    checkUsernameExistsStatement = conn.prepareStatement(CHECK_USERNAME_EXISTS);
    insertNewUserStatement = conn.prepareStatement(INSERT_NEW_USER);
    checkReservationDateStatement = conn.prepareStatement(CHECK_RESERVATION_DATE);
    getCurrentReservationidStatement = conn.prepareStatement(GET_CURRENT_RESERVATIONID);
    updateReservationidStatement = conn.prepareStatement(UPDATE_RESERVATIONID);
    addReservationStatement = conn.prepareStatement(ADD_RESERVATION);
    searchReservationStatement = conn.prepareStatement(SEARCH_RESERVATION);
    searchReservedFlightStatement = conn.prepareStatement(SEARCH_RESERVED_FLIGHT);
    searchUsernameOfReservationidStatement = conn.prepareStatement(SEARCH_USERNAME_OF_RESERVATIONID);
    searchUserBalanceStatement = conn.prepareStatement(SEARCH_USER_BALANCE);
    updateUserBalanceStatement = conn.prepareStatement(UPDATE_USER_BALANCE);
    updatePayStatusStatement = conn.prepareStatement(UPDATE_PAY_STATUS);
    reservationInfoOfCancelStatement = conn.prepareStatement(RESERVATION_INFO_OF_CANCEL);
    cancelReservationStatement = conn.prepareStatement(CANCEL_RESERVATION);
    clearTableStatement = conn.prepareStatement(CLEAR_TABLE);
    checkAvailableSeatStatement = conn.prepareStatement(CHECK_AVAILABLE_SEAT);
    /* add here more prepare statements for all the other queries you need */
    /* . . . . . . */
  }

  /**
   * Takes a user's username and password and attempts to log the user in.
   *
   * @param username
   * @param password
   *
   * @return If someone has already logged in, then return "User already logged in\n"
   * For all other errors, return "Login failed\n".
   *
   * Otherwise, return "Logged in as [username]\n".
   */
  public String transaction_login(String username, String password) // login changed completely
  {
	  if (this.username != null) {
		  return "User already logged in\n";
	  }
	  try {
		  searchUserStatement.clearParameters();
		  searchUserStatement.setString(1, username);
		  searchUserStatement.setString(2, password);
		  ResultSet results = searchUserStatement.executeQuery();
		  if (results.next()) {
			  this.username = username;
			  searchResult = null;
			  return "Logged in as " + username  + "\n";
		  }
		  return "Login failed\n";
		  
	  } catch (SQLException e) { return "Login failed\n";}	  
	  
  }

  /**
   * Implement the create user function.
   *
   * @param username new user's username. User names are unique the system.
   * @param password new user's password.
   * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure otherwise).
   *
   * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
   */
  public String transaction_createCustomer (String username, String password, int initAmount)
  {
	  if (initAmount < 0) {
		  return "Failed to create user\n";
	  }
	  try {
		  beginTransaction();
		  //check whether the username exists in the user table
		  checkUsernameExistsStatement.clearParameters();
		  checkUsernameExistsStatement.setString(1, username);
		  ResultSet results = checkUsernameExistsStatement.executeQuery();
		  results.next();
		  if (results.getInt("count") == 0) {
			  // insert new user into user table
			  insertNewUserStatement.clearParameters();
			  insertNewUserStatement.setString(1, username);
			  insertNewUserStatement.setString(2, password);
			  insertNewUserStatement.setInt(3, initAmount);
			  try {
				  insertNewUserStatement.executeUpdate();
				  commitTransaction();
				  return "Created user " + username + "\n";
			  } catch (SQLException deadlock) {
				  return transaction_createCustomer(username, password, initAmount);
			  }			  
			  
		  }		
		  
		  rollbackTransaction();
		  return "Failed to create user\n";	
		  
	  } catch (SQLException e) {e.printStackTrace();}	  
	  return "Failed to create user\n";
  }

  /**
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination
   * city, on the given day of the month. If {@code directFlight} is true, it only
   * searches for direct flights, otherwise is searches for direct flights
   * and flights with two "hops." Only searches for up to the number of
   * itineraries given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight if true, then only search for direct flights, otherwise include indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   *
   * @return If no itineraries were found, return "No flights match your selection\n".
   * If an error occurs, then return "Failed to search\n".
   *
   * Otherwise, the sorted itineraries printed in the following format:
   *
   * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time] minutes\n
   * [first flight in itinerary]\n
   * ...
   * [last flight in itinerary]\n
   *
   * Each flight should be printed using the same format as in the {@code Flight} class. Itinerary numbers
   * in each search should always start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */
  public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries)
  {
    return transaction_search_safe(originCity, destinationCity, directFlight, dayOfMonth, numberOfItineraries);
  }

  /**
   * safe transaction
   * 
   * @param originCity
   * @param destinationCity
   * @param directFlight
   * @param dayOfMonth
   * @param numberOfItineraries
   *
   * @return The search results.
   */
  
  private Flight fillFlightHelper (ResultSet results) {
	  Flight F = new Flight();
	  try {
	  F.capacity = results.getInt("capacity");
	  F.carrierId = results.getString("Carrier");
	  F.dayOfMonth = results.getInt("Day");
	  F.destCity = results.getString("Destination");
	  F.fid = results.getInt("fid");
	  F.flightNum = results.getString("Number");
	  F.originCity = results.getString("Origin");
	  F.price = results.getInt("Price");
	  F.time = results.getInt("Duration");
	  } catch (SQLException e) { e.printStackTrace();}
	  return F;
  }
  private String transaction_search_safe(String originCity, String destinationCity, boolean directFlight, 
		                                 int DayOfMonth, int numberOfItineraries)
  {
	  StringBuffer sb = new StringBuffer();
	  searchResult = new ArrayList<ArrayList<Flight>>();
	  try
	  {
		  ArrayList<Flight> FlightList = new ArrayList<Flight>();
		  searchDirectFlightStatement.clearParameters();
		  searchDirectFlightStatement.setInt(1, numberOfItineraries);
		  searchDirectFlightStatement.setString(2, originCity);
		  searchDirectFlightStatement.setString(3, destinationCity);
		  searchDirectFlightStatement.setInt(4, DayOfMonth);
		  ResultSet results = searchDirectFlightStatement.executeQuery();
		  while (results.next()) {
			  Flight Itinerary = fillFlightHelper(results);
			  FlightList.add(Itinerary);
		  }
		  results.close();
		  
		  if (!directFlight) {
			  searchResult = new ArrayList<ArrayList<Flight>>();
			  int size = FlightList.size();
			  int index = 0;
			  searchIndirectFlightStatement.clearParameters();
			  searchIndirectFlightStatement.setInt(1, numberOfItineraries);
			  searchIndirectFlightStatement.setString(2, originCity);
			  searchIndirectFlightStatement.setString(3, destinationCity);
			  searchIndirectFlightStatement.setInt(4, DayOfMonth);
			  ResultSet result2 = searchIndirectFlightStatement.executeQuery();
			  while (result2.next() && numberOfItineraries - size > 0) {
				  Flight Itinerary1 = new Flight();
				  Itinerary1.capacity = result2.getInt("capacity1");
				  Itinerary1.carrierId = result2.getString("Carrier1");
				  Itinerary1.dayOfMonth = result2.getInt("Day1");
				  Itinerary1.destCity = result2.getString("Destination1");
				  Itinerary1.fid = result2.getInt("fid1");
				  Itinerary1.flightNum = result2.getString("Number1");
				  Itinerary1.originCity = result2.getString("Origin1");
				  Itinerary1.price = result2.getInt("Price1");
				  Itinerary1.time = result2.getInt("Duration1");
				  Flight Itinerary2 = new Flight();
				  Itinerary2.capacity = result2.getInt("capacity2");
				  Itinerary2.carrierId = result2.getString("Carrier2");
				  Itinerary2.dayOfMonth = result2.getInt("Day2");
				  Itinerary2.destCity = result2.getString("Destination2");
				  Itinerary2.fid = result2.getInt("fid2");
				  Itinerary2.flightNum = result2.getString("Number2");
				  Itinerary2.originCity = result2.getString("Origin2");
				  Itinerary2.price = result2.getInt("Price2");
				  Itinerary2.time = result2.getInt("Duration2");
				  int totalTime = result2.getInt("Total_time");
				  
				  while (FlightList.size() != 0 && totalTime >= FlightList.get(0).time) {
					  sb.append("Itinerary " + index + ": 1 flight(s), " + FlightList.get(0).time + " minutes\n");
					  searchResult.add(new ArrayList<Flight>());
					  if (FlightList.get(0).capacity != 0) {
						  searchResult.get(searchResult.size() - 1).add(FlightList.get(0));
					  }
					  index++;
					  sb.append(FlightList.get(0).toString() + "\n");
					  FlightList.remove(0);
				  }
				  sb.append("Itinerary " + index + ": 2 flight(s), " + totalTime + " minutes\n");
				  searchResult.add(new ArrayList<Flight>());
				  if (Itinerary1.capacity != 0 && Itinerary2.capacity != 0) {
					  searchResult.get(searchResult.size() - 1).add(Itinerary1);
					  searchResult.get(searchResult.size() - 1).add(Itinerary2);
				  }
				  index++;
				  sb.append(Itinerary1.toString() + "\n");
				  sb.append(Itinerary2.toString() + "\n");
				  size++;
			  }
			  
			  result2.close();
			  
			  for (int i = 0; i < FlightList.size(); i++) {
				  searchResult.add(new ArrayList<Flight>());
				  if (FlightList.get(i).capacity != 0) {
					  searchResult.get(searchResult.size() - 1).add(FlightList.get(i));
				  }
				  sb.append("Itinerary " + index + ": 1 flight(s), " + FlightList.get(i).time + " minutes\n");
				  sb.append(FlightList.get(i).toString() + "\n");
				  index++;
			  }
			  
		  }
		  else {
			  for (int i = 0; i < FlightList.size(); i++) {
				  searchResult.add(new ArrayList<Flight>());
				  searchResult.get(searchResult.size() - 1).add(FlightList.get(i));
				  sb.append("Itinerary " + i + ": 1 flight(s), " + FlightList.get(i).time + " minutes\n");
				  sb.append(FlightList.get(i).toString() + "\n");
			  }	
		  }
		  return sb.toString();
		  
	  } catch (SQLException e) { e.printStackTrace();}
	  
	  return "search failed";
	
	  
  }
  
  /**
   * Implements the book itinerary function.
   *
   * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in the current session.
   *
   * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
   * If try to book an itinerary with invalid ID, then return "No such itinerary {@code itineraryId}\n".
   * If the user already has a reservation on the same day as the one that they are trying to book now, then return
   * "You cannot book two flights in the same day\n".
   * For all other errors, return "Booking failed\n".
   *
   * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n" where
   * reservationId is a unique number in the reservation system that starts from 1 and increments by 1 each time a
   * successful reservation is made by any user in the system.
   */
  public String transaction_book(int itineraryId)
  {
	  if (this.username == null) {
		  return "Cannot book reservations, not logged in\n";
	  }
	  if (searchResult == null || itineraryId > searchResult.size() - 1 || itineraryId < 0) {
		  return "No such itinerary " + itineraryId + "\n";
	  }
	  if (searchResult.get(itineraryId).isEmpty()) {
		  return "Booking failed\n";
	  }
	  
	  try {
		  ArrayList<Flight> FList = searchResult.get(itineraryId);
		  int date = FList.get(0).dayOfMonth;
		  // check whether the user has a reservation on the same day
		  checkReservationDateStatement.clearParameters();
		  checkReservationDateStatement.setString(1, username);
		  checkReservationDateStatement.setInt(2, date);
		  beginTransaction();
		  try {
			  ResultSet result = checkReservationDateStatement.executeQuery();		
			  if (result.next()) {
				  commitTransaction();
				  return "You cannot book two flights in the same day\n";
			  }
		  } catch (SQLException deadlock) {
			  return transaction_book(itineraryId);
		  }
		
		  getCurrentReservationidStatement.clearParameters();
		  int ID = 0;
		  try {
			  ResultSet reserveid = getCurrentReservationidStatement.executeQuery();		
			  reserveid.next();
			  ID = reserveid.getInt("reservationID");
		  } catch (SQLException deadlock) {
			  return transaction_book(itineraryId);
		  }
		  Flight f1 = FList.get(0);
		  checkAvailableSeatStatement.clearParameters();
		  checkAvailableSeatStatement.setInt(1, f1.fid);
		  checkAvailableSeatStatement.setInt(2, f1.fid);
		  try {
			  ResultSet availableSeat = checkAvailableSeatStatement.executeQuery();
			  availableSeat.next();
		  
			  if (f1.capacity <= availableSeat.getInt(1)) {
				  commitTransaction();
				  return "Booking failed\n";
			  }
		  } catch (SQLException deadlock) {
			  return transaction_book(itineraryId);
		  }
		  
		  int fid2 = 0;
		  int f2price = 0;
		  if (FList.size() == 2) {
			  Flight f2 = FList.get(1);
			  checkAvailableSeatStatement.clearParameters();
			  checkAvailableSeatStatement.setInt(1, f2.fid);
			  checkAvailableSeatStatement.setInt(2, f2.fid);
			  try {
				  ResultSet availableSeat = checkAvailableSeatStatement.executeQuery();
				  availableSeat.next();
			  
				  if (f2.capacity <= availableSeat.getInt(1)) {
					  commitTransaction();
					  return "Booking failed\n";
				  }
			  } catch (SQLException deadlock) {
				  return transaction_book(itineraryId);
			  }
			  fid2 = f2.fid;
			  f2price = f2.price;
			  
		  }  
			  // update reservation table
			  addReservationStatement.clearParameters();
			  addReservationStatement.setString(1, this.username);
			  addReservationStatement.setInt(2, ID + 1);
			  addReservationStatement.setInt(3, f1.dayOfMonth);
			  addReservationStatement.setInt(4, f1.fid);
			  addReservationStatement.setInt(5, fid2);
			  addReservationStatement.setString(6, "false");
			  addReservationStatement.setInt(7, f1.price + f2price);
			  try {
				  addReservationStatement.executeUpdate();
			  } catch (SQLException deadlock) {
				  return transaction_book(itineraryId);
			  }
		  
		  // UPDATE reservationId.
		  updateReservationidStatement.clearParameters();
		  updateReservationidStatement.setInt(1, ID + 1);
		  updateReservationidStatement.setInt(2, ID);
		  try {
			  updateReservationidStatement.executeUpdate();
		  } catch (SQLException deadlock) {
			  return transaction_book(itineraryId);
		  }
		  
		  commitTransaction();
		  return "Booked flight(s), reservation ID: " + (ID + 1) + "\n";
	  } catch (SQLException unknowError) {
		  unknowError.printStackTrace();
		  return "Booking failed\n";
	  }	  	  
  }

  /**
   * Implements the reservations function.
   *
   * @return If no user has logged in, then return "Cannot view reservations, not logged in\n"
   * If the user has no reservations, then return "No reservations found\n"
   * For all other errors, return "Failed to retrieve reservations\n"
   *
   * Otherwise return the reservations in the following format:
   *
   * Reservation [reservation ID] paid: [true or false]:\n"
   * [flight 1 under the reservation]
   * [flight 2 under the reservation]
   * Reservation [reservation ID] paid: [true or false]:\n"
   * [flight 1 under the reservation]
   * [flight 2 under the reservation]
   * ...
   *
   * Each flight should be printed using the same format as in the {@code Flight} class.
   *
   * @see Flight#toString()
   */
  public String transaction_reservations() // read uncommitted
  {
	  if (username == null) {
		  return "Cannot view reservations, not logged in\n";
	  }
	  try {
		  
		  searchReservationStatement.clearParameters();
		  searchReservationStatement.setString(1, username);
		  ResultSet reservation = searchReservationStatement.executeQuery();
		  
		  StringBuffer sb = new StringBuffer();
		  		  
		  // print out the information of reserved flights
		  boolean Empty = true;
		  while (reservation.next()) {
			  Empty = false;
			  sb.append("Reservation " + reservation.getInt("reservationID") + " paid: " + reservation.getString("pay") + ":\n");
			  searchReservedFlightStatement.clearParameters();
			  searchReservedFlightStatement.setInt(1, reservation.getInt("fid1"));
			  ResultSet flightInfo = searchReservedFlightStatement.executeQuery();
			  flightInfo.next();
			  Flight Itinerary = fillFlightHelper(flightInfo);
			  sb.append(Itinerary.toString() + "\n");
			  if (reservation.getInt("fid2") != 0) {
				  searchReservedFlightStatement.clearParameters();
				  searchReservedFlightStatement.setInt(1, reservation.getInt("fid1"));
				  ResultSet Info = searchReservedFlightStatement.executeQuery();
				  Info.next();
				  Flight Iti = fillFlightHelper(flightInfo);
				  sb.append(Iti.toString() + "\n");
			  }
		  }
		  
		  if (Empty) {
			  return "No reservations found\n";
		  }
		  return sb.toString();
		  
	  } catch (SQLException unknownError) { 
		  unknownError.printStackTrace();
		  return "Failed to retrieve reservations\n";
	  }
	  
  }

  /**
   * Implements the cancel operation.
   *
   * @param reservationId the reservation ID to cancel
   *
   * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n"
   * For all other errors, return "Failed to cancel reservation [reservationId]"
   *
   * If successful, return "Canceled reservation [reservationId]"
   *
   * Even though a reservation has been canceled, its ID should not be reused by the system.
   */
  public String transaction_cancel(int reservationId)
  {
    if (this.username == null) {
    		return "Cannot cancel reservations, not logged in\n";
    }
    try {
    		beginTransaction();
    		
    		reservationInfoOfCancelStatement.clearParameters();
    		reservationInfoOfCancelStatement.setInt(1, reservationId);
    		reservationInfoOfCancelStatement.setString(2, this.username);
    		ResultSet cancel = reservationInfoOfCancelStatement.executeQuery();
    		if (!cancel.next()) {
    			commitTransaction();
    			return "Failed to cancel reservation " + reservationId + "\n";
    		}
    		
    		if (cancel.getString("pay").equals("true")) {
    			
    				// get the current balance of the user
    				searchUserBalanceStatement.clearParameters();
    				searchUserBalanceStatement.setString(1, this.username);
    				ResultSet curr_balance;
    				curr_balance = searchUserBalanceStatement.executeQuery();
    				curr_balance.next();
    				
    				// refund to user's balance
    				updateUserBalanceStatement.clearParameters();
    				updateUserBalanceStatement.setInt(1, curr_balance.getInt("balance") + cancel.getInt("price"));
    				curr_balance.close();
    				updateUserBalanceStatement.setString(2, this.username);
    				try {
  					  updateUserBalanceStatement.executeUpdate();
  				} catch (SQLException deadlock) {
  					  return transaction_cancel(reservationId);
  				}
    				
    		}
    		
    		cancel.close();	
    		
    		// delete the reservation in reservation table
    		cancelReservationStatement.clearParameters();
    		cancelReservationStatement.setInt(1, reservationId);
    		try {
    			cancelReservationStatement.executeUpdate();
		} catch (SQLException deadlock) {
			return transaction_cancel(reservationId);
		}
    			   			
    		commitTransaction();
    		return "Canceled reservation " + reservationId + "\n";
    		   		
    	} catch (SQLException unknownError) {
    		unknownError.printStackTrace();
    		return "Failed to cancel reservation " + reservationId + "\n";    
    	}
    
  }

  /**
   * Implements the pay function.
   *
   * @param reservationId the reservation to pay for.
   *
   * @return If no user has logged in, then return "Cannot pay, not logged in\n"
   * If the reservation is not found / not under the logged in user's name, then return
   * "Cannot find unpaid reservation [reservationId] under user: [username]\n"
   * If the user does not have enough money in their account, then return
   * "User has only [balance] in account but itinerary costs [cost]\n"
   * For all other errors, return "Failed to pay for reservation [reservationId]\n"
   *
   * If successful, return "Paid reservation: [reservationId] remaining balance: [balance]\n"
   * where [balance] is the remaining balance in the user's account.
   */
  public String transaction_pay (int reservationId)
  {
	  if (this.username == null) {
		  return "Cannot pay, not logged in\n";
	  }
	  try {
		  beginTransaction();
		  searchUsernameOfReservationidStatement.clearParameters();
		  searchUsernameOfReservationidStatement.setInt(1, reservationId);
		  searchUsernameOfReservationidStatement.setString(2, this.username);
		  searchUsernameOfReservationidStatement.setString(3, "FALSE");
		  ResultSet resInfo = searchUsernameOfReservationidStatement.executeQuery();
		  int price = 0;
		  // get the price of the reservation if the reservation exists.
		  if (!resInfo.next()) {
			  commitTransaction();
			  return "Cannot find unpaid reservation " + reservationId + " under user: "
				  		+ this.username + "\n";
		  }
		  price = resInfo.getInt("price");
		  resInfo.close();
		  // To find the user's balance
		  searchUserBalanceStatement.clearParameters();
		  searchUserBalanceStatement.setString(1, username);
		  int Balance = 0;
		  try {
			  ResultSet userBalance = searchUserBalanceStatement.executeQuery();
			  userBalance.next();
			  Balance = userBalance.getInt("balance") - price;
			  if (Balance < 0) {
				  rollbackTransaction();
				  return "User has only " + userBalance.getInt("balance") + 
						  " in account but itinerary costs " + price + "\n";
			  }
		  } catch (SQLException deadlock) {
			  return transaction_pay(reservationId);
		  }
		  
		  // update the user's balance
		  updateUserBalanceStatement.clearParameters();
		  updateUserBalanceStatement.setInt(1, Balance);
		  updateUserBalanceStatement.setString(2, username);
		  try {
			  updateUserBalanceStatement.executeUpdate();
		  } catch (SQLException deadlock) {
			  return transaction_pay(reservationId);
		  }
		  
		  // update the pay status in reservation
		  updatePayStatusStatement.clearParameters();
		  updatePayStatusStatement.setString(1, "true");
		  updatePayStatusStatement.setString(2, username);
		  updatePayStatusStatement.setInt(3, reservationId);
		  
		  try {
			  updatePayStatusStatement.executeUpdate();
		  } catch (SQLException deadlock) {
				  return transaction_pay(reservationId);
		  }
		  
		  commitTransaction();
		  return "Paid reservation: " + reservationId + " remaining balance: " + Balance + "\n";
		  
	  } catch (SQLException unknowError) { 
		  return "Failed to cancel reservation " + reservationId + "\n";
	  }
	  
  }

  /* some utility functions below */

  public void beginTransaction() throws SQLException
  {
	  
    conn.setAutoCommit(false);
    conn.createStatement().execute("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;");
    
  }

  public void commitTransaction() throws SQLException
  {
	  conn.createStatement().execute("COMMIT TRANSACTION;");
    conn.setAutoCommit(true);
  }

  public void rollbackTransaction() throws SQLException
  {
	  conn.createStatement().execute("ROLLBACK TRANSACTION;");
    conn.setAutoCommit(true);
  }

  /**
   * Shows an example of using PreparedStatements after setting arguments. You don't need to
   * use this method if you don't want to.
   */
  
}

