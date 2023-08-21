package de.fernschulen.minimail;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NeueNachricht extends JDialog{
	//automatisch über Eclipse erzeugt
	private static final long serialVersionUID = -5496318621928815910L;

	//für die Eingabefelder
	private JTextField empfaenger, betreff;
	private JTextArea inhalt;
	//für die Schaltflächen 
	private JButton ok, abbrechen;
	private String benutzername = null;
	private String kennwort = null;

	//die innere Klasse für den ActionListener
	class NeuListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			//wurde auf OK geklickt?
			if (e.getActionCommand().equals("senden"))
				//dann die Daten übernehmen
				senden();
			//wurde auf Abbrechen geklickt?
			if (e.getActionCommand().equals("abbrechen"))
				//dann Dialog schließen
				dispose();
		}
	}

	//der Konstruktor
	public NeueNachricht(JFrame parent, boolean modal) {
		super(parent, modal);
		setTitle("Neue Nachricht");
		//die Oberfläche erstellen
		initGui();

		//Standardoperation setzen
		//hier den Dialog ausblenden und löschen
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private void initGui() {
		setLayout(new BorderLayout());
		JPanel oben = new JPanel();
		oben.setLayout(new GridLayout(0, 2));
		oben.add(new JLabel("Empfänger:"));
		empfaenger = new JTextField();
		oben.add(empfaenger);
		oben.add(new JLabel("Betreff:"));
		betreff = new JTextField();
		oben.add(betreff);
		add(oben, BorderLayout.NORTH);
		inhalt = new JTextArea();
		//den Zeilenumbruch aktivieren
		inhalt.setLineWrap(true);
		inhalt.setWrapStyleWord(true);
		//das Feld setzen wir in ein Scrollpane
		JScrollPane scroll = new JScrollPane(inhalt);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scroll);

		JPanel unten = new JPanel();
		//die Schaltflächen
		ok = new JButton("Senden");
		ok.setActionCommand("senden");
		abbrechen = new JButton("Abbrechen");
		abbrechen.setActionCommand("abbrechen");

		NeuListener listener = new NeuListener();
		ok.addActionListener(listener);
		abbrechen.addActionListener(listener);

		unten.add(ok);
		unten.add(abbrechen);
		add(unten, BorderLayout.SOUTH);

		//anzeigen
		setSize(600, 300);
		setVisible(true);
	}

	//die Methode verschickt die Nachricht
	private void senden() {
		//für die Sitzung
		Session sitzung;

		//die Verbindung herstellen
		sitzung = verbindungHerstellen();
		//die Nachricht verschicken und speichern
		nachrichtVerschicken(sitzung);
		nachrichtSpeichern();
	}

	private Session verbindungHerstellen() {
		//Aufgabe 1: Hier wird ein BufferedReader erstellt,
		//Aufgabe 1: um Benutzernamen und Passwörter aus der txt-Datei mit dem Namen 'anmeldeDaten' abzurufen.
		try (BufferedReader reader = new BufferedReader(new FileReader("src/anmeldeDaten.txt"))) {
			//Aufgabe 1:für den Benutzernamen in der ersten Zeile der txt-Datei
			benutzername = reader.readLine();
			//Aufgabe 1:für das Passwort in der zweiten Zeile der txt-Datei
			kennwort = reader.readLine();
		} catch (IOException e) {
			//Aufgabe 1:eine Fehlermeldung, wenn Benutzername und Passwort nicht gefunden werden können
			System.out.println("Die Anmeldedaten konnten nicht gefunden werden" + e);
		}

		//der Server
		String server = "smtp.gmail.com";

		//die Eigenschaften setzen
		Properties eigenschaften = new Properties();
		//die Authentifizierung über TLS
		eigenschaften.put("mail.smtp.auth", "true");
		eigenschaften.put("mail.smtp.starttls.enable", "true");
		//der Server
		eigenschaften.put("mail.smtp.host", server);
		//der Port zum Versenden
		eigenschaften.put("mail.smtp.port", "587");

		// das Session-Objekt erstellen
		Session sitzung = Session.getInstance(eigenschaften, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(benutzername, kennwort);
			}
		});

		return sitzung;
	}

	private void nachrichtVerschicken(Session sitzung) {
		//der Absender
		String absender = "emreakdaggg@gmail.com";


		try {
			//eine neue Nachricht vom Typ MimeMessage erzeugen
			MimeMessage nachricht = new MimeMessage(sitzung);
			//den Absender setzen
			nachricht.setFrom(new InternetAddress(absender));
			//den Empfänger
			nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfaenger.getText()));
			//den Betreff
			nachricht.setSubject(betreff.getText());
			//und den Text
			nachricht.setText(inhalt.getText());
			//die Nachricht verschicken
			Transport.send(nachricht);

			JOptionPane.showMessageDialog(this, "Die Nachricht wurde verschickt."); 

			//den Dialog schließen
			dispose();

		} 
		catch (MessagingException e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString()); 
		}		
	}

	private void nachrichtSpeichern() {
		//für die Verbindung
		Connection verbindung;

		//die Datenbank öffnen
		verbindung=MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
		try {
			//einen Eintrag in der Tabelle gesendet anlegen
			//über ein vorbereitetes Statement
			PreparedStatement prepState;
			prepState = verbindung.prepareStatement("insert into gesendet (empfaenger, betreff, inhalt) values (?, ?, ?)");
			prepState.setString(1, empfaenger.getText());
			prepState.setString(2, betreff.getText());
			prepState.setString(3, inhalt.getText());
			//das Statement ausführen
			prepState.executeUpdate();
			verbindung.commit();

			//Verbindung schließen
			prepState.close();
			verbindung.close();
			//und die Datenbank schließen
			MiniDBTools.schliessenDB("jdbc:derby:mailDB");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
		}
	}
}
