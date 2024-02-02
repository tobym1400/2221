import java.net.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class Server {
	public static int listNumber;
	public static int maxMembers;
	public static ArrayList<String[]>lists = null;
	static ExecutorService executor = null;
	ServerSocket serverSocket = null;

	//Create server socket, list, and executor service
	public Server(String[] args) throws IOException {
		//Try to create a server socket
		try {
			serverSocket = new ServerSocket(9001);
		} catch (IOException e) {
			System.err.println("Port 9001 not available");
			System.exit(1);
		}
		//Number of lists
		listNumber = Integer.parseInt(args[0]);
		//Max number of members per list
		maxMembers = Integer.parseInt(args[1]);

		lists = new ArrayList<>();

		for (int i = 0; i < listNumber; i++) {
			String[] list = new String[maxMembers];
			lists.add(list);
		}

		executor = Executors.newFixedThreadPool(25);

		while (true) {
			Socket client = serverSocket.accept();
			executor.submit( new ClientHandler(client) );
		}
	}

	//Check args is right length and create new server
	public static void main( String[] args ) {
		if (args.length < 2) {
			System.out.println("Server requires 2 command line arguments");
		}

		try {
			Server server = new Server(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

//Client handler class to handle client inputs
class ClientHandler extends Thread {
	private static Socket socket;

	//Create client handler
	public ClientHandler(Socket clientSocket) {
		super("ClientHandler");
		this.socket = clientSocket;
	}

	//Handle client commands
	public void run() {
		//Try and read/write to client
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			//Read client input
			String input = in.readLine();
			String[] separateInput = input.split(" ");

			try {
				FileWriter myWriter = new FileWriter("log.txt",true);
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy|HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();
				myWriter.write(dtf.format(now)+"|"+ socket.getInetAddress()+"|"+separateInput[0]+"\n");
				myWriter.close();
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}

			//Commands
			switch (separateInput[0]) {
				case "join":
					int listToJoin = Integer.parseInt(separateInput[1]);
					String name = separateInput[2];
					out.println(join(listToJoin, name));
				case "list":
					int listToRead = Integer.parseInt(separateInput[1]);
					out.println(list(listToRead));
				case "totals":
					out.println(totals());
			}
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}

	//Print list totals
	public static String totals() {
		String totalOutput;
		int numberOfMembers;
		totalOutput = "There are " + Server.listNumber + " list(s), each with a maximum size of " + Server.maxMembers + ".\n";
		for (int i = 0; i < Server.listNumber; i++) {
			numberOfMembers = 0;
			for (int j=0; j<Server.lists.get(i).length; j++) {
				if (Server.lists.get(i)[j] != null)
					numberOfMembers++;
			}
			totalOutput = totalOutput.concat("List " + (i + 1) + " has " + numberOfMembers + " member(s).\n");
		}
		totalOutput = totalOutput.concat("END");
		return totalOutput;
	}

	//Print details of list
	public static String list(int listToRead) {
		if (listToRead > 0 && listToRead <= Server.listNumber) {
			if (Server.lists.get(listToRead-1)[0] != null) {
				String listOutput = Server.lists.get(listToRead-1)[0] + "\n";
				for (int i=1; i<Server.maxMembers; i++) {
					listOutput = listOutput.concat(Server.lists.get(listToRead-1)[i] + "\n");
				}
				listOutput = listOutput.concat("END");
				return listOutput;
			}
			else {
				return ("Empty List\nEND");
			}
		}
		else {
			return("Invalid list number\nEND");
		}
	}

	//Join server list
	public static String join(int listToJoin, String name) {
		if (listToJoin > 0 && listToJoin <= Server.listNumber) {
			boolean joined = false;
			for (int i = 0; i < Server.lists.get(listToJoin-1).length; i++) {
				if (Server.lists.get(listToJoin-1)[i] == null && !joined) {
					Server.lists.get(listToJoin-1)[i] = name;
					joined = true;
				}
			}
			if (joined)
				return ("Success.\nEND");
			else
				return ("Failed.\nEND");
		}
		else {
			return("Invalid list number\nEND");
		}
	}
}

