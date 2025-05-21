package yutorinatsu;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.awt.Desktop;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * main entry.
 */
public final class Main {
    private Main() { }
    static class Graph {
    	/** graph structure. */
        public Map<String, Map<String, Integer>> adjMap = new HashMap<>();
        public void addEdge(final String source, final String destination) {
            adjMap.putIfAbsent(source, new HashMap<>());
            adjMap.putIfAbsent(destination, new HashMap<>());
            adjMap.get(source).put(destination, adjMap.get(source).getOrDefault(destination, 0) + 1);
        }
    }
    /** */
    private static Scanner scanner = new Scanner(System.in, "UTF-8");

    /**
     * function parse file and add to the graph.
     * @param filename
     * @param graph
     */
    static void addFile(final String filename, final Graph graph) {
        File file = new File(filename);
        if (!file.exists() || !file.canRead()) {
            System.out.println("File does not exist or cannot be read.");
            return;
        }
        try (Scanner fileScanner = new Scanner(file, "UTF-8")) {
            String previousWord = null;
            while (fileScanner.hasNext()) {
                String currentWord = fileScanner.next();
                if (previousWord != null) {
                    previousWord = previousWord.replaceAll("\\p{Punct}", "");
                    currentWord = currentWord.replaceAll("\\p{Punct}", "");
                    if (!previousWord.isEmpty() && !currentWord.isEmpty()) {
                    	graph.addEdge(previousWord.toLowerCase(), currentWord.toLowerCase());
                    }
                }
                previousWord = currentWord;
            }
        } catch (Exception e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
        }
    }

