package application;

public class Edge {
    private City source;
    private City destination;
    private double distance;

    public Edge(City source, City destination) {
        this.source = source;
        this.destination = destination;
        this.distance = calculateDistance(source, destination);
    }

    public City getSource() {
        return source;
    }

    public City getDestination() {
        return destination;
    }

    public double getDistance() {
        return distance;
    }

    private double calculateDistance(City c1, City c2) {
        double lat1 = c1.getLatitude();
        double lon1 = c1.getLongitude();
        double lat2 = c2.getLatitude();
        double lon2 = c2.getLongitude();

        double avgLat = Math.toRadians((lat1 + lat2) / 2.0);
        double dy = (lat2 - lat1) * 110.574;
        double dx = (lon2 - lon1) * 111.320 * Math.cos(avgLat);

        return Math.sqrt(dx * dx + dy * dy);
    }
    
    @Override
    public String toString() {
        return source.getName() + " -> " + destination.getName()
                + " (" + String.format("%.2f", distance) + " km)";
    }
}