package yutorinatsu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import yutorinatsu.Main.Graph;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.IOException;

class MainTest {

	private Graph createTestGraph(final String text) {
	    Graph graph = new Graph();
	    if (text == null || text.trim().isEmpty()) {
	        return graph;
	    }
	    
	    try (Scanner scanner = new Scanner(text)) {
	        String previousWord = null;
	        while (scanner.hasNext()) {
	            String currentWord = scanner.next();
	            if (previousWord != null) {
	                previousWord = previousWord.replaceAll("\\p{Punct}", "").toLowerCase();
	                currentWord = currentWord.replaceAll("\\p{Punct}", "").toLowerCase();
	                
	                if (!previousWord.isEmpty() && !currentWord.isEmpty()) {
	                    graph.addEdge(previousWord, currentWord);
	                }
	            }
	            previousWord = currentWord;
	        }
	    } catch (Exception e) {
	        System.err.println("Error creating test graph: " + e.getMessage());
	    }
	    
	    return graph;
	}
    
    @Test
    void test_sample1() {
        Graph graph = createTestGraph("a is b");
        
        List<String> expected = Arrays.asList("is");
        List<String> actual = yutorinatsu.Main.queryBridgeWords("a", "b", graph);
        
        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual, "应返回恰好一个桥接词");
    }

    @Test
    void test_sample2() {
        Graph graph = createTestGraph("a is b");
        
        List<String> expected = new ArrayList<>();
        List<String> actual = yutorinatsu.Main.queryBridgeWords("b", "a", graph);
        
        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual, "应返回一个空列表");
    }

    @Test
    void test_sample3() {
        Graph graph = createTestGraph("a is b and a are b");
        
        List<String> expected = Arrays.asList("is", "are");
        List<String> actual = yutorinatsu.Main.queryBridgeWords("a", "b", graph);
        
        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual, "应返回所有桥接词");
    }

    @Test
    void test_sample4() {
        Graph graph = createTestGraph("a is b");

        assertThrows(IllegalArgumentException.class, () -> {
            yutorinatsu.Main.queryBridgeWords("a", "c", graph);
        });
    }

    @Test
    void test_sample5() {
        Graph graph = createTestGraph("");

        assertThrows(IllegalArgumentException.class, () -> {
            yutorinatsu.Main.queryBridgeWords("a", "b", graph);
        });
    }

    @Test
    void test_sample6() {
        Graph graph = createTestGraph("a is b");

        assertThrows(IllegalArgumentException.class, () -> {
            yutorinatsu.Main.queryBridgeWords("123", "321", graph);
        });
    }
}
