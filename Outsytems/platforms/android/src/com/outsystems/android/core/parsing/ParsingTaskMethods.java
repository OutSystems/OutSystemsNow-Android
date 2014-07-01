package com.outsystems.android.core.parsing;

public interface ParsingTaskMethods {

    // return the parsed object
    public Object parsingMethod();

    // receives the parsed object
    public void parsingFinishMethod(Object result);

}
