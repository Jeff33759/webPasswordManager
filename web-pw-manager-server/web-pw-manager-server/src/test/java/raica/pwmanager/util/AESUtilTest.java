package raica.pwmanager.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import raica.pwmanager.exception.AESException;
import raica.pwmanager.prop.AESProps;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AESUtilTest {

    @Mock
    private AESProps mockAESProps;

    @InjectMocks
    @Spy
    private AESUtil spyAESUtil; //待測元件


    @Test
    void GivenPlainText_WhenEncryptForDB_ThenReturnExpectedBase64EncodedCipherText() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("aesSecretKeyTest");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("AES/ECB/PKCS7Padding");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("ignore");
        spyAESUtil.initializeAfterStartUp();
        String inputPlainText = "plainTextForUnitTest.";

        String actual = spyAESUtil.encryptForDB(inputPlainText);

        Assertions.assertEquals("w1P3AjNeocbXfYq68qPwZ4ETBrCG6Mpy5OQTxbgrVcI=", actual);
    }

    @Test
    void GivenInvalidSecretKeyLength_WhenEncryptForDB_ThenThrowAESExceptionWithTheExpectedMessageAndCause() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("theKeyLengthIsNot128/192/256 bits.");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("AES/ECB/PKCS7Padding");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("ignore");
        spyAESUtil.initializeAfterStartUp();
        String inputPlainText = "plainTextForUnitTest.";

        AESException actual = Assertions.assertThrows(AESException.class, () -> {
            spyAESUtil.encryptForDB(inputPlainText);
        });

        Assertions.assertEquals("AES encryption for DB failed.", actual.getMessage());
        Assertions.assertInstanceOf(InvalidKeyException.class, actual.getCause());
    }

    @Test
    void GivenInvalidTransformation_WhenEncryptForDB_ThenThrowAESExceptionWithTheExpectedMessageAndCause() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("aesSecretKeyTest");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("invalidTransformation");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("ignore");
        spyAESUtil.initializeAfterStartUp();
        String inputPlainText = "plainTextForUnitTest.";

        AESException actual = Assertions.assertThrows(AESException.class, () -> {
            spyAESUtil.encryptForDB(inputPlainText);
        });

        Assertions.assertEquals("AES encryption for DB failed.", actual.getMessage());
        Assertions.assertInstanceOf(NoSuchAlgorithmException.class, actual.getCause());
    }

    @Test
    void GivenBase64EncodedCipherText_WhenDecryptFromDB_ThenReturnExpectedPlainText() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("aesSecretKeyTest");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("AES/ECB/PKCS7Padding");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("ignore");
        spyAESUtil.initializeAfterStartUp();
        String inputBase64EncodedCipherText = "w1P3AjNeocbXfYq68qPwZ4ETBrCG6Mpy5OQTxbgrVcI=";

        String actual = spyAESUtil.decryptFromDB(inputBase64EncodedCipherText);

        Assertions.assertEquals("plainTextForUnitTest.", actual);
    }

    @Test
    void GivenInvalidSecretKeyLength_WhenDecryptFromDB_ThenThrowAESExceptionWithTheExpectedMessageAndCause() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("theKeyLengthIsNot128/192/256 bits.");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("AES/ECB/PKCS7Padding");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("ignore");
        spyAESUtil.initializeAfterStartUp();
        String inputBase64EncodedCipherText = "w1P3AjNeocbXfYq68qPwZ4ETBrCG6Mpy5OQTxbgrVcI=";

        AESException actual = Assertions.assertThrows(AESException.class, () -> {
            spyAESUtil.decryptFromDB(inputBase64EncodedCipherText);
        });

        Assertions.assertEquals("AES decryption from DB failed.", actual.getMessage());
        Assertions.assertInstanceOf(InvalidKeyException.class, actual.getCause());
    }

    @Test
    void GivenInvalidTransformation_WhenDecryptFromDB_ThenThrowAESExceptionWithTheExpectedMessageAndCause() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("aesSecretKeyTest");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("invalidTransformation");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("ignore");
        spyAESUtil.initializeAfterStartUp();
        String inputBase64EncodedCipherText = "w1P3AjNeocbXfYq68qPwZ4ETBrCG6Mpy5OQTxbgrVcI=";

        AESException actual = Assertions.assertThrows(AESException.class, () -> {
            spyAESUtil.decryptFromDB(inputBase64EncodedCipherText);
        });

        Assertions.assertEquals("AES decryption from DB failed.", actual.getMessage());
        Assertions.assertInstanceOf(NoSuchAlgorithmException.class, actual.getCause());
    }


    @Test
    void GivenPlainText_WhenEncryptForEmailActivationFlow_ThenReturnExpectedBase64EncodedCipherText() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("aesSecretKeyTest");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("AES/ECB/PKCS7Padding");
        spyAESUtil.initializeAfterStartUp();
        String inputPlainText = "plainTextForUnitTest.";

        String actual = spyAESUtil.encryptForEmailActivationFlow(inputPlainText);

        Assertions.assertEquals("w1P3AjNeocbXfYq68qPwZ4ETBrCG6Mpy5OQTxbgrVcI=", actual);
    }

    @Test
    void GivenInvalidSecretKeyLength_WhenEncryptForEmailActivationFlow_ThenThrowAESExceptionWithTheExpectedMessageAndCause() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("theKeyLengthIsNot128/192/256 bits.");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("AES/ECB/PKCS7Padding");
        spyAESUtil.initializeAfterStartUp();
        String inputPlainText = "plainTextForUnitTest.";

        AESException actual = Assertions.assertThrows(AESException.class, () -> {
            spyAESUtil.encryptForEmailActivationFlow(inputPlainText);
        });

        Assertions.assertEquals("AES encryption for e-mail auth flow failed.", actual.getMessage());
        Assertions.assertInstanceOf(InvalidKeyException.class, actual.getCause());
    }

    @Test
    void GivenInvalidTransformation_WhenEncryptForEmailActivationFlow_ThenThrowAESExceptionWithTheExpectedMessageAndCause() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("aesSecretKeyTest");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("invalidTransformation");
        spyAESUtil.initializeAfterStartUp();
        String inputPlainText = "plainTextForUnitTest.";

        AESException actual = Assertions.assertThrows(AESException.class, () -> {
            spyAESUtil.encryptForEmailActivationFlow(inputPlainText);
        });

        Assertions.assertEquals("AES encryption for e-mail auth flow failed.", actual.getMessage());
        Assertions.assertInstanceOf(NoSuchAlgorithmException.class, actual.getCause());
    }

    @Test
    void GivenBase64EncodedCipherText_WhenDecryptFromEmailActivationFlow_ThenReturnExpectedPlainText() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("aesSecretKeyTest");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("AES/ECB/PKCS7Padding");
        spyAESUtil.initializeAfterStartUp();
        String inputBase64EncodedCipherText = "w1P3AjNeocbXfYq68qPwZ4ETBrCG6Mpy5OQTxbgrVcI=";

        String actual = spyAESUtil.decryptFromEmailActivationFlow(inputBase64EncodedCipherText);

        Assertions.assertEquals("plainTextForUnitTest.", actual);
    }

    @Test
    void GivenInvalidSecretKeyLength_WhenDecryptFromEmailActivationFlow_ThenThrowAESExceptionWithTheExpectedMessageAndCause() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("theKeyLengthIsNot128/192/256 bits.");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("AES/ECB/PKCS7Padding");
        spyAESUtil.initializeAfterStartUp();
        String inputBase64EncodedCipherText = "w1P3AjNeocbXfYq68qPwZ4ETBrCG6Mpy5OQTxbgrVcI=";

        AESException actual = Assertions.assertThrows(AESException.class, () -> {
            spyAESUtil.decryptFromEmailActivationFlow(inputBase64EncodedCipherText);
        });

        Assertions.assertEquals("AES decryption from e-mail auth flow failed.", actual.getMessage());
        Assertions.assertInstanceOf(InvalidKeyException.class, actual.getCause());
    }

    @Test
    void GivenInvalidTransformation_WhenDecryptFromEmailActivationFlow_ThenThrowAESExceptionWithTheExpectedMessageAndCause() {
        Mockito.when(mockAESProps.getAesSecretKeyForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesTransformationForDB()).thenReturn("ignore");
        Mockito.when(mockAESProps.getAesSecretKeyForEmailActivationFlow()).thenReturn("aesSecretKeyTest");
        Mockito.when(mockAESProps.getAesTransformationForEmailActivationFlow()).thenReturn("invalidTransformation");
        spyAESUtil.initializeAfterStartUp();
        String inputBase64EncodedCipherText = "w1P3AjNeocbXfYq68qPwZ4ETBrCG6Mpy5OQTxbgrVcI=";

        AESException actual = Assertions.assertThrows(AESException.class, () -> {
            spyAESUtil.decryptFromEmailActivationFlow(inputBase64EncodedCipherText);
        });

        Assertions.assertEquals("AES decryption from e-mail auth flow failed.", actual.getMessage());
        Assertions.assertInstanceOf(NoSuchAlgorithmException.class, actual.getCause());
    }

}