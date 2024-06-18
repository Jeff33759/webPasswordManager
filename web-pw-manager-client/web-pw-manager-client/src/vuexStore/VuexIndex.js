import {createStore} from 'vuex';
import {jwtDecode} from "jwt-decode";

export default createStore({
    state() {
        return {
            accessToken: null,
            refreshToken: null,
            userInfo: null,
            emailForMfa: null,
            renewATTimeoutIds: [],
            categories: null,
            tags: null,
            passwords: null,
            //以下應該可以考慮用defineProps傳遞就好
            selectedPasswordsArr: [],
            selectedPasswordId: null,
            selectedPasswordInfo: null,

            activeLeftMenuItem: '',
            hasClickedAddPwButton: false,
            pwTitleKeyWord: ""
        };
    },
    mutations: {
        setAccessToken(state, accessToken) {
            state.accessToken = accessToken;
        },
        parseAccessTokenAndSetUserInfo(state, accessToken) {
            state.userInfo = jwtDecode(accessToken);
        },
        setRefreshToken(state, refreshToken) {
            state.refreshToken = refreshToken;
        },
        setEmailForMfa(state, email) {
            state.emailForMfa = email;
        },
        setRenewATTimeoutId(state, renewATTimeoutId) {
            state.renewATTimeoutIds.push(renewATTimeoutId);
        },
        setCategories(state, categories) {
            state.categories = categories.sort((a, b) => a.id - b.id); //依照ID排序
        },
        setTags(state, tags) {
            state.tags = tags.sort((a, b) => a.id - b.id); //依照ID排序
        },
        setPasswords(state, passwords) {
            state.passwords = passwords.sort((a, b) => a.id - b.id); //依照ID排序
        },
        cancelAllRenewATTimeoutIfPresent(state) {
            if (state.renewATTimeoutIds.length !== 0) {
                state.renewATTimeoutIds.forEach(renewATTimeoutId => {
                    clearTimeout(renewATTimeoutId);
                });
                state.renewATTimeoutIds = [];
                console.log("Cancel the renew accessToken task.");
            }
        },
        setSelectedPasswordsArr(state, selectedPasswordsArr) {
            state.selectedPasswordsArr = selectedPasswordsArr;
        },
        setSelectedPasswordId(state, selectedPasswordId) {
            state.selectedPasswordId = selectedPasswordId;
        },
        setSelectedPasswordInfo(state, selectedPasswordInfo) {
            state.selectedPasswordInfo = selectedPasswordInfo;
        },
        setHasClickedAddPwButton(state, hasClickedAddPwButton) {
            state.hasClickedAddPwButton = hasClickedAddPwButton;
        },
        setPWTitleKeyWord(state, pwTitleKeyWord) {
            state.pwTitleKeyWord = pwTitleKeyWord;
        },
        setActiveLeftMenuItem(state, activeLeftMenuItem) {
            state.activeLeftMenuItem = activeLeftMenuItem;
        },
        clearAllLoginStates(state) {
            state.accessToken = null;
            state.refreshToken = null;
            state.userInfo = null;
            state.categories = null;
            state.tags = null;
            state.passwords = null;
            this.commit('cancelAllRenewATTimeoutIfPresent');
            state.selectedPasswordsArr = [];
            state.selectedPasswordId = null;
            state.selectedPasswordInfo = null;
            state.activeLeftMenuItem = "";
            state.hasClickedAddPwButton = false;
            state.pwTitleKeyWord= "";

            console.log("Clear all login states successfully.")
        }

    },
    actions: {
        setAndParseAllToken({commit}, myData) {
            commit('setAccessToken', myData.accessToken);
            commit('parseAccessTokenAndSetUserInfo', myData.accessToken);
            commit('setRefreshToken', myData.refreshToken);
        },
        setEmailForMfaBeforeMFAVerification({commit}, emailForMfa) {
            commit("setEmailForMfa", emailForMfa);
        },
        setAndParseOnlyAccessToken({commit}, newAccessToken) {
            commit('setAccessToken', newAccessToken);
            commit('parseAccessTokenAndSetUserInfo', newAccessToken);
        },
        setRenewATTimeoutId({commit}, renewATTimeoutId) {
            commit('setRenewATTimeoutId', renewATTimeoutId);
        },
        setCategories({commit}, categories) {
            commit('setCategories', categories);
        },
        setTags({commit}, tags) {
            commit('setTags', tags);
        },
        setPasswords({commit}, passwords) {
            commit('setPasswords', passwords);
        },
        cancelAllRenewATTimeoutIfPresent({commit}) {
            commit('cancelAllRenewATTimeoutIfPresent');
        },
        setSelectedPasswordsArr({commit}, selectedPasswordsArr) {
            commit('setSelectedPasswordsArr', selectedPasswordsArr);
        },
        setSelectedPasswordId({commit}, selectedPasswordId) {
            commit('setSelectedPasswordId', selectedPasswordId);
        },
        setSelectedPasswordInfo({commit}, selectedPasswordInfo) {
            commit('setSelectedPasswordInfo', selectedPasswordInfo);
        },
        setHasClickedAddPwButton({commit}, hasClickedAddPwButton) {
            commit('setHasClickedAddPwButton', hasClickedAddPwButton);
        },
        clearAllLoginStates({commit}) {
            commit('clearAllLoginStates');
        },
        setPWTitleKeyWord({commit}, pwTitleKeyWord) {
            commit('setPWTitleKeyWord', pwTitleKeyWord);
        },
        setActiveLeftMenuItem({commit}, activeLeftMenuItem) {
            commit('setActiveLeftMenuItem', activeLeftMenuItem);
        },
        resetOperationMenuItemIsActive({commit}) {
            commit('setActiveLeftMenuItem', ""); //清除左邊menu的選中狀態
        }
    },
    getters: {
        accessToken: (state) => state.accessToken,
        refreshToken: (state) => state.refreshToken,
        userInfo: (state) => state.userInfo,
        emailForMfa: (state) => state.emailForMfa,
        renewATTimeoutIds: (state) => state.renewATTimeoutIds,
        categories: (state) => state.categories,
        tags: (state) => state.tags,
        passwords: (state) => state.passwords,
        selectedPasswordsArr: (state) => state.selectedPasswordsArr,
        selectedPasswordId: (state) => state.selectedPasswordId,
        selectedPasswordInfo: (state) => state.selectedPasswordInfo,
        activeLeftMenuItem: (state) => state.activeLeftMenuItem,
        hasClickedAddPwButton: (state) => state.hasClickedAddPwButton,
        pwTitleKeyWord: (state) => state.pwTitleKeyWord,
    }
});
