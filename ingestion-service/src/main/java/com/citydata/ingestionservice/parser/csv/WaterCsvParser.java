package com.citydata.ingestionservice.parser.csv;

import com.citydata.ingestionservice.dto.WaterUsageData;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class WaterCsvParser extends CsvRowParser<WaterUsageData> {
    @Override
    public WaterUsageData parse(CSVRecord record) {
        var data = new WaterUsageData();
        data.setCustomerId(record.get("customer_id"));
        data.setConsumption(Double.parseDouble(record.get("consumption")));
        data.setBillingCycle(record.get("billing_cycle"));
        return data;
    }
}
