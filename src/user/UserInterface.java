package user;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import registry.Partita;

public interface UserInterface extends Remote {

	public void notifyMatch(ArrayList<Partita> lista_partite) throws RemoteException;
	
	public String getNick() throws RemoteException;
	
	//utilizzati per la registrazione
	public void setName(String name) throws RemoteException;
				
	
	
}
