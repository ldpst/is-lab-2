package com.ldpst.service.importing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ldpst.model.domain.Color;
import com.ldpst.model.domain.Country;
import com.ldpst.model.domain.TicketType;
import com.ldpst.model.dto.Command.AddressCommand;
import com.ldpst.model.dto.Command.CoordinatesCommand;
import com.ldpst.model.dto.Command.EventCommand;
import com.ldpst.model.dto.Command.LocationCommand;
import com.ldpst.model.dto.Command.PersonCommand;
import com.ldpst.model.dto.Command.VenueCommand;
import com.ldpst.service.util.BusinessRuleException;

@Component
public class TicketCsvParser {
    public static final String HEADER = String.join(",",
            "ticket_name", "coordinate_x", "coordinate_y", "price", "type", "discount", "number",
            "person_eye_color", "person_hair_color", "person_location_x", "person_location_y",
            "person_location_name", "person_weight", "person_passport_id", "person_nationality",
            "event_name", "event_min_age", "event_description",
            "venue_name", "venue_capacity", "venue_address_street", "venue_address_zip_code");

    private static final List<String> REQUIRED_COLUMNS = List.of(HEADER.split(","));

    public List<TicketImportRow> parse(byte[] content) {
        String text = new String(content, StandardCharsets.UTF_8);
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }

        List<List<String>> rows = readRows(text);
        if (rows.isEmpty()) {
            throw new BusinessRuleException("CSV-файл пуст");
        }

