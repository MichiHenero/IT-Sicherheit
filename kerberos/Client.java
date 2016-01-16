package kerberos;

/* Simulation einer Kerberos-Session mit Zugriff auf einen Fileserver
 /* Client-Klasse
 */

import java.util.*;

public class Client extends Object {

	private KDC myKDC; // Konstruktor-Parameter

	private String currentUser; // Speicherung bei Login n�tig
	private Ticket tgsTicket = null; // Speicherung bei Login n�tig
	private long tgsSessionKey; // K(C,TGS) // Speicherung bei Login n�tig
	
	private long userPasswordKey; // K(C)
	private Ticket serverTicket = null;

	// Konstruktor
	public Client(KDC kdc) {
		myKDC = kdc;
	}

	// Kerberos-Protokoll ---> Schritt 1 und Schritt 2
	public boolean login(String userName, char[] password) {
		/* ToDo */
		//User-Registrierung
		myKDC.userRegistration(userName, password);		
		
		//Aufgabe: TGS-Ticket f�r den �bergebenen Benutzer vom KDC (AS) holen (TGS-Servername: myTGS) 
		TicketResponse ticketResponse = myKDC.requestTGSTicket(userName, "myTGS", generateNonce());
		
		//User-Passwort erstellen
		userPasswordKey = generateSimpleKeyFromPassword(password);
		
		//Ticket-Anfrage entschl�sseln
		if(!ticketResponse.decrypt(userPasswordKey)) {
			System.out.println("Ticket-Anfrage wurde schon entschluesselt, oder der Schluessel ist falsch!");
			return false;
		}
		
		//�berpr�fung, ob �berhaupt eine Ticket-Anfrage zur�ck kommt
		if(ticketResponse == null) {
			System.out.println("Ticketanfrage fehlgeschlagen");
			return false;
		}
		
		//und zusammen mit dem TGS-Sessionkey und dem UserNamen speichern aus der Ticket-Anfrage
		currentUser = userName;
		tgsTicket = ticketResponse.getResponseTicket();
		tgsSessionKey = ticketResponse.getSessionKey();
		
		return true;	
	}

	//FilePath = "E:\\Studium\\WS2015_2016\\IT-Sicherheit\\Aufgabe4\\material\\file.txt"
	// Kerberos-Protokoll ---> Schritt 3, Schritt 4 und Schritt 5
	public boolean showFile(Server fileServer, String filePath) {
		/* ToDo */
		// Authentifikation f�r den Client erstellen und verschl�sseln mit dem tgsSessionKey
		Auth auth = new Auth(currentUser, System.currentTimeMillis());
		auth.encrypt(tgsSessionKey);
		
		//Aufgabe: Serverticket vom KDC (TGS) holen
		TicketResponse ticketResponse = myKDC.requestServerTicket(tgsTicket, auth, fileServer.getName(), generateNonce());
		
		//Ticket-Anfrage entschl�sseln
		if(!ticketResponse.decrypt(userPasswordKey)) {
			System.out.println("Ticket-Anfrage wurde schon entschluesselt, oder der Schluessel ist falsch!");
			return false;
		}
		
		//�berpr�fung, ob �berhaupt eine Ticket-Anfrage zur�ck kommt
		if(ticketResponse == null) {
			System.out.println("Ticketanfrage fehlgeschlagen");
			return false;
		}
		
		//ServerTicket und SessionKEy speichern
		serverTicket = ticketResponse.getResponseTicket();
		tgsSessionKey = ticketResponse.getSessionKey();
		
		// Authentifikation f�r den Client erstellen und verschl�sseln mit dem tgsSessionKey
		auth = new Auth(currentUser, System.currentTimeMillis());
		auth.encrypt(tgsSessionKey);
		
		//Aufgabe: ...und �showFile�-Service beim �bergebenen Fileserver anfordern.
		if(!(fileServer.requestService(serverTicket, auth, "showFile", filePath))) {
			System.out.println("Anfrage zum auslesen der Datei ist fehlgeschlagen");
		}
		
		return true;
	}

	/* *********** Hilfsmethoden **************************** */

	private long generateSimpleKeyFromPassword(char[] passwd) {
		// Liefert einen eindeutig aus dem Passwort abgeleiteten Schl�ssel
		// zur�ck, hier simuliert als long-Wert
		long pwKey = 0;
		if (passwd != null) {
			for (int i = 0; i < passwd.length; i++) {
				pwKey = pwKey + passwd[i];
			}
		}
		return pwKey;
	}

	private long generateNonce() {
		// Liefert einen neuen Zufallswert
		long rand = (long) (100000000 * Math.random());
		return rand;
	}
	
	public static void main(String[] args) {
		
		
		
		KDC kdc = new KDC("myTGS");
		Client c = new Client(kdc);
		char[] password = { 'p', 'w'};
		c.login("User", password);		
	}
	
}
