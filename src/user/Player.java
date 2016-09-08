package user;

import gui.MainPanel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.jasypt.util.text.BasicTextEncryptor;

public class Player implements Runnable {

	
	private static BasicTextEncryptor encryptor = new BasicTextEncryptor();
	private static volatile boolean go = true;
	private String key;
	private Thread GestioneRec;
	private Receiver Rec;
	private static int tentativi = 9;
	private Thread GestioneTimer;
	private Timer checktime;
	private long startTimeMillis;
	
	public Player(String k){
		key = k;
		
	}
	
	public void setEncryptor(String p){
		encryptor.setPassword(p);
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
			Thread.sleep(1000);
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
	
	//devono trascorrere 1/2 secondo per tentare di nuovo
	public static void tentativo(String l){
		sendGlobalMsg(l);
		attendi();
	}
	
	public static void attendi(){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void abbandona(){
		sendGlobalMsg("leave");
		go = false;
	}
	
	public static void vittoria(){
		go = false;
		MainPanel.playerWins();
	}
	
	public static void decreaseTentativi(){
		tentativi--;
		MainPanel.UpdateTentativi(tentativi);
	}
	
	
	public static void partitaChiusa(){
		go = false;
		MainPanel.matchClosed();
	}
	
	
	public static void sconfitta(){
		go = false;
		MainPanel.playerLose();
	}
	
	public void run(){
		setEncryptor(key);
		startTimeMillis = System.currentTimeMillis();
		checktime = new Timer(startTimeMillis,1);
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
	
}
