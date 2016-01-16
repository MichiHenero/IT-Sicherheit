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

	// Konstruktor
	public Client(KDC kdc) {
		myKDC = kdc;
	}

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

	public boolean showFile(Server fileServer, String filePath) {
		/* ToDo */
		//TGS-Ticket mit TGS-Session-Key verschl�sseln
		if (tgsTicket.encrypt(tgsSessionKey)) {
			myKDC.requestServerTicket(tgsTicket, tgsAuth, serverName, nonce)
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