        Map<String, Integer> columns = columns(rows.get(0));
        List<TicketImportRow> tickets = new ArrayList<>();

        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (row.stream().allMatch(String::isBlank)) {
                continue;
            }
            tickets.add(toImportRow(row, columns, index + 1));
        }

        if (tickets.isEmpty()) {
            throw new BusinessRuleException("CSV-файл не содержит билетов");
        }
        return tickets;
    }

    private Map<String, Integer> columns(List<String> header) {
        Map<String, Integer> columns = new HashMap<>();
        for (int index = 0; index < header.size(); index++) {
            columns.put(header.get(index).trim().toLowerCase(), index);
        }
        for (String required : REQUIRED_COLUMNS) {
            if (!columns.containsKey(required)) {
                throw new BusinessRuleException("В CSV отсутствует колонка «" + required + "»");
            }
        }
        return columns;
    }

    private TicketImportRow toImportRow(List<String> row, Map<String, Integer> columns, int line) {
        try {
            String ticketName = requiredText(row, columns, "ticket_name");
            long coordinateX = longValue(row, columns, "coordinate_x");
            long coordinateY = longValue(row, columns, "coordinate_y");
            float price = positiveFloat(row, columns, "price");
            TicketType type = requiredEnum(row, columns, "type", TicketType.class);
            Float discount = optionalPositiveFloat(row, columns, "discount");
            Integer number = optionalPositiveInteger(row, columns, "number");
            if (discount != null && discount > 100) {
                throw new IllegalArgumentException("discount должен быть не больше 100");
            }

            Color eyeColor = optionalEnum(row, columns, "person_eye_color", Color.class);
            Color hairColor = requiredEnum(row, columns, "person_hair_color", Color.class);
            double locationX = finiteDouble(row, columns, "person_location_x");
            float locationY = finiteFloat(row, columns, "person_location_y");
            String locationName = requiredText(row, columns, "person_location_name");
            if (locationName.length() > 32) {
                throw new IllegalArgumentException("person_location_name должен содержать не более 32 символов");
            }
            int weight = positiveInteger(row, columns, "person_weight");
            String passportId = optionalText(row, columns, "person_passport_id");
            if (passportId != null && passportId.length() > 26) {
                throw new IllegalArgumentException("person_passport_id должен содержать не более 26 символов");
            }
            Country nationality = optionalEnum(row, columns, "person_nationality", Country.class);

            String eventName = requiredText(row, columns, "event_name");
            int eventMinAge = integerValue(row, columns, "event_min_age");
            String eventDescription = requiredText(row, columns, "event_description");

            String venueName = requiredText(row, columns, "venue_name");
            int venueCapacity = positiveInteger(row, columns, "venue_capacity");
            String venueStreet = optionalText(row, columns, "venue_address_street");
            String venueZipCode = requiredText(row, columns, "venue_address_zip_code");
            if (venueZipCode.length() < 7) {
                throw new IllegalArgumentException("venue_address_zip_code должен содержать не менее 7 символов");
            }

            PersonCommand person = new PersonCommand(eyeColor, hairColor,
                    new LocationCommand(locationX, locationY, locationName), weight, passportId, nationality);
            EventCommand event = new EventCommand(eventName, eventMinAge, eventDescription);
            VenueCommand venue = new VenueCommand(venueName, venueCapacity,
                    new AddressCommand(venueStreet, venueZipCode));

            return new TicketImportRow(line, ticketName, new CoordinatesCommand(coordinateX, coordinateY),
                    price, type, discount, number, person, event, venue);
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("Ошибка в строке " + line + ": " + exception.getMessage());
        }
    }

    private String requiredText(List<String> row, Map<String, Integer> columns, String column) {
        String result = value(row, columns, column).trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(column + " не может быть пустым");
        }
        return result;
    }

    private String optionalText(List<String> row, Map<String, Integer> columns, String column) {
        String result = value(row, columns, column).trim();
        return result.isEmpty() ? null : result;
    }

    private int integerValue(List<String> row, Map<String, Integer> columns, String column) {
        return Integer.parseInt(value(row, columns, column).trim());
    }

    private int positiveInteger(List<String> row, Map<String, Integer> columns, String column) {
        int result = integerValue(row, columns, column);
        if (result <= 0) {
            throw new IllegalArgumentException(column + " должен быть больше 0");
        }
        return result;
    }

    private long longValue(List<String> row, Map<String, Integer> columns, String column) {
        return Long.parseLong(value(row, columns, column).trim());
    }

    private float positiveFloat(List<String> row, Map<String, Integer> columns, String column) {
        float result = finiteFloat(row, columns, column);
        if (result <= 0) {
            throw new IllegalArgumentException(column + " должен быть больше 0");
        }
        return result;
    }

    private Float optionalPositiveFloat(List<String> row, Map<String, Integer> columns, String column) {
        String text = value(row, columns, column).trim();
        if (text.isEmpty()) {
            return null;
        }
        float result = Float.parseFloat(text);
        if (!Float.isFinite(result) || result <= 0) {
            throw new IllegalArgumentException(column + " должен быть конечным числом больше 0");
        }
        return result;
    }

    private Integer optionalPositiveInteger(List<String> row, Map<String, Integer> columns, String column) {
        String text = value(row, columns, column).trim();
        if (text.isEmpty()) {
            return null;
        }
        int result = Integer.parseInt(text);
        if (result <= 0) {
            throw new IllegalArgumentException(column + " должен быть больше 0");
        }
        return result;
    }

    private double finiteDouble(List<String> row, Map<String, Integer> columns, String column) {
        double result = Double.parseDouble(value(row, columns, column).trim());
        if (!Double.isFinite(result)) {
            throw new IllegalArgumentException(column + " должен быть конечным числом");
        }
        return result;
    }

    private float finiteFloat(List<String> row, Map<String, Integer> columns, String column) {
        float result = Float.parseFloat(value(row, columns, column).trim());
        if (!Float.isFinite(result)) {
            throw new IllegalArgumentException(column + " должен быть конечным числом");
        }
        return result;
    }

    private <T extends Enum<T>> T requiredEnum(List<String> row, Map<String, Integer> columns,
            String column, Class<T> type) {
        String text = requiredText(row, columns, column);
        return Enum.valueOf(type, text.toUpperCase());
    }

    private <T extends Enum<T>> T optionalEnum(List<String> row, Map<String, Integer> columns,
            String column, Class<T> type) {
        String text = optionalText(row, columns, column);
        return text == null ? null : Enum.valueOf(type, text.toUpperCase());
    }

    private String value(List<String> row, Map<String, Integer> columns, String column) {
        int index = columns.get(column);
        return index >= row.size() ? "" : row.get(index);
    }

    private List<List<String>> readRows(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < text.length() && text.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                row.add(value.toString());
                value.setLength(0);
            } else if ((current == '\n' || current == '\r') && !quoted) {
                if (current == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                row.add(value.toString());
                value.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else {
                value.append(current);
            }
        }

        if (quoted) {
            throw new BusinessRuleException("В CSV не закрыта кавычка");
        }
        if (!row.isEmpty() || value.length() > 0) {
            row.add(value.toString());
            rows.add(row);
        }
        return rows;
    }
}
