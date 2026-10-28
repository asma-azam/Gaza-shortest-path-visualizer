package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MapFileReader {

    public static Graph readGraph(String filePath) {
        Graph graph = new Graph();

        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);

            int numberOfCities = scanner.nextInt();
            int numberOfEdges = scanner.nextInt();

            for (int i = 0; i < numberOfCities; i++) {
                String cityName = scanner.next();
                double latitude = scanner.nextDouble();
                double longitude = scanner.nextDouble();

                City city = new City(cityName, latitude, longitude);
                graph.addCity(city);
            }

            for (int i = 0; i < numberOfEdges; i++) {
                String sourceName = scanner.next();
                String destinationName = scanner.next();

                graph.addEdge(sourceName, destinationName);
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        } catch (Exception e) {
            System.out.println("Error while reading file: " + e.getMessage());
        }

        return graph;
    }
}