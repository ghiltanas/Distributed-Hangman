package user;

import gui.MainPanel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jasypt.util.text.BasicTextEncryptor;


public class Master implements Runnable{

	private static BasicTextEncryptor encryptor = new BasicTextEncryptor();
	private static volatile boolean go = true;
	private String key;
	private static int players;
	private static int tentativi = 9;
	private static String word;
	private static int count;//mi serve per controllare se ho indovinato tutte le lettere
	private Thread GestioneRec;
	private Thread GestioneTimer;
	private Receiver Rec;
	private Timer checktime;
	private final long timeLimitMillis = 600000; //10 minuti, timer per la partita
	private long startTimeMillis;
	private static ArrayList<Character> cache;
	private static ArrayList<Character> word_chars;
	
	public Master(String k, int mP, String w, String name){
		key = k;
		encryptor = new BasicTextEncryptor();
		word = w;
		count = 0;
		players = mP;
		cache = new ArrayList<Character>();
		word_chars = new ArrayList<Character>();
		
	}
	
	public void setEncryptor(String p){
		encryptor.setPassword(p);
	}
	
	public void setCache(){
		for(int i = 0; i<word.length(); i++){
			cache.add(Character.valueOf("-".charAt(0)));
			word_chars.add(Character.valueOf(word.charAt(i)));
		}
	}
	
	//funzione per invio messaggi criptati in multicast
	public static void sendGlobalMsg(String msg){
		try {
			byte[] OutBuffer = new byte[1024];
			String cryptedMsg = encryptor.encrypt(msg);
			OutBuffer = cryptedMsg.getBytes("US-ASCII");
			DatagramPacket sent = new DatagramPacket(OutBuffer, OutBuffer.length,Utente.group, Utente.portMulti);
			DatagramSocket exit = new DatagramSocket();
			exit.send(sent);
			Thread.sleep(500);
			exit.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void decreasePlayers(){
		players--;
		//se non ci sono più giocatori chiudo il match
		if(players==0){
			go = false;
			MainPanel.masterWins();
		}
	}
	
	//funzione di controllo del timer-partita
	public synchronized boolean checkTime(){
		boolean esito = (System.currentTimeMillis() - startTimeMillis < timeLimitMillis);
		return esito;
	}
	
	//
	public static void checkLetter(String l){
		boolean esito = false;
		for (int i = 0;i<word.length();i++){
			if(cache.get(i).compareTo("-".charAt(0))==0){
				if(word_chars.get(i).compareTo(l.charAt(0))==0){
					cache.set(i, word_chars.get(i));
					MainPanel.UpdateWordMaster(i,l);
					esito = true;
					sendGlobalMsg("update"+i+l);
					count++;
					sendGlobalMsg("update"+i+l);
				}
			}
			else if(cache.get(i).compareTo(l.charAt(0))==0){
				esito = true;
			}
			if(count==word.length()){
				sendGlobalMsg("win");
				MainPanel.masterLose();
				sendGlobalMsg("win");
				go = false;
			}
		}
		if(!esito){
			tentativi--;
			if(tentativi==0){
				sendGlobalMsg("lose");
				MainPanel.masterWins();
				sendGlobalMsg("lose");
				go = false;
			}
			else{
				sendGlobalMsg("less");
			}
		}
		
	}
	
	public static synchronized void terminaMatch(){
		sendGlobalMsg("closing");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sendGlobalMsg("closing");
		go = false;
		MainPanel.matchClosed();
	}
	
	
	public void run(){
		startTimeMillis = System.currentTimeMillis();
		setEncryptor(key);
		setCache();
		checktime = new Timer(startTimeMillis,0);
		GestioneTimer = new Thread(checktime);
		GestioneTimer.start();
		while(go){
			Rec = new Receiver(encryptor);
			GestioneRec = new Thread(Rec);
			GestioneRec.start();
			try {
				GestioneRec.join();
			} catch (InterruptedException e) {
					e.printStackTrace();
			}
		}	
	}
	/*
	 * public void run(){
		startTimeMillis = System.currentTimeMillis();
		setEncryptor(key);
		setCache();
		while(go){
			Rec = new Receiver(encryptor);
			GestioneRec = new Thread(Rec);
			GestioneRec.start();
			try {
				GestioneRec.join();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			//devo controllare non sia stato modificato il flag
			if(go){
				//a questo punto controllo non sia esaurito il timer
				go = checkTime();
				if(!go){
					sendGlobalMsg("lose");
					MainPanel.masterWins();
				}
			}
			
		}
	}
	 */
	
}
