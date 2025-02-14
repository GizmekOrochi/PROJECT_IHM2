package com.rpg.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BFSPathfinding {
    private static final int[][] DIRECTIONS = {
        {0, -1},
        {1, 0},
        {0, 1},
        {-1, 0}
    };

    public static List<int[]> findPath(int[][] matrix, int startX, int startY, int goalX, int goalY) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        if (!isValidTile(startX, startY, rows, cols) || !isValidTile(goalX, goalY, rows, cols)) {
            return Collections.emptyList();
        }
        if (matrix[startY][startX] == 1 || matrix[goalY][goalX] == 1) {
            return Collections.emptyList();
        }
        boolean[][] visited = new boolean[rows][cols];
        int[][] parent = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                parent[i][j] = -1;
            }
        }

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[] {startX, startY});
        visited[startY][startX] = true;
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];
            if (cx == goalX && cy == goalY) {
                return reconstructPath(parent, startX, startY, goalX, goalY, cols);
            }
            for (int[] dir : DIRECTIONS) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (isValidTile(nx, ny, rows, cols) && !visited[ny][nx] && matrix[ny][nx] == 0) {
                    visited[ny][nx] = true;
                    parent[ny][nx] = cy * cols + cx;
                    queue.offer(new int[] {nx, ny});
                }
            }
        }
        return Collections.emptyList();
    }

    private static List<int[]> reconstructPath(int[][] parent, int startX, int startY, int goalX, int goalY, int cols) {
        List<int[]> path = new ArrayList<>();
        int gx = goalX;
        int gy = goalY;
        while (!(gx == startX && gy == startY)) {
            path.add(new int[] {gx, gy});
            int p = parent[gy][gx];
            gy = p / cols;
            gx = p % cols;
        }
        path.add(new int[] {startX, startY});
        Collections.reverse(path);
        return path;
    }

    private static boolean isValidTile(int x, int y, int rows, int cols) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }
}

