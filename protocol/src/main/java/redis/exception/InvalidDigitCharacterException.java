package redis.exception;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public class InvalidDigitCharacterException extends CommandErrorException {
    public InvalidDigitCharacterException(String s) {
        super(s);
    }
}
