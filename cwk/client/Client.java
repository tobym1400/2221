import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.stream.Stream;

public class Client
{
	public void connect(String command) {
		//try to create a socket
		try {
			Socket s = new Socket("localhost", 9001);
			PrintWriter socketOut = new PrintWriter(s.getOutputStream(), true);
			BufferedReader socketIn = new BufferedReader(new InputStreamReader(s.getInputStream()));

			//Write command to server and receive output
			socketOut.println(command);
			String output = "";// = socketIn.readLine();
			String read;
			while ((read=socketIn.readLine()) != null && !read.equals("END")) {
				output = output.concat(read + "\n");
			}
			System.out.println(output);

			//Close connection
			socketOut.close();
			socketIn.close();
			s.close();
		}
		catch (UnknownHostException e) {
			System.out.println("Unknown host");
			System.exit(1);
		}
		catch (IOException e) {
			System.out.println("Unable to get an I/O with server");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		//Check arguments are provided
		if (args.length == 0) {
			System.out.println("Program requires command line argument");
			System.exit(1);
		}

		//Create client and run connect()
		Client client = new Client();

		String outputToServer;
		//Check commands are valid
		switch (args[0]) {
			case "totals":
				outputToServer = "totals";
				client.connect(outputToServer);
				System.exit(0);
			case "list":
				if (args.length != 2) {
					System.out.println("Program requires 3 command line argument for the command");
					System.exit(1);
				}
				outputToServer = "list " + args[1];
				client.connect(outputToServer);
				System.exit(0);
			case "join":
				if (args.length != 3) {
					System.out.println("Program requires 3 command line argument for the command");
					System.exit(1);
				}
				outputToServer = "join " + args[1] + " " + args[2];
				client.connect(outputToServer);
				System.exit(0);
			default:
				System.out.println("Invalid command");
		}
	}
}