    /**
     * function render directed graph.
     * @param graph
     */
    static void showDirectedGraph(final Graph graph) {
        try {
            guru.nidi.graphviz.model.MutableGraph toolgraph = guru.nidi.graphviz.model.Factory.mutGraph("Graph").setDirected(true);

            for (Map.Entry<String, Map<String, Integer>> entry : graph.adjMap.entrySet()) {
            String source = entry.getKey();
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                String destination = edge.getKey();
                int weight = edge.getValue();
                toolgraph.add(guru.nidi.graphviz.model.Factory.mutNode(source).addLink(guru.nidi.graphviz.model.Factory.to(guru.nidi.graphviz.model.Factory.mutNode(destination)).with("label", String.valueOf(weight))));
            }
            }
            // Render the graph to a file
            File outputFile = new File("graph.png");
            guru.nidi.graphviz.engine.Graphviz.fromGraph(toolgraph).render(guru.nidi.graphviz.engine.Format.PNG).toFile(outputFile);
            System.out.println("Graph rendered to graph.png");

            // Open the graph.png in a new window
            if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(outputFile);
            } else {
            System.out.println("Desktop is not supported. Please open graph.png manually.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred while rendering the graph: " + e.getMessage());
        }
    }

    /**
     * function finds bridge words.
     * @param word1
     * @param word2
     * @param graph
     * @return List<String> of bridge words
     */
    static List<String> queryBridgeWords(final String word1, final String word2, final Graph graph) {
        boolean isWord1InGraph = graph.adjMap.containsKey(word1);
        boolean isWord2InGraph = graph.adjMap.containsKey(word2);
        if (!isWord1InGraph && !isWord2InGraph) {
            throw new IllegalArgumentException("No \"" + word1 + "\" and \"" + word2 + "\" in the graph!");
        }
        if (!isWord1InGraph) {
            throw new IllegalArgumentException("No \"" + word1 + "\" in the graph!");
        }
        if (!isWord2InGraph) {
            throw new IllegalArgumentException("No \"" + word2 + "\" in the graph!");
        }

        List<String> result = new ArrayList<>();
        Map<String, Integer> neighborsOfWord1 = graph.adjMap.get(word1);

        for (String intermediate : neighborsOfWord1.keySet()) {
            if (graph.adjMap.get(intermediate).containsKey(word2)) {
                result.add(intermediate);
            }
        }

        return result;
    }

    /**
     * function inserts bridge words.
     * @param inputText
     * @param graph
     * @return String sentence
     */
    static String generateNewText(final String inputText, final Graph graph) {
        String result = "";
        String[] words = inputText.split(" ");
        String previousWord = null;
        for (String currentWord: words) {
            if (previousWord != null) {
                String cleanedPrev = previousWord.replaceAll("\\p{Punct}", "").toLowerCase();
                String cleanedCur = currentWord.replaceAll("\\p{Punct}", "").toLowerCase();

                // Check for a bridge word
                if (graph.adjMap.containsKey(cleanedPrev)) {
                    for (String intermediate : graph.adjMap.get(cleanedPrev).keySet()) {
                        if (graph.adjMap.containsKey(intermediate) && graph.adjMap.get(intermediate).containsKey(cleanedCur)) {
                            result += intermediate;
                            result += " ";
                            break; // Insert only one bridge word
                        }
                    }
                }
            }
            previousWord = currentWord;
            result += previousWord;
            result += " ";
        }
        return result;
    }

    /**
     * function gets shortest path of words.
     * @param word1
     * @param word2
     * @param graph
     */
    static void calcShortestPath(final String word1, final String word2, final Graph graph) {
        boolean isWord1InGraph = graph.adjMap.containsKey(word1);
        boolean isWord2InGraph = graph.adjMap.containsKey(word2);
        if (!isWord1InGraph && !isWord2InGraph) {
            throw new IllegalArgumentException("No \"" + word1 + "\" and \"" + word2 + "\" in the graph!");
        }
        if (!isWord1InGraph) {
            throw new IllegalArgumentException("No \"" + word1 + "\" in the graph!");
        }
        if (!isWord2InGraph) {
            throw new IllegalArgumentException("No \"" + word2 + "\" in the graph!");
        }
        // Dijkstra's algorithm
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(Comparator.comparing(Map.Entry::getValue));
        // Initialize distances
        for (String node : graph.adjMap.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);
        pq.add(Map.entry(word1, 0));

        while (!pq.isEmpty()) {
            String current = pq.poll().getKey();

            // Check neighbors
            for (Map.Entry<String, Integer> neighbor : graph.adjMap.get(current).entrySet()) {
                String neighborNode = neighbor.getKey();
                int weight = neighbor.getValue();
                int newDist = distances.get(current) + weight;

                if (newDist < distances.get(neighborNode)) {
                    distances.put(neighborNode, newDist);
                    previous.put(neighborNode, current);
                    pq.add(Map.entry(neighborNode, newDist));
                }
            }
        }

        // Reconstruct the shortest path
        List<String> path = new ArrayList<>();
        for (String at = word2; at != null; at = previous.get(at)) {
            path.add(0, at);
        }

        if (!path.get(0).equals(word1)) {
            System.out.println("No path exists from \"" + word1 + "\" to \"" + word2 + "\".");
            return;
        }

        // Convert path to String[]
        String[] result = path.toArray(new String[0]);
        System.out.println(String.join(" -> ", result) + " (" + distances.get(word2) + ")");
    }

    /**
     * returns pagerank value.
     * @param word
     * @param graph
     * @return Double pagerank
     */
    static Double calPageRank(final String word, final Graph graph) {
        final double dampingFactor = 0.85; // 阻尼因子
        final double epsilon = 1e-6; // 收敛阈值
        int numNodes = graph.adjMap.size(); // 节点总数

        // 检查节点是否在图中
        if (!graph.adjMap.containsKey(word)) {
            throw new IllegalArgumentException("No \"" + word + "\" in the graph!");
        }

        // 初始化 PageRank 值
        Map<String, Double> pageRank = new HashMap<>();
        for (String node : graph.adjMap.keySet()) {
            pageRank.put(node, 1.0 / numNodes);
        }

        boolean converged = false;
        while (!converged) {
            Map<String, Double> newPageRank = new HashMap<>();
            double diff = 0.0;

            // 计算悬挂节点的总 PageRank 值
            double danglingRank = 0.0;
            for (String node : graph.adjMap.keySet()) {
                if (graph.adjMap.get(node).isEmpty()) { // 出度为 0 的节点
                    danglingRank += pageRank.get(node);
                }
            }

            // 计算每个节点的新 PageRank 值
            for (String node : graph.adjMap.keySet()) {
                double rank = (1 - dampingFactor) / numNodes;

                // 加上悬挂节点的贡献
                rank += dampingFactor * danglingRank / numNodes;

                // 加上其他节点的贡献
                for (Map.Entry<String, Map<String, Integer>> entry : graph.adjMap.entrySet()) {
                    String neighbor = entry.getKey();
                    Map<String, Integer> edges = entry.getValue();

                    if (edges.containsKey(node)) { // 如果邻居指向当前节点
                        rank += dampingFactor * pageRank.get(neighbor) / edges.size();
                    }
                }
                newPageRank.put(node, rank);
                diff += Math.abs(rank - pageRank.get(node));
            }

            // 更新 PageRank 值
            pageRank = newPageRank;

            // 检查是否收敛
            if (diff < epsilon) {
                converged = true;
            }
        }

        // 返回指定节点的 PageRank 值
        return pageRank.getOrDefault(word, 0.0);
    }

    /** */
    private static int randomwalkinterval = 125;
    /**
     * function walk randomly to generate sentence.
     * @param graph
     * @return String sentence
     */
    static String randomWalk(final Graph graph) {
        if (graph.adjMap.isEmpty()) {
            throw new IllegalArgumentException("The graph is empty.");
        }

        StringBuilder result = new StringBuilder();
        Random random = new Random();
        Map<String, Map<String, Integer>> visitedEdges = new HashMap<>();

        // 随机选择起点
        String currentNode = graph.adjMap.keySet().toArray(new String[0])[random.nextInt(graph.adjMap.size())];
        result.append(currentNode);
        try {
            while (true) {
                try {
                    if (System.in.available() > 0) {
                        String userInput = scanner.nextLine();
                        if (userInput.equalsIgnoreCase("q")) {
                            result.append(" (stopped by user)");
                            break;
                        }
                    }
                } catch (Exception e) {
                    // System.out.println(e.getMessage());
                }
                Map<String, Integer> neighbors = graph.adjMap.get(currentNode);

                // 如果当前节点没有出边，结束遍历
                if (neighbors.isEmpty()) {
                    // result.append(" (no outgoing edges)");
                    break;
                }
                // 初始化当前节点的访问记录
                visitedEdges.putIfAbsent(currentNode, new HashMap<>());

                // 随机选择一条未重复的边
                String nextNode = null;
                for (String neighbor : neighbors.keySet()) {
                    if (!visitedEdges.get(currentNode).containsKey(neighbor)) {
                        nextNode = neighbor;
                        break;
                    }
                }

                // 如果所有边都已访问过，结束遍历
                if (nextNode == null) {
                    // result.append(" (all edges visited)");
                    break;
                }

                // 记录边的访问
                visitedEdges.get(currentNode).put(nextNode, 1);

                // 更新结果并移动到下一个节点
                result.append(" -> ").append(nextNode);
                currentNode = nextNode;

                // 模拟遍历延迟
                Thread.sleep(randomwalkinterval); // 每次遍历等待 500 毫秒
            }
        } catch (InterruptedException e) {
            result.append(" (interrupted)");
        }

        // 将结果写入文件
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("random_walk_result.txt"), StandardCharsets.UTF_8)) {
            writer.write(result.toString());
        } catch (IOException e) {
            System.out.println("Failed to write the result to file: " + e.getMessage());
        }
        return result.toString();
    }

    /**
     * main function entry.
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        int choice = 0;
        Graph graph = new Graph();
        String word1;
        String word2;
        while (true) {
            System.out.println("Choose an option:");
            System.out.println("1. Import text from file");
            System.out.println("2. Show graph");
            System.out.println("3. Get bridge words");
            System.out.println("4. Generate new text from bridge words");
            System.out.println("5. Get shortest path");
            System.out.println("6. Calculate PageRank");
            System.out.println("7. Do some random walk");
            System.out.println("8. Exit");
            boolean validInput = false;
            while (!validInput) {
                try {
                    choice = scanner.nextInt();
                    validInput = true;
                } catch (Exception e) {
                    System.out.println("Invalid option.");
                    scanner.nextLine();
                    validInput = false;
                }
            }
            switch (choice) {
                case 1:
                    System.out.println("Choose your file:");
                    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                    fileChooser.setCurrentDirectory(new java.io.File(System.getProperty("user.dir")));
                    int returnValue = fileChooser.showOpenDialog(null);
                    if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION) {
                        String filename = fileChooser.getSelectedFile().getAbsolutePath();
                        addFile(filename, graph);
                        System.out.println("Parsed file successfully.");
                    } else {
                        System.out.println("No file selected.");
                    }
                    break;
                case 2:
                    showDirectedGraph(graph);
                    System.out.println("Graph was saved to graph.png");
                    break;
                case 3:
                    System.out.println("Choose your words to query:");
                    word1 = "";
                    word2 = "";
                    System.out.print("Word 1: ");
                    word1 = scanner.next();
                    System.out.print("Word 2: ");
                    word2 = scanner.next();
                    try {
                        List<String> result = queryBridgeWords(word1, word2, graph);
                        if (result.isEmpty()) {
                            System.out.println("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
                        } else {
                            System.out.print("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" ");
                            if (result.size() == 1) {
                                System.out.print("is: ");
                                System.out.print("\"" + result.get(0) + "\"");
                                System.out.println(".");
                            } else {
                                System.out.print("are: ");
                                for (int i = 0; i < result.size(); i++) {
                                    System.out.print(result.get(i));
                                    if (i == result.size() - 2) {
                                        System.out.print(", and ");
                                    } else if (i == result.size() - 1) {
                                        System.out.println(".");
                                    } else {
                                        System.out.print(", ");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 4:
                    System.out.println("Input your sentence to query:");
                    scanner.nextLine();
                    String sentence = scanner.nextLine();
                    String result = generateNewText(sentence, graph);
                    System.out.println(result);
                    break;
                case 5:
                    System.out.println("Choose your words to query:");
                    word1 = "";
                    word2 = "";
                    System.out.print("Word 1: ");
                    word1 = scanner.next();
                    System.out.print("Word 2: ");
                    word2 = scanner.next();
                    try {
                        calcShortestPath(word1, word2, graph);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 6:
                    System.out.println("Choose your word to query:");
                    word1 = "";
                    System.out.print("Word: ");
                    word1 = scanner.next();
                    try {
                        Double resultPageRank = calPageRank(word1, graph);
                        System.out.println("The PR value of word \"" + word1 + "\" is " + resultPageRank + ".");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 7:
                    System.out.println("Generating random sentence:");
                    try {
                        String resultWalk = randomWalk(graph);
                        System.out.println("The sentence generated is: " + resultWalk);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 8:
                    scanner.close();
                    System.out.println("Bye~");
                    return;
                default:
                    System.out.println("Undefined option.");
                    break;
            }
        }
    }
}

// change 1?

// change 2
// aaaaaa
