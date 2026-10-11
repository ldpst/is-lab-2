package com.ldpst.service.importing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.ldpst.model.domain.TicketType;
import com.ldpst.service.util.BusinessRuleException;

class TicketCsvParserTest {
    private final TicketCsvParser parser = new TicketCsvParser();

    @Test
    void parsesTicketAndOptionalValues() {
        String csv = TicketCsvParser.HEADER + "\n"
                + "\"Билет, первый ряд\",10,20,1500.5,VIP,,,,GREEN,1.2,2.3,Москва,70,,,"
                + "Концерт,0,Описание,Арена,100,,1234567\n";

        var tickets = parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).name()).isEqualTo("Билет, первый ряд");
        assertThat(tickets.get(0).type()).isEqualTo(TicketType.VIP);
        assertThat(tickets.get(0).discount()).isNull();
        assertThat(tickets.get(0).number()).isNull();
        assertThat(tickets.get(0).person().passportID()).isNull();
        assertThat(tickets.get(0).event().name()).isEqualTo("Концерт");
    }

    @Test
    void reportsLineWithInvalidDomainValue() {
        String csv = TicketCsvParser.HEADER + "\n"
                + "Билет,10,20,-1,USUAL,10,4,GREEN,BROWN,1.2,2.3,Москва,70,P123,RUSSIA,"
                + "Концерт,0,Описание,Арена,100,Ленина 1,1234567\n";

        assertThatThrownBy(() -> parser.parse(csv.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("строке 2")
                .hasMessageContaining("price");
    }
}
