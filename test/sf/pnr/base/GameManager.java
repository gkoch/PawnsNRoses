package sf.pnr.base;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GameManager {
    private static enum Result {UNFINISHED, TIME_OUT, MATE, THREEFOLD_REPETITION, INSUFFICIENT_MATERIAL, ERROR}

    private final UciRunner[] players;
    private final int[] initialTimes;
    private final int[] increments;

    public GameManager(final UciRunner white, final UciRunner black, final int initialTime, final int increment) {
        players = new UciRunner[] {white, black};
        this.initialTimes = new int[] {initialTime, initialTime};
        this.increments = new int[] {increment, increment};
    }

    public void play(final int games) {
        final double[] score = new double[2];
        final StringBuilder history = new StringBuilder(games);
        for (int i = 0; i < games; i++) {
            final Board board = new Board();
            board.restart();
            final int[] times = new int[2];
            System.arraycopy(initialTimes, 0, times, 0, 2);
            int currentPlayer = 0;
            Result result;
            try {
                final UciRunner player = players[currentPlayer];
                player.uciNewGame();
                currentPlayer = 1;
                player.uciNewGame();
                currentPlayer = 0;
                while (true) {
                    player.position(board);
                    player.go(times[0], times[1], increments[0], increments[1]);
                    final long moveTime = player.getMoveTime();
                    times[currentPlayer] += increments[currentPlayer] - moveTime;
                    if (times[currentPlayer] < 0) {
                        result = Result.TIME_OUT;
                        break;
                    }
                    final String bestMove = player.getBestMove();
                    final int move = StringUtils.fromLong(board, bestMove);
                    final String whiteMove;
                    final String blackMove;
                    if (currentPlayer == 0) {
                        whiteMove = StringUtils.toShort(board, move);
                        blackMove = "";
                    } else {
                        whiteMove = "";
                        blackMove = StringUtils.toShort(board, move);
                    }
                    System.out.printf(
                        "[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL]\t%2$d.\t%3$6s\t%4$6s\t%5$6dms\t%6$7dms\t%7$9.2f nps\t%8$5d cp\r\n",
                        System.currentTimeMillis(), board.getFullMoveCount(), whiteMove, blackMove, moveTime,
                        times[currentPlayer], ((double) player.getNodeCount() * 1000) / times[currentPlayer],
                        player.getScore());
                    board.move(move);
                    if (board.isMate()) {
                        result = Result.MATE;
                        break;
                    }
                    if (board.getRepetitionCount() >= 3) {
                        result = Result.THREEFOLD_REPETITION;
                        break;
                    }
                    if (Evaluation.drawByInsufficientMaterial(board)) {
                        result = Result.INSUFFICIENT_MATERIAL;
                        break;
                    }
                    currentPlayer = 1 - currentPlayer;
                }
            } catch (IOException e) {
                result = Result.ERROR;
            }

            switch (result) {
                case UNFINISHED:
                    throw new IllegalStateException("Game is still running!");
                case ERROR:
                    history.append(currentPlayer == 0? 'E': 'e');
                    score[1 - currentPlayer] += 1.0;
                    break;
                case TIME_OUT:
                    history.append(currentPlayer == 0? 'T': 't');
                    score[1 - currentPlayer] += 1.0;
                    break;
                case MATE:
                    history.append(currentPlayer == 0? '1': '0');
                    score[currentPlayer] += 1.0;
                    break;
                case THREEFOLD_REPETITION:
                    history.append(currentPlayer == 0? 'R': 'r');
                    score[0] += 0.5;
                    score[1] += 0.5;
                    break;
                case INSUFFICIENT_MATERIAL:
                    history.append(currentPlayer == 0? 'I': 'i');
                    score[0] += 0.5;
                    score[1] += 0.5;
                    break;
            }

            System.out.printf("[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL] %s - %s: %.1f:%.1f (%s)\r\n",
                System.currentTimeMillis(), players[0].getName(), players[1].getName(), score[0], score[1], history);
        }
    }
}