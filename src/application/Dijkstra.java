package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Dijkstra {

    public static class Result {
        private ArrayList<City> path;
        private double distance;

        public Result(ArrayList<City> path, double distance) {
            this.path = path;
            this.distance = distance;
        }

        public ArrayList<City> getPath() {
            return path;
        }

        public double getDistance() {
            return distance;
        }
    }

    private static class Node implements Comparable<Node> {
        private City city;
        private double distance;

        public Node(City city, double distance) {
            this.city = city;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public static Result findShortestPath(Graph graph, City source, City target) {

        HashMap<City, Double> distances = new HashMap<>();
        HashMap<City, City> previous = new HashMap<>();

        PriorityQueue<Node> pq = new PriorityQueue<>();

        for (City city : graph.getCities()) {
            distances.put(city, Double.MAX_VALUE);
            previous.put(city, null);
        }

        distances.put(source, 0.0);
        pq.add(new Node(source, 0.0));

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            City currentCity = currentNode.city;

            if (currentNode.distance > distances.get(currentCity)) {
                continue;
            }

            if (currentCity == target) {
                break;
            }

            for (Edge edge : currentCity.getEdges()) {
                City neighbor = edge.getDestination();

                double newDistance = distances.get(currentCity) + edge.getDistance();

                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, currentCity);
                    pq.add(new Node(neighbor, newDistance));
                }
            }
        }

        ArrayList<City> path = buildPath(previous, source, target);
        double totalDistance = distances.get(target);

        return new Result(path, totalDistance);
    }

    private static ArrayList<City> buildPath(HashMap<City, City> previous, City source, City target) {
        ArrayList<City> path = new ArrayList<>();

        City current = target;

        while (current != null) {
            path.add(current);

            if (current == source) {
                break;
            }

            current = previous.get(current);
        }

        Collections.reverse(path);

        if (path.isEmpty() || path.get(0) != source) {
            path.clear();
        }

        return path;
    }
}