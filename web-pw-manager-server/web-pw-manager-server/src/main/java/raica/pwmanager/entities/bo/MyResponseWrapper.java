package raica.pwmanager.entities.bo;

import lombok.Getter;
import raica.pwmanager.entities.dto.send.ResponseBodyTemplate;
import raica.pwmanager.enums.MyHttpStatus;

@Getter
public class MyResponseWrapper {

    MyHttpStatus myHttpStatus;

    ResponseBodyTemplate body;

    public <D> MyResponseWrapper(MyHttpStatus myHttpStatus, ResponseBodyTemplate<D> body) {
        this.myHttpStatus = myHttpStatus;
        this.body = body;
    }

}


