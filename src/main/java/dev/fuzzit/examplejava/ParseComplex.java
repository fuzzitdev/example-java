package dev.fuzzit.examplejava;

public class ParseComplex {
    public static boolean parse(String data) {
        if (data.length() > 4) {
            return false;
        }
        return data.charAt(0) == 'F' &&
                data.charAt(1) == 'U' &&
                data.charAt(2) == 'Z' &&
                data.charAt(3) == 'Z';
    }
}
