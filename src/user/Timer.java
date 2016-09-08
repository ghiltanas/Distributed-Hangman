package user;

import gui.MainPanel;

public class Timer implements Runnable{

	private final long timeLimitMillis = 600000; //10 minuti, timer per la partita
	private long startTimeMillis;
	private int flag; //0 master, 1 player
	
	
	public Timer(long tempo, int f){
		this.startTimeMillis = tempo;
		this.flag = f;
	}
	
	//funzione di controllo del timer-partita
	public boolean checkTime(){
			boolean esito = (System.currentTimeMillis() - startTimeMillis < timeLimitMillis);
			return esito;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(checkTime()){
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(flag==0){
			Master.sendGlobalMsg("lose");
			MainPanel.masterWins();
		}
		else if(flag==1){
			Player.sendGlobalMsg("leave");
			MainPanel.playerLose();
		}
	}

	
}
