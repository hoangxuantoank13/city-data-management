package com.citydata.ingestionservice.parser.csv;

import com.citydata.ingestionservice.dto.ElectricityUsageData;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ElectricityCsvParser extends CsvRowParser<ElectricityUsageData> {
    @Override
    public ElectricityUsageData parse(CSVRecord record) {
        var data = new ElectricityUsageData();
        data.setCustomerId(record.get("customer_id"));
        data.setConsumption(Double.parseDouble(record.get("consumption")));
        data.setBillingCycle(record.get("billing_cycle"));
        return data;
    }
}
