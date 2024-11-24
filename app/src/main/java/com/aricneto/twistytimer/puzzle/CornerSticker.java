package com.aricneto.twistytimer.puzzle;

public enum CornerSticker {
    UBL, UBR, UFR, UFL,
    LBU, LFU, LFD, LBD,
    FLU, FRU, FRD, FLD,
    RFU, RBU, RBD, RFD,
    BRU, BLU, BLD, BRD,
    DLF, DRF, DRB, DLB;

    /**
     * @param s A sticker specified as a string (e.g., "UFR").
     * @return The corresponding sticker enum.
     */
    public static CornerSticker parse(String s) {
        switch ((s == null ? "" : s).toUpperCase()) {
            case "UBL":
            case "ULB":
                return CornerSticker.UBL;
            case "UBR":
            case "URB":
                return CornerSticker.UBR;
            case "UFR":
            case "URF":
                return CornerSticker.UFR;
            case "UFL":
            case "ULF":
                return CornerSticker.UFL;
            case "LBU":
            case "LUB":
                return CornerSticker.LBU;
            case "LFU":
            case "LUF":
                return CornerSticker.LFU;
            case "LFD":
            case "LDF":
                return CornerSticker.LFD;
            case "LBD":
            case "LDB":
                return CornerSticker.LBD;
            case "FLU":
            case "FUL":
                return CornerSticker.FLU;
            case "FRU":
            case "FUR":
                return CornerSticker.FRU;
            case "FRD":
            case "FDR":
                return CornerSticker.FRD;
            case "FLD":
            case "FDL":
                return CornerSticker.FLD;
            case "RFU":
            case "RUF":
                return CornerSticker.RFU;
            case "RBU":
            case "RUB":
                return CornerSticker.RBU;
            case "RBD":
            case "RDB":
                return CornerSticker.RBD;
            case "RFD":
            case "RDF":
                return CornerSticker.RFD;
            case "BRU":
            case "BUR":
                return CornerSticker.BRU;
            case "BLU":
            case "BUL":
                return CornerSticker.BLU;
            case "BLD":
            case "BDL":
                return CornerSticker.BLD;
            case "BRD":
            case "BDR":
                return CornerSticker.BRD;
            case "DLF":
            case "DFL":
                return CornerSticker.DLF;
            case "DRF":
            case "DFR":
                return CornerSticker.DRF;
            case "DRB":
            case "DBR":
                return CornerSticker.DRB;
            case "DLB":
            case "DBL":
                return CornerSticker.DLB;
            default:
                throw new IllegalArgumentException(String.format("\"%s\" is not a valid corner sticker", s));
        }
    }
}
