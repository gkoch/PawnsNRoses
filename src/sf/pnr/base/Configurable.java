package sf.pnr.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {

    public static enum Key {
        POLYGLOT_BOOK("polyglot.book"),
        TRANSP_TABLE_SIZE("transposition.table.size"), EVAL_TABLE_SIZE("evaluation.table.size"),
        EVAL_PENALTY_DOUBLE_PAWN("evaluation.penalty.doublePawn"), EVAL_PENALTY_TRIPLE_PAWN("evaluation.penalty.triplePawn"),
        EVAL_PENALTY_ISOLATED_PAWN("evaluation.penalty.isolatedPawn"), EVAL_PENALTY_WEAK_PAWN("evaluation.penalty.weakPawn"),
        EVAL_BONUS_PAWN_SHIELD("evaluation.bonus.pawnShield"), EVAL_BONUS_PAWN_STORM_MAX("evaluation.bonus.pawnStormMax"),
        EVAL_BONUS_DEFENSE("evaluation.bonus.defense"), EVAL_BONUS_ATTACK("evaluation.bonus.attack"),
        EVAL_BONUS_HUNG_PIECE("evaluation.bonus.hungPiece"), EVAL_BONUS_MOBILITY("evaluation.bonus.mobility"),
        EVAL_BONUS_UNSTOPPABLE_PAWN("evaluation.bonus.unstoppablePawn"),
        EVAL_BONUS_DISTANCE_KNIGHT("evaluation.bonus.distanceKnight"),
        EVAL_BONUS_DISTANCE_BISHOP("evaluation.bonus.distanceBishop"),
        EVAL_BONUS_DISTANCE_ROOK("evaluation.bonus.distanceRook"),
        EVAL_BONUS_DISTANCE_QUEEN("evaluation.bonus.distanceQueen"),
        DEPTH_EXT_CHECK("engine.depthExt.check"), DEPTH_EXT_7TH_RANK_PAWN("engine.depthExt.7thRankPawn"),
        ;

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