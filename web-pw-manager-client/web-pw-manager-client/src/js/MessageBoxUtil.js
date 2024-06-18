import {ElMessage, ElMessageBox} from "element-plus";

/**
 * @param message 訊息
 * @param title 標題
 * @param confirmButtonText 確認紐字樣
 * @param thenFn 按下確認後要做什麼，傳func
 */
export function showSuccessMessage(message, title = '成功', confirmButtonText = '確認', thenFn) {
    return ElMessageBox.confirm(message, title, {
        confirmButtonText: confirmButtonText,
        showCancelButton: false,
        type: 'success'
    }).then(() => {
        if(thenFn && typeof thenFn === 'function') {
            thenFn();
        }
    }).catch((error) => {
        ElMessage.error('發生未知的錯誤，請聯繫我們。');
        console.error(error);
    });
}


/**
 * @param message 訊息
 * @param title 標題
 * @param confirmButtonText 確認紐字樣
 * @param thenFn 按下確認後要做什麼，傳func
 */
export function showNotifyMessage(message, title = '提示', confirmButtonText = '確認', thenFn) {
    return ElMessageBox.confirm(message, title, {
        confirmButtonText: confirmButtonText,
        showCancelButton: false,
        type: 'info'
    }).then(() => {
        if(thenFn && typeof thenFn === 'function') {
            thenFn();
        }
    }).catch((error) => {
        ElMessage.error('發生未知的錯誤，請聯繫我們。');
        console.error(error);
    });
}


/**
 * @param message 訊息
 * @param title 標題
 * @param confirmButtonText 確認紐字樣
 * @param thenFn 按下確認後要做什麼，傳func
 */
export function showAlertMessage(message, title = '錯誤', confirmButtonText = '確認', thenFn) {
    return ElMessageBox.alert(message, title, {
        confirmButtonText: confirmButtonText,
        showCancelButton: false,
        type: 'error'
    }).then(() => {
        if(thenFn && typeof thenFn === 'function') {
            thenFn();
        }
    }).catch((error) => {
        ElMessage.error('發生未知的錯誤，請聯繫我們。');
        console.error(error);
    });
}


/**
 * @param message 訊息
 * @param title 標題
 * @param confirmButtonText 確認按鈕字樣
 * @param cancelButtonText  取消按鈕字樣
 * @param thenFn 按下確認後要做什麼，傳func
 * @param cancelFn 按下取消後要做什麼，傳func
 */
export function showConfirmMessage(message, title = '確認', confirmButtonText = '是', cancelButtonText = '否', thenFn, cancelFn) {
    return ElMessageBox.confirm(message, title, {
        confirmButtonText: confirmButtonText,
        cancelButtonText: cancelButtonText,
        type: 'info'
    }).then(() => {
        if(thenFn && typeof thenFn === 'function') {
            thenFn();
        }
    }).catch((error) => {

        if (error === 'cancel') {
            if(cancelFn) {
                cancelFn();
                return;
            }

            return;
        }

        ElMessage.error('發生未知的錯誤，請聯繫我們。');
        console.error(error);
    });
}