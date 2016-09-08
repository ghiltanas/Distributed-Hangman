package registry;

import java.sql.*;

public interface DBinterface {
	
	//connessione al DB
	public static Connection connessioneDB(String tipo, String path) {
		return null;
	}
	
	//controlla diritti User
	public boolean checkUserRight(String user, String passwd);
	
	//controllo presenza User
	public boolean checkuser(String user);
	
	//inserimento utente
	public boolean insertUser(String user, String passwd);
	
	//filtra tutti i guesser
	public boolean selectGuests();
	
	public void svuotaDB();
	
}
