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

class MainTest2 {

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
        Graph graph = createTestGraph("a and b and c or d");
        
        assertThrows(IllegalArgumentException.class, () -> {
            yutorinatsu.Main.calcShortestPath("e", "d", graph);
        });
    }

    @Test
    void test_sample2() {
        Graph graph = createTestGraph("a and b and c or d");

        assertThrows(IllegalArgumentException.class, () -> {
            yutorinatsu.Main.calcShortestPath("d", "e", graph);
        });
    }

    @Test
    void test_sample3() {
        Graph graph = createTestGraph("a and b and c or d");
        
        int expected = 4;
        int actual = yutorinatsu.Main.calcShortestPath("a", "d", graph);
        assertEquals(expected, actual, "最短路长度不正确");
    }

    @Test
    void test_sample4() {
        Graph graph = createTestGraph("a and b and c or d");

        int expected = -1;
        int actual = yutorinatsu.Main.calcShortestPath("d", "a", graph);
        assertEquals(expected, actual, "最短路存在性判断不正确");
    }
}
