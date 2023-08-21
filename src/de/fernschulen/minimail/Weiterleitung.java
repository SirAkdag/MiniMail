package de.fernschulen.minimail;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
//Aufgabe 2: hier wurde Weiterleitung-Klasse erstellt um Mails weiterzuleiten
public class Weiterleitung extends JDialog{

    private static String ID;
    private JTextField empfanger = new JTextField();
    protected JTextField betreff = new JTextField();
    private JTextArea inhalt = new JTextArea();
    private JButton ok, abbrechen;

    //Aufgabe 1: die Variablen für die Benutzername und Kennwort festzulegen
    String benutzerName = null;
    String passwort = null;


    //die innere Klasse fuer den ActionListener
    class MeinListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //wurde auf Weiterleiten geklickt?
            if (e.getActionCommand().equals("weiterleiten"))
                try {
                    senden();
                } catch (IOException e1) {
                     e1.printStackTrace();
                }
            //wurde auf Abbrechen geklickt?
            if (e.getActionCommand().equals("abbrechen"))
                //dann Dialog schliessen
                dispose();
        }
    }

    //der Konstruktor
    public Weiterleitung(JFrame parent, boolean modal, String ID, String empfanger, String betreff, String inhalt) {
        super(parent,modal);
        this.ID = ID;
        this.betreff.setText(betreff);
        this.inhalt.setText(inhalt);
        this.empfanger.setText(empfanger);

        setTitle("Nachricht weiterleiten");

        //die Oberfläche erstellen
        initGui();

        //Standardoperation setzen
        //hier den Dialog ausblenden und loeschen
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initGui() {
        setLayout(new BorderLayout());
        JPanel oben = new JPanel();
        oben.setLayout(new GridLayout(0, 2));
        oben.add(new JLabel("Empfänger:"));
        oben.add(empfanger);
        oben.add(new JLabel("Betreff:"));
        oben.add(betreff);

        add(oben, BorderLayout.NORTH);
        //den Zeilenumbruch aktivieren
        inhalt.setLineWrap(true);
        inhalt.setWrapStyleWord(true);
        //das Feld setzen wir in ein Scrollpane
        JScrollPane scroll = new JScrollPane(inhalt);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll);

        JPanel unten = new JPanel();
        //die Schaltflaechen
        ok = new JButton("Weiterleiten");
        ok.setActionCommand("weiterleiten");
        abbrechen = new JButton("Abbrechen");
        abbrechen.setActionCommand("abbrechen");

        MeinListener listener = new MeinListener();
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
    private void senden() throws IOException {
        //fuer die Sitzung
        Session sitzung;

        //die Verbindung herstellen
        sitzung = verbindungHerstellen();
        //die Nachricht verschicken und speichern
        nachrichtVerschicken(sitzung);
        nachrichtSpeichern();
    }

    private Session verbindungHerstellen() {
        //Aufgabe 1: Hier wird ein BufferedReader erstellt,
        //Aufgabe 1: um Benutzernamen und Passwörter aus der txt-Datei mit dem Namen 'anmeldeDaten.txt' abzurufen.
        try (BufferedReader reader = new BufferedReader(new FileReader("src/anmeldeDaten.txt"))) {
            //Aufgabe 1:für den Benutzernamen in der ersten Zeile der txt-Datei
            benutzerName = reader.readLine();
            //Aufgabe 1:für das Passwort in der zweiten Zeile der txt-Datei
            passwort = reader.readLine();
        }
        catch (IOException e) {
            //Aufgabe 1:eine Fehlermeldung, wenn Benutzername und Passwort nicht gefunden werden können
            System.out.println("Die Anmeldedaten konnten nicht gefunden werden" + e);
        }
        //der Server
        String server = "smtp.gmail.com";

        //die Eigenschaften setzen
        Properties eigenschaften = new Properties();
        //die Authentifizierung ueber TLS
        eigenschaften.put("mail.smtp.auth", "true");
        eigenschaften.put("mail.smtp.starttls.enable", "true");
        //der Server
        eigenschaften.put("mail.smtp.host", server);
        //der Port zum Versenden
        eigenschaften.put("mail.smtp.port", "587");


        // das Session-Objekt erstellen
        Session sitzung = Session.getInstance(eigenschaften, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(benutzerName, passwort);
            }
        });

        return sitzung;

    }

    private void nachrichtVerschicken(Session sitzung) {
        //der Absender
        String absender = "emre.ils.test@gmail.com";

        try {
            //eine neue Nachricht vom Typ MimeMessage erzeugen
            MimeMessage nachricht = new MimeMessage(sitzung);
            //den Absender setzen
            nachricht.setFrom(new InternetAddress(absender));
            //den EmpfÃ¤nger
            nachricht.setRecipients(Message.RecipientType.TO, InternetAddress.parse(empfanger.getText()));
            //den Betreff
            nachricht.setSubject(betreff.getText());
            //und den Text
            nachricht.setText(inhalt.getText());
            //die Nachricht verschicken
            Transport.send(nachricht);

            JOptionPane.showMessageDialog(this, "Die Nachricht wurde verschickt.");

            //den Dialog schliessen
            dispose();

        }
        catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtSpeichern() {
        //fÃ¼r die Verbindung
        Connection verbindung;

        //die Datenbank Oeffnen
        verbindung=MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
        try {
            //einen Eintrag in der Tabelle gesendet anlegen
            //Ã¼ber ein vorbereitetes Statement
            PreparedStatement prepState;
            prepState = verbindung.prepareStatement("insert into gesendet (empfaenger, betreff, inhalt) values (?, ?, ?)");
            prepState.setString(1, empfanger.getText());
            prepState.setString(2, betreff.getText());
            prepState.setString(3, inhalt.getText());
            //das Statement ausfÃ¼hren
            prepState.executeUpdate();
            verbindung.commit();

            //Verbindung schliessen
            prepState.close();
            verbindung.close();
            //und die Datenbank schliessen
            MiniDBTools.schliessenDB("jdbc:derby:mailDB");
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

}