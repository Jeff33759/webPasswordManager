package raica.pwmanager.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * User的多重身份認證類型。
 */
@Getter
public enum MFAType {

    /**
     * 未知的型別。
     */
    UNKNOWN(Integer.MIN_VALUE, "未知"),

    /**
     * 未設置。
     */
    NONE(0, "未設置"),

    /**
     * 郵箱認證
     */
    EMAIL(1, "Email");

    private final int typeNum;

    private final String typeName;

    MFAType(int typeNum, String typeName) {
        this.typeNum = typeNum;
        this.typeName = typeName;
    }

    private static final Map<Integer, MFAType> typeNumToEnumMap = new HashMap<>();

    static {
        for (MFAType type : MFAType.values()) {
            typeNumToEnumMap.put(type.getTypeNum(), type);
        }
    }

    /**
     * 用typeNum得出對應的MFAType
     * 若輸入的typeNum不匹配任何一個MFAType，則回傳UNKNOWN。
     */
    public static MFAType fromTypeNum(int typeNum) {
        MFAType mfaType = typeNumToEnumMap.get(typeNum);

        if (mfaType == null) {
            return MFAType.UNKNOWN;
        }

        return mfaType;
    }


}
