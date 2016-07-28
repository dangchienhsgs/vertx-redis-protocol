package com.dangchienhsgs.redis.exception;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public class CommandErrorException extends Exception {
    public CommandErrorException(String s) {
        super(s);
    }
}
