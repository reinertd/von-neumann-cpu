Hinweise zum CPU-Simulator:

Inhalt:
 
1) Voraussetzungen

2) Start des Programms

3) Zur Benutzung des Programms


zu 1)

Zum Ausfuehren des Programms benoetigt man einen Computer mit installierter Java Virtual Maschine wie z.B. JDK Version 1.2 ( Java-Developement-Kit, eventuell funktioniert auch eine aeltere Version).
Die meissten Rechner mit Installiertem Windows 95/98 besitzen eine Java VM im Windows Systemverzeichnis. ( z.B.: C:\Windows\System\ )
Falls noch kein Java auf dem Computer installiert ist, kann man die JDK im Internet unter der Adresse "http://java.sun.com/products/jdk/1.2/" kostenlos heruterladen ( nicht zu empfehlen, das 20 MB gross ) oder die Internet-CD des Rechenzentrums kaufen, auf der beim Internetzubehoer auch die JDK ist ( kostet 5 DM ), oder man kann eine neue Version des Windows Explorers installieren, die Java mitliefert (IE 4.0) 
Java laeuft auf allen neueren Rechnern sowohl mit Windows 95 als auch mit Linux oder auf einem Mac. Bei aelteren Rechnern (486er o.a. ) laeuft wahrscheinlich nur eine aeltere Version, die aber wahrscheinlich auch funktioniert.

zu 2) 

Unter der Voraussetzung, das Java auf dem Computer korrekt (insbesondere mit richtigem Classpath) installiert ist, und sich im Path befindet, startet man den CPU-Simulator mit folgendem Befehl (unter Windows 95 im Dos-Fenster):

java -cp . usecpu

dabei muss man sich in einem Verzeichniss befinden, in dem die Dateien usecpu.class, CPU.class, MEMORY.class, ALU.class sowie REGISTER.class liegen.
Sollte java von diesem Verzeichniss aus nicht erreichbar sein, muss der Pfad in dem Java installiert wurde mit angegeben werden. Falls Java im Verzeichnis C:\Programme\jdk1.2\ befindet :

c:\Programme\jdk1.2\bin\java -cp . usecpu

Oder falls man das Java von Windows benutzen moechte:

c:\windows\system\java -cp . usecpu

zu 3)

Das Programm meldet sich mit folgendem Promt:

USECPU>

Jetzt sind folgende Befehle moeglich: ( weitere Erlaeuterungen spaeter )
 e [Line]              : Editiere den Speicher angefangen mit Zeile Line (Edit)
 m [line1[,line2]]     : Zeigt den Speicher von Line1 bis Line2 (Memory)
 c [line]              : Startet das Programm beginnend mit Speicherzelle Line (falls Line nicht angegeben wird geht es bei der Speichestelle 
                         weiter, an der zuletzt gestoppt wurde. ) (Continue)
 v [line[,wait]]       : Startet das Programm beginnend mit Speicherzelle Line und zeigt dabei bei jedem "Schritt" die Registerinhalte an.
                         Der Parameter wait bestimmt die Zeitdauer, die zwischen zwei Schritten gewartet wird. (Je groesser wait desto laenger)
                         (View)
 s [line[,wait]]       : "Step one Line": Fuehre den Befehl an Speicherstelle Line oder der Letzten Speicherposition aus. (Step)
 b line                : Fuege einen Haltepunkt in Zeile line ein. Die CPU wird nun immer stoppen, wenn sie zu dieser Zeile kommt. (Breakpoint)
 d line                : Loesche den Haltepunkt in Zeile line. Die CPU wird nun nicht mehr in dieser Zeile stoppen. ( Delete Breakpoint)
 r                     : Zeige die Registerinhalte an. (Register)
 q                     : Beende das Programm (Quit)

Wenn eine leere Zeile oder ein ungueltiger Befehl eingegeben wird, wird eine Hilfe angezeigt.
ACHTUNG: Die Befehle muessen exakt so eingegeben werden, wie oben aufgefuehrt. Gross und Kleinschreibung wird nicht!! ignoriert. D.h. alle Befehle,
die im "USECPU>"-Promt eigegeben werden (und nicht in den Speicher) muessen klein!! geschrieben werden. Ausserdem darf zwischen Befehl und Parameter nur exakt ein Leerzeichen sein. Am Ende eines Befehls darf kein weiteres Leerzeichen stehen.

Ein kleiner Beispieldialog:

USECPU> e 0
 Type in Commands and Data: (Empty Line to Exit, END to Exit and insert endless-slope)
0 : 1
1 : 2
2 : 0
3 : LOAD 0
4 : ADD 1
5 : STORE 2
6 : END
Adding endless-slope at the End of the Program!
USECPU> c 3
Starting CPU at Line 3
Stopped at Line 
6
USECPU> m 2,2
Speicherinhalt: 
2 : LOAD 3 : 3

