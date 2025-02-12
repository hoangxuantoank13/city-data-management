package com.citydata.ingestionservice.parser.csv;

import com.citydata.ingestionservice.dto.WasteUsageData;
import com.citydata.ingestionservice.dto.WaterUsageData;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WasteCsvParser extends CsvRowParser<WasteUsageData> {
    @Override
    public WasteUsageData parse(CSVRecord record) {
        var data = new WasteUsageData();
        data.setCustomerId(record.get("customer_id"));
        data.setWasteAmount(Double.parseDouble(record.get("waste_amount")));
        data.setCollectionDate(record.get("collection_date"));
        return data;
    }
}

