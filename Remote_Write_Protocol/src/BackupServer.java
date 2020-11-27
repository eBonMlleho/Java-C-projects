/**
 * @author: Zhanghao
 * 10/28/2020
 */

package cs454hw5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class BackupServer {
	static int backUpData = 0;

	public static void main(String[] args) throws IOException {

		Scanner input = new Scanner(System.in);

		System.out.print("Enter this backup server port number followed by primary server port number: ");
		String line = input.nextLine();
		int backupPort = Integer.parseInt(line.split(" ")[0]);

		int primaryPort = Integer.parseInt(line.split(" ")[1]);

		if (joinBackupPort(backupPort, primaryPort)) {
			// TODO: set up serversocket and listen on port backupPort

			setUpTCPChannel(backupPort, primaryPort);

		} else {
			System.out.println("JOIN primary port: " + primaryPort + " failed!");
		}

	}

	public static boolean joinBackupPort(int backupPort, int primaryPort) {
		Socket socket = null;

		try {
			socket = new Socket("localhost", primaryPort);

			InputStream inStream = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));

			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);

			// send backupPort to primary server
			writer.println("JOIN:backupPort " + backupPort);

			System.out.println("Just send out: JOIN: " + backupPort + " to port: " + primaryPort);

			// get acknowledgement from primary server
			String message = reader.readLine();
			// close the socket
			socket.close();

			if (message.equals("ACK")) {
				System.out.println("Got acknowledgement: COMPLETE_JOIN");
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void setUpTCPChannel(int backUpPort, int primaryPort) throws IOException {

		// 1. create TCP server socket with backUpPort port
		ServerSocket serverSocket = null;

		serverSocket = new ServerSocket(backUpPort);
		System.out.println("Backup Server is listening on port...... " + backUpPort);
		while (true) {
			Socket clientSocket = null;
			try {
				// WAIT FOR REQUESTS
				clientSocket = serverSocket.accept(); //
				System.out.println("\nGot a new request");

				OutputStream output = clientSocket.getOutputStream();
				PrintWriter writer = new PrintWriter(output, true);
				InputStream inStream = clientSocket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));

				// got message from request
				String message = reader.readLine();
				System.out.println("The request is: " + message);

				// handle messages
				if (message.split(" ").length == 1) { // command is read/write/update

					String command = message.split(":")[0];
					if (command.equals("READ")) {
						writer.println("COMPLETE_READ:" + backUpData);
						System.out.println("READ operation done!");

					} else if (command.equals("WRITE")) { // need to send update to primary server
						int newValue = Integer.parseInt(message.split(":")[1]);
						backUpData = newValue;
						// send new value to primary server
						oneTimeCommunicate(primaryPort, "UPDATE:" + backUpData);

						writer.println("COMPLETE_WRITE");
						System.out.println("WRITE operation done!, new value is: " + backUpData);
					} else if (command.equals("UPDATE")) {// only need to update own data
						int newValue = Integer.parseInt(message.split(":")[1]);
						backUpData = newValue;
						writer.println("COMPLET_UPDATE");
						System.out.println("command" + " operation done!, new value is: " + backUpData);
					}

				}
			} catch (IOException e) {
				System.out.println("Accept failed" + primaryPort);
				System.exit(-1);
			}
		}

	}

	public static void oneTimeCommunicate(int port, String message) {
		Socket socket = null;
		try {
			socket = new Socket("localhost", port);

			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);

			// send message
			writer.println(message);

			System.out.println("Just send out: " + message + " to port: " + port);

			// get response
			String line = reader.readLine();

			System.out.println("Got response: " + line + "\n");

			// close the socket
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}