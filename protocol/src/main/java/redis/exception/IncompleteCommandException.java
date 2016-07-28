package redis.exception;

/**
 * Created by Nguyen Dang Chien on 7/28/16.
 */
public class IncompleteCommandException extends Exception {
    public IncompleteCommandException(String s) {
        super(s);
    }
}
