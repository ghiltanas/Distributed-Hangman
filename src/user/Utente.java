package user;


import gui.MainPanel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import registry.Partita;
import registry.RegistryInterface;

public class Utente implements UserInterface {

	/**
	 * 
	 */
	private static String name;
	public static Socket soc;
	public static UserInterface stub = null;
	public static RegistryInterface server_interface = null;
	public static ArrayList<Partita> open_match = null;
	private String IPgroup;
	public static int portMulti;
	public static InetAddress group = null;
	public static BufferedWriter writer;
	public static BufferedReader reader;
	public static boolean go = true;
	public static boolean master_flag = false;
	private static Thread Wait = null;
	private static Thread GestioneMatch = null;
	private static Master master = null;
	private static Player player = null;
	private static EcoUser Eco = null;
	private static String key;
	private static String maxPlayers;
	private static String word;
	private static int wordlength;
	
	public Utente(String nome) {
		super();
		name = nome;
		makeJobj();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void makeJobj(){
		JSONObject obj = new JSONObject();
		obj.put("server-stubName", "HANGMAN-SERVER");
		obj.put("server-host", "localhost");
		obj.put("server-port", 1800);
		try {
			FileWriter file = new FileWriter("Risorse\\user.json");
			file.write(obj.toJSONString());
			file.flush();
			file.close();
	 	} catch (IOException e) {
			e.printStackTrace();
		}
	 
	}
	
	public void setServerInterface(RegistryInterface si){
		server_interface = si;
	}
	
	public void setUI(UserInterface si){
		stub = si;
	}
	
	public void setName(String nome) throws RemoteException {
		name = nome;
	}

	public String getNick() throws RemoteException {
		return name;
	}
	
	public static String getWord(){
		return word;
	}
	
	public ArrayList<Partita> getMatchList(){
		return open_match;
	}
	
	public synchronized void notifyMatch(ArrayList<Partita> lista_partite) throws RemoteException {
		open_match = new ArrayList<Partita>();
		if(lista_partite!=null){
			if(lista_partite.size()==0){
				System.out.println("Ancora nessuna partita aperta.");
			}
			else{
				for(int i = 0;i<lista_partite.size();i++){
					System.out.println(lista_partite.get(i).getNameMatch());
					if(!lista_partite.get(i).isFull()){
						open_match.add(lista_partite.get(i));
					}
				}
			}
			MainPanel.UpdatePlayers();
		}
		else System.out.println("Ancora nessuna partita aperta.");
	}
		
	public static void close(){
		System.exit(0);
	}
	
	//metodo richiesta di apertura del match
	public synchronized void OpenMatch(String nome, String parola){
		try {
			soc = new Socket("localhost", 1800);
			System.out.println("connessione TCP stabilita");
		} catch (UnknownHostException e1) {
			System.out.println("OpenMatch error:");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("OpenMatch error:");
			e1.printStackTrace();
		}
		try{
			writer = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			String op = "open";
			writer.write(op);
			writer.newLine();
			writer.flush();
			System.out.println("Inviata operazione");
			writer.write(nome);
			writer.newLine();
			writer.flush();
			System.out.println("Inviato nome");
			writer.write(parola);
			writer.newLine();
			writer.flush();
			System.out.println("Inviata parola");
			String ris = reader.readLine();
			System.out.println(ris);
			if(ris.compareTo("partitacreata")==0){
				word = parola;
				IPgroup = reader.readLine();
				String tmp = reader.readLine();
				portMulti = Integer.parseInt(tmp);
				maxPlayers = reader.readLine();
				key = nome;
				System.out.println(key);
				try{
					group = InetAddress.getByName(IPgroup);
					master_flag = true;
					Eco = new EcoUser();
					Wait = new Thread(Eco);
					Wait.start();
				}
				catch(Exception e){
					System.out.println("Uso:wrong port");
					System.exit(1);
				}
			}
			else{
				soc.close();
				System.out.println("nessuno slot disponibile");
				MainPanel.matchClosed();
			}	
		}
		catch (IOException e) {
			System.out.println("problemi con il server");
			System.exit(1);
		} 
	}
	
	//metodo per unirsi ad un match
	public synchronized void JoinMatch(String player, String partita){
		try {
			soc = new Socket("localhost", 1800);
			System.out.println("connessione TCP stabilita");
		} catch (UnknownHostException e1) {
			System.out.println("JoinMatch error:");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("JoinMatch error:");
			e1.printStackTrace();
		}
		try{
			writer = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			String op = "join";
			writer.write(op);
			writer.newLine();
			writer.flush();
			System.out.println("Inviata operazione");
			writer.write(player);
			writer.newLine();
			writer.flush();
			System.out.println("Inviato nome");
			writer.write(partita);
			writer.newLine();
			writer.flush();
			System.out.println("Inviato nome partita");
			String ris = reader.readLine();
			System.out.println(ris);
			if(ris.compareTo("ingame")==0){
				IPgroup = reader.readLine();
				String tmp = reader.readLine();
				portMulti = Integer.parseInt(tmp);
				maxPlayers = reader.readLine();
				key = partita;
				System.out.println(key);
				String tmp2 = reader.readLine();
				wordlength = Integer.parseInt(tmp2);
				try{
					group = InetAddress.getByName(IPgroup);
					Eco = new EcoUser();
					Wait = new Thread(Eco);
					Wait.start();
				}
				catch(Exception e){
					System.out.println("Uso:wrong port");
					System.exit(1);
				}
			}
			else{
				soc.close();
				System.out.println("nessuno slot disponibile");
				MainPanel.matchClosed();
			}
		}
		catch (IOException e) {
			System.out.println("JoinMatch error:");
			e.printStackTrace();
		} 
		
	}
		
	
	//viene chiamato in caso scada il timeout o il master non sia più attivo, quindi devo chiudere la partita e informare la gui
	public synchronized static void ChiudiMatch(){
			try {
				master_flag = false;
				soc.close();
				Eco.termina();
				MainPanel.matchClosed();
				System.out.println("match chiuso");
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
	}
	
	//metodo per inizio match
	public synchronized static void startMatch(){
			try {
				if(master_flag){
					soc.close();
					Eco.termina();
					System.out.println("match started");
					int tmpMaxP = Integer.parseInt(maxPlayers);
					master = new Master(key,tmpMaxP,word,name);
					GestioneMatch = new Thread(master);
					GestioneMatch.start();
					Thread.sleep(500);
					MainPanel.matchStartedMaster();
						
				}
				else{
					soc.close();
					Eco.termina();
					System.out.println("match started");
					player = new Player(key);
					GestioneMatch = new Thread(player);
					GestioneMatch.start();
					Thread.sleep(1000);
					MainPanel.matchStartePlayer(wordlength);
					
				}
			}catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	//nel caso si verifichi abbandono non volontario del giocatore in attesa inizio partita
	public synchronized static void exitPlayer(){
			Eco.termina();
			System.out.println("giocatore uscito");
			MainPanel.matchClosed();
					
	}
	
	public static void main(String[] args) throws RemoteException {
		Utente user = null;
		UserInterface stub = null;
		RegistryInterface server_interface;
		Registry server_registry;
		BufferedReader inBuffer = new BufferedReader(new InputStreamReader(System.in));
		String user_name = "user";
		String user_password = "12345";
		boolean ok = false;
		try {
			server_registry = LocateRegistry.getRegistry("localhost");
			server_interface = (RegistryInterface)server_registry.lookup("HANGMAN-SERVER");
			while(!ok){
				System.out.println("REGISTRAZIONE UTENTE: \n");
				System.out.println("USERNAME: ");
				user_name = inBuffer.readLine();
				System.out.println("PASSWORD: ");
				user_password = inBuffer.readLine();
				user = new Utente(user_name);
				ok = server_interface.registrazione(user_name,user_password);
				if(!ok){
					System.out.println("utente già presente, scegliere un altro username.");
				}
				else{ 
					System.out.println("registrazione avvenuta con successo!");
					user.setName(user_name);
					stub = (UserInterface) UnicastRemoteObject.exportObject(user,0);
					user.setServerInterface(server_interface);
					user.setUI(stub);
				}	
			}
			Thread.sleep(2000);
			
		} catch (RemoteException | InterruptedException | NotBoundException e) {
			System.out.println("UI export problems");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MainPanel startPanel = new MainPanel(user);
		startPanel.setVisible(true);
				
	}
}

