package com.study.ticket.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ExceptionCode code;

    public CustomException(ExceptionCode code) {
        super(code.getMessage()); // ✅ e.getMessage()에도 메시지가 찍힘
        this.code = code;
        //Exception부분은 깊게 들어기니까 뭔소린지 잘 모르겠지만.. 이렇게 하면 좀 더
        //기계가 처리하는 코드와 사람이 볼 메세지를 동시에 제공할 수 있다고 해서..
        //아마 404에러같은 코드랑 메세지랑 동시에 나온다는 거 같은데 정확히는 잘 모르겠어요
    }
}
