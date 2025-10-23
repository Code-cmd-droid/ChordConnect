private static final String URL  = "jdbc:mysql://localhost:3306/chord_connect";
private static final String USER = "root";
private static final String PASS = "12345678";

Connection conn = Database.getConnection();
System.out.println("Connected to: " + conn.getMetaData().getURL());

