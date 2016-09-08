package user;


import java.io.IOException;


public class EcoUser implements Runnable{

	private volatile boolean go; 
		
	public EcoUser(){
		go = true;
	}
	
	public void termina(){
		go = false;
	}
	
	public void run(){
		while(go){
			try {
				String tmp = Utente.reader.readLine();
				if(tmp!=null){
				 if(tmp.compareTo("eco")==0){	
					Utente.writer.write("ack");
					Utente.writer.newLine();
					Utente.writer.flush();
					
				 }
				 else if(tmp.compareTo("start")==0){
					go = false;
					Utente.startMatch();
				 }
				 else if(tmp.compareTo("full")==0){
						go = false;
						Utente.ChiudiMatch();
					 }
				}
				else{
					System.out.println("soc lato server chiusa");
					go = false;
					Utente.ChiudiMatch();
				}
			} catch (IOException e) {
				if(go){
					go = false;
					Utente.ChiudiMatch();
				}
			} 
			
		}		
		
	}
	
}
