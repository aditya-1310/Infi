import java.io.File;
import java.util.Scanner;
import org.w3c.dom.*;
import javax.xml.parsers.*;

public class Main {
    private static int totalSum = 0;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <input.xml>");
            return;
        }

        String xmlFile = args[0];
        try {