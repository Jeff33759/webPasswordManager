package raica.pwmanager.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ResponseUtilTest {

    @Mock
    private ObjectMapper mockObjectMapper;

    @InjectMocks
    @Spy
    private ResponseUtil spyResponseUtil; //待測元件

    @Test
    void GivenArgs_WhenGenerateResponseBodyTemplate_ThenReturnExpectedResponseBodyTemplate() {
        Object inputData = new Object();
        String inputMsg = "msgForTest.";
        Instant mockInstant = Instant.ofEpochMilli(1716780260066L);

        ResponseBodyTemplate<Object> actual;
        try (MockedStatic<Instant> instantMockStatic = Mockito.mockStatic(Instant.class)) {
            instantMockStatic.when(Instant::now).thenReturn(mockInstant);
            actual = spyResponseUtil.generateResponseBodyTemplate(inputData, inputMsg);
        }

        Assertions.assertSame(inputData, actual.getData());
        Assertions.assertEquals(inputMsg, actual.getMsg());
        Assertions.assertEquals(mockInstant.toEpochMilli(), actual.getTimestamp());
    }

    @Test
    public void GivenArgs_WhenWriteJsonResponse_ThenInvokeExpectMethod() throws IOException {
        HttpServletResponse inputResponse = Mockito.mock(HttpServletResponse.class);
        PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);
        Object inputPojo = new Object();
        String stubJsonStr = "{\"key\":\"value\"}";
        HttpStatus inputHttpStatus = HttpStatus.OK;
        Mockito.when(inputResponse.getWriter()).thenReturn(mockPrintWriter);
        Mockito.when(mockObjectMapper.writeValueAsString(inputPojo)).thenReturn(stubJsonStr);

        spyResponseUtil.writeJsonResponse(inputResponse, inputHttpStatus, inputPojo);

        Mockito.verify(mockPrintWriter, Mockito.times(1)).print(stubJsonStr);
    }


}