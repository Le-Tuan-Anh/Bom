package Code.Entity.Moveable.Enemies;

import java.util.ArrayList;
import java.util.List;
import Code.Entity.Entity;
import Code.Entity.Moveable.Moveable;

public abstract class Enemy extends Moveable {
    protected final int RIGHT = 0, UP = 1, LEFT = 2, DOWN = 3;
    protected int currentDirection = (int) Math.round(Math.random() * 3);

    public Enemy(int x, int y) {
        super(x, y);
    }

    public Enemy(int x, int y, int step) {
        super(x, y);
        this.step = step;
    }

    public void move(Entity[][] map) {
        boolean change = false;

        switch (currentDirection) {
            case RIGHT:
                if (!canMoveRight(map))
                    change = true;
                break;
            case UP:
                if (!canMoveUp(map))
                    change = true;
                break;
            case LEFT:
                if (!canMoveLeft(map))
                    change = true;
                break;
            case DOWN:
                if (!canMoveDown(map))
                    change = true;
                break;
            default:
                break;
        }
        
        if (change)
            navigation(map);
        else
            switch (currentDirection) {
                case RIGHT:
                    moveRight(map);
                    break;
                case UP:
                    moveUp(map);
                    break;
                case LEFT:
                    moveLeft(map);
                    break;
                case DOWN:
                    moveDown(map);
                    break;
                default:
                    break;
            }
    }

    public void navigation(Entity[][] map) {
        List<Integer> ls = new ArrayList<>();
        if (canMoveRight(map))  ls.add(RIGHT);
        if (canMoveUp(map))     ls.add(UP);
        if (canMoveLeft(map))   ls.add(LEFT);
        if (canMoveDown(map))   ls.add(DOWN);

        currentDirection = ls.get((int) Math.round(Math.random() * (ls.size()-1)));
    }
}
