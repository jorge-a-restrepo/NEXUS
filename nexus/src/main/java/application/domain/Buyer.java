package application.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a user who participates in the system to acquire products
 * published in the catalog. Extends User. Never manages information of
 * other buyers or inventory.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Buyer extends User {

    private String mainAddress;
    private List<String> additionalAddresses;
    private String commercialStatus;
}
