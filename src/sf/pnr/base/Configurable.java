package sf.pnr.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {

    public static enum Key {
        POLYGLOT_BOOK("polyglot.book"),
        TRANSP_TABLE_SIZE("transposition.table.size"), EVAL_TABLE_SIZE("evaluation.table.size"),
        EVAL_PAWNTABLE_SIZE("evaluation.pawnTable.size"),
        EVAL_POSITION_PAWN_OPENING("evaluation.position.pawn.opening"),
        EVAL_POSITION_PAWN_ENDGAME("evaluation.position.pawn.endgame"),
        EVAL_POSITION_KNIGHT_OPENING("evaluation.position.knight.opening"),
        EVAL_POSITION_KNIGHT_ENDGAME("evaluation.position.knight.endgame"),
        EVAL_POSITION_BISHOP_OPENING("evaluation.position.bishop.opening"),
        EVAL_POSITION_BISHOP_ENDGAME("evaluation.position.bishop.endgame"),
        EVAL_POSITION_ROOK_OPENING("evaluation.position.rook.opening"),
        EVAL_POSITION_ROOK_ENDGAME("evaluation.position.rook.endgame"),
        EVAL_POSITION_QUEEN_OPENING("evaluation.position.queen.opening"),
        EVAL_POSITION_QUEEN_ENDGAME("evaluation.position.queen.endgame"),
        EVAL_POSITION_KING_OPENING("evaluation.position.king.opening"),
        EVAL_POSITION_KING_ENDGAME("evaluation.position.king.endgame"),
        EVAL_PENALTY_DOUBLE_PAWN("evaluation.penalty.doublePawn"), EVAL_PENALTY_TRIPLE_PAWN("evaluation.penalty.triplePawn"),
        EVAL_PENALTY_ISOLATED_PAWN("evaluation.penalty.isolatedPawn"), EVAL_PENALTY_WEAK_PAWN("evaluation.penalty.weakPawn"),
        EVAL_BONUS_PAWN_SHIELD("evaluation.bonus.pawnShield"), EVAL_BONUS_PAWN_STORM_MAX("evaluation.bonus.pawnStormMax"),
        EVAL_PENALTY_PAWN_STORM_MAX_MAIN("evaluation.penalty.pawnStormMax.main"),
        EVAL_PENALTY_PAWN_STORM_MAX_SIDE("evaluation.penalty.pawnStormMax.side"),
        EVAL_BONUS_DEFENSE("evaluation.bonus.defense"), EVAL_BONUS_ATTACK("evaluation.bonus.attack"),
        EVAL_BONUS_HUNG_PIECE("evaluation.bonus.hungPiece"), EVAL_BONUS_MOBILITY("evaluation.bonus.mobility"),
        EVAL_BONUS_UNSTOPPABLE_PAWN("evaluation.bonus.unstoppablePawn"),
        EVAL_BONUS_DISTANCE_KNIGHT("evaluation.bonus.distanceKnight"),
        EVAL_BONUS_DISTANCE_BISHOP("evaluation.bonus.distanceBishop"),
        EVAL_BONUS_DISTANCE_ROOK("evaluation.bonus.distanceRook"),
        EVAL_BONUS_DISTANCE_QUEEN("evaluation.bonus.distanceQueen"),
        EVAL_BONUS_MOBILITY_KNIGHT("evaluation.bonus.mobilityKnight"),
        EVAL_BONUS_MOBILITY_BISHOP("evaluation.bonus.mobilityBishop"),
        EVAL_BONUS_MOBILITY_ROOK("evaluation.bonus.mobilityRook"),
        EVAL_BONUS_MOBILITY_QUEEN("evaluation.bonus.mobilityQueen"),
        EVAL_BONUS_MOBILITY_KING("evaluation.bonus.mobilityKing"),
        EVAL_PENALTY_CASTLING_MISSED("evaluation.penalty.castling.missed"),
        EVAL_PENALTY_CASTLING_PENDING("evaluation.penalty.castling.pending"),
        EVAL_PENALTY_CASTLING_PENDING_BOTH("evaluation.penalty.castling.pendingBoth"),
        EVAL_BONUS_ROOK_SAMEFILE("evaluation.bonus.rook.sameFile"),
        EVAL_BONUS_ROOK_SAMERANK("evaluation.bonus.rook.sameRank"),
        EVAL_PENALTY_ATTACKS_AROUND_KING("evaluation.penalty.attacksAroundKing"),
        ENGINE_DEPTH_EXT_CHECK("engine.depthExt.check"), ENGINE_DEPTH_EXT_7TH_RANK_PAWN("engine.depthExt.7thRankPawn"),
        ENGINE_DEPTH_EXT_MATE_THREAT("engine.depthExt.mateThreat"), ENGINE_DEPTH_EXT_MAX("engine.depthExt.max"),
        ENGINE_NULL_MOVE_MIN_DEPTH("engine.nullMove.minDepth"), ENGINE_NULL_MOVE_DEPTH_CHANGE_THRESHOLD("engine.nullMove.depthChangeThreshold"),
        ENGINE_NULL_MOVE_DEPTH_HIGH("engine.nullMove.depthHigh"), ENGINE_NULL_MOVE_DEPTH_LOW("engine.nullMove.depthLow"),
        ENGINE_FUTILITY_THRESHOLD("engine.futility.threshold"), ENGINE_DEEP_FUTILITY_THRESHOLD("engine.deepFutility.threshold"),
        ENGINE_RAZORING_THRESHOLD("engine.razoring.threshold"),
        ENGINE_LMR_MIN_DEPTH("engine.lmr.minDepth"), ENGINE_LMR_MIN_MOVE("engine.lmr.minMove"),
        ENGINE_ITERATIVE_DEEPENING_TIME_LIMIT("engine.iterative.deepening.time.limit"),
        ENGINE_SEARCH_ROOT_MIN_MOVE("engine.searchRoot.minMove"),
        ENGINE_MOVE_ORDER_CHECK_BONUS("engine.moveOrder.checkBonus"),
        ENGINE_MOVE_ORDER_BLOCKED_CHECK_BONUS("engine.moveOrder.blockedCheckBonus"),
        ENGINE_MOVE_ORDER_7TH_RANK_PAWN_BONUS("engine.moveOrder.7thRankPawnBonus"),
        ENGINE_MOVE_ORDER_CASTLING_BONUS("engine.moveOrder.castlingBonus"),
        ENGINE_MOVE_ORDER_POSITIONAL_GAIN_SHIFT("engine.moveOrder.positionalGainShift"),
        ENGINE_MOVE_ORDER_HISTORY_MAX_BITS("engine.moveOrder.historyMaxBits"),
        ENGINE_MOVE_ORDER_RND_MAX("engine.moveOrder.rndMax");

        private final String key;
        Key(final String key) {
            this.key = key;
            Configuration.STORE.put(key, this);
        }

        public String getKey() {
            return key;
        }
    }

    public Key value();
}