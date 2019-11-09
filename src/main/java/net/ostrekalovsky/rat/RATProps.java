package net.ostrekalovsky.rat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Component
@ConfigurationProperties(prefix = "rat")
@Data
@Primary
@Validated
public class RATProps {

    @Size(min = 1, message = "Import file path should be at least one character long")
    private String importDir;

    @Min(value = 1, message = "Max favourite products in report should be positive")
    private int reportsFavouriteProductsLimit = 3;

}
