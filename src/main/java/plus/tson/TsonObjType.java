package plus.tson;

import plus.tson.exception.NoSearchException;

import static plus.tson.TsonMap.getSubStrAfter;


enum TsonObjType {
    BASIC, STR, LIST, MAP, FIELD;

    static TsonObjType scanType(String s) {
        s = getSubStrAfter(s, "=").trim();
        return scanType(s.charAt(0));
    }


    static TsonObjType scanType(char c) {
        switch (c) {
            default:
                throw new NoSearchException(c);
            case '(':
                return TsonObjType.BASIC;
            case '"':
                return TsonObjType.STR;
            case '{':
                return TsonObjType.MAP;
            case '[':
                return TsonObjType.LIST;
            case '<':
                return TsonObjType.FIELD;
        }
    }
}
