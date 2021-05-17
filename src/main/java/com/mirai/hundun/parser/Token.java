package com.mirai.hundun.parser;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/04/27
 */
@Data
public class Token {
    private TokenType type;
    private String textContent;
    private String extraContent;
}
