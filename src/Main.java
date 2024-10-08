import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Scanner;
import java.sql.Statement;
import java.sql.ResultSet;

public class Main {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "Choudhary@123";

    //----------------main class-----------------
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded succesfully ...");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            while (true) {
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                Scanner scanner = new Scanner(System.in);
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservations");
                System.out.println("5. Delete Reservations");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        reserveRoom(connection, scanner);
                        break;
                    case 2:
                        viewReservations(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, scanner);
                        break;
                    case 4:
                        updateReservation(connection, scanner);
                        break;
                    case 5:
                        deleteReservation(connection, scanner);
                        break;
                    case 0:
                        exit();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static void reserveRoom(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter guest name : ");
            String guestName = scanner.next();

            System.out.println("Enter Room number :");
            int roomNumber = scanner.nextInt();

            String contactNumber;

            while (true){
                System.out.println("Enter the contact number : ");
                contactNumber = scanner.nextLine();
                if(contactNumber.length() == 10 && contactNumber.matches("\\d{10}")){
                    break;
                }else {
                    System.out.println("Invalid input number. Please enter a 10-digit number.");
                }
            }

            String sql = "INSERT INTO reservations (guest_name,room_number,contact_number)" +
                    "VALUES('" + guestName + "', " + roomNumber + ",'" + contactNumber + "');";
            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation successfully!!  ");
                } else {
                    System.out.println("Reservation failed. ");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewReservations(Connection connection) throws SQLException {
        String sql = "SELECT reservation_id,guest_name,room_number,contact_number,reservation_date FROM reservations ;";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)){

            System.out.println("Current Reservations:");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");
            System.out.println("| Reservation ID | Guest           | Room Number   | Contact Number      | Reservation Date        |");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");

            while (resultSet.next()) {
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();

                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s   |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
            }
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");
            System.out.println("------------------------------------------THANK YOU-------------------------------------------------");
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner) {
        try{
            System.out.println("Enter the reservations ID : ");
            int reservationId = scanner.nextInt();
            System.out.println("Enter the guest name : ");
            String guestname = scanner.nextLine();

            String sql = "SELECT room_number FROM reservations " + "WHERE reservation_id = " + reservationId + " AND guest_name = '" + guestname + "'";

            try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){

                if(resultSet.next()){
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("Room number for Reservation ID " + reservationId + " and guest " + guestname + " is : "+ roomNumber);
                }else{
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void updateReservation(Connection connection, Scanner scanner) {
        try{
            System.out.println("Enter the reservation ID .");
            int reservationId = scanner.nextInt();
            scanner.nextLine();
            if(!reservationExists(connection,reservationId)){
                System.out.println("Reservation not found for the given ID.");
                return;
            }
            System.out.println("Enter new guest name : ");
            String newGuestName = scanner.nextLine();
            System.out.println("Enter room number: ");
            int newRoomNumber = scanner.nextInt();
            System.out.println("Enter new contact number : ");
            String newContactNumber = scanner.next();

            String sql = "UPDATE reservations SET guest_name = '" + newGuestName + "'," +
                    "room_number = " + newRoomNumber + ", " +
                    "contact_number = '" + newContactNumber + "' " +
                    "WHERE reservation_id = " + reservationId;
            try(Statement statement = connection.createStatement()){
                int affectedRows =statement.executeUpdate(sql);
                if(affectedRows>0){
                    System.out.println("Reservation update succesfully!");
                }else{
                    System.out.println("Reservation update  faild.");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private static void deleteReservation(Connection connection, Scanner scanner)throws InterruptedException {
        try {
            System.out.println("Enter reservation ID to delete: ");
            int reservationId = scanner.nextInt();

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            String sql = "DELETE FROM reservations WHERE reservation_id = " + reservationId;
            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);
                if (affectedRows > 0) {
                    System.out.println("Reservation deleting ");
                    int i = 4;
                    while(i!=0){
                        System.out.print(".");
                        Thread.sleep(1000);
                        i--;
                    }
                    System.out.println("Delet succesceful!");
                } else {
                    System.out.println("Reservation deletion failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean reservationExists(Connection connection, int reservationId) {
        try{
            String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + reservationId;
            try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){
                return resultSet.next();
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while(i!=0){
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("Thank You for Using Hotel Reservations System!!!");
    }
}