/*
Sample 1.
Input:
5 7
1 2 2
2 5 5
1 3 6
3 4 2
4 5 1
3 2 3
2 4 1
Output:
6


Sample 2.
Input:
4 5
1 2 10000
1 3 10000
2 3 1
3 4 10000
2 4 10000
Output:
20000



*/

import java.io.*;
import java.util.*;

public class Evacuation {
    private static FastScanner in;

    public static void main(String[] args) throws IOException {
        in = new FastScanner();

        FlowGraph graph = readGraph();
        System.out.println(maxFlow(graph, 0, graph.size() - 1));
    }

    private static int maxFlow(FlowGraph graph, int from, int to) {
        int flow = 0;
        boolean existAugmentingPath = true;
        while (existAugmentingPath) {
            Map<Integer, Integer> prev = bfs(graph, from, to);
            if (prev.get(to) == null) break;
            existAugmentingPath = recalculateFlow(graph, to, prev);
        }

        for (Integer edge : graph.getIds(from)) {
            Edge current = graph.getEdge(edge);
            if (current.capacity > 0) flow += current.flow;
        }
        return flow;
    }

    private static Map<Integer, Integer> bfs(FlowGraph graph, int from, int to) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(from);
        boolean[] visited = new boolean[graph.size()];
        Map<Integer, Integer> prev = new HashMap<>();

        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            visited[current] = true;
            List<Integer> edges = graph.getIds(current);

            for (Integer edgeId : edges) {
                Edge edge = graph.getEdge(edgeId);
                if (!visited[edge.to] && edge.capacity > edge.flow) {
                    prev.put(edge.to, current);
                    queue.add(edge.to);
                    if (edge.to == to) break;
                }
            }
        }
        return prev;
    }

    private static boolean recalculateFlow(FlowGraph graph, int to, Map<Integer, Integer> prev) {
        int current = to;
        int minCap = Integer.MAX_VALUE;
        List<Integer> path = new LinkedList<>();
        while (true) {
            int previous = prev.get(current);
            List<Integer> edges = graph.getIds(previous);
            for (Integer edgeId : edges) {
                Edge edge = graph.getEdge(edgeId);
                if (edge.to == current && edge.from == previous && edge.capacity > edge.flow) {
                    path.add(0, edgeId);
                    minCap = Math.min(minCap, edge.capacity - edge.flow);
                    break;
                }
            }
            current = previous;
            if (previous == 0) break;
        }

        for (Integer edge : path) {
            graph.addFlow(edge, minCap);
        }
        return (minCap < Integer.MAX_VALUE && minCap > 0);
    }

    static FlowGraph readGraph() throws IOException {
        int vertex_count = in.nextInt();
        int edge_count = in.nextInt();
        FlowGraph graph = new FlowGraph(vertex_count);

        for (int i = 0; i < edge_count; ++i) {
            int from = in.nextInt() - 1, to = in.nextInt() - 1, capacity = in.nextInt();
            graph.addEdge(from, to, capacity);
        }
        return graph;
    }

    static class Edge {
        int from, to, capacity, flow;

        public Edge(int from, int to, int capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }
    }

    /* This class implements a bit unusual scheme to store the graph edges, in order
     * to retrieve the backward edge for a given edge quickly. */
    static class FlowGraph {
        /* List of all - forward and backward - edges */
        private List<Edge> edges;

        /* These adjacency lists store only indices of edges from the edges list */
        private List<Integer>[] graph;

        public FlowGraph(int n) {
            this.graph = (ArrayList<Integer>[]) new ArrayList[n];
            for (int i = 0; i < n; ++i)
                this.graph[i] = new ArrayList<>();
            this.edges = new ArrayList<>();
        }

        public void addEdge(int from, int to, int capacity) {
            /* Note that we first append a forward edge and then a backward edge,
             * so all forward edges are stored at even indices (starting from 0),
             * whereas backward edges are stored at odd indices. */
            Edge forwardEdge = new Edge(from, to, capacity);
            Edge backwardEdge = new Edge(to, from, 0);
            graph[from].add(edges.size());
            edges.add(forwardEdge);
            graph[to].add(edges.size());
            edges.add(backwardEdge);
        }

        public int size() {
            return graph.length;
        }

        public List<Integer> getIds(int from) {
            return graph[from];
        }

        public Edge getEdge(int id) {
            return edges.get(id);
        }

        public void addFlow(int id, int flow) {
            /* To get a backward edge for a true forward edge (i.e id is even), we should get id + 1
             * due to the described above scheme. On the other hand, when we have to get a "backward"
             * edge for a backward edge (i.e. get a forward edge for backward - id is odd), id - 1
             * should be taken.
             *
             * It turns out that id ^ 1 works for both cases. Think this through! */
            edges.get(id).flow += flow;
            edges.get(id ^ 1).flow -= flow;
        }
    }

    static class FastScanner {
        private BufferedReader reader;
        private StringTokenizer tokenizer;

        public FastScanner() {
            reader = new BufferedReader(new InputStreamReader(System.in));
            tokenizer = null;
        }

        public String next() throws IOException {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                tokenizer = new StringTokenizer(reader.readLine());
            }
            return tokenizer.nextToken();
        }

        public int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }
}
