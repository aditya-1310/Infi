import java.io.File;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

class Troop {
    int rate;
    int availableTime;

    public Troop(int rate) {
        this.rate = rate;
        this.availableTime = 0;
    }
}

class Ship {
    int id, strength, timeLimit;
    boolean destroyed;
    int timeTaken;

    public Ship(int id, int strength, int timeLimit) {
        this.id = id;
        this.strength = strength;
        this.timeLimit = timeLimit;
        this.destroyed = false;
        this.timeTaken = 0;
    }
}

public class Main {
    private static List<Troop> troops = new ArrayList<>();
    private static List<Ship> ships = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <input.xml>");
            return;
        }

        String xmlFile = args[0];
        try {
            parseXML(xmlFile);
            processShips();
        } catch (Exception e) {
            System.out.println("Error processing XML: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("exit")) {
                break;
            } else if (userInput.equalsIgnoreCase("print destroyed ships")) {
                printDestroyedShips();
            } else if (userInput.equalsIgnoreCase("print remaining ships")) {
                printRemainingShips();
            } else {
                System.out.println("Unknown command. Use 'print destroyed ships', 'print remaining ships', or 'exit'.");
            }
        }
        scanner.close();
    }

    private static void parseXML(String xmlFile) throws Exception {
        File file = new File(xmlFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList troopNodes = doc.getElementsByTagName("troop");
        for (int i = 0; i < troopNodes.getLength(); i++) {
            Element troopElement = (Element) troopNodes.item(i);
            int rate = Integer.parseInt(troopElement.getElementsByTagName("rate").item(0).getTextContent());
            troops.add(new Troop(rate));
        }

        NodeList shipNodes = doc.getElementsByTagName("ship");
        for (int i = 0; i < shipNodes.getLength(); i++) {
            Element shipElement = (Element) shipNodes.item(i);
            int id = Integer.parseInt(shipElement.getElementsByTagName("id").item(0).getTextContent());
            int strength = Integer.parseInt(shipElement.getElementsByTagName("strength").item(0).getTextContent());
            int timeLimit = Integer.parseInt(shipElement.getElementsByTagName("time-limit").item(0).getTextContent());
            ships.add(new Ship(id, strength, timeLimit));
        }
    }

    private static void processShips() {
        ships.sort(Comparator.comparingInt((Ship s) -> s.timeLimit).thenComparingInt(s -> s.id));
        for (Ship ship : ships) {
            if (ship.timeLimit == 0) continue;
            int requiredRate = (ship.strength + ship.timeLimit - 1) / ship.timeLimit;

            List<Troop> possibleTroops = new ArrayList<>();
            for (Troop troop : troops) {
                if (troop.rate >= requiredRate) {
                    possibleTroops.add(troop);
                }
            }
            if (possibleTroops.isEmpty()) continue;
            
            possibleTroops.sort(Comparator.comparingInt((Troop t) -> -t.rate).thenComparingInt(t -> t.availableTime));
            for (Troop troop : possibleTroops) {
                int duration = (ship.strength + troop.rate - 1) / troop.rate;
                int latestStart = ship.timeLimit - duration;
                if (latestStart < 0) continue;
                int possibleStart = Math.max(troop.availableTime, 0);
                if (possibleStart <= latestStart) {
                    ship.destroyed = true;
                    ship.timeTaken = duration;
                    troop.availableTime = possibleStart + duration;
                    break;
                }
            }
        }
    }

    private static void printDestroyedShips() {
        List<Ship> destroyedShips = new ArrayList<>();
        for (Ship ship : ships) {
            if (ship.destroyed) {
                destroyedShips.add(ship);
            }
        }
        destroyedShips.sort(Comparator.comparingInt(s -> s.id));
        System.out.println(destroyedShips.size() + ", " + destroyedShips.stream().map(s -> "(" + s.id + ", " + s.timeTaken + ")").toList());
    }

    private static void printRemainingShips() {
        List<Ship> remainingShips = new ArrayList<>();
        for (Ship ship : ships) {
            if (!ship.destroyed) {
                remainingShips.add(ship);
            }
        }
        remainingShips.sort(Comparator.comparingInt(s -> s.id));
        System.out.println(remainingShips.size() + ", " + remainingShips.stream().map(s -> "(" + s.id + ", " + s.strength + ")").toList());
    }
}
