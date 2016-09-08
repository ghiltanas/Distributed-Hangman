package gui;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JLabel;

import user.Master;
import user.Player;
import user.UserInterface;
import user.Utente;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;



import javax.swing.JProgressBar;

public class MainPanel extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JTextField UserField_Log;
	private static JPasswordField passwdField_Log;
	private JPanel Match;
	private static JPanel Login;
	private static JPanel ListaPartite;
	private static JPanel MasterWait;
	private static JPanel PlayerWait;
	private static JPanel MasterPanel;
	private static JPanel PlayerPanel;
	private static JPanel Vittoria;
	private static JPanel Loser;
	private String user;
	private String password;
	private static Utente User;
	private static ArrayList<JLabel> VisibleMatch;
	private static ArrayList<JButton> BottoniPartite;
	private static ArrayList<JLabel> Trattini;
	private static JLabel tentativi;
	private static JLabel label_2;
	private static boolean logged = false;
	private static JTextField parola;
	private static JTextField tentativo;
	private JButton OpenMatch;
	private JButton JoinMatch;
	
	/**
	 * Create the application.
	 */
	public MainPanel(Utente u) {
		User = u;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
		
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		setBounds(100, 100, 640, 520);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new CardLayout(0,0));
		try {
			this.setTitle(User.getNick());
		} catch (RemoteException e3) {
			
			e3.printStackTrace();
		}
		Login = new JPanel();
		try {
			Login.setName(User.getNick());
		} catch (RemoteException e2) {
			
			e2.printStackTrace();
		}
		getContentPane().add(Login);
		Login.setLayout(null);
		Login.setVisible(true);
		
		UserField_Log = new JTextField();
		UserField_Log.setHorizontalAlignment(SwingConstants.CENTER);
		UserField_Log.setColumns(10);
		UserField_Log.setBounds(197, 73, 227, 20);
		Login.add(UserField_Log);
		
		passwdField_Log = new JPasswordField();
		passwdField_Log.setHorizontalAlignment(SwingConstants.CENTER);
		passwdField_Log.setColumns(10);
		passwdField_Log.setBounds(197, 163, 227, 20);
		Login.add(passwdField_Log);
		
		JButton button = new JButton("Login");
		button.setForeground(Color.GREEN);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
						user = UserField_Log.getText();
						password = new String(passwdField_Log.getPassword());
						UserInterface ui = Utente.stub;
						logged = Utente.server_interface.login(user, password, ui);
						if(logged){
							Login.setVisible(false);
							//se ho già 10 partite aperte, il bottone di apertura non sarà visibile
							if(Utente.open_match.size()>9){
								parola.setVisible(false);
								OpenMatch.setVisible(false);
							}
							Match.setVisible(true);
						}
						else{
							Login.revalidate();
						}
					
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		button.setBounds(236, 246, 150, 50);
		Login.add(button);
		JLabel label = new JLabel("PASSWORD");
		label.setForeground(Color.RED);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(266, 122, 90, 30);
		Login.add(label);
		
		JLabel label_1 = new JLabel("USERNAME");
		label_1.setForeground(Color.RED);
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setBounds(266, 32, 90, 30);
		Login.add(label_1);
		
		JButton button_1 = new JButton("Quit");
		button_1.setForeground(Color.BLUE);
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Utente.server_interface.deleteUser(User.getNick());
					Utente.close();
				} catch (RemoteException e1) {
					
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		button_1.setBounds(257, 388, 110, 40);
		Login.add(button_1);
		
		Match = new JPanel();
		getContentPane().add(Match);
		Match.setLayout(null);
		
		OpenMatch = new JButton("OpenMatch");
		OpenMatch.setForeground(Color.RED);
		OpenMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					try {
						if(parola.getText().isEmpty()){
							Match.revalidate();
						}
						else{
							User.OpenMatch(User.getNick(), parola.getText());							
							Match.setVisible(false);
							parola.setText("");
							MasterWait.setVisible(true);
						}	
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
			}
		});
		OpenMatch.setBounds(194, 105, 236, 52);
		Match.add(OpenMatch);
		
		JoinMatch = new JButton("JoinMatch");
		JoinMatch.setForeground(Color.MAGENTA);
		JoinMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(Utente.open_match.size()>0){
					JoinMatch.setVisible(false);
					Match.setVisible(false);
					UpdatePlayers();
					ListaPartite.setVisible(true);			
					ListaPartite.revalidate();
				}	
			}
		});
		JoinMatch.setBounds(194, 211, 236, 52);
		Match.add(JoinMatch);
		
		JButton button_4 = new JButton("Quit");
		button_4.setForeground(Color.BLUE);
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Utente.server_interface.logout(User.getNick(), Utente.stub);
					Utente.server_interface.deleteUser(User.getNick());
					Utente.close();
				} catch (RemoteException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		button_4.setBounds(399, 380, 110, 40);
		Match.add(button_4);
		
		JButton btnLogout = new JButton("Logout");
		btnLogout.setForeground(Color.GREEN);
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Utente.server_interface.logout(User.getNick(), Utente.stub);
					Match.setVisible(false);
					parola.setText("");
					UserField_Log.setText("");
					passwdField_Log.setText("");
					Login.setVisible(true);
				} catch (RemoteException e1) {
					
					e1.printStackTrace();
				}
				
			}
		});
		btnLogout.setBounds(149, 380, 110, 40);
		Match.add(btnLogout);
		
		parola = new JTextField();
		parola.setBounds(271, 74, 159, 20);
		Match.add(parola);
		parola.setColumns(15);
		
		JLabel lblNewLabel = new JLabel("Word:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(Color.RED);
		lblNewLabel.setBounds(194, 74, 77, 20);
		Match.add(lblNewLabel);
		
		ListaPartite = new JPanel();
		getContentPane().add(ListaPartite, "name_6919674302065");
		ListaPartite.setLayout(null);
		
		JButton btnQuit = new JButton("Quit");
		btnQuit.setForeground(Color.BLUE);
		btnQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Utente.server_interface.logout(User.getNick(), Utente.stub);
					Utente.server_interface.deleteUser(User.getNick());
					Utente.close();
				} catch (RemoteException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		btnQuit.setBounds(410, 410, 110, 40);
		ListaPartite.add(btnQuit);
		
		JLabel lblListaPartite = new JLabel("Lista Partite:");
		lblListaPartite.setFont(new Font("MV Boli", Font.BOLD, 14));
		lblListaPartite.setForeground(Color.RED);
		lblListaPartite.setBounds(252, 11, 120, 25);
		ListaPartite.add(lblListaPartite);
		
		JButton btnLogout2 = new JButton("Logout");
		btnLogout2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Utente.server_interface.logout(User.getNick(), Utente.stub);
					Utente.server_interface.deleteUser(User.getNick());
					Utente.close();
				} catch (RemoteException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		btnLogout2.setForeground(Color.GREEN);
		btnLogout2.setBounds(110, 410, 110, 40);
		ListaPartite.add(btnLogout2);
		
		MasterWait = new JPanel();
		getContentPane().add(MasterWait, "name_916339347448");
		MasterWait.setLayout(null);
		
		JButton AnnullaMatch = new JButton("Annulla_Partita");
		AnnullaMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Utente.ChiudiMatch();
				MasterWait.setVisible(false);
				
			}
		});
		AnnullaMatch.setBounds(240, 337, 144, 50);
		MasterWait.add(AnnullaMatch);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setToolTipText("");
		progressBar.setBounds(157, 193, 310, 30);
		progressBar.setIndeterminate(true);
		MasterWait.add(progressBar);
		
		JLabel lblNewLabel_1 = new JLabel("Partita creata, in attesa dei partecipanti...");
		lblNewLabel_1.setForeground(Color.RED);
		lblNewLabel_1.setFont(new Font("MV Boli", Font.BOLD, 15));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(122, 111, 380, 50);
		MasterWait.add(lblNewLabel_1);
		
		PlayerWait = new JPanel();
		getContentPane().add(PlayerWait, "name_3005671460166");
		PlayerWait.setLayout(null);
		
		JButton attesaPlayers = new JButton("abbandona");
		attesaPlayers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Utente.ChiudiMatch();
			}
		});
		attesaPlayers.setBounds(240, 337, 144, 50);
		PlayerWait.add(attesaPlayers);
		
		JLabel lblInAttesaDell = new JLabel("In attesa dell' inizio del match...");
		lblInAttesaDell.setHorizontalAlignment(SwingConstants.CENTER);
		lblInAttesaDell.setForeground(Color.RED);
		lblInAttesaDell.setFont(new Font("MV Boli", Font.BOLD, 15));
		lblInAttesaDell.setBounds(122, 111, 380, 50);
		PlayerWait.add(lblInAttesaDell);
		
		JProgressBar progressBar_1 = new JProgressBar();
		progressBar_1.setToolTipText("");
		progressBar_1.setIndeterminate(true);
		progressBar_1.setBounds(157, 193, 310, 30);
		PlayerWait.add(progressBar_1);
		
		MasterPanel = new JPanel();
		getContentPane().add(MasterPanel, "name_3179707461860");
		MasterPanel.setLayout(null);
		
		JButton button_2 = new JButton("Leave");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Master.terminaMatch();
			}
		});
		button_2.setBounds(272, 417, 89, 23);
		MasterPanel.add(button_2);
		
		PlayerPanel = new JPanel();
		getContentPane().add(PlayerPanel, "name_5227947580397");
		PlayerPanel.setLayout(null);
		
		JButton btnNewButton = new JButton("Leave");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Player.abbandona();
				PlayerPanel.setVisible(false);
				logged = false;
				try{
					Utente.server_interface.logout(User.getNick(), Utente.stub);
					Utente.server_interface.deleteUser(User.getNick());
					Utente.close();
					
				}
				catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnNewButton.setBounds(272, 433, 89, 23);
		PlayerPanel.add(btnNewButton);
		
		tentativo = new JTextField();
		tentativo.setBounds(274, 250, 86, 20);
		PlayerPanel.add(tentativo);
		tentativo.setColumns(1);
		
		JButton btnNewButton_1 = new JButton("Enter");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//prende solo una lettera per volta
				Player.tentativo(tentativo.getText().substring(0,1));
				tentativo.setText("");
			}
		});
		btnNewButton_1.setBounds(272, 292, 89, 23);
		PlayerPanel.add(btnNewButton_1);
		
		Vittoria = new JPanel();
		getContentPane().add(Vittoria, "name_2615073876392");
		Vittoria.setLayout(null);
		
		JButton Restart = new JButton("Complimenti, hai vinto!");
		Restart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				Utente.close();
			}
		});
		Restart.setForeground(Color.RED);
		Restart.setBounds(120, 220, 405, 43);
		Vittoria.add(Restart);
		
		Loser = new JPanel();
		getContentPane().add(Loser, "name_2630902655592");
		Loser.setLayout(null);
		
		JButton LoserRestart = new JButton("Perdente!");
		LoserRestart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Utente.close();
			}
		});
		LoserRestart.setForeground(Color.RED);
		LoserRestart.setBounds(114, 224, 405, 43);
		Loser.add(LoserRestart);
		Match.setVisible(false);
	}
	
	public static void masterWins(){
		logged = false;
		try {
			Utente.server_interface.logout(User.getNick(),Utente.stub);
			Utente.server_interface.deleteUser(User.getNick());
			MasterPanel.setVisible(false);
			UserField_Log.setText("");
			passwdField_Log.setText("");
			Vittoria.setVisible(true);
			Vittoria.revalidate();
		} catch (RemoteException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public static void masterLose(){
		logged = false;
		try {
			Utente.server_interface.logout(User.getNick(),Utente.stub);
			Utente.server_interface.deleteUser(User.getNick());
			MasterPanel.setVisible(false);
			UserField_Log.setText("");
			passwdField_Log.setText("");
			Loser.setVisible(true);
			Loser.revalidate();
		} catch (RemoteException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public static void playerWins(){
		logged = false;
		try {
			Utente.server_interface.logout(User.getNick(),Utente.stub);
			Utente.server_interface.deleteUser(User.getNick());
			PlayerPanel.setVisible(false);
			UserField_Log.setText("");
			passwdField_Log.setText("");
			Vittoria.setVisible(true);
			Vittoria.revalidate();
		} catch (RemoteException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public static void playerLose(){
		try{
			logged = false;
			Utente.server_interface.logout(User.getNick(),Utente.stub);
			Utente.server_interface.deleteUser(User.getNick());
			PlayerPanel.setVisible(false);
			UserField_Log.setText("");
			passwdField_Log.setText("");
			Loser.setVisible(true);
			Loser.revalidate();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void matchClosed(){
		logged = false;
		try {
			Utente.server_interface.logout(User.getNick(),Utente.stub);
			Utente.server_interface.deleteUser(User.getNick());
			Utente.close();
		} catch (RemoteException e) {
			System.out.println("errore nella chiusura del match");
		}
		
	}
	
	public static void matchStartedMaster(){
		Trattini = new ArrayList<JLabel>();
		label_2 = new JLabel("");
		label_2.setBounds(233, 44, 168, 32);
		MasterPanel.add(label_2);
		label_2.setText(Utente.getWord());
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		for(int i = 0;i<Utente.getWord().length();i++){
			Trattini.add(new JLabel("-"));
			Trattini.get(i).setBounds(100+30*i, 200, 20, 20);
			Trattini.get(i).setHorizontalAlignment(SwingConstants.CENTER);
			MasterPanel.add(Trattini.get(i));
		}
		MasterWait.setVisible(false);
		MasterPanel.validate();
		MasterPanel.setVisible(true);
		MasterPanel.revalidate();
	}
	
	public static void matchStartePlayer(int len){
		Trattini = new ArrayList<JLabel>();
		tentativi = new JLabel("9");
		tentativi.setBounds(50,100,20,20);
		for(int i = 0;i<len;i++){
			Trattini.add(new JLabel("-"));
			Trattini.get(i).setBounds(100+30*i, 150, 20, 20);
			PlayerPanel.add(Trattini.get(i));
		}
		PlayerPanel.add(tentativi);
		PlayerWait.setVisible(false);
		PlayerPanel.setVisible(true);
		PlayerPanel.validate();
		PlayerPanel.revalidate();
	}
	
	public static void UpdateTentativi(int t){
		String ts = ""+t;
		tentativi.setText(ts);
		tentativi.revalidate();
		PlayerPanel.validate();
		PlayerPanel.revalidate();
	}
	
	public static void UpdateWordMaster(int i, String w){
		Trattini.get(i).setText(w);
		MasterPanel.revalidate();
	}
	
	public static void UpdateWordPlayer(int i, String w){
		Trattini.get(i).setText(w);
		PlayerPanel.revalidate();
	}
	
	public static void UpdatePlayers(){
		ListaPartite.revalidate();
		VisibleMatch = new ArrayList<JLabel>();
		BottoniPartite = new ArrayList<JButton>();
		for(int i = 0; i<Utente.open_match.size();i++){
		  if(!Utente.open_match.get(i).isFull()){	
			VisibleMatch.add(new JLabel(Utente.open_match.get(i).getNameMatch()+" posti: " + Utente.open_match.get(i).postiDisponibili()));
			VisibleMatch.get(i).setBounds(35, 50+40*i, 250, 30);
			BottoniPartite.add(new JButton("join"));
			BottoniPartite.get(i).setBounds(496, 50+40*i, 90, 30);
			String tmp_name = Utente.open_match.get(i).getNameMatch();
			BottoniPartite.get(i).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					User.JoinMatch(User.getNick(),tmp_name);
					ListaPartite.setVisible(false);
					PlayerWait.setVisible(true);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				
			}
		});
			ListaPartite.add(VisibleMatch.get(i));
			ListaPartite.validate();
			ListaPartite.add(BottoniPartite.get(i));
			ListaPartite.validate();
		}
		}
		ListaPartite.revalidate();
		
	}
}
