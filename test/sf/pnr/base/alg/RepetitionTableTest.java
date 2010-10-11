package sf.pnr.base.alg;

import junit.framework.TestCase;
import sf.pnr.base.Board;
import sf.pnr.base.StringUtils;

public class RepetitionTableTest extends TestCase {
    
    public void testNoRepetition() {
        final Board board = new Board();
        board.restart();
        assertEquals(1, board.getRepetitionCount());
    }

    public void testThreefoldRepetition() {
        final Board board = StringUtils.fromPgn("[Date \"2010.10.11\"]\n" +
            "[Round \"2\"]\n" +
            "[Game \"5\"]\n" +
            "[White \"Pawns N' Roses Latest\"]\n" +
            "[Black \"Pawns N' Roses v0.054\"]\n" +
            "[Result \"1/2-1/2\"]\n" +
            "[Time \"09:20:51\"]\n" +
            "[PlyCount \"185\"]\n" +
            "[Termination \"normal\"]\n" +
            "[RemainingTimeWhite \"00:01.897\"]\n" +
            "[RemainingTimeBlack \"00:01.915\"]\n" +
            "\n" +
            "1. Nf3 Nf6 2. Nc3 d5 3. d3 d4 4. Ne4 e6 5. e3 Nxe4 6. dxe4 Nc6 7. Bb5 Bb4+ 8. Bd2 Bc5 \n" +
            "9. Bxc6+ bxc6 10. exd4 Bxd4 11. Bg5 Bxf2+ 12. Kxf2 Qxd1 13. Rhxd1 O-O 14. Rd8 Rxd8 15. Bxd8 Rb8 16. b3 Rb7 \n" +
            "17. Ke3 f5 18. e5 c5 19. Kd2 c4 20. bxc4 a6 21. h3 h6 22. Rg1 Bd7 23. Nd4 Rb4 24. Kc3 c5 \n" +
            "25. Nb3 Rb8 26. Be7 Rc8 27. Nxc5 Re8 28. Bd6 Bc8 29. Nxa6 Rd8 30. Nc5 Kf7 31. Kd2 g6 32. Rf1 Bd7 \n" +
            "33. Rf2 Bc6 34. a3 Re8 35. Nd3 Rd8 36. Re2 Ra8 37. Kc3 Ra4 38. Nb2 Ra7 39. Nd3 Ra4 40. Nb2 Ra8 \n" +
            "41. Rf2 g5 42. Kd2 f4 43. Nd3 Rd8 44. c5 Rd7 45. g3 Kg8 46. Nxf4 Rf7 47. g4 Be4 48. Nd3 Rxf2+ \n" +
            "49. Nxf2 Bg2 50. Kc1 Kf7 51. Kb1 Bc6 52. Kc1 Bg2 53. Kb1 Bc6 54. Kc1 Kg8 55. Ne4 Bxe4 56. a4 Kf7 \n" +
            "57. a5 Ke8 58. a6 Bc6 59. a7 Kd7 60. Bf8 Kc8 61. Bxh6 Bb7 62. Bxg5 Kd7 63. h4 Ke8 64. Kb1 Bc6 \n" +
            "65. h5 Kf7 66. h6 Kg6 67. Be3 Kh7 68. g5 Be4 69. Bd2 Bg2 70. Bc3 Ba8 71. Kb2 Bf3 72. Bd4 Ba8 \n" +
            "73. Kb1 Kg6 74. Bc3 Be4 75. Bd2 Ba8 76. Be3 Bd5 77. Kb2 Kh7 78. Bd4 Bh1 79. Kb3 Bg2 80. Bc3 Bd5+ \n" +
            "81. Kb2 Bf3 82. Bd4 Ba8 83. Bc3 Kg6 84. Kb1 Be4 85. Bd2 Ba8 86. Be3 Be4 87. Kb2 Ba8 88. Bf2 Bc6 \n" +
            "89. Kb1 Bd5 90. Be3 Be4 91. Kb2 Kh7 92. Bd4 Kg6 93. Be3");
        assertEquals(3, board.getRepetitionCount());
    }
}