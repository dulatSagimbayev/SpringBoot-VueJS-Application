package full.network.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonView;
import full.network.domain.Views;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonView(Views.IdName.class)
public class WebSocketEventDto {
    private ObjectType objectType;
    private EventType eventType;
    @JsonRawValue
    private String body;


}
