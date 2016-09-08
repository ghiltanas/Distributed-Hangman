package registry;

import java.rmi.Remote;
import java.rmi.RemoteException;

import user.UserInterface;

public interface RegistryInterface extends Remote {

	public boolean registrazione (String nickname, String password) throws RemoteException;
	public boolean login (String nickname, String password, UserInterface ui) throws RemoteException;
	public boolean logout (String nickname, UserInterface ui) throws RemoteException;
	
	public boolean deleteUser(String name) throws RemoteException;
		
}
