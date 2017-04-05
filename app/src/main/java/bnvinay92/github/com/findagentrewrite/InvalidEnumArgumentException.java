package bnvinay92.github.com.findagentrewrite;

class InvalidEnumArgumentException extends RuntimeException {
    public InvalidEnumArgumentException(Enum anEnum) {
        super("Switch does not handle case: " + anEnum.name());
    }
}
