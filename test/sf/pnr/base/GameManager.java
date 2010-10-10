package sf.pnr.base;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class GameManager {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat PERIOD_FORMAT = new SimpleDateFormat("mm:ss.SSS");

    static {
        PERIOD_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final int initialTimes;
    private final int increments;
    private final int rounds;

    public GameManager(final int initialTime, final int increment, final int rounds) {
        this.initialTimes = initialTime;
        this.increments = increment;
        this.rounds = rounds;
    }

    public TournamentResult play(final UciRunner... players) {

        final TournamentResult tournamentResult = new TournamentResult();
        int index = 1;
        for (int i = 0; i < rounds; i++) {
            for (int first = 0; first < players.length - 1; first++) {
                for (int second = first + 1; second < players.length; second++) {
                    play(i, index++, tournamentResult, players[first], players[second]);
                    play(i, index++, tournamentResult, players[second], players[first]);
                }
            }
        }
        return tournamentResult;
    }

    private void play(final int round, final int index, final TournamentResult tournamentResult,
                      final UciRunner white, final UciRunner black) {
        System.out.printf("[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL]\t%2$s - %3$s\r\n",
            System.currentTimeMillis(), white.getName(), black.getName());
        System.out.printf(
            "[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL]\t%2$s\t%3$6s\t%4$6s\t%5$6s\t%6$7s\t%7$7s\t%8$9s\t%9$5s\r\n",
            System.currentTimeMillis(), "mc", "white", "black", "mt[ms]", "rtw[ms]", "rtb[ms]", "nps", "cp");
        final UciRunner[] players = new UciRunner[] {white, black};
        final int[] times = new int[]{initialTimes, initialTimes};
        GameResult result;
        final List<Integer> moves = new ArrayList<Integer>(100);
        final long startTime = System.currentTimeMillis();
        final Board board = new Board();
        Exception ex = null;
        board.restart();
        try {
            white.restart();
            white.uciNewGame();
            black.restart();
            black.uciNewGame();
            int currentPlayer = 0;
            while (true) {
                final UciRunner player = players[currentPlayer];
                player.position(board);
                player.go(times[0], times[1], increments, increments, times[currentPlayer] + increments);
                final long moveTime = player.getMoveTime();
                times[currentPlayer] += increments - moveTime;
                if (times[currentPlayer] < 0) {
                    result = GameResult.TIME_OUT;
                    break;
                }
                final String bestMove = player.getBestMove();
                final int move = StringUtils.fromLong(board, bestMove);
                if (move == 0) {
                    throw new IOException("Zero move. Best line: " + player.getBestMoveLine());
                }
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
                    "[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL]\t%2$d.\t%3$6s\t%4$6s\t%5$6d\t%6$7d\t%7$7d\t%8$9.0f\t%9$5d\r\n",
                    System.currentTimeMillis(), board.getFullMoveCount(), whiteMove, blackMove, moveTime,
                    times[0], times[1], ((double) player.getNodeCount() * 1000) / times[currentPlayer],
                    player.getScore());
                board.move(move);
                moves.add(move);
                if (board.isMate()) {
                    result = GameResult.MATE;
                    break;
                }
                if (board.getRepetitionCount() >= 3) {
                    result = GameResult.THREEFOLD_REPETITION;
                    break;
                }
                if (Evaluation.drawByInsufficientMaterial(board)) {
                    result = GameResult.INSUFFICIENT_MATERIAL;
                    break;
                }
                currentPlayer = 1 - currentPlayer;
            }
        } catch (IOException e) {
            result = GameResult.ERROR;
            e.printStackTrace();
            System.out.println(StringUtils.toFen(board));
            ex=e;
        }

        final int[] movesArr = new int[moves.size()];
        for (int j = 0; j < moves.size(); j++) {
            movesArr[j] = moves.get(j);
        }

        final GameDetails details = new GameDetails(white, black, round, index, startTime, result, movesArr, times, ex);
        tournamentResult.registerResult(white, black, details);
        System.out.printf("[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL] %s\r\n",
            System.currentTimeMillis(), tournamentResult.toString(white, black));
        System.out.println(details.toPgn());
    }

    private static enum GameResult {
        TIME_OUT(0, 'T', 't'), MATE(1, '1', '0'), THREEFOLD_REPETITION(0.5, 'R', 'r'),
        INSUFFICIENT_MATERIAL(0.5, 'I', 'i'), ERROR(1, 'e', 'E');

        private final double whiteScore;
        private final double blackScore;
        private final char whiteCode;
        private final char blackCode;

        private GameResult(final double whiteScore, final char whiteCode, final char blackCode) {
            this(whiteScore, 1 - whiteScore, whiteCode, blackCode);
        }

        private GameResult(final double whiteScore, final double blackScore, final char whiteCode, final char blackCode) {
            this.whiteScore = whiteScore;
            this.blackScore = blackScore;
            this.whiteCode = whiteCode;
            this.blackCode = blackCode;
        }

        public char getCode(final boolean white) {
            return white? whiteCode: blackCode;
        }

        public double getScore(final boolean white) {
            return white? whiteScore: blackScore;
        }
    }

    public static class GameDetails {
        private final UciRunner white;
        private final UciRunner black;
        private final int round;
        private final int gameIndex;
        private final long startTime;
        private final GameResult result;
        private final int[] moves;
        private final int[] remainedTimes;
        private final Exception ex;

        public GameDetails(final UciRunner white, final UciRunner black, final int round, final int gameIndex,
                           final long startTime, final GameResult result, final int[] moves, final int[] remainedTimes,
                           final Exception ex) {
            this.white = white;
            this.black = black;
            this.round = round;
            this.gameIndex = gameIndex;
            this.startTime = startTime;
            this.result = result;
            this.moves = moves;
            this.remainedTimes = remainedTimes;
            this.ex = ex;
        }

        public char getCode(final UciRunner player) {
            return result.getCode((player == white) ^ (moves.length % 2 == 0));
        }

        public double getScore(final UciRunner player) {
            return result.getScore((player == white) ^ (moves.length % 2 == 0));
        }

        public String getScoreStr(final UciRunner player) {
            final double score = result.getScore((player == white) ^ (moves.length % 2 == 0));
            return score == 0.5? "1/2": String.format("%.0f", score);
        }

        public String getTerminationStr() {
            final String termination;
            if (result == GameResult.ERROR) {
                termination = "error";
            } else if (result == GameResult.TIME_OUT) {
                termination = "time forfeit";
            } else {
                termination = "normal";
            }
            return termination;
        }

        public String toPgn() {
            final StringBuilder builder = new StringBuilder();
            final Date startDateTime = new Date(startTime);
            builder.append(StringUtils.createPgnEntry("Date", DATE_FORMAT.format(startDateTime)));
            builder.append(StringUtils.createPgnEntry("Round", Integer.toString(round)));
            builder.append(StringUtils.createPgnEntry("Game", Integer.toString(gameIndex)));
            builder.append(StringUtils.createPgnEntry("White", white.getName()));
            builder.append(StringUtils.createPgnEntry("Black", black.getName()));
            builder.append(StringUtils.createPgnEntry("Result", getScoreStr(white) + "-" + getScoreStr(black)));
            builder.append(StringUtils.createPgnEntry("Time", TIME_FORMAT.format(startDateTime)));
            builder.append(StringUtils.createPgnEntry("PlyCount", Integer.toString(moves.length)));
            builder.append(StringUtils.createPgnEntry("Termination", getTerminationStr()));
            builder.append(StringUtils.createPgnEntry("RemainingTimeWhite", PERIOD_FORMAT.format(new Date(remainedTimes[0]))));
            builder.append(StringUtils.createPgnEntry("RemainingTimeBlack", PERIOD_FORMAT.format(new Date(remainedTimes[1]))));
            if (ex != null) {
                builder.append(StringUtils.createPgnEntry("Exception", ex.getMessage()));
            }
            final Board board = new Board();
            board.restart();
            for (int i = 0; i < moves.length; i++) {
                final int move = moves[i];
                if (i % 2 == 0) {
                    if (i % 16 == 0) {
                        builder.append("\r\n");
                    }
                    builder.append((i / 2) + 1).append(". ");
                }
                builder.append(StringUtils.toShort(board, move)).append(" ");
                board.move(move);
            }
            return builder.toString();
        }
    }

    public static class GameSeries {
        private final List<GameDetails> series = new ArrayList<GameDetails>();

        public void add(final GameDetails details) {
            series.add(details);
        }

        public String toStringShort(final UciRunner player) {
            final StringBuilder builder = new StringBuilder();
            for (GameDetails details: series) {
                builder.append(details.getCode(player));
            }
            return builder.toString();
        }

        public double getScore(final UciRunner player) {
            double score = 0.0;
            for (GameDetails details: series) {
                score += details.getScore(player);
            }
            return score;
        }
    }

    public static class TournamentResult {
        private final Map<UciRunner, Map<UciRunner, GameSeries>> games =
            new HashMap<UciRunner, Map<UciRunner, GameSeries>>();


        public void registerResult(final UciRunner white, final UciRunner black, final GameDetails gameDetails) {
            addResult(white, black, gameDetails);
            addResult(black, white, gameDetails);
        }

        private void addResult(final UciRunner player, final UciRunner opponent, final GameDetails gameDetails) {
            Map<UciRunner, GameSeries> matches = games.get(player);
            if (matches == null) {
                matches = new HashMap<UciRunner, GameSeries>();
                games.put(player, matches);
            }
            GameSeries series = matches.get(opponent);
            if (series == null) {
                series = new GameSeries();
                matches.put(opponent, series);
            }
            series.add(gameDetails);
        }

        public String toString(final UciRunner player, final UciRunner opponent) {
            final StringBuilder builder = new StringBuilder();
            builder.append(player.getName());
            builder.append(" - ");
            builder.append(opponent.getName());
            builder.append(": ");
            final Map<UciRunner, GameSeries> matches = games.get(player);
            if (matches == null) {
                return builder.toString();
            }
            final GameSeries series = matches.get(opponent);
            if (series == null) {
                return builder.toString();
            }
            final double playersScore = series.getScore(player);
            final double opponentsScore = series.getScore(opponent);
            builder.append(String.format("%.1f:%.1f (%s)", playersScore, opponentsScore, series.toStringShort(player)));
            return builder.toString();
        }
    }
}