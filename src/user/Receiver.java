package user;

import gui.MainPanel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import org.jasypt.util.text.BasicTextEncryptor;

public class Receiver implements Runnable{

	private static MulticastSocket ms = null;
	private BasicTextEncryptor encryptor = new BasicTextEncryptor();
		
	
	public Receiver(BasicTextEncryptor be){
		encryptor = be;
	}
	
	public void run(){
	  	try {
	  		byte[] inBuffer = new byte[1024];
	  		ms = new MulticastSocket(Utente.portMulti);
			ms.joinGroup(Utente.group);
			DatagramPacket dp = new DatagramPacket(inBuffer, inBuffer.length);
			ms.receive(dp);
			String txt = encryptor.decrypt(new String(dp.getData(),"US-ASCII"));
			System.out.println(txt);
			//sono il master, quindi posso ricevere o un messaggio di abbandono o un tentativo
			if(Utente.master_flag){
				if(txt.compareTo("leave")==0){
					ms.leaveGroup(Utente.group);
					Master.decreasePlayers();
				}
				else{
					ms.leaveGroup(Utente.group);
					Master.checkLetter(txt);
				}
				
			}
			//sono un giocatore
			else{
				if(txt.compareTo("win")==0){
					ms.leaveGroup(Utente.group);
					Player.vittoria();
				}
				else if(txt.compareTo("lose")==0){
					ms.leaveGroup(Utente.group);
					Player.sconfitta();
				}
				else if(txt.length()==1){
					//ignora, non è un mess per lui
				}
				else if(txt.compareTo("less")==0){
					ms.leaveGroup(Utente.group);
					Player.decreaseTentativi();
				}
				else if(txt.compareTo("closing")==0){
					ms.leaveGroup(Utente.group);
					Player.partitaChiusa();
				}
				//messaggio di update + indice + lettera che estraggo con substring
				else if(txt.length()==8){
					ms.leaveGroup(Utente.group);	
					String tmp_i = txt.substring(6,7);
					int i = Integer.parseInt(tmp_i);
					String l = txt.substring(7,8);
					MainPanel.UpdateWordPlayer(i, l);
				}
				else if(txt.length()==9){
					ms.leaveGroup(Utente.group);	
					String tmp_i = txt.substring(6,8);
					int i = Integer.parseInt(tmp_i);
					String l = txt.substring(8,9);
					MainPanel.UpdateWordPlayer(i, l);
				}
				
			}
			ms.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	  	finally{
			if(ms!=null){
				try{
					ms.leaveGroup(Utente.group);
					ms.close();
				}
				catch(IOException ex){}
			}
	  	}
	}
	
}
