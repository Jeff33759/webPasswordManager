const MFATypeMapping = {
    0: 'None',
    1: 'Email'
};

export const getMFATypeByNum = (mfaTypeNum) => {
    return MFATypeMapping[mfaTypeNum] || 'Unknown';
}

export const getMFATypeNumByName = (nameStr) => {
    for (const [mfaTypeNum, mfaTypeName] of Object.entries(MFATypeMapping)) {
        if (mfaTypeName === nameStr) {
            return mfaTypeNum;
        }
    }

    return null;
}

export const getMFATypeMapping = () => {
    return MFATypeMapping;
}