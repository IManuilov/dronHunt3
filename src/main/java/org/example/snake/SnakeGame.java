package org.example.snake;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

public class SnakeGame {
    private final int width;
    private final int height;
    private final RandomGenerator random;

    private final Deque<Position> snake = new ArrayDeque<>();
    private Direction direction;
    private Direction pendingDirection;
    private Position food;
    private int score;
    private boolean gameOver;
    private boolean paused;

    public SnakeGame(int width, int height, RandomGenerator random) {
        if (width < 5 || height < 5) {
            throw new IllegalArgumentException("Grid size must be at least 5x5");
        }
        this.width = width;
        this.height = height;
        this.random = random;
        reset();
    }

    public final void reset() {
        snake.clear();
        int centerX = width / 2;
        int centerY = height / 2;
        snake.addFirst(new Position(centerX, centerY));
        snake.addLast(new Position(centerX - 1, centerY));
        snake.addLast(new Position(centerX - 2, centerY));

        direction = Direction.RIGHT;
        pendingDirection = Direction.RIGHT;
        score = 0;
        gameOver = false;
        paused = false;
        spawnFood();
    }

    public void tick() {
        if (gameOver || paused) {
            return;
        }

        direction = pendingDirection;
        Position nextHead = snake.peekFirst().move(direction);

        if (isOutOfBounds(nextHead)) {
            gameOver = true;
            return;
        }

        boolean grows = nextHead.equals(food);
        Set<Position> occupied = new HashSet<>(snake);
        if (!grows) {
            occupied.remove(snake.peekLast());
        }
        if (occupied.contains(nextHead)) {
            gameOver = true;
            return;
        }

        snake.addFirst(nextHead);
        if (grows) {
            score++;
            spawnFood();
        } else {
            snake.removeLast();
        }
    }

    public void setDirection(Direction newDirection) {
        if (newDirection == null || gameOver) {
            return;
        }
        if (!newDirection.isOpposite(direction)) {
            pendingDirection = newDirection;
        }
    }

    public void togglePause() {
        if (!gameOver) {
            paused = !paused;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Position> getSnake() {
        return List.copyOf(snake);
    }

    public Position getFood() {
        return food;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return paused;
    }

    private boolean isOutOfBounds(Position position) {
        return position.x() < 0 || position.x() >= width || position.y() < 0 || position.y() >= height;
    }

    private void spawnFood() {
        List<Position> emptyCells = new ArrayList<>();
        Set<Position> snakeCells = new HashSet<>(snake);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position position = new Position(x, y);
                if (!snakeCells.contains(position)) {
                    emptyCells.add(position);
                }
            }
        }

        if (emptyCells.isEmpty()) {
            food = null;
            gameOver = true;
            return;
        }

        int index = random.nextInt(emptyCells.size());
        food = emptyCells.get(index);
    }

    void setStateForTest(List<Position> body, Direction currentDirection, Position food, int score, boolean paused, boolean gameOver) {
        snake.clear();
        for (Position position : body) {
            snake.addLast(position);
        }
        this.direction = currentDirection;
        this.pendingDirection = currentDirection;
        this.food = food;
        this.score = score;
        this.paused = paused;
        this.gameOver = gameOver;
    }

    List<Position> getEmptyCellsForTest() {
        List<Position> emptyCells = new ArrayList<>();
        Set<Position> snakeCells = new HashSet<>(snake);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position position = new Position(x, y);
                if (!snakeCells.contains(position)) {
                    emptyCells.add(position);
                }
            }
        }
        return Collections.unmodifiableList(emptyCells);
    }
}
