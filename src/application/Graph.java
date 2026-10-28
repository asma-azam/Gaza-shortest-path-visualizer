package application;

import java.util.ArrayList;
import java.util.HashMap;

public class Graph {

    private ArrayList<City> cities;
    private HashMap<String, City> cityMap;

    public Graph() {
        cities = new ArrayList<>();
        cityMap = new HashMap<>();
    }

    public void addCity(City city) {
        cities.add(city);
        cityMap.put(city.getName(), city);
    }

    public City getCity(String name) {
        return cityMap.get(name);
    }

    public ArrayList<City> getCities() {
        return cities;
    }

    public void addEdge(String sourceName, String destinationName) {
        City source = getCity(sourceName);
        City destination = getCity(destinationName);

        if (source == null || destination == null) {
            System.out.println("Error: City not found for edge: " 
                    + sourceName + " - " + destinationName);
            return;
        }

        Edge edge1 = new Edge(source, destination);
        source.addEdge(edge1);

        Edge edge2 = new Edge(destination, source);
        destination.addEdge(edge2);
    }

    public void printGraph() {
        for (City city : cities) {
            System.out.print(city.getName() + " -> ");

            for (Edge edge : city.getEdges()) {
                System.out.print(edge.getDestination().getName()
                        + "(" + String.format("%.2f", edge.getDistance()) + " km) ");
            }

            System.out.println();
        }
    }
}