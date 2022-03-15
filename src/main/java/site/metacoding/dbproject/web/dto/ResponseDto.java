package site.metacoding.dbproject.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseDto<T> {
    private Integer code; // -1 실패, 1 성공 -> 통신이 잘 됐는지 확인
    private String msg; // 실패원인, 통신 등의 메시지를 담아줌
    private T data; // 응답할 때 뭘 담을지 타입을 결정하도록 제네릭 설정

}
