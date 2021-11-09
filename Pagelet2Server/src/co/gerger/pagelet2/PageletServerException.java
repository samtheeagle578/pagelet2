package co.gerger.pagelet2;

public class PageletServerException extends Exception {
    public PageletServerException(String string, Throwable throwable, boolean b, boolean b1) {
        super(string, throwable, b, b1);
    }

    public PageletServerException(Throwable throwable) {
        super(throwable);
    }

    public PageletServerException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public PageletServerException(String string) {
        super(string);
    }

    public PageletServerException() {
        super();
    }
}
