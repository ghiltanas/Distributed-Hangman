package registry;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;




import org.json.simple.JSONObject;

import user.UserInterface;

public class Server extends RemoteServer implements RegistryInterface, DBinterface, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Connection conn = null;
	private static String dB_Type;
	private static String dB_Path;
	public static String[] index;
	public static ArrayList<Partita> partite; //lista partite aperte
	public static ArrayList<UserInterface> guest_user; //lista utenti guest loggati, SOLO guest
	
	//Costruttore: DataBase partecipanti, lista loggati, lista partite, chiamata di makeJobj() 
	public Server(String dbType, String dbPath){
		dB_Path = dbPath;
		dB_Type = dbType;
		partite = new ArrayList<Partita>();
		guest_user = new ArrayList<UserInterface>();
		index = new String[10];
		for(int i = 0;i<index.length;i++){
			index[i]="empty";
		}
		makeJobj();
	}
	
	//funzione per costruzione del file JSON
	@SuppressWarnings("unchecked")
	public synchronized void makeJobj(){
		JSONObject obj = new JSONObject();
		obj.put("stubName", "HANGMAN-SERVER");
		obj.put("host", "localhost");
		obj.put("port", 1800);
		obj.put("num-max-match", "10");
		obj.put("start group", "228.5.6.0");
		obj.put("end group", "228.5.6.9");
		try {
			FileWriter file = new FileWriter("Risorse\\server.json");
			file.write(obj.toJSONString());
			file.flush();
			file.close();
	 	} catch (IOException e) {
			e.printStackTrace();
		}
	 
	}
	
	
	//Metodi per la gestione del database
	
	//connessione al DB
	public synchronized static Connection connessioneDB(String tipo, String path) {
		try {
			Class.forName(tipo);
			conn = DriverManager.getConnection(path);
			if(conn!=null)System.out.println("connection ok");
			conn.setAutoCommit(true);
			return conn;
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("connection db problems!");
			e.printStackTrace();
			return null;
		}
	}

	//controlla la presenza di un utente nel DB
	public synchronized boolean checkuser(String user) {
		boolean esito = false;
		connessioneDB(dB_Type, dB_Path);
		String select_query = "SELECT * FROM utenti WHERE nickname = ?";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(select_query);
			pstmt.setString(1, user);
			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			while(rs.next()){
			    count++;
			}
			if(count==1) esito = true;
			rs.close();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Select query problems!");
			e.printStackTrace();
		}
		
		return esito;
	}
	
	//controlla i diritti di utente esistente
	public synchronized boolean checkUserRight(String user, String passwd) {
		boolean esito = false;
		connessioneDB(dB_Type, dB_Path);
		String select_query = "SELECT * FROM utenti WHERE nickname=? AND password=?";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(select_query);
			pstmt.setString(1, user);
			pstmt.setString(2, passwd);
			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			while(rs.next()){
			    count++;
			}
			if(count==1) esito = true;
			rs.close();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Select query problems!");
			e.printStackTrace();
		}
		return esito;
	}

	//inserimento di un nuovo utente nel DB
	public synchronized boolean insertUser(String user, String passwd) {
		boolean insert = false;
		connessioneDB(dB_Type, dB_Path);
		String insertQuery = "INSERT INTO utenti VALUES (?,?,?)";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(insertQuery);
			pstmt.setString(1, user);
			pstmt.setString(2, passwd);
			pstmt.setString(3, "guest");
			int entriesModificate = pstmt.executeUpdate();
			if(entriesModificate<=0){
			    System.out.println("errore nell'inserimento user " + user + " nel database");
			}
			else{
				insert = true;
				System.out.println("Utente "+user+" inserito");
			}
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return insert;
	}

	//cancellazione utente dal DB
	public synchronized boolean deleteUser(String name) throws RemoteException{
		boolean delete = false;
		connessioneDB(dB_Type, dB_Path);
		String deleteQuery = "DELETE FROM utenti WHERE nickname = ?";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(deleteQuery);
			pstmt.setString(1, name);
			int entriesModificate = pstmt.executeUpdate();
			if(entriesModificate!=1){
			    System.out.println("errore nell'eliminazione dell' utente " + name + " nel database");
			}
			else{
				delete = true;
				System.out.println("Utente "+name+" cancellato");
				
			}
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return delete;
	}
	
	public synchronized void svuotaDB() {
		connessioneDB(dB_Type, dB_Path);
		String deleteQuery = "DELETE FROM utenti";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(deleteQuery);
			pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	//seleziona tutti gli utenti GUEST nel DB
	public synchronized boolean selectGuests() {
		boolean esito = false;
		connessioneDB(dB_Type, dB_Path);
		String select_query = "SELECT * FROM utenti WHERE usertype=?";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(select_query);
			pstmt.setString(3, "guest");
			ResultSet rs = pstmt.executeQuery();
			int count = 0;
			while(rs.next()){
			    count++;
			}
			if(count>1) esito = true;
			rs.close();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Select query problems!");
			e.printStackTrace();
		}
		return esito;
		
	}

	//Funzioni per la modifica del tipo di utente nel Database
	public synchronized static boolean FromUserToGuest(String user){
		boolean esito = false;
		connessioneDB(dB_Type, dB_Path);
		String update_query = "UPDATE utenti SET usertype = ? WHERE nickname = ?";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(update_query);
			pstmt.setString(1, "guest");
			pstmt.setString(2, user);
			int count = pstmt.executeUpdate();
			if(count==1)
				esito = true;
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Select query problems!");
			e.printStackTrace();
		}
		
		return esito;
	}
	
	public synchronized static boolean FromGuestToPlayer(String user){
		boolean esito = false;
		connessioneDB(dB_Type, dB_Path);
		String update_query = "UPDATE utenti SET usertype = ? WHERE nickname = ?";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(update_query);
			pstmt.setString(1, "player");
			pstmt.setString(2, user);
			int count = pstmt.executeUpdate();
			if(count==1)
				esito = true;
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Select query problems!");
			e.printStackTrace();
		}
		
		return esito;
	}
	
	public synchronized static boolean FromGuestToMaster(String user){
		boolean esito = false;
		connessioneDB(dB_Type, dB_Path);
		String update_query = "UPDATE utenti SET usertype = ? WHERE nickname = ?";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(update_query);
			pstmt.setString(1, "master");
			pstmt.setString(2, user);
			int count = pstmt.executeUpdate();
			if(count==1)
				esito = true;
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Select query problems!");
			e.printStackTrace();
		}
		
		return esito;
	}
	
	public synchronized boolean registrazione(String nickname, String password) throws RemoteException {
		boolean esito = false;
		//controllo che l'utente non sia già presente
		if(!(checkuser(nickname))){
			//inserisco nel DB e aggiungo l'interfaccia user alla lista
			if((insertUser(nickname, password))){
				System.out.println("Utente "+nickname+" registrato.");
				esito = true;
			}
			
		}
		else
			System.out.println("errore nella registrazione utente.");
		return esito;
	}
	
	//metodo per notificare ai guest le partite in corso
	public synchronized static void callbackClient() throws RemoteException {
		System.out.println("Comunicazione lista partite aperte ai guest.");
		if(guest_user!=null){
			for(int i = 0; i<guest_user.size();i++){
				UserInterface u = guest_user.get(i);
				u.notifyMatch(partite);
			}
		}
		System.out.println("Notifica completata.");
	}
		
	public synchronized boolean login(String nickname, String password, UserInterface ui) throws RemoteException {
		boolean esito = false;
		//controllo correttezza utente e password
		if(checkUserRight(nickname, password)){
			guest_user.add(ui);
			callbackClient();
			System.out.println("Utente "+ui.getNick()+" loggato.");
			esito = true;
		}
		else System.out.println("wrong username or password.");
		return esito;
	}
	
	//oltre ad effettuare il logout, controlla non vi sia una partita nickname aperta, in tal caso la elimina
	public synchronized boolean logout(String nickname, UserInterface ui) throws RemoteException {
		boolean esito = false;
		if(checkuser(nickname)){
			removeMatch(nickname);
			guest_user.remove(ui);
			FromUserToGuest(nickname);
			System.out.println("Sessione terminata");
			esito = true;
			callbackClient();
		}		
		else System.out.println("logout error.");
		return esito;
	}

	public synchronized static boolean openMatch(String name, String parola){
		boolean esito = false;
		int i = 0;
		System.out.println("sono nell'open match");
		if(!matchisPresent(name)&&partite.size()<10){
			//non è presente, la creo allora, come nome prende il nick del master
			//devo aggiornare il db e togliere l'utente dai guest settandolo come master
			boolean go = FromGuestToMaster(name);
			System.out.println("utente aggiornato");
			if(go){
				while(guest_user.size()>0&&go){
					try {
						if(guest_user.get(i).getNick().compareTo(name)==0){
							UserInterface master = guest_user.get(i);
							Partita p = new Partita(name, parola, master);
							partite.add(p);
							boolean found = false;
							int j = 0;
							while(!found&&j<index.length){
								if(index[j].compareTo("empty")==0){
									found = true;
									index[j] = name;
								}
								else j++;
							}
							System.out.println("partita non presente, la creo");
							System.out.println("rimosso guest "+name);
							guest_user.remove(i);
							callbackClient();
							go = false;
							esito = true;
						}
						else i++;
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return esito;
	}	
	
	public synchronized static int matchIndexArray(String name){
		int i = -1;
		if(matchisPresent(name)){
			i = 0;
			boolean found = false;
			while(!found){
				
				if(partite.get(i).getNameMatch().compareTo(name)==0){
					found = true;
				}
				else i++;
				
			}
		}
		return i;	
	}
		
	public synchronized static int matchIndex(String name){
		int i = -1;
		if(matchisPresent(name)){
			i = 0;
			boolean found = false;
			while(!found){
				if(index[i].compareTo(name)==0){
					found = true;
				}
				else i++;
			}
		}
		return i;	
	}
	
	public synchronized static boolean isFull(String name){
		boolean full = false;
		int i = 0;
		boolean found = false;
		while(!found && i<partite.size()){
			if(partite.get(i).getNameMatch().compareTo(name)==0){
				full = partite.get(i).isFull();
				found = true;
			}
			else i++;
		}
		return full;
	}
	
	public synchronized static boolean joinMatch(String name, String player){
		boolean esito = false;
		if(matchisPresent(name)&&!isFull(name)){
			System.out.println("inside join");
			//aggiorno il profilo utente nel DB
			int i = 0;
			boolean found = false;
			while(!found){
				
				if(partite.get(i).getNameMatch().compareTo(name)==0){
					found = true;
				}
				else i++;
				
			}	
			FromGuestToPlayer(player);
			boolean go = true;
			int j = 0;
			//rimuovo il giocare dai guest e lo aggiungo alla lista dei partecipanti
			while(j<guest_user.size()&&go){
				try {
					if(guest_user.get(j).getNick().compareTo(player)==0){
						partite.get(i).partecipanti.add(guest_user.get(j));
						System.out.println("rimosso guest "+player);
						guest_user.remove(j);
						partite.get(i).AddPlayer();
						callbackClient();
						go = false;
						esito = true;
					}
					else j++;
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		return esito;
	}	
	
	public synchronized static void removeMatch(String m){
		if(matchisPresent(m)){
			boolean found = false;
			int i = 0;
			i = matchIndex(m);
			index[i] = "empty";
			i = 0;
			while(!found&&i<partite.size()){
				if(partite.get(i).getNameMatch().compareTo(m)==0){
					found = true;
					partite.remove(i);
					System.out.println("partita "+m+ " rimossa");
				}
				else i++;
			}
			
		}
	}
	
	public synchronized static boolean matchisPresent(String val){
		boolean esito = false;
		int i = 0;
		while(!esito && i<partite.size()){
			if(partite.get(i).getNameMatch().compareTo(val)==0){
				esito = true;
			}	
			else i++;
		}
		return esito;
	}
	
	public static void main(String[] args) {
		
		Server s = new Server("org.sqlite.JDBC","jdbc:sqlite:Risorse\\Hangman.db");
		try{
			long starttime;
			RegistryInterface stub = (RegistryInterface)UnicastRemoteObject.exportObject(s,0);
			LocateRegistry.createRegistry(1099);
			Registry r=LocateRegistry.getRegistry();
			r.rebind("HANGMAN-SERVER", stub);
			System.out.println("Server started.");
			//distinguere la gestione delle 10 partite mediante 10 porte
			int port = 1800;
			ThreadPoolExecutor executorPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
			System.out.println("Server satellite lanciati");
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("Server listening");
			String group = "228.5.6.";
			boolean loop = true;
			executorPool.setKeepAliveTime(50, TimeUnit.MINUTES);
			while(loop&&!executorPool.isTerminated()){
				starttime = System.currentTimeMillis();
				Socket socket = serverSocket.accept();
				//server_socket inattiva per un'ora, chiudo il server
				if(System.currentTimeMillis() - starttime > 3600000){
					loop = false;
					executorPool.shutdown();
				}
				else{
					executorPool.execute(new MiniServerTCP(socket, group));
				}
			}
			serverSocket.close();
			System.out.println("Server chiuso.");
			s.svuotaDB();
			System.exit(0);
		}
		
		catch (IOException e) {
			System.out.println("errore nel server: " + e.toString());
		}
	}
}