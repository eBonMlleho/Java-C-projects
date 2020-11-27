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
import java.util.ArrayList;
import java.util.Scanner;

public class PrimaryServer {
	public static void main(String[] args) throws IOException {
		// 1. set up primary port value
		Scanner input = new Scanner(System.in);

		System.out.print("Enter primary port number:");
		int primaryPort = input.nextInt();

		// System.out.println("primaryPort port: " + primaryPort);

		// 2. set up data
		int data = 0;

		// 3. create TCP server socket with primary port
		ServerSocket serverSocket = null;

		serverSocket = new ServerSocket(primaryPort);
		System.out.println("Waiting for connetion ......");
		// 4. keep listening on TCP channel
		ArrayList<Integer> backupPorts = new ArrayList<Integer>(); // use arraylist to record backup server
		while (true) {
			Socket clientSocket = null;
			// Scanner inStream;

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
						writer.println("COMPLETE_READ:" + data);
						System.out.println("READ operation done!");

					} else if (command.equals("UPDATE") || command.equals("WRITE")) {
						int newValue = Integer.parseInt(message.split(":")[1]);
						data = newValue;
						// request each of the backup server to update its replica
						// of the data store and get acknowledgement
						writer.println("COMPLETE_" + command);

						for (int i = 0; i < backupPorts.size(); i++) {

							oneTimeCommunicate(backupPorts.get(i), "UPDATE:" + data);

						}

						System.out.println("command" + " operation done!, new value is: " + data);
					}
				} else if (message.split(" ").length == 2) { // JOIN:backupPort port value
					int newBackupPort = Integer.parseInt(message.split(" ")[1]);
					backupPorts.add(newBackupPort); // add new backup server port into arraylist
					writer.println("ACK");
					// todo

					System.out.println("JOIN:backupPort operation done! and port is: " + newBackupPort);
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
