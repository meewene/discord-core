package com.research.discord.gateway;

public class CloseCode {
    public static final int UNKNOWN_ERROR = 4000;
    public static final int UNKNOWN_OPCODE = 4001;
    public static final int DECODE_ERROR = 4002;
    public static final int NOT_AUTHENTICATED = 4003;
    public static final int AUTHENTICATION_FAILED = 4004;
    public static final int ALREADY_AUTHENTICATED = 4005;
    public static final int INVALID_SEQ = 4007;
    public static final int RATE_LIMITED = 4008;
    public static final int SESSION_TIMED_OUT = 4009;

    public static boolean canReconnect(int code) {
        return code == UNKNOWN_ERROR ||
               code == UNKNOWN_OPCODE ||
               code == DECODE_ERROR ||
               code == NOT_AUTHENTICATED ||
               code == ALREADY_AUTHENTICATED ||
               code == INVALID_SEQ ||
               code == RATE_LIMITED ||
               code == SESSION_TIMED_OUT;
        // 4004 Authentication Failed is fatal.
    }
}
