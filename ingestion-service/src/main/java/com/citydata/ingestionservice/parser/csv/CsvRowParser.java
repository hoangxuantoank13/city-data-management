package com.citydata.ingestionservice.parser.csv;

import com.citydata.ingestionservice.parser.RowParser;
import org.apache.commons.csv.CSVRecord;

public abstract class CsvRowParser<S> implements RowParser<CSVRecord, S> {}