Der Befehl e 0 ermoeglicht eine Speichereditierung. Die Daten und Befehle eines kleinen Programmes werden nun in den Speicher geschrieben. Das Programm addiert einfach nur die Speicherstelle 0 und die Speicherstelle 1 und Speichert dieses in 2 ab. Mit dem "uneigentlichen" Befehl END wird die Speichereditierung beendet, und das ¨USECPU>"-Promt erscheint. Mit c 3 wird die CPU in Zeile 3 gestartet. Sie stoppt automatisch bei dem "END" Befehl. Mit m 2,2 wird nun der Specherinhalt von Speicherstelle 2 angezeigt, welcher wie zu erwarten 3 = 1+2 betraegt.
(Nun koennte man die CPU mit q beenden.)

Zum Befehl e:
Mit dem Befehl e Line editiert man den Speicher beginnend mit Zeile Line. Fehlt die Zeilenangabe, so startet das Editieren mit Zeile 0. Vor jeder Eingabe wird die aktuelle Zeilennummer angezeigt. Als Eingabe ist ein CPU-Befehl mit Adresse oder eine Zahl moeglich. Da die CPU intern nicht zwischen Zahlen und Befehlen unterscheidet, wird auch ein Befehl letztendlich in eine Zahl umgewandelt. Die Eingabe von Zahlen kann nur in Dezimaler Form erfolgen. Die CPU kennt die in der Vorlesung Besprochenen Befehle LOAD, STORE, AND, ADD, JUMP, JUMPZ, COMP und RSHIFT. Alle Befehle muessen gross eingegeben werden. Es ist nicht erlaubt die Adresse bei den Befehlen COMP bzw. RSHIFT wegzulassen, dh. jeder Befehl besteht aus einem Befehl und einer Adresse. Soll kein Speicher mehr editiert werden, kann entweder der Befehl END oder eine leere Zeile eingegeben werden. Der Befehl END veranlasst den Speichereditor eine Endlosschleife an das Ende der Befehle zu setzen, der die CPU dort zum Halten bringt, da sie erkennt, dass sich keine Register mehr aendern. Wird nur eine leere Zeile eingegeben, so bleibt die Speicherstelle, in der man die leere Zeile eingegeben hat unveraendert. Aber Achtung: Wenn kein END Befehl am Ende des Programmes steht und nicht noch irgendwo ein Haltepunkt gesetzt ist, wird die CPU den kompletten Speicher (8192 Speicherstellen) durchlaufen und danach springt der Programmzaehler wieder auf Speicherstelle 0 und der Speicher wird erneut durchlaufen. Das heisst die CPU wird nicht mehr stoppen. Moechte man allerdings ein Bestehenden Speicherinhalt editieren, so ist es sinnvoll nur eine leere Zeile am Ende einzugeben um den urspruenglichen Speicherinhalt nicht zu zerstoeren.  

Zum Befehl m:
Der Befehl m Line1,Line2 ermoeglicht ein anzeigen, des Speicherinhaltes von Line1 bis Line2. Wird kein Parameter angegeben, so wird der Speicherinhalt von 0 bis zur zuletzt editierten Zeile angezeigt. Es wird der Speicherinhalt sowohl als Befehl, als auch als Zahl angezeigt.

Zu den Befehlen c,v,s:
Sowohl c Line, als auch v Line und s Line ermoeglichen, die Ausfuehrung des Befehls in der Speicherstelle Line von der CPU. Wird keine Adresse angegeben, so wird die Zeile ausgefuehrt, an der die CPU zuletzt gestoppt hat. s fuehrt nur eine Zeile aus. c und v fuehren so lange weiterhin die nachfolgenden Zeilen aus, bis sie auf einen Haltepunkt stossen oder sich der Registerinhalt nicht mehr aendert. Dies geschieht eigentlich nur, wenn die CPU auf eine Endlosschleife trifft, die nur einen Befehl lang ist, dh. der Befehl, der Bedingt oder unbedingt zu sich selbst Springt (JUMP 3 falls der Befehl in Zeile 3 steht) Diese Endlosschleife wird im Speichereditor durch den Befehl END generiert. Der unterschied zwischen c und v besteht darin, das v zwischen jedem (CPU-)Befehl die Register anzeigt. 

Zu den Befehlen b,d:
b Line setzt eine Haltemarke in der Zeile Line und d Line loescht sie wieder. Tip: Wenn man ein neues Programm ausfuehrt sollte man irgendwo am Ende des Speichers einen Breakpoint setzen, um, falls ein Jump-Befehl aus dem Programm irgendwo hinausspringt, die CPU spaetestens dort zum Halten zu bringen.

Zum Befehl q:
Mit q Beendet man das Programm.

Andre Hinweise:
Obwohl es nicht moeglich ist Dateien direckt in den Speicher der CPU zu laden, kann man dies mit einem einfachen Trick unter Windows 95 oder Unix erreichen: Man zeigt den Inhalt der auszufuehrenden Datei an, und leitet die Ausgabe an die Eingabe des CPU-Programmes weiter: 
Beispiel:

Wenn es z.B. eine Datei "and.cpu" mit folgendem Inhalt gibt:

e 0
1
2
0
LOAD 0
ADD 1
STORE 2
END
c 3
m 2,2
q

kann man diese Datei mit dem Befehl:

more and.cpu|java -cp . usecpu

"ausfuehren". Unter Dos ist evtl. der Befehl Type besser zum anzeigen der Dateien geeignet und unter Unix sollte man cat nehmen.
