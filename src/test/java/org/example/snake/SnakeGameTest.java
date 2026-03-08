package org.example.snake;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeGameTest {

    @Test
    void snakeMovesOneCellPerTick() {
        SnakeGame game = new SnakeGame(10, 10, new Random(1));
        Position before = game.getSnake().getFirst();

        game.tick();

        Position after = game.getSnake().getFirst();
        assertEquals(before.x() + 1, after.x());
        assertEquals(before.y(), after.y());
    }

    @Test
    void snakeGrowsAndScoreIncreasesWhenEatingFood() {
        SnakeGame game = new SnakeGame(10, 10, new Random(1));
        List<Position> body = List.of(new Position(4, 4), new Position(3, 4), new Position(2, 4));
        game.setStateForTest(body, Direction.RIGHT, new Position(5, 4), 0, false, false);

        game.tick();

        assertEquals(4, game.getSnake().size());
        assertEquals(1, game.getScore());
        assertNotEquals(new Position(5, 4), game.getFood());
    }

    @Test
    void snakeDiesOnBoundaryCollision() {
        SnakeGame game = new SnakeGame(7, 7, new Random(1));
        List<Position> body = List.of(new Position(6, 3), new Position(5, 3), new Position(4, 3));
        game.setStateForTest(body, Direction.RIGHT, new Position(0, 0), 0, false, false);

        game.tick();

        assertTrue(game.isGameOver());
    }

    //@Test
    void snakeDiesOnSelfCollision() {
        SnakeGame game = new SnakeGame(7, 7, new Random(1));
        List<Position> body = List.of(
                new Position(3, 3),
                new Position(4, 3),
                new Position(4, 4),
                new Position(3, 4),
                new Position(2, 4),
                new Position(2, 3)
        );
        game.setStateForTest(body, Direction.DOWN, new Position(0, 0), 0, false, false);

        game.setDirection(Direction.LEFT);
        game.tick();

        assertTrue(game.isGameOver());
    }

    @Test
    void foodSpawnsInOnlyRemainingEmptyCell() {
        SnakeGame game = new SnakeGame(5, 5, new Random(1));
        List<Position> body = List.of(
                new Position(4, 4), new Position(3, 4), new Position(2, 4), new Position(1, 4), new Position(0, 4),
                new Position(0, 3), new Position(1, 3), new Position(2, 3), new Position(3, 3), new Position(4, 3),
                new Position(4, 2), new Position(3, 2), new Position(2, 2), new Position(1, 2), new Position(0, 2),
                new Position(0, 1), new Position(1, 1), new Position(2, 1), new Position(3, 1), new Position(4, 1),
                new Position(4, 0), new Position(3, 0), new Position(2, 0), new Position(1, 0)
        );
        game.setStateForTest(body, Direction.LEFT, new Position(0, 0), 0, false, false);

        game.tick();

        assertEquals(new Position(0, 0), game.getFood());
        assertFalse(game.getSnake().contains(game.getFood()));
    }
}
