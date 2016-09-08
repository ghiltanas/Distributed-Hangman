package registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

//import org.jasypt.util.text.BasicTextEncryptor;

import user.UserInterface;

public class Partita implements Serializable{

		/**
	 * 
	 */
		private static final long serialVersionUID = 1L;
		private final int kPlayers;
		private final String master;
		private final String name; 
		private int playerAttuali;
		private String word;
		public UserInterface masterUI;
		public ArrayList<UserInterface> partecipanti;
		
		
		/*
		 * il costruttore istanzia un oggetto Partita settando casualmente i players in un range compreso tra 2 e 6
		 */
		public Partita(String passphrase, String parola, UserInterface m){
			this.kPlayers = new Random().nextInt(4) + 2;
			this.master = passphrase;
			this.name = master;
			playerAttuali = 0;
			this.word = parola;
			masterUI = m;
			partecipanti = new ArrayList<UserInterface>();
		}
		
		public String getMaster(){
			return this.master;
		}
		
		public void AddPlayer(){
			playerAttuali++;
			
		}
		
		public void RemovePlayer(){
			playerAttuali--;
			
		}
		
		public String wordLength(){
			String ris = "" + word.length();
			return ris;
		}
		
		public String postiDisponibili(){
			int p = kPlayers - playerAttuali;
			String ris = ""+p+"/"+kPlayers;
			return ris;
		}
		
		public boolean isFull(){
			if(playerAttuali==kPlayers)
				return true;
			else return false;
		}
		
		public String getNameMatch(){
			return this.name;
		}
		
		public int numPlayersmax(){
			int tmp = kPlayers;
			return tmp;
		}
		
				
}
