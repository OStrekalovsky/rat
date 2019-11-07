package net.ostrekalovsky.rat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;

@Component
@ConfigurationProperties(prefix = "rat")
@Data
@Validated
public class ImportProps {

    @Size(min = 1, message = "Import file path should be at least one character long")
    private String importDir;
}
