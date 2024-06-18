import axios from "axios";
import {ElMessage} from "element-plus";


/**
 * @param serverAddr
 * @param apiPath
 * @param queryStrMap 如果沒有，就傳null。
 * @param headers
 * @param thenFn 成功後要做什麼，傳func，使用預設則傳null
 * @param errorFn 失敗要做什麼，傳func，使用預設則傳null
 */
export function callGetApi(serverAddr, apiPath, queryStrMap, headers, thenFn, errorFn) {
    let fullApiPath = serverAddr + apiPath;

    if (queryStrMap && queryStrMap.size > 0) {
        const queryString = Array.from(queryStrMap.entries())
            .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
            .join('&');

        fullApiPath += `?${queryString}`;
    }

    axios
        .get(fullApiPath, {headers})
        .then((response) => {

            if(thenFn && typeof thenFn === "function") {
                thenFn(response);
                return;
            }

            ElMessage.success("操作成功。");

        })
        .catch((error) => {

            if(errorFn && typeof errorFn === "function") {
                errorFn(error);
                return;
            }

            ElMessage.error('未知的錯誤，請聯繫網站管理員。');
            console.error(error);
        });
}


/**
 * @param serverAddr
 * @param apiPath
 * @param queryStrMap 如果沒有，就傳null。
 * @param headers
 * @param body
 * @param thenFn 成功後要做什麼，傳func，使用預設則傳null
 * @param errorFn 失敗要做什麼，傳func，使用預設則傳null
 */
export function callPostApi(serverAddr, apiPath, queryStrMap, headers, body, thenFn, errorFn) {
    let fullApiPath = serverAddr + apiPath;

    if (queryStrMap && queryStrMap.size > 0) {
        const queryString = Array.from(queryStrMap.entries())
            .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
            .join('&');

        fullApiPath += `?${queryString}`;
    }

    axios
        .post(fullApiPath, body, {headers})
        .then((response) => {

            if(thenFn && typeof thenFn === "function") {
                thenFn(response);
                return;
            }

            ElMessage.success("操作成功。");

        })
        .catch((error) => {

            if(errorFn && typeof errorFn === "function") {
                errorFn(error);
                return;
            }

            ElMessage.error('未知的錯誤，請聯繫網站管理員。');
            console.error(error);
        });
}


/**
 * @param serverAddr
 * @param apiPath
 * @param queryStrMap 如果沒有，就傳null。
 * @param headers
 * @param body
 * @param thenFn 成功後要做什麼，傳func，使用預設則傳null
 * @param errorFn 失敗要做什麼，傳func，使用預設則傳null
 */
export function callPutApi(serverAddr, apiPath, queryStrMap, headers, body, thenFn, errorFn) {
    let fullApiPath = serverAddr + apiPath;

    if (queryStrMap && queryStrMap.size > 0) {
        const queryString = Array.from(queryStrMap.entries())
            .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
            .join('&');

        fullApiPath += `?${queryString}`;
    }

    axios
        .put(fullApiPath, body, {headers})
        .then((response) => {

            if(thenFn && typeof thenFn === "function") {
                thenFn(response);
                return;
            }

            ElMessage.success("操作成功。");
        })
        .catch((error) => {

            if(errorFn && typeof errorFn === "function") {
                errorFn(error);
                return;
            }

            ElMessage.error('未知的錯誤，請聯繫網站管理員。');
            console.error(error);
        });
}

/**
 * @param serverAddr
 * @param apiPath
 * @param queryStrMap 如果沒有，就傳null。
 * @param headers
 * @param thenFn 成功後要做什麼，傳func，使用預設則傳null
 * @param errorFn 失敗要做什麼，傳func，使用預設則傳null
 */
export function callDeleteApi(serverAddr, apiPath, queryStrMap, headers, thenFn, errorFn) {
    let fullApiPath = serverAddr + apiPath;

    if (queryStrMap && queryStrMap.size > 0) {
        const queryString = Array.from(queryStrMap.entries())
            .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
            .join('&');

        fullApiPath += `?${queryString}`;
    }

    axios
        .delete(fullApiPath, {headers})
        .then((response) => {

            if(thenFn && typeof thenFn === "function") {
                thenFn(response);
                return;
            }

            ElMessage.success("操作成功。");

        })
        .catch((error) => {

            if(errorFn && typeof errorFn === "function") {
                errorFn(error);
                return;
            }

            ElMessage.error('未知的錯誤，請聯繫網站管理員。');
            console.error(error);
        });
}