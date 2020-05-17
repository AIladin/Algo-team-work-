import java.util.*;


class Main {
    private static class Coordinate {
        int x;
        int y;

        Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }

        int distance(Coordinate thr) {
            return Math.abs(x - thr.x) + Math.abs(y - thr.y);
        }

        @Override
        public boolean equals(Object o) {
            //if (this == o) return true;
            if (!(o instanceof Coordinate)) return false;
            Coordinate that = (Coordinate) o;
            return x == that.x &&
                    y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "{" + x + ',' + y + '}';
        }
    }

    private static class CoordinateComparator implements Comparator<Coordinate> {
        private final Coordinate end;
        private final Coordinate start;

        public CoordinateComparator(Coordinate start, Coordinate end) {
            assert end != null;
            this.end = end;
            this.start = start;
        }

        @Override
        public int compare(Coordinate ths, Coordinate thr) {
            return -Integer.compare(thr.distance(end) * 2 + thr.distance(start),
                                    ths.distance(end) * 2 + ths.distance(start));
        }
    }

    private static class Collision {
        private final HashMap<Integer, TreeSet<Integer>> xMap;
        private final HashMap<Integer, TreeSet<Integer>> yMap;

        Collision(Coordinate[] coordinates) {
            xMap = new HashMap<>();
            yMap = new HashMap<>();

            for (Coordinate coordinate: coordinates) {
                if (!xMap.containsKey(coordinate.x)) xMap.put(coordinate.x, new TreeSet<>());
                if (!yMap.containsKey(coordinate.y)) yMap.put(coordinate.y, new TreeSet<>());

                xMap.get(coordinate.x).add(coordinate.y);
                yMap.get(coordinate.y).add(coordinate.x);

            }

        }

        int xDistance(Coordinate thx, Coordinate end) {
            int distance = end.x - thx.x;
            TreeSet<Integer> xs = yMap.get(thx.y);
            if (xs != null) {
                Integer l = xs.lower(thx.x);
                Integer r = xs.higher(thx.x);

               if (l != null && l != thx.x && l != end.x && distance < 0) {
                   distance = -Math.min(Math.abs(distance), Math.abs(l - thx.x + 1));
               }

               if (r != null && r != thx.x && r != end.x && distance > 0) {
                   distance = Math.min(Math.abs(distance), Math.abs(r - thx.x - 1));
               }
            }

            return distance;
        }

        int yDistance(Coordinate thx, Coordinate end) {
            int distance = end.y - thx.y;
            TreeSet<Integer> xs = xMap.get(thx.x);
            if (xs != null) {
                Integer l = xs.lower(thx.y);
                Integer r = xs.higher(thx.y);

                if (l != null && l != thx.y && l != end.y && distance < 0) {
                    distance = -Math.min(Math.abs(distance), Math.abs(l - thx.y + 1));
                }

                if (r != null && r != thx.y && r != end.y && distance > 0) {
                    distance = Math.min(Math.abs(distance), Math.abs(r - thx.y - 1));
                }
            }

            return distance;

        }
    }

    private final Collision collision;
    private final Coordinate[] coordinates;
    private final int cutoff;
    private final static boolean debug = true;

    public Main(Scanner sc){

        int N = sc.nextInt();
        cutoff = N*N;
        coordinates = new Coordinate[N];

        for (int i=0; i<N; i++) {
            coordinates[i] = new Coordinate(sc.nextInt(), sc.nextInt());
        }

        collision = new Collision(coordinates);
    }

    private int relax(Coordinate start, Coordinate end, HashMap<Coordinate, Integer> relaxed) {


        System.out.println(start.toString() + "->" + end.toString());

        int k = 0;
        int prev_dist = 0;
        CoordinateComparator cComp = new CoordinateComparator(start, end);
        PriorityQueue<Coordinate> pq = new PriorityQueue<>(cComp);
        HashMap<Coordinate, Integer> distance = new HashMap<>();
        HashSet<Coordinate> visited = new HashSet<>();

        for (Coordinate coordinate: coordinates) {
            if (!(coordinate.equals(start) || coordinate.equals(end))){
                visited.add(coordinate);
            }
        }

        pq.add(start);
        distance.put(start, 0);

        while (!pq.isEmpty()) {

            Coordinate node = pq.poll();
            int dist = distance.get(node);

            if (visited.contains(node)) continue;
            visited.add(node);
            if (debug) System.out.println(node);
            //System.out.println(node.distance(start));

            if (node.equals(end)) return distance.get(end);

            if (node.distance(start) < dist && !relaxed.containsKey(node)) {
                if (debug) System.out.println("Relaxation start " +node.distance(start) + " " + dist);

                relaxed.put(node, dist);
                dist = relax(start, node, relaxed);
                distance.put(node, dist);
                relaxed.put(node, dist);

                if (debug) System.out.println("Relaxation end " + dist);

            } else if (relaxed.containsKey(node)) {
                dist = relaxed.get(node);
            }

            if (node.distance(end) < prev_dist) {
                k = 0;
                prev_dist = node.distance(end);
            }else k++;
            if (k>=cutoff) return -1;

            // Adding new nodes to pq.
            int xDist = collision.xDistance(node, end);
            int yDist = collision.yDistance(node, end);
            System.out.println("x=" + xDist + "y=" + yDist);

            //small steps
            Coordinate a = new Coordinate(node.x, node.y - 1);
            if (distance.getOrDefault(a, dist+2) > dist+1) {
                distance.put(a, dist + 1);
            }
            pq.add(a);
            a = new Coordinate(node.x, node.y + 1);
            if (distance.getOrDefault(a, dist+2) > dist+1) {
                distance.put(a, dist + 1);
            }
            pq.add(a);
            a = new Coordinate(node.x - 1, node.y);
            if (distance.getOrDefault(a, dist+2) > dist+1) {
                distance.put(a, dist + 1);
            }
            pq.add(a);
            a = new Coordinate(node.x + 1, node.y);
            if (distance.getOrDefault(a, dist+2) > dist+1) {
                distance.put(a, dist + 1);
            }
            pq.add(a);

            // big steps
            if (xDist != 0) {
                a = new Coordinate(node.x + xDist, node.y);
                if (distance.getOrDefault(a, dist+Math.abs(xDist)+1) > dist+Math.abs(xDist)) {
                    distance.put(a, dist + Math.abs(xDist));
                }
                pq.add(a);
            }

            if (yDist != 0) {
                a = new Coordinate(node.x, node.y + yDist);
                if (distance.getOrDefault(a, dist + Math.abs(yDist) +1) > dist+Math.abs(yDist)) {
                    distance.put(a, dist + Math.abs(yDist));
                }
                pq.add(a);
            }
        }
        return -1;
    }


    public int deliveryTime() {
        int time = 0;
        int tmp;

        for (int i = 1; i <= coordinates.length; i++) {
            tmp = relax(coordinates[i-1], coordinates[i % coordinates.length], new HashMap<>());
            System.out.println(tmp);
            if (tmp == -1) return -1;
            time += tmp;
        }
        return time;
    }


    public static void main(String[] args) {
        Main old = new Main(new Scanner(System.in));
        System.out.println(old.deliveryTime());
    }
}
