package registry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;
import java.net.SocketException;

import java.rmi.ConnectException;

public class MiniServerTCP implements Runnable{
	
	private Socket soc;
	private boolean loop = true;
	private boolean full = false;
	private boolean started = false;
	private final long timeLimitMillis = 900000;
	private long startTimeMillis;
	private String group;
	private BufferedReader reader;
	private BufferedWriter writer;
	private boolean masterFlag = false;
	
	public MiniServerTCP(Socket s, String g){
		soc = s;
		this.group = g;
		startTimeMillis = System.currentTimeMillis();
	}
	
	//funziona per il controllo del timer
	public synchronized boolean checkTime(){
		boolean esito = (System.currentTimeMillis() - startTimeMillis < timeLimitMillis);
		return esito;
	}
	
	public void run(){
		//prendo il tempo di sistema
		startTimeMillis = System.currentTimeMillis();
		int i = 0;
		String name = null;
		String nome_partita = null;
		//start
		try {
				reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
				writer = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
				String service = reader.readLine();
				System.out.println(service);
				switch(service){
				case "open":{
					name = reader.readLine();
					nome_partita = name;
					System.out.println(name);
					String temp_master = name;
					String parola = reader.readLine();
					System.out.println(temp_master);
					System.out.println(parola);
					//controllo che la partita non sia già presente
					if(Server.openMatch(temp_master, parola)){
							System.out.println("concludo");
							writer.write("partitacreata");
							writer.newLine();
							writer.flush();
							masterFlag = true;
							//assegno un gruppo univoco ad ogni partita
							i = Server.matchIndex(nome_partita);
							group = group + "" + i;
							System.out.println("gruppo: " + group);
							writer.write(group);
							writer.newLine();
							writer.flush();
							writer.write("6000");
							writer.newLine();
							writer.flush();
							writer.write(""+Server.partite.get(Server.matchIndexArray(nome_partita)).numPlayersmax());
							writer.newLine();
							writer.flush();
							System.out.println(Server.partite.get(Server.matchIndexArray(nome_partita)).numPlayersmax());
					}
					else{
						writer.write("impossibilecreare");
						writer.newLine();
						writer.flush();
						full = true;
					}
					break;
				}
				case "join":{
					name = reader.readLine();
					System.out.println(name);
					nome_partita = reader.readLine();
					if(Server.joinMatch(nome_partita, name)){
							writer.write("ingame");
							writer.newLine();
							writer.flush();
							i = Server.matchIndex(nome_partita);
							group = group + "" + i;
							System.out.println("gruppo: " + group);
							writer.write(group);
							writer.newLine();
							writer.flush();
							writer.write("6000");
							writer.newLine();
							writer.flush();
							writer.write(""+Server.partite.get(Server.matchIndexArray(nome_partita)).numPlayersmax());
							writer.newLine();
							writer.flush();
							writer.write(Server.partite.get(Server.matchIndexArray(nome_partita)).wordLength());
							writer.newLine();
							writer.flush();
							//devo controllare se ho raggiunto il numero di giocatori, se si informo dell'inizio del match e chiudo la socket
						if(Server.partite.get(Server.matchIndexArray(nome_partita)).isFull()){
							System.out.println("raggiunto il numero di giocatori");
							started = true;
							writer.write("start");
							writer.newLine();
							writer.flush();
							
						}
					}
					else{
						writer.write("impossibilepartecipare");
						writer.newLine();
						writer.flush();
						full = true;
					}
					break;
				}
				
				}
				
		
		Thread.sleep(2000);	
		//ciclo in attesa dell'inizio della partita o dello scadere del timer o dell'eventuale caduta di connessione
		boolean active = true;
		boolean found = false;
		String ris = null;
		while(!started&&loop&&active){
			
			found = Server.matchisPresent(nome_partita);
			ris = null;
			try {
				if(found){
					
					if(started = Server.isFull(nome_partita)){
						System.out.println("raggiunto il numero di giocatori");
						writer.write("start");
						writer.newLine();
						writer.flush();
						Server.callbackClient();
					}	
					else{
						writer.write("eco");
						writer.newLine();
						writer.flush();
						ris = reader.readLine();
						if(ris==null){
							active = false;
							System.out.println("n)soc lato client chiusa");
						}
						else loop = checkTime();
					}
				}
				else{
					active = false;
					System.out.println("Partita non trovata");
				}
			}  catch (SocketException e){
				active = false;
				System.out.println("e)soc lato client chiusa");
			} 
			Thread.sleep(3000);
		}
		try{
			//non è nè iniziata nè piena, allora c'è un problema
		if(!started&&!full){
			//i = 0;
			found = false;
			Thread.sleep(500);
			//ragiono in termini di master, in quanto sarà sicuramente il primo a cui scadrà il timer
			if(!loop){
					Server.removeMatch(name);
					soc.close();
			}
			else if(!active){
					System.out.println("uscito " + name);
					found = Server.matchisPresent(nome_partita);
					//il master non è più attivo, attendo che tutti i partecipanti escano e poi elimino la partita
					if(found){
						if(masterFlag){
							Server.removeMatch(nome_partita);
							soc.close();
							
						}
						//non è più attivo un player o la partita è stata cancellata, lo controllo con found
						else{
							soc.close();
						}
					}
			}
		}
		//la partita è già iniziata ed è piena
		else if(full){
			System.out.println("è pieno");
			writer.write("full");
			writer.newLine();
			writer.flush();
			try {
				soc.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
		
		
		} catch (ConnectException e){
			soc.close();
		}
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e1) {
			
			e1.printStackTrace();
		} finally{
			try {
				soc.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
			
	}
	
}		

