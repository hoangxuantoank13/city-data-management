package com.citydata.ingestionservice.parser;

import java.util.Map;

public interface RowParser<T, S> {
    S parse(T record);

}
