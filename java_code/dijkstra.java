import java.io.InputStream;
import java.util.*;

public class Main {
    private static class Coordinate implements Comparable<Coordinate>{
       final int x;
       final int y;

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }

        int distance(Coordinate thr) {
            return Math.abs(x - thr.x) + Math.abs(y - thr.y);
        }

        boolean inLine(Coordinate thr){return x == thr.x || y == thr.y;}

        @Override
        public boolean equals(Object o) {
            //if (this == o) return true;
            if (!(o instanceof Coordinate)) return false;
            Coordinate that = (Coordinate) o;
            return x == that.x &&
                    y == that.y;
        }

        @Override
        public String toString() {
            return "{" + x + ',' + y + '}';
        }


        @Override
        public int compareTo(Coordinate coordinate) {
            if (x > coordinate.x) return 1;
            else if (x == coordinate.x && y > coordinate.y) return 1;
            else if (x == coordinate.x && y == coordinate.y) return 0;
            else return -1;
        }
    }

    private static class Collision {
        private final HashMap<Integer, TreeSet<Integer>> xMap;
        private final HashMap<Integer, TreeSet<Integer>> yMap;

        Collision(Coordinate[] coordinates) {
            xMap = new HashMap<>();
            yMap = new HashMap<>();

            for (Coordinate coordinate : coordinates) {
                if (!xMap.containsKey(coordinate.x)) xMap.put(coordinate.x, new TreeSet<>());
                if (!yMap.containsKey(coordinate.y)) yMap.put(coordinate.y, new TreeSet<>());

                xMap.get(coordinate.x).add(coordinate.y);
                yMap.get(coordinate.y).add(coordinate.x);

            }

        }

        boolean noCollision(Coordinate a, Coordinate b) {
            TreeSet<Integer> set;
            int min;
            int max;
            if (a.x == b.x) {
                set = xMap.get(a.x);
                min = Math.min(a.y, b.y);
                max = Math.max(a.y, b.y);

            } else if (a.y == b.y) {
                set = yMap.get(a.y);
                min = Math.min(a.x, b.x);
                max = Math.max(a.x, b.x);
            } else throw new IllegalArgumentException();

            if (set == null) return true;

            Integer l = set.higher(min);
            if (l == null) l = - 1;
            Integer r = set.lower(max);
            if (r == null) r = max + 1;

            return (l <= min || l >= max) && (r <= min || r>= max);
        }

    }

    private static class EdgeWeightedGraph {
        final static class Edge implements Comparable<Edge>{
            private final Coordinate from;
            private final Coordinate to;
            private final int weight;

            public Edge (Coordinate to, Coordinate from, int weight) {
                this.from = from;
                this.to = to;
                this.weight = weight;
            }

            public Coordinate to() {
                return to;
            }

            public int weight() {
                return weight;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Edge)) return false;
                Edge edge = (Edge) o;
                return weight == edge.weight &&
                        Objects.equals(to, edge.to);
            }

            @Override
            public int hashCode() {
                return Objects.hash(to, weight);
            }

            @Override
            public String toString() {
                return "->" + to + ", w=" + weight;
            }

            @Override
            public int compareTo(Edge edge) {
                return to.compareTo(edge.to);
            }
            
        }

        private final HashMap<Coordinate, HashSet<Edge>> adj;

        EdgeWeightedGraph() {
            adj = new HashMap<>();
        }

        void addVertex(Coordinate vertex) {
            if (!adj.containsKey(vertex)) adj.put(vertex, new HashSet<>());
        }

        void addEdge(Coordinate a, Coordinate b, int weight) {
            if (adj.containsKey(a) && adj.containsKey(b)) {
                adj.get(a).add(new Edge(b, a, weight));
                adj.get(b).add(new Edge(a, b, weight));
            }
        }

        boolean containsVertex(Coordinate vertex) {
            return adj.containsKey(vertex);
        }

        HashSet<Edge> adjacent(Coordinate vertex) {
            assert containsVertex(vertex);
            return adj.get(vertex);
        }

    }

    class UndirectedDijkstra {
        private final PriorityQueue<Coordinate> pq;
        private final HashMap<Coordinate, Integer> distance;

        private class TComp implements Comparator<Coordinate> {

            @Override
            public int compare(Coordinate t, Coordinate t1) {
                return Integer.compare(distance.get(t), distance.get(t1));
            }
        }

        UndirectedDijkstra(Coordinate start, Coordinate end1, Coordinate end2) {
            pq = new PriorityQueue<>(new TComp());
            distance = new HashMap<>();
            //HashSet<Coordinate> visited = new HashSet<>();
            boolean first = false;
            boolean second = false;
            assert graph.containsVertex(start);

            for (Coordinate vertex: nodes) {
                distance.put(vertex, Integer.MAX_VALUE);
            }
            
            HashSet<Coordinate> visited = new HashSet<>(farms);
            visited.remove(start);
            visited.remove(end1);
            visited.remove(end2);

            distance.put(start, 0);
            pq.add(start);

            while (!pq.isEmpty()) {
                Coordinate vertex = pq.poll();
                if (visited.contains(vertex)) continue;

                visited.add(vertex);
                for (EdgeWeightedGraph.Edge edge: graph.adjacent(vertex)) {
                    if(!visited.contains(edge.to()) && !edge.from.equals(end1) && !edge.from.equals(end2)) {
                        relax(edge.to(), distance.get(vertex) + edge.weight());

                        if (edge.to() == end1) first = true;
                        if (edge.to() == end2) second = true;
                        if (first && second) return;
                    }
                }
            }
        }

        private void relax(Coordinate vertex, int dist) {
            if (distance.get(vertex) > dist) {
                distance.put(vertex, dist);
                pq.add(vertex);
            }
        }

        private int distance(Coordinate vertex) {
            return distance.get(vertex) == Integer.MAX_VALUE ? -1: distance.get(vertex);
        }
    }



    private final Collision collision;
    private final EdgeWeightedGraph graph;
    private final Coordinate[] order;
    private final HashSet<Coordinate> farms;
    private final HashSet<Coordinate> nodes;

    public Main(InputStream io) {
        Scanner sc = new Scanner(io);
        int n = sc.nextInt();
        order = new Coordinate[n];
        nodes = new HashSet<>();
        farms = new HashSet<>();
        for (int i = 0; i < n; i++) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            order[i] = new Coordinate(x, y);
            nodes.add(order[i]);
            farms.add(order[i]);
        }

        collision = new Collision(order);
        graph = new EdgeWeightedGraph();
        generateVertexesAdjEdges();
        generateExtEdges();
    }

    void generateVertexesAdjEdges() {
        for (Coordinate node: order) {
            graph.addVertex(node);
            nodes.add(node);
            for (int a = -1; a <= 1; a++)
                for (int b = -1; b <= 1; b++) {
                    if (a * b == 0 && (a != 0 || b != 0)) {
                        Coordinate adj = new Coordinate(node.x + a, node.y + b);
                        graph.addVertex(adj);
                        nodes.add(adj);
                        graph.addEdge(node, adj, node.distance(adj));
                    }
                }
        }
    }

    void generateExtEdges() {
        for (Coordinate a: nodes)
            for (Coordinate b: nodes) {
                if (a!=b && !farms.contains(a) && !farms.contains(b)) {
                    if (a.inLine(b) && collision.noCollision(a, b)) graph.addEdge(a, b, a.distance(b));
                    else if (!a.inLine(b)) {
                        Coordinate corner = new Coordinate(a.x, b.y);
                        if (collision.noCollision(a, corner) && collision.noCollision(b, corner) && !farms.contains(corner)) {
                            graph.addEdge(a, b, a.distance(b));
                            continue;
                        }
                        corner = new Coordinate(b.x, a.y);
                        if (collision.noCollision(a, corner) && collision.noCollision(b, corner) && !farms.contains(corner))
                            graph.addEdge(a, b, a.distance(b));
                    }
                }
            }
    }

    int solve() {
        int result = 0;
        int N = order.length;
        for (int i = 1; i < N; i+=2) {
            UndirectedDijkstra dijkstra = new UndirectedDijkstra(order[i % N], order[i-1], order[(i+1)%N]);
            if (dijkstra.distance(order[i - 1]) == -1) return -1;

            if (dijkstra.distance(order[(i + 1)%N]) == -1) return -1;
            result += dijkstra.distance(order[i - 1]) + dijkstra.distance(order[(i + 1)%N]);
        }
        if (N % 2 == 1) {
            UndirectedDijkstra dijkstra = new UndirectedDijkstra(order[N - 1], order[0], order[1]);
            if (dijkstra.distance(order[0]) == -1) return -1;
            result += dijkstra.distance(order[0]);
        }
        return result;
    }

    public static void main(String[] args) {
        Main main = new Main(System.in);
        System.out.println(main.solve());
    }
}
