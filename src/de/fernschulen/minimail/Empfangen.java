package de.fernschulen.minimail;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Empfangen extends JFrame {

    //für die Tabelle
    private JTable tabelle;
    //für das Modell
    private DefaultTableModel modell;

    //Aufgabe 1: die Variablen für die Benutzername und Kennwort festzulegen
    private String benutzername = null;
    private String kennwort = null;

    private MeineAktionen beantwortenButton, weiterleitenButton;

    //eine innere Klasse für den WindowListener und den ActionListener
    //die Klasse ist von WindowAdapter abgeleitet
    class MeinWindowAdapter extends WindowAdapter {
        //für das Öffnen des Fensters
        @Override
        public void windowOpened(WindowEvent e) {
            //die Methode nachrichtenEmpfangen() aufrufen
            nachrichtenEmpfangen();
        }
    }

    class MeineAktionen extends AbstractAction {
        public MeineAktionen(String text, ImageIcon icon, String beschreibung, KeyStroke shortcut, String actionText) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, beschreibung);
            putValue(ACCELERATOR_KEY, shortcut);
            putValue(ACTION_COMMAND_KEY, actionText);
        }
        //Aufgabe 2: hier wurden die Schaltflaechen für Beantworten und Weiterleiten erstellt
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("beantworten"))
                beantworten();
            if (e.getActionCommand().equals("weiterleiten"))
                weiterleiten();
        }
    }

    //der Konstruktor
    Empfangen() {
        super();

        setTitle("E-Mail empfangen");
        //wir nehmen ein Border-Layout
        setLayout(new BorderLayout());

        setVisible(true);
        setSize(700, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Aufgabe 2: hier wurden die Schaltflaechen hinzugefügt.
        beantwortenButton = new MeineAktionen("Beantworten", new ImageIcon("icons/mail-reply.gif"), "Beantwortet diese E-mail", null, "beantworten");
        weiterleitenButton = new MeineAktionen("Weiterleiten", new ImageIcon("icons/mail-forward.gif"), "Leitet diese E-mail weiter", null, "weiterleiten");

        add(symbolleiste(), BorderLayout.NORTH);

        //den Listener verbinden
        addWindowListener(new MeinWindowAdapter());

        //die Tabelle erstellen und anzeigen
        tabelleErstellen();
        tabelleAktualisieren();
    }
    //Aufgabe 2: Hier wurden der Betreff der Mail,
    //der Inhalt und der Absender für die Antwortung bearbeitet.
    private void beantworten() {
        int zeile = tabelle.getSelectedRow();
        if (zeile > -1) {
            String sender, betreff, inhalt, ID;
            ID = tabelle.getModel().getValueAt(zeile, 0).toString();
            sender = tabelle.getModel().getValueAt(zeile, 1).toString();
            betreff = "AW: " + tabelle.getModel().getValueAt(zeile, 2).toString();
            inhalt = "\n------------------Text der ursprünglichen Nachricht------------------\n" + tabelle.getModel().getValueAt(zeile, 3).toString();
            // Die benötigten Parameter sind mit dem Konstruktor der Klasse Antwortung übergeben worden.
            new Antwortung(Empfangen.this,true,ID,sender,betreff,inhalt);


        } else
            JOptionPane.showMessageDialog(null, "Es wurde keine Zeile markiert");

    }

    //Aufgabe 2: Hier wurden der Betreff der Mail,
    //der Inhalt und der Empfanger für die Weiterleitung bearbeitet.
    private void weiterleiten() {
        int zeile = tabelle.getSelectedRow();
        if (zeile > -1) {
            String emfanger, betreff, inhalt, ID;
            ID = tabelle.getModel().getValueAt(zeile, 0).toString();
            emfanger = null;
            betreff = "WG: " + tabelle.getModel().getValueAt(zeile, 2).toString();
            inhalt = "\n------------------Text der ursprünglichen Nachricht------------------\n" + tabelle.getModel().getValueAt(zeile, 3).toString();

            //Aufgabe 2: Die benötigten Parameter sind mit dem Konstruktor der Klasse Weiterleitung übergeben worden.
            new Weiterleitung(Empfangen.this,true,ID,emfanger,betreff,inhalt);
            tabelleAktualisieren();

        } else
            JOptionPane.showMessageDialog(null, "Es wurde keine Zeile markiert");

    }
    //Aufgabe 2: hier wurden die Schaltflaechen im Toolbar hinzugefügt.
    private JToolBar symbolleiste() {
        JToolBar leiste = new JToolBar();
        leiste.add(beantwortenButton);
        leiste.add(weiterleitenButton);
        return leiste;
    }

    //zum Erstellen der Tabelle
    private void tabelleErstellen() {
        //für die Spaltenbezeichner
        String[] spaltenNamen = {"ID", "Sender", "Betreff", "Text"};

        //ein neues Standardmodell erstellen
        modell = new DefaultTableModel();
        //die Spaltennamen setzen
        modell.setColumnIdentifiers(spaltenNamen);
        //die Tabelle erzeugen
        tabelle = new JTable();
        //und mit dem Modell verbinden
        tabelle.setModel(modell);
        //wir haben keinen Editor, können die Tabelle also nicht bearbeiten
        tabelle.setDefaultEditor(Object.class, null);
        //es sollen immer alle Spalten angepasst werden
        tabelle.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        //und die volle Größe genutzt werden
        tabelle.setFillsViewportHeight(true);
        //die Tabelle setzen wir in ein Scrollpane
        JScrollPane scroll = new JScrollPane(tabelle);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll);

        //einen Maus-Listener ergänzen
        tabelle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //war es ein Doppelklick?
                if (e.getClickCount() == 2) {
                    //die Zeile beschaffen
                    int zeile = tabelle.getSelectedRow();
                    //die Daten beschaffen
                    String sender, betreff, inhalt, ID;
                    ID = tabelle.getModel().getValueAt(zeile, 0).toString();
                    sender = tabelle.getModel().getValueAt(zeile, 1).toString();
                    betreff = tabelle.getModel().getValueAt(zeile, 2).toString();
                    inhalt = tabelle.getModel().getValueAt(zeile, 3).toString();
                    //und anzeigen
                    //übergeben wird der Frame der äußeren Klasse
                    new Anzeige(Empfangen.this, true, ID, sender, betreff, inhalt);
                }
            }
        });
    }

    private void tabelleAktualisieren() {
        //für den Datenbankzugriff
        Connection verbindung;
        ResultSet ergebnisMenge;

        //für die Spalten
        String sender, betreff, inhalt, ID;
        //die Inhalte löschen
        modell.setRowCount(0);

        try {
            //Verbindung herstellen und Ergebnismenge beschaffen
            verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
            ergebnisMenge = MiniDBTools.liefereErgebnis(verbindung, "SELECT * FROM empfangen");
            //die Einträge in die Tabelle schreiben
            while (ergebnisMenge.next()) {
                ID = ergebnisMenge.getString("iNummer");
                sender = ergebnisMenge.getString("sender");
                betreff = ergebnisMenge.getString("betreff");
                //den Inhalt vom CLOB beschaffen und in einen String umbauen
                Clob clob;
                clob = ergebnisMenge.getClob("inhalt");
                inhalt = clob.getSubString(1, (int) clob.length());

                //die Zeile zum Modell hinzufügen
                //dazu benutzen wir ein Array vom Typ Object
                modell.addRow(new Object[]{ID, sender, betreff, inhalt});
            }
            //die Verbindungen wieder schließen und trennen
            ergebnisMenge.close();
            verbindung.close();
            MiniDBTools.schliessenDB("jdbc:derby:mailDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtenEmpfangen() {
        nachrichtenAbholen();
        //nach dem Empfangen lassen wir die Anzeige aktualisieren
        tabelleAktualisieren();
    }

    private void nachrichtenAbholen() {
        //Aufgabe 1: Hier wird ein BufferedReader erstellt,
        //Aufgabe 1: um Benutzernamen und Passwörter aus der txt-Datei mit dem Namen 'anmeldeDaten' abzurufen.
        try (BufferedReader reader = new BufferedReader(new FileReader("src/anmeldeDaten.txt"))) {
            //Aufgabe 1:für den Benutzernamen in der ersten Zeile der txt-Datei
            benutzername = reader.readLine();
            //Aufgabe 1:für das Passwort in der zweiten Zeile der txt-Datei
            kennwort = reader.readLine();
        } catch (IOException e) {
            //Aufgabe 1:eine Fehlermeldung, wenn Benutzername und Passwort nicht gefunden werden können
            System.out.println("Die Anmeldedaten konnten nicht gefunden werden");
        }

        //der Server
        String server = "pop.gmail.com";


        //die Eigenschaften setzen
        Properties eigenschaften = new Properties();
        //das Protokoll
        eigenschaften.put("mail.store.protocol", "pop3");
        //den Host
        eigenschaften.put("mail.pop3.host", server);
        //den Port zum Empfangen
        eigenschaften.put("mail.pop3.port", "995");
        //die Authentifizierung über TLS
        eigenschaften.put("mail.pop3.starttls.enable", "true");
        //das Session-Objekt erstellen
        Session sitzung = Session.getDefaultInstance(eigenschaften);

        //das Store-Objekt über die Sitzung erzeugen
        try (Store store = sitzung.getStore("pop3s")) {
            //und verbinden
            store.connect(server, benutzername, kennwort);
            //ein Ordnerobjekt für den Posteingang erzeugen
            Folder posteingang = store.getFolder("INBOX");
            //und öffnen
            //dabei sind auch Änderungen zugelassen
            posteingang.open(Folder.READ_WRITE);

            //die Nachrichten beschaffen
            Message nachrichten[] = posteingang.getMessages();

            //gibt es neue Nachrichten?
            if (nachrichten.length != 0) {
                //dann die Anzahl zeigen
                JOptionPane.showMessageDialog(this, "Es gibt " + posteingang.getUnreadMessageCount() + " neue Nachrichten.");
                //jede Nachricht verarbeiten
                for (Message nachricht : nachrichten)
                    nachrichtVerarbeiten(nachricht);
            } else
                JOptionPane.showMessageDialog(this, "Es gibt keine neue Nachrichten.");

            //den Ordner schließen
            //durch das Argument true werden die Nachrichten gelöscht
            posteingang.close(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }

    private void nachrichtVerarbeiten(Message nachricht) {
        try {
            //ist es einfacher Text?
            if (nachricht.isMimeType("text/plain")) {
                //den ersten Sender beschaffen
                String sender = nachricht.getFrom()[0].toString();
                //den Betreff
                String betreff = nachricht.getSubject();
                //den Inhalt
                String inhalt = nachricht.getContent().toString();
                //und die Nachricht speichern
                nachrichtSpeichern(sender, betreff, inhalt);
                //und zum Löschen markieren
                nachricht.setFlag(Flags.Flag.DELETED, true);
            }
            //sonst geben wir eine Meldung aus
            else
                JOptionPane.showMessageDialog(this, "Der Typ der Nachricht " + nachricht.getContentType() + "kann nicht verarbeitet werden.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }


    private void nachrichtSpeichern(String sender, String betreff, String inhalt) {
        //für die Verbindung
        Connection verbindung;

        //die Datenbank öffnen
        verbindung = MiniDBTools.oeffnenDB("jdbc:derby:mailDB");
        try {
            //einen Eintrag in der Tabelle empfangen anlegen
            //über ein vorbereitetes Statement
            PreparedStatement prepState;
            prepState = verbindung.prepareStatement("insert into empfangen (sender, betreff, inhalt) values (?, ?, ?)");
            prepState.setString(1, sender);
            prepState.setString(2, betreff);
            prepState.setString(3, inhalt);
            //das Statement ausführen
            prepState.executeUpdate();
            verbindung.commit();

            //Verbindung schließen
            prepState.close();
            verbindung.close();
            //und die Datenbank schließen
            MiniDBTools.schliessenDB("jdbc:derby:mailDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Problem: \n" + e.toString());
        }
    }
}