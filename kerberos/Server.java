package kerberos;

/* Simulation einer Kerberos-Session mit Zugriff auf einen Fileserver
 /* Server-Klasse
 */

import java.util.*;
import java.io.*;

public class Server extends Object {

	private final long fiveMinutesInMillis = 300000; // 5 Minuten in
														// Millisekunden

	private String myName; // Konstruktor-Parameter
	private KDC myKDC; // wird bei KDC-Registrierung gespeichert
	private long myKey; // wird bei KDC-Registrierung gespeichert
	
	private final String COMMAND = "showFile";
	private long serverSessionKey; // K(C,S)

	// Konstruktor
	public Server(String name) {
		myName = name;
	}

	public String getName() {
		return myName;
	}

	public void setupService(KDC kdc) {
		// Anmeldung des Servers beim KDC
		myKDC = kdc;
		myKey = myKDC.serverRegistration(myName);
		System.out.println("Server " + myName
				+ " erfolgreich registriert bei KDC " + myKDC.getName()
				+ " mit ServerKey " + myKey);
	}

	public boolean requestService(Ticket srvTicket, Auth srvAuth, String command, String parameter) {
		/* ToDo */		
		if(!this.COMMAND.equals(command)) {
			System.out.println("Unerwartetes Kommando");
			return false;
		} else {
			//Serverticket mit eigenen Schl�ssel entschl�sseln
			if(!srvTicket.decrypt(this.myKey)) {System.out.println("Entschluesselung des ServerTickets fehlgeschlagen");}
			//ServerSessionKey aus dem Ticket holen
			serverSessionKey = srvTicket.getSessionKey();
			//Authentifikation entschl�sseln mit dem serverSessionKey
			if(!srvAuth.decrypt(serverSessionKey)) {System.out.println("Entschluesseln der Authentifikation fehlgeschlagen");}
			
			return showFile(parameter);
		}
	}

	/* *********** Services **************************** */

	private boolean showFile(String filePath) {
		/*
		 * Angegebene Datei auf der Konsole ausgeben. R�ckgabe: Status der
		 * Operation
		 */
		String lineBuf = null;
		File myFile = new File(filePath);
		boolean status = false;

		if (!myFile.exists()) {
			System.out.println("Datei " + filePath + " existiert nicht!");
		} else {
			try {
				// Datei �ffnen und zeilenweise lesen
				BufferedReader inFile = new BufferedReader(
						new InputStreamReader(new FileInputStream(myFile)));
				lineBuf = inFile.readLine();
				while (lineBuf != null) {
					System.out.println(lineBuf);
					lineBuf = inFile.readLine();
				}
				inFile.close();
				status = true;
			} catch (IOException ex) {
				System.out.println("Fehler beim Lesen der Datei " + filePath
						+ ex);
			}
		}
		return status;
	}

	/* *********** Hilfsmethoden **************************** */

	private boolean timeValid(long lowerBound, long upperBound) {
		/*
		 * Wenn die aktuelle Zeit innerhalb der �bergebenen Zeitgrenzen liegt,
		 * wird true zur�ckgegeben
		 */

		long currentTime = (new Date()).getTime(); // Anzahl mSek. seit 1.1.1970
		if (currentTime >= lowerBound && currentTime <= upperBound) {
			return true;
		} else {
			System.out.println("-------- Time not valid: " + currentTime
					+ " not in (" + lowerBound + "," + upperBound + ")!");
			return false;
		}
	}

	boolean timeFresh(long testTime) {
		/*
		 * Wenn die �bergebene Zeit nicht mehr als 5 Minuten von der aktuellen
		 * Zeit abweicht, wird true zur�ckgegeben
		 */
		long currentTime = (new Date()).getTime(); // Anzahl mSek. seit 1.1.1970
		if (Math.abs(currentTime - testTime) < fiveMinutesInMillis) {
			return true;
		} else {
			System.out.println("-------- Time not fresh: " + currentTime
					+ " is current, " + testTime + " is old!");
			return false;
		}
	}
	
	public static void main(String[] args) {
		Server s = new Server("test");
		s.showFile(".\\material\\file.txt");
	}
	
}
