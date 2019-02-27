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

  // DB Connection
  private Connection conn;

  // Logged In User
  private String username; // customer username is unique

  // itinerary
  private ArrayList<Itinerary> itineraries;

  // offset for indirect queries
  private static final int OFFSET = 19;
  
  // Canned queries

  private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement checkFlightCapacityStatement;

  private static final String GET_USER = "SELECT * FROM user WHERE username = ? and password = ?;";
  private PreparedStatement getUserStatement;

  private static final String GET_DIRECT_SEARCH = "SELECT TOP (?) * FROM Flights "
          + "WHERE origin_city = ? AND dest_city = ? AND day_of_month =  ? "
          + "AND canceled <> 1 ORDER BY actual_time ASC, fid ASC;";
  private PreparedStatement getDirectSearchStatement;

  private static final String GET_INDIRECT_SEARCH = "SELECT TOP (?) * FROM Flights F1, Flights F2"
          + "WHERE F1.origin_city = ? AND F1.dest_city = F2.origin_city AND F2.dest_city = ?"
          + "AND F1.day_of_month = ? AND F1.day_of_month = F2.day_of_month"
          + "AND F1.canceled <> 1 AND F2.canceled <> 1"
          + "ORDER BY (F1.actual_time + F2.actual_time), F1.fid ASC;";
  private PreparedStatement getIndirectSearchStatement;

  private static final String GET_RESERVATIONS = "SELECT * FROM RESERVATIONS WHERE username = ?;";
  private PreparedStatement getReservationsStatement;

  private static final String GET_BALANCE = "SELECT balance FROM USERS WHERE username = ?;";
  private PreparedStatement getBalanceStatement;

  private static final String SET_BALANCE = "UPDATE USERS SET balance = ? WHERE username = ?;";
  private PreparedStatement setBalanceStatement;

  private static final String GET_PAID_STATUS = "SELECT * FROM RESERVATIONS WHERE rid = ?;";
  private PreparedStatement getPaidStatusStatement;

  private static final String SET_PAID_STATUS = "UPDATE RESERVATIONS SET paid = ? WHERE rid = ?;";
  private PreparedStatement setPaidStatusStatement;

  private static final String GET_FLIGHT = "SELECT * FROM Flights WHERE fid = ?;";
  private PreparedStatement getFlightStatement;

  private static final String GET_CAPACITIES = "SELECT * FROM Capacities WHERE fid = ?;";
  private PreparedStatement getCapacitiesStatement;

  private static final String UPDATE_CAPACITIES = "UPDATE CAPACITIES SET capacity = ((SELECT capacity FROM CAPACITIES WHERE fid = ?) - 1) WHERE fid = ?;";
  private PreparedStatement updateCapacitiesStatement;

  private static final String GET_RESERVATION_COUNT = "SELECT count FROM RESERVATION_COUNT;";
  private PreparedStatement getReservationCountStatement;

  private static final String SET_RESERVATION_COUNT = "UPDATE RESERVATION_COUNT SET count = ((SELECT count FROM RESERVATION_COUNT) + 1) WHERE username = ?;";
  private PreparedStatement setReservationsCountStatement;

  // transactions
  private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
  private PreparedStatement beginTransactionStatement;

  private static final String COMMIT_SQL = "COMMIT TRANSACTION";
  private PreparedStatement commitTransactionStatement;

  private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
  private PreparedStatement rollbackTransactionStatement;

  //insertions
  private static final String CREATE_USER = "INSERT INTO USERS VALUES (?,?,?);";
  private PreparedStatement createUserStatement;

  private static final String INSERT_RESERVATION = "INSERT INTO RESERVATIONS VALUES (?,?,?,?,?,?);";
  private PreparedStatement insertReservationStatement;

  private static final String INSERT_CAPACITY = "INSERT INTO CAPACITIES "
  		+ "SELECT F.fid, F.capacity "
  		+ "FROM Flights F "
  		+ "WHERE f.fid = ? "
  		+ "AND NOT EXISTS "
  		+ "(SELECT * FROM Capacity c WHERE c.fid = f.fid);";
  private PreparedStatement insertCapacityStatement;

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

  class Itinerary
  {
    public Flight first_flight;
    public Flight second_flight;
    public int day;
    public int cost;
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
    // your code here
    /*
    beginTransaction();
    Statement clear = conn.createStatement();
    clear.executeUpdate("DELETE FROM RESERVATIONS");
    clear.executeUpdate("DELETE FROM USERS");
    clear.executeUpdate("DELETE FROM CAPACITIES");
    clear.executeUpdate("DELETE FROM RESERVATION_COUNT");
    commitTransaction();
    */
    try {
	  clearTableStatement.executeUpdate();
    } 
    catch (SQLException e) { e.printStackTrace();}
  }

  /**
   * prepare all the SQL statements in this method.
   * "preparing" a statement is almost like compiling it.
   * Note that the parameters (with ?) are still not filled in
   */
  public void prepareStatements() throws Exception
  {
    beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
    commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
    rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);

    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);

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
  public String transaction_login(String username, String password)
  {
    if (this.username != null){
      return "User already logged in\n";
    }
    try {
      beginTransaction();
      //get user data
      getUserStatement.clearParameters();
      getUserStatement.setString(1, username);
      getUserStatement.setString(2, password);
      ResultSet getRes = getUserStatement.executeQuery();
      if (getRes.next()){
        this.username = username;
        getRes.close();
        //set itinerary
        commitTransaction();
        return "Logged in as " + this.username + "\n";
      }
      rollbackTransaction();
      getRes.close();
    } catch (SQLException e) { e.printStackTrace();}
    return "Login failed\n";
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
    if (initAmount >=0){
      try{
        beginTransaction();
        getUserStatement.clearParameters();
        getUserStatement.setString(1,username);
        ResultSet getRes = getUserStatement.executeQuery();
        //THERE ALREADY EXISTS A USER W SAME USERNAME
        if (getRes.next()){
          getRes.close();
          rollbackTransaction();
          return "Failed to create user";
        }
        createUserStatement.clearParameters();
        createUserStatement.setString(1,username);
        createUserStatement.setString(2,password);
        createUserStatement.setInt(3,initAmount);
        createUserStatement.execute();
        commitTransaction();
        return "Created user" + username + "\n";
        } catch (SQLException e) { e.printStackTrace();}
    }
    return "Failed to create user";
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
   // return transaction_search_unsafe(originCity, destinationCity, directFlight, dayOfMonth, numberOfItineraries);
  
    StringBuffer sb = new StringBuffer();

    try
    {
      beginTransaction();

      //getDirectData
      getDirectSearchStatement.clearParameters();
      getDirectSearchStatement.setString(1,originCity);
      getDirectSearchStatement.setString(2,destinationCity);
      getDirectSearchStatement.setInt(3, dayOfMonth);

      ResultSet oneHopResults = getDirectSearchStatement.executeQuery();

      int numItineraries = numberOfItineraries;

      while (oneHopResults.next() && numItineraries > 0)
      {
        Flight singleFlight = flightParser(oneHopResults, true);
        Itinerary it = new Itinerary();
        it.first_flight = singleFlight;
        it.second_flight = null;
        it.day = dayOfMonth;
        it.cost = singleFlight.price;
        this.itineraries.add(it);

        sb.append("Itinerary " + (numberOfItineraries - numItineraries) + ": 1 flight(s), " + singleFlight.time + " minutes\n");
        sb.append(singleFlight.toString() + "\n");
        numItineraries--;

      }
      oneHopResults.close();

      if (numItineraries > 0 && !directFlight){
        getIndirectSearchStatement.clearParameters();
        getIndirectSearchStatement.setString(1,originCity);
        getIndirectSearchStatement.setString(2,destinationCity);
        getIndirectSearchStatement.setInt(3,dayOfMonth);

        ResultSet twoHopResults = getIndirectSearchStatement.executeQuery();

        while (twoHopResults.next() && numItineraries > 0){
          //*****figure out what to do with itineraries
          Flight firstFlight = flightParser(twoHopResults, true);
          Flight secondFlight = flightParser(twoHopResults, false);

          Itinerary it = new Itinerary();
          it.first_flight = firstFlight;
          it.second_flight = secondFlight;
          it.day = dayOfMonth;
          it.cost = firstFlight.price + secondFlight.price;
          this.itineraries.add(it);

          int totalTime = firstFlight.time + secondFlight.time;
          sb.append("Itinerary " + (numberOfItineraries - numItineraries) + ": 2 flight(s), " + totalTime + " minutes\n");
          sb.append(firstFlight.toString() + "\n");
          sb.append(secondFlight.toString() + "\n");
          numItineraries--;
        }
        twoHopResults.close();
      }
      commitTransaction();
    } catch (SQLException e) { e.printStackTrace(); }

    return sb.toString();
  }

  private Flight flightParser(ResultSet results, boolean direct) throws SQLException{
    int offset = 0;
    if (!direct){
      offset = this.OFFSET;
    }
    Flight flight_info = new Flight();
    flight_info.fid = results.getInt(1 + offset);
    flight_info.dayOfMonth = results.getInt(3 + offset);
    flight_info.carrierId = results.getString(5 + offset);
    flight_info.flightNum = results.getString(6 + offset);
    flight_info.originCity = results.getString(7 + offset);
    flight_info.destCity = results.getString(9 + offset);
    flight_info.time = results.getInt(15 + offset);
    flight_info.capacity = results.getInt(17 + offset);
    flight_info.price = results.getInt(18 + offset);
    return flight_info;
  }

  /**
   * Same as {@code transaction_search} except that it only performs single hop search and
   * do it in an unsafe manner.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight
   * @param dayOfMonth
   * @param numberOfItineraries
   *
   * @return The search results. Note that this implementation *does not conform* to the format required by
   * {@code transaction_search}.
   */
  private String transaction_search_unsafe(String originCity, String destinationCity, boolean directFlight,
                                          int dayOfMonth, int numberOfItineraries)
  {
    StringBuffer sb = new StringBuffer();

    try
    {
      // one hop itineraries
      String unsafeSearchSQL =
              "SELECT TOP (" + numberOfItineraries + ") day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
                      + "FROM Flights "
                      + "WHERE origin_city = \'" + originCity + "\' AND dest_city = \'" + destinationCity + "\' AND day_of_month =  " + dayOfMonth + " "
                      + "ORDER BY actual_time ASC";

      Statement searchStatement = conn.createStatement();
      ResultSet oneHopResults = searchStatement.executeQuery(unsafeSearchSQL);

      while (oneHopResults.next())
      {
        int result_dayOfMonth = oneHopResults.getInt("day_of_month");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        String result_originCity = oneHopResults.getString("origin_city");
        String result_destCity = oneHopResults.getString("dest_city");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");

        sb.append("Day: " + result_dayOfMonth + " Carrier: " + result_carrierId + " Number: " + result_flightNum + " Origin: " + result_originCity + " Destination: " + result_destCity + " Duration: " + result_time + " Capacity: " + result_capacity + " Price: " + result_price + "\n");
      }
      oneHopResults.close();
    } catch (SQLException e) { e.printStackTrace(); }

    return sb.toString();
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
    //user not logged in
    if(this.username == null){
      return "Cannot book reservations, not logged in\n.";
    }

    //no previous search results
    if (this.itineraries.size() == 0){
      return "Booking failed\n";
    }

    //itineraryId out of bounds
    if (itineraryId < 0 || itineraryId >= this.itineraries.size()){
      return "No such itinerary " + itineraryId + "\n";
    }

    Itinerary it = itineraries.get(itineraryId);

    try{
    //trying to book two flights on the same day
    //need to check current reservations
    //check if user already has a reservation of the same day
    beginTransaction();
    getReservationsStatement.clearParameters();
    getReservationsStatement.setString(1,this.username);
    ResultSet getResReservation = getReservationsStatement.executeQuery();
    while(getResReservation.next()){
      int curr = getResReservation.getInt("day");

      if (it.day == curr){
        getResReservation.close();
        rollbackTransaction();
        return "You cannot book two flights in the same day\n";
      }
    }
    getResReservation.close();

    //make a reservation
    //need to check Capacity

    //CAPACITIES table needs to be on hold
    //check curr_capacity
    //if curr_capacity < max_capacity
    //set curr_capacity to curr_capacity + 1

    //check if there is space in the first flight

    insertCapacityStatement.clearParameters();
    insertCapacityStatement.setInt(1, it.first_flight.fid);
    insertCapacityStatement.execute();

    getCapacitiesStatement.clearParameters();
    getCapacitiesStatement.setInt(1,it.first_flight.fid);
    ResultSet getResCapacities1 = getCapacitiesStatement.executeQuery();

    int capacity1 = getResCapacities1.getInt("capacity");

    getResCapacities1.close();
    if (capacity1 == 0){
       rollbackTransaction();
       return "Booking failed\n";
    }


    //check if there is space in the second flight if there is a second flight
    if (it.second_flight != null){
      insertCapacityStatement.clearParameters();
      insertCapacityStatement.setInt(1, it.second_flight.fid);
      insertCapacityStatement.execute();

      getCapacitiesStatement.clearParameters();
      getCapacitiesStatement.setInt(1,it.second_flight.fid);
      ResultSet getResCapacities2 = getCapacitiesStatement.executeQuery();

      int capacity2 = getResCapacities2.getInt("capacity");

      getResCapacities2.close();
      if (capacity2 == 0){
         rollbackTransaction();
         return "Booking failed\n";
      }
    }

    //update capacities of flights
    updateCapacitiesStatement.clearParameters();
    updateCapacitiesStatement.setInt(1,it.first_flight.fid);
    updateCapacitiesStatement.setInt(2,it.first_flight.fid);
    updateCapacitiesStatement.execute();

    if (it.second_flight != null){
      updateCapacitiesStatement.clearParameters();
      updateCapacitiesStatement.setInt(1,it.second_flight.fid);
      updateCapacitiesStatement.setInt(2,it.second_flight.fid);
      updateCapacitiesStatement.execute();
    }

    //rservation stuff
    getReservationCountStatement.clearParameters();
    ResultSet getResCount = getReservationCountStatement.executeQuery();
    int count = getResCount.getInt("count");

    setReservationsCountStatement.clearParameters();
    setReservationsCountStatement.execute();

    insertReservationStatement.clearParameters();
    insertReservationStatement.setInt(1,count+1);
    insertReservationStatement.setInt(2,0);
    insertReservationStatement.setInt(3, it.cost);
    insertReservationStatement.setInt(4,it.first_flight.fid);
		insertReservationStatement.setInt(5, it.second_flight.fid);
    insertReservationStatement.setString(6, this.username);
    insertReservationStatement.execute();
    commitTransaction();
  } catch (SQLException e){ e.printStackTrace(); }
    return "Booked flight(s), reservation ID: " + "\n"; 
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
  public String transaction_reservations()
  {
    if (this.username == null) {
      return "Cannot view reservations, not logged in\n";
    }
    try{
      beginTransaction();
      getReservationsStatement.clearParameters();
      getReservationsStatement.setString(1, this.username);
      ResultSet getRes = getReservationsStatement.executeQuery();
      if (getRes.isBeforeFirst()){
        StringBuilder sb = new StringBuilder();
        while (getRes.next()){
          int resid = getRes.getInt("rid");
          int respaid = getRes.getInt("paid");
          String paid = "";
          if (respaid == 1){
            paid = "true";
          }
          else{
            paid = "false";
          }
          sb.append("Reversation" + resid + " paid: " + paid + ":\n");
          sb.append(this.parseFlightGivenID(getRes.getInt("flight1")));
          sb.append(this.parseFlightGivenID(getRes.getInt("flight2")));
        }
      }
      else{
        getRes.close();
        commitTransaction();
        return "No reservations found\n";
      }
    } catch (SQLException e) { e.printStackTrace(); }
    return "Failed to retrieve reservations\n";
  }

  private String parseFlightGivenID(int flight_id) throws SQLException
  {
      beginTransaction();
      getFlightStatement.clearParameters();
      getFlightStatement.setInt(1,flight_id);
      ResultSet getRes = getFlightStatement.executeQuery();
      Flight flightInfo = flightParser(getRes, false);
      getRes.close();
      commitTransaction();
      return flightInfo.toString();

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
    // only implement this if you are interested in earning extra credit for the HW!
    return "Failed to cancel reservation " + reservationId;
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
    if (this.username == null){
      return "Cannot pay, not logged in\n";
    }
    //*****check database for the reservation and return
    try{
      beginTransaction();

      getReservationsStatement.clearParameters();

      getPaidStatusStatement.clearParameters();
      getPaidStatusStatement.setInt(1,reservationId);

      ResultSet getResReservation = getPaidStatusStatement.executeQuery();
      if (!getResReservation.next()){
        rollbackTransaction();
        return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
      }
      else{

        int paid = getResReservation.getInt("paid");
        if (paid == 1){
          getResReservation.close();
          rollbackTransaction();
          return "Failed to pay for reservation " + reservationId + "\n";
        }

        int cost = getResReservation.getInt("cost");

        getBalanceStatement.clearParameters();
        getBalanceStatement.setString(1,this.username);
        ResultSet getResBalance = getBalanceStatement.executeQuery();
        int balance = getResBalance.getInt("balance");
        if (cost > balance){
          getResReservation.close();
          getResBalance.close();
          rollbackTransaction();
          return "User has only " + balance + " in account but itinerary costs " + cost + "\n";
        }
        setBalanceStatement.clearParameters();
        setBalanceStatement.setInt(1, balance - cost);
        setBalanceStatement.setString(2,this.username);
        setBalanceStatement.executeUpdate();

        setPaidStatusStatement.clearParameters();
        setPaidStatusStatement.setInt(1,1);
        setPaidStatusStatement.setInt(2,reservationId);
        setPaidStatusStatement.executeUpdate();
        commitTransaction();
        return "Paid reservation: " + reservationId + " remaining balance: " + (balance - cost) + "\n";
      }
    } catch (SQLException e) { e.printStackTrace(); }
    return "Failed to pay for reservation " + reservationId + "\n";
  }

  /* some utility functions below */

  public void beginTransaction() throws SQLException
  {
    conn.setAutoCommit(false);
    beginTransactionStatement.executeUpdate();
  }

  public void commitTransaction() throws SQLException
  {
    commitTransactionStatement.executeUpdate();
    conn.setAutoCommit(true);
  }

  public void rollbackTransaction() throws SQLException
  {
    rollbackTransactionStatement.executeUpdate();
    conn.setAutoCommit(true);
  }

  /**
   * Shows an example of using PreparedStatements after setting arguments. You don't need to
   * use this method if you don't want to.
   */
  private int checkFlightCapacity(int fid) throws SQLException
  {
    checkFlightCapacityStatement.clearParameters();
    checkFlightCapacityStatement.setInt(1, fid);
    ResultSet results = checkFlightCapacityStatement.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }
}
