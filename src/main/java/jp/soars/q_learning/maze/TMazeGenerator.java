package jp.soars.q_learning.maze;

import jp.soars.utils.random.ICRandom;

public class TMazeGenerator {

    /**
     * 穴掘り法によって作成された2次元平面上の迷路を返す．
     * @param width 横幅 (奇数)
     * @param hight 縦幅 (奇数)
     * @param startX スタートのx座標
     * @param startY スタートのy座標
     * @param goalX ゴールのx座標
     * @param goalY ゴールのy座標
     * @param random 乱数発生器
     * @return 迷路を表現するboolean配列．true -> 通路，false -> 壁
     * @throws RuntimeException 横幅，縦幅が奇数ではない場合．
     * @throws RuntimeException スタート，ゴールの座標が設定不可能な座標の場合．(偶数，範囲外)
     */
    public static final boolean[][] generate2DMaze(int width, int hight,
            int startX, int startY, int goalX, int goalY, ICRandom random) {
        if (width % 2 == 0 || width < 5) {
            throw new RuntimeException("Width must be an odd number greater than or equal to 5.");
        }
        if (hight % 2 == 0 || hight < 5) {
            throw new RuntimeException("Hight must be an odd number greater than or equal to 5.");
        }
        if (startX % 2 == 0 || startY % 2 == 0 || startX <= -1 || width <= startX || startY <= -1 || hight <= startY) {
            throw new RuntimeException("The start coordinates are invalid.");
        }
        if (goalX % 2 == 0 || goalY % 2 == 0 || goalX <= -1 || width <= goalX || goalY <= -1 || hight <= goalY) {
            throw new RuntimeException("The goal coordinates are invalid.");
        }
        // 迷路初期化
        boolean[][] maze = new boolean[width][hight];
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < hight; ++j) {
                maze[i][j] = false;
            }
        }
        // 迷路作成の開始位置選択．(偶数)
        int x = (random.nextInt(1, width >> 1) << 1) - 1;
        int y = (random.nextInt(1, hight >> 1) << 1) - 1;
        maze[x][y] = true;
        // 穴掘り法開始
        dig(maze, x, y, width, hight, startX, startY, goalX, goalY, random);
        return maze;
    }

    /**
     * 穴掘り法によって作成された2次元平面上の迷路を返す．
     * @param width 横幅 (奇数)
     * @param hight 縦幅 (奇数)
     * @param random 乱数発生器
     * @return 迷路を表現するboolean配列．true -> 通路，false -> 壁
     * @throws RuntimeException 横幅，縦幅が奇数ではない場合．
     * @throws RuntimeException スタート，ゴールの座標が設定不可能な座標の場合．(偶数，範囲外)
     */
    public static final boolean[][] generate2DMaze(int width, int hight, ICRandom random) {
        return generate2DMaze(width, hight, 1, 1, width - 2, hight - 2, random);
    }

    /**
     * 穴掘り法．再帰実装
     * @param maze 迷路
     * @param x 現在位置 x座標
     * @param y 現在位置 y座標
     * @param width 横幅
     * @param hight 縦幅
     * @param startX スタートのx座標
     * @param startY スタートのy座標
     * @param goalX ゴールのx座標
     * @param goalY ゴールのy座標
     * @param random 乱数発生器
     */
    private static final void dig(boolean[][] maze, int x, int y,
            int width, int hight, int startX, int startY, int goalX, int goalY, ICRandom random) {
        // スタート，ゴール座標の場合は穴掘りせずに終了. (行き止まりにする)
        if ((x == startX && y == startY) || (x == goalX && y == goalY)) {
            return;
        }

        int[] directions = new int[]{0, 1, 2, 3};
        // Fisher–Yates shuffle
        for (int i = directions.length - 1; 0 < i; --i) {
            int index = random.nextInt(i + 1);
            int tmp = directions[index];
            directions[index] = directions[i];
            directions[i] = tmp;
        }

        for (int direction : directions) {
            int x1, y1; // 1マス先座標
            int x2, y2; // 2マス先座標
            if (direction == 0) { // 上
                x1 = x;
                y1 = y + 1;
                x2 = x;
                y2 = y + 2;
            } else if (direction == 1) { // 右
                x1 = x + 1;
                y1 = y;
                x2 = x + 2;
                y2 = y;
            } else if (direction == 2) { // 下
                x1 = x;
                y1 = y - 1;
                x2 = x;
                y2 = y - 2;
            } else if (direction == 3) { // 左
                x1 = x - 1;
                y1 = y;
                x2 = x - 2;
                y2 = y;
            } else {
                throw new RuntimeException("Unknown error.");
            }

            // 2マス先が範囲外
            if (x2 <= -1 || width <= x2 || y2 <= -1 || hight <= y2) {
                continue;
            }
            // 2マス先がすでに通路
            if (maze[x2][y2]) {
                continue;
            }
            // 穴掘りして移動先座標で再帰呼び出し
            maze[x1][y1] = true;
            maze[x2][y2] = true;
            dig(maze, x2, y2, width, hight, startX, startY, goalX, goalY, random);
        }
    }
}
